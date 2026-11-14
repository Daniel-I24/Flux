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
}
