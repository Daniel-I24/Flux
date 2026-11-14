package com.flux.wear.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.speech.VoiceError
import com.flux.core.data.speech.VoiceRecognizer
import com.flux.core.data.speech.VoiceState
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.model.Category
import com.flux.core.model.DefaultCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val recognizer: VoiceRecognizer,
    repository: WatchCaptureRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {
    private val machine = CaptureMachine(viewModelScope, repository)

    val state: StateFlow<CaptureState> = machine.state

    /** Voice level 0–1 for the lava (read in the draw phase, never recomposes the screen). */
    val level: StateFlow<Float> = recognizer.level

    /** What the recognizer heard so far, shown under "Escuchando…". */
    val partial: StateFlow<String> = recognizer.state
        .map { (it as? VoiceState.Listening)?.partial.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), "")

    /** Categories cached from the phone; defaults until the first snapshot arrives. */
    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .map { it.ifEmpty { DefaultCategory.all() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DefaultCategory.all())

    private val systemDictationRequests = Channel<VoiceError>(Channel.CONFLATED)

    /** In-app recognition could not run: the screen tries the system dictation, then Wear's voice input. */
    val systemDictation: Flow<VoiceError> = systemDictationRequests.receiveAsFlow()

    init {
        viewModelScope.launch {
            recognizer.state.collect { voice ->
                when (voice) {
                    is VoiceState.Heard -> machine.send(CaptureEvent.SpeechResult(voice.text))
                    is VoiceState.Failed -> if (voice.error.trySystemDictation) {
                        systemDictationRequests.trySend(voice.error)
                    } else {
                        machine.send(CaptureEvent.SpeechFailed(voice.error.toFailure()))
                    }
                    else -> Unit
                }
            }
        }
    }

    /** False when the watch has no recognition service: the screen uses the system dictation activity. */
    fun canListenInApp(): Boolean = recognizer.isAvailable()

    /** Moves to "Escuchando…" before the permission check or the system dictation screen. */
    fun beginDictation() = machine.send(CaptureEvent.StartDictation)

    /** Starts in-app listening (permission already granted). */
    fun listen() {
        beginDictation()
        recognizer.start()
    }

    fun stopListening() = recognizer.stop()

    fun send(event: CaptureEvent) {
        if (event == CaptureEvent.Cancel) recognizer.release()
        machine.send(event)
    }

    override fun onCleared() {
        recognizer.release()
        super.onCleared()
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
