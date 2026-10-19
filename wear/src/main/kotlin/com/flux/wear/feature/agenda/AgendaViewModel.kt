package com.flux.wear.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.ObserveUpcomingRemindersUseCase
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

@HiltViewModel
class AgendaViewModel @Inject constructor(
    observeUpcoming: ObserveUpcomingRemindersUseCase,
    categoryRepository: CategoryRepository,
    private val actions: ReminderActions,
    clock: Clock,
) : ViewModel() {
    private val today = Instant.ofEpochMilli(clock.now()).atZone(clock.zone()).toLocalDate()
    private val startOfToday = today.atStartOfDay(clock.zone()).toInstant().toEpochMilli()

    private val zone = clock.zone()

    val uiState: StateFlow<AgendaUiState> = combine(
        observeUpcoming(startOfToday, AGENDA_SIZE),
        categoryRepository.observeCategories(),
        ::toUiState
    )
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AgendaUiState())

    private fun toUiState(notes: List<Note>, categories: List<Category>): AgendaUiState {
        val (todayNotes, later) = notes.partition { note ->
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
