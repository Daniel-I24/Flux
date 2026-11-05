package com.flux.mobile.feature.capture

import com.flux.core.data.speech.VoiceError
import com.flux.core.model.Classification

enum class CapturePhase { EDITING, LISTENING, CLASSIFYING, PREVIEW, SAVED }

data class PhoneCaptureUi(
    val text: String = "",
    val phase: CapturePhase = CapturePhase.EDITING,
    val classification: Classification? = null,
    val secondsLeft: Int = AUTO_SAVE_SECONDS,
    val corrected: Boolean = false,
    val voiceError: VoiceError? = null,
    val errorMessage: Int? = null,
) {
    /** Questions pause the countdown: the note is saved only after the user answers (or taps Guardar). */
    val counting: Boolean get() = phase == CapturePhase.PREVIEW && classification?.needsAnswer == false

    companion object {
        const val AUTO_SAVE_SECONDS = 5
    }
}
