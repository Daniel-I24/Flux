package com.flux.mobile.sound

import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundPickerRulesTest {
    private val system = listOf(SystemTone(SoundRef.System("content://media/internal/audio/1", "Argon")))
    private val imported = listOf(ImportedTone("tone_a.opus", "Audio de WhatsApp 21/07/2026"))

    @Test
    fun `one list - add from files first, then FLUX, phone and imported tones`() {
        val options = SoundOptions.build(inherit = false, system, imported)
        assertEquals(SoundOption.AddFromFiles, options.first())
        val sections = options.filterIsInstance<SoundOption.Header>().map { it.section }
        assertEquals(listOf(SoundSection.BUNDLED, SoundSection.SYSTEM, SoundSection.IMPORTED), sections)
        assertEquals(SoundRef.Custom("tone_a.opus", "Audio de WhatsApp 21/07/2026"), (options.last() as SoundOption.Tone).sound)
        assertEquals(BundledSound.entries.size + 2, options.filterIsInstance<SoundOption.Tone>().size)
    }

    @Test
    fun `the inherit option goes on top and empty sections are hidden`() {
        val options = SoundOptions.build(inherit = true, system = emptyList(), imported = emptyList())
        assertEquals(listOf(SoundOption.Inherit, SoundOption.AddFromFiles, SoundOption.Header(SoundSection.BUNDLED)), options.take(3))
        assertEquals(1, options.filterIsInstance<SoundOption.Header>().size)
    }

    @Test
    fun `a renamed imported tone is still the selected one`() {
        assertTrue(SoundOptions.isSelected(SoundRef.Custom("tone_a.opus", "Nuevo"), SoundRef.Custom("tone_a.opus", "Viejo")))
        assertFalse(SoundOptions.isSelected(SoundRef.Custom("tone_a.opus", "x"), SoundRef.Custom("tone_b.opus", "x")))
        assertTrue(SoundOptions.isSelected(SoundRef.System("content://1", "A"), SoundRef.System("content://1", "")))
    }

    @Test
    fun `preview - play, play again stops, another tone replaces it`() {
        val events = mutableListOf<String>()
        val preview = PreviewToggle<String>(start = { events += "start $it" }, halt = { events += "stop" })
        preview.toggle("a")
        assertEquals("a", preview.playing.value)
        preview.toggle("a")
        assertNull(preview.playing.value)
        preview.toggle("a")
        preview.toggle("b")
        assertEquals("b", preview.playing.value)
        assertEquals(listOf("start a", "stop", "start a", "stop", "start b"), events)
    }

    @Test
    fun `preview - choosing a tone or closing the screen stops it, an ended tone is not stopped twice`() {
        val events = mutableListOf<String>()
        val preview = PreviewToggle<String>(start = { events += "start" }, halt = { events += "stop" })
        preview.toggle("a")
        preview.stop()
        assertNull(preview.playing.value)
        preview.toggle("b")
        preview.finished("b")
        preview.stop()
        assertEquals(listOf("start", "stop", "start"), events)
    }
}
