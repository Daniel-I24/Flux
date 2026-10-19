package com.flux.core.domain.usecase

import com.flux.core.domain.policy.WatchLinkDecision
import com.flux.core.domain.policy.WatchLinkPolicy
import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.domain.repository.AuthRepository
import com.flux.core.model.AccountLink
import com.flux.core.model.AppError
import com.flux.core.model.FluxResult
import java.security.MessageDigest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** The watch never sees the Firebase uid: only a stable, non-reversible account id. */
object AccountIds {
    private const val ID_LENGTH = 32

    fun fromUid(uid: String): String = MessageDigest.getInstance("SHA-256")
        .digest("flux-account:$uid".toByteArray())
        .joinToString("") { "%02x".format(it) }
        .take(ID_LENGTH)
}

class ObserveAccountLinkUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val links: AccountLinkRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<AccountLink?> = auth.observeSession().flatMapLatest { session ->
        session?.let { links.observeLink(it.uid) } ?: flowOf(null)
    }
}

/** Stores the watch in the account after the user confirmed the code on the watch. */
class LinkWatchToAccountUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val links: AccountLinkRepository,
) {
    suspend operator fun invoke(watchNodeId: String, watchOwnerAccountId: String?): FluxResult<WatchLinkDecision> {
        val session = auth.currentSession() ?: return FluxResult.Failure(AppError.Unauthorized)
        val link = when (val current = links.getLink(session.uid)) {
            is FluxResult.Success -> current.value
            is FluxResult.Failure -> return current
        }
        val myAccountId = AccountIds.fromUid(session.uid)
        val ownerUid = watchOwnerAccountId?.let { if (it == myAccountId) session.uid else it }
        val decision = WatchLinkPolicy.decide(session.uid, watchNodeId, ownerUid, link?.watchNodeId)
        if (decision == WatchLinkDecision.WatchOwnedByOtherAccount) return FluxResult.Success(decision)
        val saved = links.linkWatch(session.uid, watchNodeId)
        return if (saved is FluxResult.Failure) saved else FluxResult.Success(decision)
    }
}

class UnlinkWatchUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val links: AccountLinkRepository,
) {
    suspend operator fun invoke(): FluxResult<Unit> {
        val session = auth.currentSession() ?: return FluxResult.Failure(AppError.Unauthorized)
        return links.unlinkWatch(session.uid)
    }
}

class CurrentAccountIdUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(): String? = auth.currentSession()?.uid?.let(AccountIds::fromUid)
}
