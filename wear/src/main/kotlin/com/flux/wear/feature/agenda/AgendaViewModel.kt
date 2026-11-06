package com.flux.wear.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.NoteSections
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.wear.alarm.ReminderActions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AgendaUiState(
    val today: List<Note> = emptyList(),
    val upcoming: List<Note> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val loading: Boolean = true,
)

/** Upcoming reminders, live: one that becomes overdue moves to Vencidos without reopening the screen. */
@HiltViewModel
class AgendaViewModel @Inject constructor(
    observeSections: ObserveNoteSectionsUseCase,
    categoryRepository: CategoryRepository,
    private val actions: ReminderActions,
    private val clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<AgendaUiState> = combine(observeSections(), categoryRepository.observeCategories(), ::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AgendaUiState())

    private fun toUiState(sections: NoteSections, categories: List<Category>): AgendaUiState {
        val zone = clock.zone()
        val today = Instant.ofEpochMilli(clock.now()).atZone(zone).toLocalDate()
        val (todayNotes, later) = sections.upcoming.take(AGENDA_SIZE).partition { note ->
            note.dueAt?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() == today } ?: false
        }
        return AgendaUiState(todayNotes, later, categories.associateBy { it.id }, loading = false)
    }

    fun complete(noteId: String) {
        viewModelScope.launch { actions.complete(noteId) }
    }

    private companion object {
        const val AGENDA_SIZE = 30
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
