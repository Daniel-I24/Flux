package com.flux.mobile.feature.capture

import com.flux.core.data.speech.VoiceError
import com.flux.core.data.speech.VoiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceFallbackStateTest {
    @Test
    fun `no Spanish recognizer opens the system dictation instead of an error`() {
        val state = PhoneCaptureUi(phase = CapturePhase.LISTENING).after(VoiceState.Failed(VoiceError.LANGUAGE))
        assertEquals(VoiceError.LANGUAGE, state.systemDictation)
        assertNull(state.voiceError)
    }

    @Test
    fun `the error is shown only when the system dictation is missing too`() {
        val requested = PhoneCaptureUi(systemDictation = VoiceError.LANGUAGE)
        assertEquals(VoiceError.LANGUAGE, requested.afterSystemDictation(failed = true).voiceError)
        assertNull(requested.afterSystemDictation(failed = false).voiceError)
        assertNull(requested.afterSystemDictation(failed = false).systemDictation)
    }

    @Test
    fun `not hearing anything keeps its own message`() {
        val state = PhoneCaptureUi(phase = CapturePhase.LISTENING).after(VoiceState.Failed(VoiceError.NOT_UNDERSTOOD))
        assertEquals(VoiceError.NOT_UNDERSTOOD, state.voiceError)
        assertNull(state.systemDictation)
    }

    @Test
    fun `small talk shows its message, keeps the text and opens no preview`() {
        val state = PhoneCaptureUi(text = "hola", phase = CapturePhase.CLASSIFYING).notANote()
        assertEquals(CapturePhase.EDITING, state.phase)
        assertEquals(com.flux.mobile.R.string.capture_error_not_a_note, state.errorMessage)
        assertEquals("hola", state.text)
        assertNull(state.classification)
    }

    @Test
    fun `a note created inside a category is previewed in it`() {
        val classification = com.flux.core.model.Classification(
            categoryId = "tasks",
            title = "Pan",
            confidence = 0.9,
            engine = com.flux.core.model.ClassifierEngine.LOCAL,
        )
        val state = PhoneCaptureUi(fixedCategoryId = "shopping").previewing(classification, corrected = false)
        assertEquals("shopping", state.classification?.categoryId)
        assertEquals(true, state.corrected)
    }
}
