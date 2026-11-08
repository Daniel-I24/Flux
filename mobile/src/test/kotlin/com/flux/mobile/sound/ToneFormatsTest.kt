package com.flux.mobile.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneFormatsTest {
    @Test
    fun `every requested format is accepted by type or by extension`() {
        listOf("a.mp3", "a.m4a", "a.ogg", "a.opus", "a.aac", "a.wav").forEach { name ->
            assertTrue(name, ToneFormats.accepts("application/octet-stream", name))
        }
        assertTrue(ToneFormats.accepts("audio/mpeg", null))
        assertTrue(ToneFormats.accepts("application/ogg", "AUD-20260721-WA0078"))
    }

    @Test
    fun `documents and images are not even tried`() {
        assertFalse(ToneFormats.accepts("application/pdf", "factura.pdf"))
        assertFalse(ToneFormats.accepts("image/jpeg", "foto.jpg"))
        assertFalse(ToneFormats.accepts(null, null))
    }

    @Test
    fun `the copy keeps an extension the decoder recognises`() {
        assertEquals("opus", ToneFormats.extension("application/octet-stream", "AUD-20260721-WA0078.opus"))
        assertEquals("ogg", ToneFormats.extension("application/ogg", "AUD-20260721-WA0078"))
        assertEquals("m4a", ToneFormats.extension("audio/mp4", null))
        assertEquals("audio", ToneFormats.extension("audio/x-unknown", null))
    }

    @Test
    fun `a WhatsApp audio is named by its date`() {
        assertEquals(ToneName.WhatsApp(2026, 7, 21), ToneFormats.name("AUD-20260721-WA0078.opus"))
        assertEquals(ToneName.WhatsApp(2026, 7, 21), ToneFormats.name("AUD-20260721-WA0078"))
    }

    @Test
    fun `other names lose the extension and the underscores`() {
        assertEquals(ToneName.Plain("mi tono favorito"), ToneFormats.name("mi_tono__favorito.mp3"))
        assertEquals(ToneName.Unknown, ToneFormats.name(".mp3"))
        assertEquals(ToneName.Unknown, ToneFormats.name(null))
    }

    @Test
    fun `a typed name is trimmed and blank is rejected`() {
        assertEquals("Despertar suave", ToneFormats.cleanTitle("  Despertar   suave "))
        assertNull(ToneFormats.cleanTitle("   "))
    }
}
