package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** What the machine needs from the outside world (injected, so it is testable). */
interface CaptureEffects {
    suspend fun classify(text: String): Classification
    suspend fun save(text: String, classification: Classification, corrected: Boolean): SaveResult
}

data class SaveResult(val note: Note?, val queued: Boolean)

/**
 * Drives [CaptureState]: pure transitions plus side effects run when entering a state
 * (classification on Processing, the 5-second countdown on Preview, saving on Saving).
 */
class CaptureMachine(
    private val scope: CoroutineScope,
    private val effects: CaptureEffects,
    private val tickMillis: Long = TICK_MS,
) {
    private val current = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val state: StateFlow<CaptureState> = current.asStateFlow()
    private var effectJob: Job? = null

    fun send(event: CaptureEvent) {
        val previous = current.value
        val next = previous.on(event)
        if (next == previous) return
        current.value = next
        if (next::class != previous::class || next is CaptureState.Saving) onEnter(next)
    }

    private fun onEnter(state: CaptureState) {
        effectJob?.cancel()
        effectJob = when (state) {
            is CaptureState.Processing -> scope.launch { send(CaptureEvent.Classified(effects.classify(state.text))) }
            is CaptureState.Preview -> scope.launch { tickWhilePreviewing() }
            is CaptureState.Saving -> scope.launch {
                val preview = state.preview
                val result = effects.save(preview.text, preview.classification, preview.corrected)
                send(result.note?.let { CaptureEvent.Saved(it, result.queued) } ?: CaptureEvent.SaveFailed)
            }
            else -> null
        }
    }

    private suspend fun tickWhilePreviewing() {
        while (scope.isActive && current.value is CaptureState.Preview) {
            delay(tickMillis)
            send(CaptureEvent.Tick)
        }
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}
