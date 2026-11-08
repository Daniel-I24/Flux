package com.flux.mobile.link

import com.flux.core.model.WatchLink
import com.flux.core.sync.datalayer.NodeInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchLinkRulesTest {
    private val emulator = NodeInfo("emu", "FLUX Watch", isNearby = true)
    private val galaxy = NodeInfo("gw", "Galaxy Watch", isNearby = true)

    @Test
    fun `nothing paired and nothing linked`() {
        val view = WatchLinkRules.resolve(linkedId = null, withApp = emptyList(), connected = emptyList())
        assertEquals(WatchLink.NoWatchFound, view.link)
        assertTrue(view.candidates.isEmpty())
    }

    @Test
    fun `a linked watch that is away stays linked, so it can be unlinked or replaced`() {
        val view = WatchLinkRules.resolve(linkedId = "emu", withApp = emptyList(), connected = emptyList())
        assertEquals(WatchLink.Linked("emu", "", connected = false), view.link)
    }

    @Test
    fun `a linked watch in range is connected and is not offered again`() {
        val view = WatchLinkRules.resolve(linkedId = "emu", withApp = listOf(emulator, galaxy), connected = listOf(emulator, galaxy))
        assertEquals(WatchLink.Linked("emu", "FLUX Watch", connected = true), view.link)
        assertEquals(listOf(WatchCandidate("gw", "Galaxy Watch", hasApp = true)), view.candidates)
    }

    @Test
    fun `after unlinking, the watch with FLUX is available again`() {
        val view = WatchLinkRules.resolve(linkedId = null, withApp = listOf(emulator), connected = listOf(emulator))
        assertEquals(WatchLink.Available("emu", "FLUX Watch"), view.link)
        assertEquals(listOf(WatchCandidate("emu", "FLUX Watch", hasApp = true)), view.candidates)
    }

    @Test
    fun `a paired watch without FLUX is listed to install it, once`() {
        val view = WatchLinkRules.resolve(linkedId = null, withApp = emptyList(), connected = listOf(galaxy, galaxy))
        assertEquals(WatchLink.AppMissingOnWatch("gw", "Galaxy Watch"), view.link)
        assertEquals(listOf(WatchCandidate("gw", "Galaxy Watch", hasApp = false)), view.candidates)
    }
}
