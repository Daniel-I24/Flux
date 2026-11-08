package com.flux.mobile.link

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.flux.core.domain.policy.WatchLinkDecision
import com.flux.core.domain.policy.WatchLinkPolicy
import com.flux.core.domain.usecase.CurrentAccountIdUseCase
import com.flux.core.domain.usecase.LinkWatchToAccountUseCase
import com.flux.core.domain.usecase.ObserveAccountLinkUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.UnlinkWatchUseCase
import com.flux.core.model.FluxResult
import com.flux.core.model.WatchLink
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.LinkLog
import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.mobile.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await

enum class LinkProblem { REJECTED, TIMEOUT, INSTALL_FAILED, STORAGE }

/** Phone side of linking: discovery, 6-digit confirmation on the watch, account update. */
@Singleton
class WatchLinkCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gateway: DataLayerGateway,
    private val inbox: SyncInbox,
    private val factory: SyncMessageFactory,
    private val observeLink: ObserveAccountLinkUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val currentAccountId: CurrentAccountIdUseCase,
    private val linkWatch: LinkWatchToAccountUseCase,
    private val unlinkWatch: UnlinkWatchUseCase,
) {
    private val state = MutableStateFlow<WatchLink>(WatchLink.Searching)
    private val candidateList = MutableStateFlow<List<WatchCandidate>>(emptyList())
    val watchLink: StateFlow<WatchLink> = state.asStateFlow()

    /** Watches that could be linked now (or replace the linked one), FLUX installed or not. */
    val candidates: StateFlow<List<WatchCandidate>> = candidateList.asStateFlow()
    val protocolMismatch: StateFlow<Boolean> = inbox.versionMismatch

    suspend fun refresh() {
        state.value = WatchLink.Searching
        val linkedId = observeLink().first()?.watchNodeId
        val withApp = gateway.nodesWithCapability(SyncProtocol.WATCH_CAPABILITY, reachableOnly = false)
        val connected = gateway.connectedNodes()
        val view = WatchLinkRules.resolve(linkedId, withApp, connected)
        LinkLog.info(
            "phone linked=${linkedId != null} withApp=${withApp.size} connected=${connected.size} " +
                "state=${view.link.javaClass.simpleName} candidates=${view.candidates.size}",
        )
        candidateList.value = view.candidates
        state.value = view.link
    }

    /** Links [nodeId]; if the account had another watch, that one is told it was unlinked ("Cambiar de reloj"). */
    suspend fun link(nodeId: String, displayName: String): LinkProblem? {
        val accountId = currentAccountId() ?: return LinkProblem.STORAGE
        val previous = observeLink().first()?.watchNodeId
        val code = WatchLinkPolicy.confirmationCode(SecureRandom().nextInt())
        state.value = WatchLink.AwaitingConfirmation(nodeId, displayName, code)
        val request = factory.envelope(SyncMessage.LinkRequest(code, accountId, observeProfile().first()?.displayName))
        val reply = inbox.request<SyncMessage.LinkResponse>(SyncInbox.linkCorrelation(code), CONFIRM_TIMEOUT_MS) {
            gateway.send(nodeId, request) is FluxResult.Success
        }
        val problem = when {
            reply == null -> LinkProblem.TIMEOUT
            !reply.accepted && reply.ownerAccountId != null && reply.ownerAccountId != accountId -> {
                state.value = WatchLink.WatchLinkedToOtherAccount
                return null
            }
            !reply.accepted -> LinkProblem.REJECTED
            else -> when (val saved = linkWatch(nodeId, reply.ownerAccountId)) {
                is FluxResult.Failure -> LinkProblem.STORAGE
                is FluxResult.Success -> if (saved.value == WatchLinkDecision.WatchOwnedByOtherAccount) LinkProblem.REJECTED else null
            }
        }
        if (problem == null && previous != null && previous != nodeId) {
            gateway.send(previous, factory.envelope(SyncMessage.Unlink(accountId)))
        }
        LinkLog.info("phone link result=${problem ?: "LINKED"} replaced=${problem == null && previous != null && previous != nodeId}")
        refresh()
        return problem
    }

    suspend fun unlink() {
        val accountId = currentAccountId()
        val linkedId = observeLink().first()?.watchNodeId
        if (accountId != null && linkedId != null) gateway.send(linkedId, factory.envelope(SyncMessage.Unlink(accountId)))
        unlinkWatch()
        LinkLog.info("phone unlink sent=${linkedId != null}")
        refresh()
    }

    /** Opens FLUX's Play Store page on the watch so the user can install it there. */
    suspend fun installOnWatch(nodeId: String): LinkProblem? = runCatching {
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(BuildConfig.PLAY_STORE_URL.toUri())
        RemoteActivityHelper(context, ContextCompat.getMainExecutor(context)).startRemoteActivity(intent, nodeId).await()
    }.fold(onSuccess = { null }, onFailure = { LinkProblem.INSTALL_FAILED })

    private companion object {
        const val CONFIRM_TIMEOUT_MS = 60_000L
    }
}
