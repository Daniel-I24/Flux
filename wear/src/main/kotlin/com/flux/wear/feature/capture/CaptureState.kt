package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.Note

enum class CaptureFailure { NO_SPEECH, NOT_SAVED }

sealed interface CaptureEvent {
    data object StartDictation : CaptureEvent
    data class SpeechResult(val text: String?) : CaptureEvent
    data class TextEntered(val text: String) : CaptureEvent
    data class Classified(val classification: Classification) : CaptureEvent
    data object Tick : CaptureEvent
    data class ChangeCategory(val categoryId: String) : CaptureEvent
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
                ?: Error(CaptureFailure.NO_SPEECH)
            is CaptureEvent.TextEntered -> Processing(event.text)
            CaptureEvent.Cancel -> Idle
            else -> this
        }
    }

    data class Processing(val text: String) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            is CaptureEvent.Classified -> Preview(text, event.classification, AUTO_SAVE_SECONDS, corrected = false)
            CaptureEvent.Cancel -> Idle
            else -> this
        }
    }

    /** Auto-saves when [secondsLeft] reaches 0; changing the category pauses the countdown. */
    data class Preview(
        val text: String,
        val classification: Classification,
        val secondsLeft: Int,
        val corrected: Boolean,
        val paused: Boolean = false,
    ) : CaptureState {
        override fun on(event: CaptureEvent): CaptureState = when (event) {
            CaptureEvent.Tick -> if (paused) {
                this
            } else if (secondsLeft <= 1) {
                Saving(this)
            } else {
                copy(secondsLeft = secondsLeft - 1)
            }
            is CaptureEvent.ChangeCategory -> copy(
                classification = classification.copy(categoryId = event.categoryId),
                corrected = event.categoryId != classification.categoryId || corrected,
                paused = true,
            )
            CaptureEvent.Confirm -> Saving(this)
            CaptureEvent.Cancel -> Idle
            CaptureEvent.StartDictation -> Listening
            is CaptureEvent.TextEntered -> Processing(event.text)
            else -> this
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
    }
}
