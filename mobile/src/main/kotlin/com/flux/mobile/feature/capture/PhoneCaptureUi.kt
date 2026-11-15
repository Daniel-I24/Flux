package com.flux.mobile.feature.capture

import com.flux.core.data.speech.VoiceError
import com.flux.core.data.speech.VoiceState
import com.flux.core.model.Classification
import com.flux.mobile.R

enum class CapturePhase { EDITING, LISTENING, CLASSIFYING, PREVIEW, SAVED }

data class PhoneCaptureUi(
    val text: String = "",
    val phase: CapturePhase = CapturePhase.EDITING,
    val classification: Classification? = null,
    val secondsLeft: Int = AUTO_SAVE_SECONDS,
    val corrected: Boolean = false,
    val voiceError: VoiceError? = null,
    /** In-app recognition could not run: the sheet opens the system dictation once, with this error as fallback. */
    val systemDictation: VoiceError? = null,
    /** "Nueva nota" inside a category: the note is saved there whatever the classifier says. */
    val fixedCategoryId: String? = null,
    /** This note's own "Avisarme" lead; null = the global one. Choosing it stops the countdown. */
    val alarmLeadMinutes: Int? = null,
    val leadChosen: Boolean = false,
    val errorMessage: Int? = null,
) {
    /** Questions pause the countdown: the note is saved only after the user answers (or taps Guardar). */
    val counting: Boolean get() = phase == CapturePhase.PREVIEW && classification?.needsAnswer == false && !leadChosen

    companion object {
        const val AUTO_SAVE_SECONDS = 5
    }
}

/** What the recognizer's new state means for the sheet. */
internal fun PhoneCaptureUi.after(voice: VoiceState): PhoneCaptureUi = when (voice) {
    is VoiceState.Listening -> if (voice.partial.isBlank()) this else copy(text = voice.partial)
    is VoiceState.Heard -> copy(text = voice.text, phase = CapturePhase.EDITING)
    is VoiceState.Failed -> if (voice.error.trySystemDictation) {
        copy(phase = CapturePhase.EDITING, systemDictation = voice.error)
    } else {
        copy(phase = CapturePhase.EDITING, voiceError = voice.error)
    }
    VoiceState.Idle -> this
}

/** The system dictation was opened (its result arrives later) or could not be: the request is consumed. */
internal fun PhoneCaptureUi.afterSystemDictation(failed: Boolean) = copy(
    phase = CapturePhase.EDITING,
    voiceError = if (failed) systemDictation ?: VoiceError.UNAVAILABLE else null,
    systemDictation = null,
)

/** Small talk or noise: no preview and nothing saved; the text stays so the user can fix it or dictate again. */
internal fun PhoneCaptureUi.notANote() = copy(
    phase = CapturePhase.EDITING,
    classification = null,
    errorMessage = R.string.capture_error_not_a_note,
)

internal fun PhoneCaptureUi.previewing(classification: Classification, corrected: Boolean) = copy(
    phase = CapturePhase.PREVIEW,
    classification = fixedCategoryId?.let(classification::inCategory) ?: classification,
    // A category chosen by the user is never moved by the later reclassification.
    corrected = corrected || fixedCategoryId != null,
    secondsLeft = PhoneCaptureUi.AUTO_SAVE_SECONDS,
)
