package com.flux.wear.feature.overdue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.wear.alarm.ReminderActions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OverdueUiState(
    val notes: List<Note> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val loading: Boolean = true,
)

/** Reminders whose time passed: complete, reschedule (in 1 hour / tomorrow morning) or delete. */
@HiltViewModel
class OverdueViewModel @Inject constructor(
    observeSections: ObserveNoteSectionsUseCase,
    categoryRepository: CategoryRepository,
    private val actions: ReminderActions,
    private val clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<OverdueUiState> = combine(observeSections(), categoryRepository.observeCategories()) { sections, categories ->
        OverdueUiState(sections.overdue, categories.associateBy { it.id }, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OverdueUiState())

    fun complete(noteId: String) = launch { actions.complete(noteId) }

    fun inOneHour(noteId: String) = launch { actions.reschedule(noteId, clock.now() + HOUR_MS) }

    fun tomorrowMorning(noteId: String) = launch {
        val tomorrow = Instant.ofEpochMilli(clock.now()).atZone(clock.zone()).toLocalDate().plusDays(1)
        actions.reschedule(noteId, tomorrow.atTime(LocalTime.of(MORNING_HOUR, 0)).atZone(clock.zone()).toInstant().toEpochMilli())
    }

    fun delete(noteId: String) = launch { actions.delete(noteId) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val HOUR_MS = 60 * 60 * 1_000L
        const val MORNING_HOUR = 9
    }
}
