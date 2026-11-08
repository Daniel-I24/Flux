package com.flux.wear.sync

import com.flux.core.model.PhoneConnection
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.WatchOwnership

/** What the Data Layer reports before asking the phone anything. Counts only, so it can be logged. */
data class LinkProbe(val nearbyNodes: Int, val remoteNodes: Int, val phonesWithApp: Int, val reachablePhones: Int)

/** The rules that turn a probe or a phone status into one clear state. Pure, so each case is tested. */
object ConnectionDiagnosis {
    /**
     * Null when a phone with FLUX is reachable: then the phone itself says whether it has a session
     * and whether this watch is linked. Only a nearby node counts as a paired phone: an unpaired
     * watch can still see remote (cloud) nodes, which made it say "install FLUX" instead of "pair".
     */
    fun fromProbe(probe: LinkProbe): PhoneConnection? = when {
        probe.phonesWithApp == 0 && probe.nearbyNodes == 0 -> PhoneConnection.NoPairedPhone
        probe.phonesWithApp == 0 -> PhoneConnection.AppNotInstalled
        probe.reachablePhones == 0 -> PhoneConnection.PhoneUnreachable
        else -> null
    }

    fun fromStatus(status: SyncMessage.Status): PhoneConnection = when {
        status.accountState != AccountState.READY -> PhoneConnection.SetupIncomplete
        status.ownership == WatchOwnership.NOT_LINKED -> PhoneConnection.NotLinked
        status.ownership == WatchOwnership.LINKED_TO_OTHER -> PhoneConnection.LinkedToOtherAccount
        else -> PhoneConnection.Ready(status.userName, status.serverReachable)
    }
}
