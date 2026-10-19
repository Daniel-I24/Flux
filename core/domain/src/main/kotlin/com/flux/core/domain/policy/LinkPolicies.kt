package com.flux.core.domain.policy

import com.flux.core.model.AccountLink
import com.flux.core.model.PhoneOwnership

/** Business rule: an account has exactly ONE phone and ONE watch. */
object PhoneOwnershipPolicy {
    fun evaluate(link: AccountLink?, installationId: String): PhoneOwnership = when (link?.phoneInstallationId) {
        null -> PhoneOwnership.Unclaimed
        installationId -> PhoneOwnership.ThisPhone
        else -> PhoneOwnership.OtherPhone
    }
}

sealed interface WatchLinkDecision {
    data object Allowed : WatchLinkDecision
    data object AlreadyLinkedHere : WatchLinkDecision

    /** The account already owns another watch: linking this one replaces it. */
    data object ReplacesPreviousWatch : WatchLinkDecision

    /** The watch belongs to another account: it must be unlinked there first. */
    data object WatchOwnedByOtherAccount : WatchLinkDecision
}

object WatchLinkPolicy {
    /**
     * @param watchOwnerUid account the watch reports it is linked to (null = free).
     * @param accountWatchNodeId watch currently stored for this account (null = none).
     */
    fun decide(currentUid: String, watchNodeId: String, watchOwnerUid: String?, accountWatchNodeId: String?): WatchLinkDecision =
        when {
            watchOwnerUid != null && watchOwnerUid != currentUid -> WatchLinkDecision.WatchOwnedByOtherAccount
            accountWatchNodeId == watchNodeId && watchOwnerUid == currentUid -> WatchLinkDecision.AlreadyLinkedHere
            accountWatchNodeId != null && accountWatchNodeId != watchNodeId -> WatchLinkDecision.ReplacesPreviousWatch
            else -> WatchLinkDecision.Allowed
        }

    private const val CODE_LENGTH = 6
    private const val CODE_BOUND = 1_000_000

    /** 6-digit confirmation code shown on both devices. */
    fun confirmationCode(random: Int): String = Math.floorMod(random, CODE_BOUND).toString().padStart(CODE_LENGTH, '0')
}
