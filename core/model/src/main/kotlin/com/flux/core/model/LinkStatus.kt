package com.flux.core.model

/** Watch connection as seen from the watch (each state maps to a message + suggested action). */
sealed interface PhoneConnection {
    data object Checking : PhoneConnection
    data object NoPairedPhone : PhoneConnection
    data object AppNotInstalled : PhoneConnection
    data object PhoneUnreachable : PhoneConnection
    data object SetupIncomplete : PhoneConnection
    data object NotLinked : PhoneConnection
    data object LinkedToOtherAccount : PhoneConnection
    data object UpdateRequired : PhoneConnection
    data class Ready(val userName: String?, val serverReachable: Boolean) : PhoneConnection
}

/** Watch link as seen from the phone. */
sealed interface WatchLink {
    data object Searching : WatchLink
    data object NoWatchFound : WatchLink
    data class AppMissingOnWatch(val nodeId: String, val displayName: String) : WatchLink
    data class Available(val nodeId: String, val displayName: String) : WatchLink
    data class AwaitingConfirmation(val nodeId: String, val displayName: String, val code: String) : WatchLink
    data class Linked(val nodeId: String, val displayName: String, val connected: Boolean) : WatchLink
    data object WatchLinkedToOtherAccount : WatchLink
    data object UpdateRequired : WatchLink
}

/** Account-level link stored in Firestore users/{uid}.link. */
data class AccountLink(
    val phoneInstallationId: String?,
    val watchNodeId: String?,
    val linkedAt: Long?,
)

/** Result of checking whether this phone may use the account. */
sealed interface PhoneOwnership {
    data object ThisPhone : PhoneOwnership
    data object Unclaimed : PhoneOwnership
    data object OtherPhone : PhoneOwnership
}
