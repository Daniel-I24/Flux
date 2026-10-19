package com.flux.mobile.link

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.mobile.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import java.util.concurrent.Executors
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
    val watchLink: StateFlow<WatchLink> = state.asStateFlow()
    val protocolMismatch: StateFlow<Boolean> = inbox.versionMismatch

    suspend fun refresh() {
        state.value = WatchLink.Searching
        val linkedId = observeLink().first()?.watchNodeId
        val withApp = gateway.nodesWithCapability(SyncProtocol.WATCH_CAPABILITY, reachableOnly = false)
        val connected = gateway.connectedNodes()
        val linked = withApp.firstOrNull { it.id == linkedId }
        state.value = when {
            linked != null -> WatchLink.Linked(linked.id, linked.displayName, connected = connected.any { it.id == linked.id })
            withApp.isNotEmpty() -> withApp.first().let { WatchLink.Available(it.id, it.displayName) }
            connected.isNotEmpty() -> connected.first().let { WatchLink.AppMissingOnWatch(it.id, it.displayName) }
            else -> WatchLink.NoWatchFound
        }
    }

    suspend fun link(nodeId: String, displayName: String): LinkProblem? {
        val accountId = currentAccountId() ?: return LinkProblem.STORAGE
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
        refresh()
        return problem
    }

    suspend fun unlink() {
        val accountId = currentAccountId()
        val linkedId = observeLink().first()?.watchNodeId
        if (accountId != null && linkedId != null) gateway.send(linkedId, factory.envelope(SyncMessage.Unlink(accountId)))
        unlinkWatch()
        refresh()
    }

    /** Opens FLUX's Play Store page on the watch so the user can install it there. */
    suspend fun installOnWatch(nodeId: String): LinkProblem? = runCatching {
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse(BuildConfig.PLAY_STORE_URL))
        RemoteActivityHelper(context, Executors.newSingleThreadExecutor()).startRemoteActivity(intent, nodeId).await()
    }.fold(onSuccess = { null }, onFailure = { LinkProblem.INSTALL_FAILED })

    private companion object {
        const val CONFIRM_TIMEOUT_MS = 60_000L
    }
}
