package com.flux.wear.feature.capture

import com.flux.core.data.speech.VoiceError

/** Each failure has its own message and a "Reintentar" that fixes it. */
enum class CaptureFailure { NOT_UNDERSTOOD, NO_PERMISSION, NO_NETWORK, BUSY, LANGUAGE, UNAVAILABLE, NOT_SAVED }

/** Each recognizer error keeps its own meaning on screen (v1.1 showed "no disponible" for a busy service). */
fun VoiceError.toFailure(): CaptureFailure = when (this) {
    VoiceError.NO_PERMISSION -> CaptureFailure.NO_PERMISSION
    VoiceError.NO_NETWORK -> CaptureFailure.NO_NETWORK
    VoiceError.NOT_UNDERSTOOD -> CaptureFailure.NOT_UNDERSTOOD
    VoiceError.BUSY -> CaptureFailure.BUSY
    VoiceError.LANGUAGE -> CaptureFailure.LANGUAGE
    VoiceError.UNAVAILABLE -> CaptureFailure.UNAVAILABLE
}
