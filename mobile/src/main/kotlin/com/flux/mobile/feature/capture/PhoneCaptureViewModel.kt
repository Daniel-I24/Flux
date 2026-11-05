package com.flux.mobile.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.speech.VoiceError
import com.flux.core.data.speech.VoiceRecognizer
import com.flux.core.data.speech.VoiceState
import com.flux.core.domain.usecase.CaptureNoteUseCase
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ResolveAmbiguityUseCase
import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.core.model.FluxResult
import com.flux.mobile.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The phone's own capture, voice or text, with the same classification, questions and countdown as the watch. */
@HiltViewModel
class PhoneCaptureViewModel @Inject constructor(
    private val recognizer: VoiceRecognizer,
    observeCategories: ObserveCategoriesUseCase,
    private val capture: CaptureNoteUseCase,
    private val resolve: ResolveAmbiguityUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(PhoneCaptureUi())

    /** Classification or countdown in progress; closing the sheet cancels it so nothing is saved behind the user's back. */
    private var work: Job? = null
    private var saving = false

    val uiState: StateFlow<PhoneCaptureUi> = state.asStateFlow()
    val level: StateFlow<Float> = recognizer.level
    val categories: StateFlow<List<Category>> = observeCategories().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { recognizer.state.collect(::onVoice) }
    }

    private fun onVoice(voice: VoiceState) {
        when (voice) {
            is VoiceState.Listening -> if (voice.partial.isNotBlank()) state.update { it.copy(text = voice.partial) }
            is VoiceState.Heard -> {
                state.update { it.copy(text = voice.text, phase = CapturePhase.EDITING) }
                classify()
            }
            is VoiceState.Failed -> state.update { it.copy(phase = CapturePhase.EDITING, voiceError = voice.error) }
            VoiceState.Idle -> Unit
        }
    }

    fun setText(text: String) = state.update { it.copy(text = text, errorMessage = null) }

    fun canListenInApp() = recognizer.isAvailable()

    /** Microphone permission already granted. */
    fun listen() {
        state.update { it.copy(phase = CapturePhase.LISTENING, voiceError = null, errorMessage = null) }
        recognizer.start()
    }

    fun stopListening() = recognizer.stop()

    fun permissionDenied() = state.update { it.copy(phase = CapturePhase.EDITING, voiceError = VoiceError.NO_PERMISSION) }

    fun voiceUnavailable() = state.update { it.copy(phase = CapturePhase.EDITING, voiceError = VoiceError.UNAVAILABLE) }

    fun heardFromSystem(text: String?) {
        if (text.isNullOrBlank()) return
        state.update { it.copy(text = text) }
        classify()
    }

    fun classify() {
        val text = state.value.text.trim()
        if (text.isEmpty()) return
        state.update { it.copy(phase = CapturePhase.CLASSIFYING, voiceError = null) }
        work?.cancel()
        work = viewModelScope.launch {
            when (val result = capture.classify(text)) {
                is FluxResult.Success -> showPreview(result.value, corrected = false)
                is FluxResult.Failure -> state.update { it.copy(phase = CapturePhase.EDITING, errorMessage = result.error.messageRes()) }
            }
        }
    }

    fun answerTime(dueAt: Long) = state.value.classification?.let { showPreview(resolve.chooseTime(it, dueAt), state.value.corrected) }

    fun answerCategory(categoryId: String) = state.value.classification?.let {
        showPreview(resolve.chooseCategory(it, categoryId), state.value.corrected || categoryId != it.categoryId)
    }

    fun edit() {
        work?.cancel()
        state.update { it.copy(phase = CapturePhase.EDITING, classification = null) }
    }

    /** Saved once, even if "Guardar" is tapped twice or the countdown ends at the same time. */
    fun save() {
        val current = state.value
        val classification = current.classification ?: return
        if (saving) return
        saving = true
        work?.cancel()
        viewModelScope.launch {
            capture.save(current.text, resolve.acceptDefaults(classification), current.corrected)
            state.update { it.copy(phase = CapturePhase.SAVED) }
        }
    }

    /** The sheet closed: this ViewModel outlives it, so the next "Nueva nota" must start clean. */
    fun reset() {
        work?.cancel()
        recognizer.release()
        saving = false
        state.value = PhoneCaptureUi()
    }

    private fun showPreview(classification: Classification, corrected: Boolean) {
        work?.cancel()
        state.update {
            it.copy(
                phase = CapturePhase.PREVIEW,
                classification = classification,
                corrected = corrected,
                secondsLeft = PhoneCaptureUi.AUTO_SAVE_SECONDS,
            )
        }
        if (classification.needsAnswer) return
        work = viewModelScope.launch {
            while (state.value.secondsLeft > 0) {
                delay(TICK_MS)
                state.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
            save()
        }
    }

    override fun onCleared() {
        recognizer.release()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}
