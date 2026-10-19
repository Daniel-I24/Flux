package com.flux.mobile.feature.notes

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.CaptureNoteUseCase
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.DeleteNoteUseCase
import com.flux.core.domain.usecase.EditNoteUseCase
import com.flux.core.domain.usecase.NoteEdit
import com.flux.core.domain.usecase.NoteQuery
import com.flux.core.domain.usecase.NoteSort
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveNotesUseCase
import com.flux.core.domain.usecase.RestoreNoteUseCase
import com.flux.core.model.Category
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.mobile.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val query: NoteQuery = NoteQuery(),
    val notes: List<Note> = emptyList(),
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true,
)

/** One-shot events for the screen (snackbar with "Deshacer", errors). */
sealed interface NotesEvent {
    data class Deleted(val noteId: String) : NotesEvent
    data class Failed(@param:StringRes val message: Int) : NotesEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    observeNotes: ObserveNotesUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val capture: CaptureNoteUseCase,
    private val complete: CompleteNoteUseCase,
    private val delete: DeleteNoteUseCase,
    private val restore: RestoreNoteUseCase,
    private val edit: EditNoteUseCase,
) : ViewModel() {
    private val query = MutableStateFlow(NoteQuery())
    private val events = MutableStateFlow<NotesEvent?>(null)
    val event: StateFlow<NotesEvent?> = events.asStateFlow()

    val uiState: StateFlow<NotesUiState> = combine(query, query.flatMapLatest(observeNotes::invoke), observeCategories()) {
            q,
            notes,
            categories
        ->
        NotesUiState(q, notes, categories, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NotesUiState())

    fun setText(text: String) = query.update { it.copy(text = text) }

    fun setCategory(categoryId: String?) = query.update { it.copy(categoryId = categoryId) }

    fun setSort(sort: NoteSort) = query.update { it.copy(sort = sort) }

    fun setIncludeCompleted(include: Boolean) = query.update { it.copy(includeCompleted = include) }

    fun toggleCompleted(note: Note) = launchResult { complete(note.id, completed = !note.completed) }

    fun delete(note: Note) = viewModelScope.launch {
        if (delete.invoke(note.id) is FluxResult.Success) events.value = NotesEvent.Deleted(note.id)
    }

    fun undoDelete(noteId: String) = launchResult { restore(noteId) }

    fun save(noteId: String, change: NoteEdit, onDone: () -> Unit) = launchResult(onDone) { edit(noteId, change) }

    /** The dialog closes either way; a failure is reported in the snackbar. */
    fun create(text: String, onDone: () -> Unit) = viewModelScope.launch {
        val result = capture(text)
        onDone()
        if (result is FluxResult.Failure) events.value = NotesEvent.Failed(result.error.messageRes())
    }

    fun consumeEvent() {
        events.value = null
    }

    private fun launchResult(onSuccess: () -> Unit = {}, block: suspend () -> FluxResult<*>) {
        viewModelScope.launch {
            when (val result = block()) {
                is FluxResult.Success -> onSuccess()
                is FluxResult.Failure -> events.value = NotesEvent.Failed(result.error.messageRes())
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
