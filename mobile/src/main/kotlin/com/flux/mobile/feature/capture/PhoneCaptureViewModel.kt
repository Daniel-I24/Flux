package com.flux.mobile.feature.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.speech.VoiceRecognizer
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
    recognizer: VoiceRecognizer,
    observeCategories: ObserveCategoriesUseCase,
    private val capture: CaptureNoteUseCase,
    private val resolve: ResolveAmbiguityUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(PhoneCaptureUi())

    /** Classification or countdown in progress; closing the sheet cancels it so nothing is saved behind the user's back. */
    private var work: Job? = null
    private var saving = false

    val uiState: StateFlow<PhoneCaptureUi> = state.asStateFlow()
    val categories: StateFlow<List<Category>> = observeCategories().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Voice input; whatever it hears is classified like typed text. */
    val voice = PhoneCaptureVoice(recognizer, state, onHeard = ::classify).also { it.observe(viewModelScope) }

    fun setText(text: String) = state.update { it.copy(text = text, errorMessage = null) }

    /** The user is choosing: the countdown stops and "Guardar" saves. */
    fun setLead(minutes: Int?) {
        work?.cancel()
        state.update { it.copy(alarmLeadMinutes = minutes, leadChosen = true) }
    }

    /** "Nueva nota" inside a category (null = the classifier decides). */
    fun fixCategory(categoryId: String?) = state.update { it.copy(fixedCategoryId = categoryId) }

    fun classify() {
        val text = state.value.text.trim()
        if (text.isEmpty()) return
        state.update { it.copy(phase = CapturePhase.CLASSIFYING, voiceError = null) }
        work?.cancel()
        work = viewModelScope.launch {
            when (val result = capture.classify(text)) {
                is FluxResult.Success -> result.value.takeIf { it.isNote }?.let { showPreview(it, corrected = false) }
                    ?: state.update { it.notANote() }
                is FluxResult.Failure -> state.update { it.copy(phase = CapturePhase.EDITING, errorMessage = result.error.messageRes()) }
            }
        }
    }

    fun answerTime(dueAt: Long) = state.value.classification?.let { showPreview(resolve.chooseTime(it, dueAt), state.value.corrected) }

    /** A suggestion, a picked date and time, or null for "Guardar sin recordatorio". */
    fun answerReminder(dueAt: Long?) = state.value.classification?.let {
        showPreview(resolve.chooseReminder(it, dueAt), state.value.corrected)
    }

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
            capture.save(current.text, resolve.acceptDefaults(classification), current.corrected, current.alarmLeadMinutes)
            state.update { it.copy(phase = CapturePhase.SAVED) }
        }
    }

    /** The sheet closed: this ViewModel outlives it, so the next "Nueva nota" must start clean. */
    fun reset() {
        work?.cancel()
        voice.release()
        saving = false
        state.value = PhoneCaptureUi()
    }

    private fun showPreview(classification: Classification, corrected: Boolean) {
        work?.cancel()
        state.update { it.previewing(classification, corrected) }
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
        voice.release()
        super.onCleared()
    }

    private companion object {
        const val TICK_MS = 1_000L
    }
}
