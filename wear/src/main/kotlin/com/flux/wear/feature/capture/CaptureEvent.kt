package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.Note

/** Everything the capture flow reacts to: voice, classification, answers and the countdown. */
sealed interface CaptureEvent {
    data object StartDictation : CaptureEvent
    data class SpeechResult(val text: String?) : CaptureEvent
    data class SpeechFailed(val failure: CaptureFailure) : CaptureEvent

    /** Debug builds only: typed text instead of voice. */
    data class TextEntered(val text: String) : CaptureEvent
    data class Classified(val classification: Classification) : CaptureEvent
    data object Tick : CaptureEvent
    data class ChangeCategory(val categoryId: String) : CaptureEvent
    data class AnswerTime(val dueAt: Long) : CaptureEvent
    data class AnswerCategory(val categoryId: String) : CaptureEvent

    /** The note's own lead in the preview (null = the phone's global one). */
    data class ChangeLead(val minutes: Int?) : CaptureEvent

    /** A suggestion, a picked date and time, or null for "Sin recordatorio". */
    data class AnswerReminder(val dueAt: Long?) : CaptureEvent
    data object Confirm : CaptureEvent
    data object Cancel : CaptureEvent
    data class Saved(val note: Note, val queued: Boolean) : CaptureEvent
    data object SaveFailed : CaptureEvent
    data object Dismiss : CaptureEvent
}
