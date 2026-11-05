package com.flux.mobile.feature.notes

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.DeleteNoteUseCase
import com.flux.core.domain.usecase.EditNoteUseCase
import com.flux.core.domain.usecase.NoteEdit
import com.flux.core.domain.usecase.NoteQuery
import com.flux.core.domain.usecase.NoteSections
import com.flux.core.domain.usecase.NoteSort
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.domain.usecase.RescheduleNoteUseCase
import com.flux.core.domain.usecase.RestoreNoteUseCase
import com.flux.core.model.Category
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.mobile.sync.ReminderSync
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val query: NoteQuery = NoteQuery(),
    val sections: NoteSections = NoteSections(),
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true,
) {
    fun find(id: String?): Note? = id?.let { wanted ->
        (sections.upcoming + sections.pending + sections.overdue + sections.completed).firstOrNull { it.id == wanted }
    }
}

/** One-shot events for the screen (snackbar with "Deshacer", errors). */
sealed interface NotesEvent {
    data class Deleted(val noteId: String) : NotesEvent
    data class Completed(val noteId: String) : NotesEvent
    data class Failed(@param:StringRes val message: Int) : NotesEvent
}

/** Notes in four live sections (Próximos, Pendientes, Vencidos, Completados) with search and filters. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    observeSections: ObserveNoteSectionsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val complete: CompleteNoteUseCase,
    private val delete: DeleteNoteUseCase,
    private val restore: RestoreNoteUseCase,
    private val edit: EditNoteUseCase,
    private val reschedule: RescheduleNoteUseCase,
    private val reminderSync: ReminderSync,
    private val clock: Clock,
) : ViewModel() {
    private val query = MutableStateFlow(NoteQuery())
    private val events = MutableStateFlow<NotesEvent?>(null)
    val event: StateFlow<NotesEvent?> = events.asStateFlow()

    val uiState: StateFlow<NotesUiState> = combine(
        query.flatMapLatest { q -> observeSections(q).map { q to it } },
        observeCategories(),
    ) { (q, sections), categories -> NotesUiState(q, sections, categories, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), NotesUiState())

    fun setText(text: String) = query.update { it.copy(text = text) }

    fun setCategory(categoryId: String?) = query.update { it.copy(categoryId = categoryId) }

    fun setSort(sort: NoteSort) = query.update { it.copy(sort = sort) }

    fun setIncludeCompleted(include: Boolean) = query.update { it.copy(includeCompleted = include) }

    /** Completing offers "Deshacer"; the watch stops ringing too. */
    fun toggleCompleted(note: Note) {
        viewModelScope.launch {
            val done = !note.completed
            if (complete(note.id, completed = done) is FluxResult.Success && done) {
                reminderSync.completed(note.id)
                events.value = NotesEvent.Completed(note.id)
            }
        }
    }

    fun undoComplete(noteId: String) = launchResult { complete(noteId, completed = false) }

    fun delete(note: Note) = viewModelScope.launch {
        if (delete.invoke(note.id) is FluxResult.Success) events.value = NotesEvent.Deleted(note.id)
    }

    fun undoDelete(noteId: String) = launchResult { restore(noteId) }

    fun inOneHour(noteId: String) = launchResult { reschedule(noteId, clock.now() + HOUR_MS) }

    fun save(noteId: String, change: NoteEdit, onDone: () -> Unit) = launchResult(onDone) { edit(noteId, change) }

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
        const val HOUR_MS = 60 * 60 * 1_000L
    }
}
