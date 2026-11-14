package com.flux.core.data.speech

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceErrorPolicyTest {
    private val colombia = VoiceErrorPolicy.PRIMARY_LANGUAGE
    private val spain = VoiceErrorPolicy.FALLBACK_LANGUAGE

    @Test
    fun `a missing Colombian Spanish pack moves on to Spain Spanish without an error`() {
        assertEquals(VoiceRecovery.Restart(spain), VoiceErrorPolicy.recover(LANGUAGE_NOT_SUPPORTED, colombia, busyRetries = 0))
        assertEquals(VoiceRecovery.Fail(VoiceError.LANGUAGE), VoiceErrorPolicy.recover(LANGUAGE_UNAVAILABLE, spain, busyRetries = 0))
    }

    @Test
    fun `a busy service is retried once, then reported as busy and not as unavailable`() {
        assertEquals(VoiceRecovery.Restart(colombia), VoiceErrorPolicy.recover(SpeechRecognizer.ERROR_RECOGNIZER_BUSY, colombia, 0))
        assertEquals(VoiceRecovery.Fail(VoiceError.BUSY), VoiceErrorPolicy.recover(SpeechRecognizer.ERROR_RECOGNIZER_BUSY, colombia, 1))
        assertEquals(VoiceRecovery.Restart(colombia), VoiceErrorPolicy.recover(SpeechRecognizer.ERROR_CLIENT, colombia, 0))
    }

    @Test
    fun `each error code keeps its own meaning`() {
        assertEquals(VoiceError.NOT_UNDERSTOOD, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals(VoiceError.NOT_UNDERSTOOD, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        assertEquals(VoiceError.NO_NETWORK, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_NETWORK))
        assertEquals(VoiceError.NO_NETWORK, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_SERVER_DISCONNECTED))
        assertEquals(VoiceError.NO_PERMISSION, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
        assertEquals(VoiceError.BUSY, VoiceErrorPolicy.errorOf(SpeechRecognizer.ERROR_TOO_MANY_REQUESTS))
        assertEquals(VoiceError.UNAVAILABLE, VoiceErrorPolicy.errorOf(UNKNOWN))
    }

    @Test
    fun `only language and unavailable errors fall back to the system dictation`() {
        assertTrue(VoiceError.LANGUAGE.trySystemDictation)
        assertTrue(VoiceError.UNAVAILABLE.trySystemDictation)
        listOf(VoiceError.NOT_UNDERSTOOD, VoiceError.BUSY, VoiceError.NO_NETWORK, VoiceError.NO_PERMISSION)
            .forEach { assertFalse(it.trySystemDictation) }
    }

    private companion object {
        const val LANGUAGE_NOT_SUPPORTED = 12
        const val LANGUAGE_UNAVAILABLE = 13
        const val UNKNOWN = 99
    }
}
