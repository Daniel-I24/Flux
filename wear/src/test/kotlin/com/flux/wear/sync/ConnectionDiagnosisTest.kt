package com.flux.wear.sync

import com.flux.core.model.PhoneConnection
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.WatchOwnership
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConnectionDiagnosisTest {
    private fun probe(
        nearby: Int = 0,
        remote: Int = 0,
        withApp: Int = 0,
        reachable: Int = 0
    ) = LinkProbe(nearby, remote, withApp, reachable)

    private fun status(account: AccountState = AccountState.READY, ownership: WatchOwnership = WatchOwnership.LINKED_HERE) =
        SyncMessage.Status(account, ownership, userName = "Ana", serverReachable = true)

    @Test
    fun `no node at all means the watch is not paired with a phone`() {
        assertEquals(PhoneConnection.NoPairedPhone, ConnectionDiagnosis.fromProbe(probe()))
    }

    @Test
    fun `only a remote node, as on an unpaired Galaxy Watch, is still not paired`() {
        assertEquals(PhoneConnection.NoPairedPhone, ConnectionDiagnosis.fromProbe(probe(remote = 1)))
    }

    @Test
    fun `a nearby phone without the FLUX capability needs FLUX installed`() {
        assertEquals(PhoneConnection.AppNotInstalled, ConnectionDiagnosis.fromProbe(probe(nearby = 1)))
    }

    @Test
    fun `a phone with FLUX that is not reachable now is out of range`() {
        assertEquals(PhoneConnection.PhoneUnreachable, ConnectionDiagnosis.fromProbe(probe(nearby = 1, withApp = 1)))
    }

    @Test
    fun `a reachable phone with FLUX is asked for its status`() {
        assertNull(ConnectionDiagnosis.fromProbe(probe(nearby = 1, withApp = 1, reachable = 1)))
    }

    @Test
    fun `FLUX without a session on the phone`() {
        assertEquals(PhoneConnection.SetupIncomplete, ConnectionDiagnosis.fromStatus(status(account = AccountState.SIGNED_OUT)))
        assertEquals(PhoneConnection.SetupIncomplete, ConnectionDiagnosis.fromStatus(status(account = AccountState.ONBOARDING)))
    }

    @Test
    fun `a ready phone that has not linked this watch offers the link`() {
        assertEquals(PhoneConnection.NotLinked, ConnectionDiagnosis.fromStatus(status(ownership = WatchOwnership.NOT_LINKED)))
    }

    @Test
    fun `a watch of another account`() {
        assertEquals(
            PhoneConnection.LinkedToOtherAccount,
            ConnectionDiagnosis.fromStatus(status(ownership = WatchOwnership.LINKED_TO_OTHER)),
        )
    }

    @Test
    fun `linked and ready`() {
        assertEquals(PhoneConnection.Ready("Ana", serverReachable = true), ConnectionDiagnosis.fromStatus(status()))
    }
}
