package com.flux.wear.feature.capture

import com.flux.core.domain.usecase.ResolveAmbiguityUseCase
import com.flux.core.model.Classification
import com.flux.core.model.Note

/** Each failure has its own message and a "Reintentar" that fixes it. */
enum class CaptureFailure { NOT_UNDERSTOOD, NO_PERMISSION, NO_NETWORK, UNAVAILABLE, NOT_SAVED }

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

    /** A suggestion, a picked date and time, or null for "Sin recordatorio". */
    data class AnswerReminder(val dueAt: Long?) : CaptureEvent
    data object Confirm : CaptureEvent
    data object Cancel : CaptureEvent
    data class Saved(val note: Note, val queued: Boolean) : CaptureEvent
    data object SaveFailed : CaptureEvent
    data object Dismiss : CaptureEvent
}

/**
 * State pattern: each state decides the next one for every event. Unknown events
 * keep the current state, so impossible transitions cannot happen.
 */
sealed interface CaptureState {
    fun on(event: CaptureEvent): CaptureState

    data object Idle : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            CaptureEvent.StartDictation -> Listening
            is CaptureEvent.TextEntered -> Processing(event.text)
            else -> this
        }
    }

    data object Listening : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            is CaptureEvent.SpeechResult -> event.text?.trim()?.takeIf { it.isNotEmpty() }?.let(::Processing)
                ?: Error(CaptureFailure.NOT_UNDERSTOOD)
            is CaptureEvent.SpeechFailed -> Error(event.failure)
            is CaptureEvent.TextEntered -> Processing(event.text)
            CaptureEvent.Cancel -> Idle
            else -> this
        }
    }

    data class Processing(val text: String) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            is CaptureEvent.Classified -> Preview.of(text, event.classification)
            CaptureEvent.Cancel -> Idle
            else -> this
        }
    }

    /**
     * Auto-saves when [secondsLeft] reaches 0. The countdown waits while there are questions
     * (ambiguities) and after a manual category change.
     */
    data class Preview(
        val text: String,
        val classification: Classification,
        val secondsLeft: Int,
        val corrected: Boolean,
        val paused: Boolean = false,
    ) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            CaptureEvent.Tick -> when {
                paused -> this
                secondsLeft <= 1 -> Saving(this)
                else -> copy(secondsLeft = secondsLeft - 1)
            }
            is CaptureEvent.ChangeCategory -> copy(
                classification = classification.copy(categoryId = event.categoryId),
                corrected = event.categoryId != classification.categoryId || corrected,
                paused = true,
            )
            is CaptureEvent.AnswerTime -> answered(RESOLVER.chooseTime(classification, event.dueAt), corrected)
            is CaptureEvent.AnswerReminder -> answered(RESOLVER.chooseReminder(classification, event.dueAt), corrected)
            is CaptureEvent.AnswerCategory -> answered(
                RESOLVER.chooseCategory(classification, event.categoryId),
                corrected || event.categoryId != classification.categoryId,
            )
            CaptureEvent.Confirm -> Saving(copy(classification = RESOLVER.acceptDefaults(classification)))
            CaptureEvent.Cancel -> Idle
            CaptureEvent.StartDictation -> Listening
            is CaptureEvent.TextEntered -> Processing(event.text)
            else -> this
        }

        /** Once every question is answered the countdown starts again from the top. */
        private fun answered(next: Classification, corrected: Boolean): Preview = if (next == classification) {
            this
        } else {
            copy(classification = next, corrected = corrected, paused = next.needsAnswer, secondsLeft = AUTO_SAVE_SECONDS)
        }

        companion object {
            fun of(text: String, classification: Classification) =
                Preview(text, classification, AUTO_SAVE_SECONDS, corrected = false, paused = classification.needsAnswer)
        }
    }

    data class Saving(val preview: Preview) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            is CaptureEvent.Saved -> Saved(event.note, event.queued)
            CaptureEvent.SaveFailed -> Error(CaptureFailure.NOT_SAVED)
            else -> this
        }
    }

    data class Saved(val note: Note, val queued: Boolean) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            CaptureEvent.Dismiss, CaptureEvent.Cancel -> Idle
            CaptureEvent.StartDictation -> Listening
            else -> this
        }
    }

    data class Error(val failure: CaptureFailure) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            CaptureEvent.StartDictation -> Listening
            is CaptureEvent.TextEntered -> Processing(event.text)
            CaptureEvent.Dismiss, CaptureEvent.Cancel -> Idle
            else -> this
        }
    }

    companion object {
        const val AUTO_SAVE_SECONDS = 5
        private val RESOLVER = ResolveAmbiguityUseCase()
    }
}
