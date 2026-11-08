package com.flux.mobile.sound

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ToneIndexTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val directory: File get() = folder.root
    private fun index() = ToneIndex(directory, orphanTitle = "Tono importado")

    private fun tone(name: String, title: String): ImportedTone {
        File(directory, name).writeBytes(byteArrayOf(1, 2, 3))
        return ImportedTone(name, title)
    }

    @Test
    fun `an imported tone is listed with its name and survives a new instance`() {
        index().add(tone("tone_a.opus", "Audio de WhatsApp 21/07/2026"))
        assertEquals(listOf(ImportedTone("tone_a.opus", "Audio de WhatsApp 21/07/2026")), index().list())
    }

    @Test
    fun `renaming keeps the file`() {
        index().add(tone("tone_a.mp3", "a"))
        assertEquals(listOf(ImportedTone("tone_a.mp3", "Despertar")), index().rename("tone_a.mp3", "Despertar"))
    }

    @Test
    fun `deleting removes the entry and the file`() {
        index().add(tone("tone_a.mp3", "a"))
        index().add(tone("tone_b.mp3", "b"))
        assertEquals(listOf(ImportedTone("tone_b.mp3", "b")), index().delete("tone_a.mp3"))
        assertFalse(File(directory, "tone_a.mp3").exists())
    }

    @Test
    fun `a tone whose file vanished is dropped`() {
        index().add(tone("tone_a.mp3", "a"))
        File(directory, "tone_a.mp3").delete()
        assertEquals(emptyList<ImportedTone>(), index().list())
    }

    @Test
    fun `tones imported by v1_1_0, before the list existed, are adopted`() {
        File(directory, "tone_old.m4a").writeBytes(byteArrayOf(1))
        File(directory, "notes.txt").writeBytes(byteArrayOf(1))
        assertEquals(listOf(ImportedTone("tone_old.m4a", "Tono importado")), index().list())
    }

    @Test
    fun `a damaged list does not lose the files`() {
        File(directory, "tone_a.mp3").writeBytes(byteArrayOf(1))
        File(directory, "tones.json").writeText("{not json")
        assertEquals(listOf(ImportedTone("tone_a.mp3", "Tono importado")), index().list())
    }
}
