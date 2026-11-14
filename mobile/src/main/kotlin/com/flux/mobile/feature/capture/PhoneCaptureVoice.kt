package com.flux.mobile.feature.capture

import com.flux.core.data.speech.VoiceError
import com.flux.core.data.speech.VoiceRecognizer
import com.flux.core.data.speech.VoiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The voice half of the phone capture: FLUX's own recognizer, the system dictation fallback and
 * the microphone permission. What is heard goes into the shared sheet state and [onHeard] classifies it.
 */
class PhoneCaptureVoice(
    private val recognizer: VoiceRecognizer,
    private val state: MutableStateFlow<PhoneCaptureUi>,
    private val onHeard: () -> Unit,
) {
    /** Voice level 0–1 for the lava. */
    val level: StateFlow<Float> = recognizer.level

    fun observe(scope: CoroutineScope) {
        scope.launch {
            recognizer.state.collect { voice ->
                state.update { it.after(voice) }
                if (voice is VoiceState.Heard) onHeard()
            }
        }
    }

    fun canListenInApp() = recognizer.isAvailable()

    /** Microphone permission already granted. */
    fun listen() {
        state.update { it.copy(phase = CapturePhase.LISTENING, voiceError = null, errorMessage = null) }
        recognizer.start()
    }

    fun stop() = recognizer.stop()

    fun release() = recognizer.release()

    fun permissionDenied() = state.update { it.copy(phase = CapturePhase.EDITING, voiceError = VoiceError.NO_PERMISSION) }

    fun requestSystemDictation() = state.update { it.copy(systemDictation = VoiceError.UNAVAILABLE) }

    fun systemDictationHandled(failed: Boolean) = state.update { it.afterSystemDictation(failed) }

    fun heardFromSystem(text: String?) {
        if (text.isNullOrBlank()) return
        state.update { it.copy(text = text) }
        onHeard()
    }
}
