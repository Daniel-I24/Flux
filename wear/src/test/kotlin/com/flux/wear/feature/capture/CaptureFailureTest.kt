package com.flux.wear.feature.capture

import com.flux.core.data.speech.VoiceError
import org.junit.Assert.assertEquals
import org.junit.Test

class CaptureFailureTest {
    @Test
    fun `a busy recognizer is no longer shown as unavailable`() {
        assertEquals(CaptureFailure.BUSY, VoiceError.BUSY.toFailure())
        assertEquals(CaptureFailure.LANGUAGE, VoiceError.LANGUAGE.toFailure())
        assertEquals(CaptureFailure.NOT_UNDERSTOOD, VoiceError.NOT_UNDERSTOOD.toFailure())
        assertEquals(CaptureFailure.UNAVAILABLE, VoiceError.UNAVAILABLE.toFailure())
    }
}
