package com.flux.mobile.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.alarm.ActiveAlarm
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveNoteUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.Category
import com.flux.core.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlarmUiState(
    val note: Note? = null,
    val category: Category? = null,
    val snoozeMinutes: Int = 0,
    val ringing: Boolean = false,
    val loading: Boolean = true,
    /** Completed, snoozed or deleted here or on the watch: the screen closes itself. */
    val handled: Boolean = false,
)

@HiltViewModel
class AlarmViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeNote: ObserveNoteUseCase,
    observeCategories: ObserveCategoriesUseCase,
    observeSettings: ObserveSettingsUseCase,
    activeAlarm: ActiveAlarm,
    private val actions: ReminderActions,
    private val clock: Clock,
) : ViewModel() {
    private val noteId: String = checkNotNull(savedState[PhoneAlarmTarget.EXTRA_NOTE_ID])

    val uiState: StateFlow<AlarmUiState> = combine(
        observeNote(noteId),
        observeCategories(),
        observeSettings(),
        activeAlarm.noteId,
    ) { note, categories, settings, ringing ->
        AlarmUiState(
            note = note,
            category = categories.firstOrNull { it.id == note?.categoryId },
            snoozeMinutes = settings.snoozeMinutes,
            ringing = ringing == noteId,
            loading = false,
            handled = note == null || !note.isActive || note.completed || (note.dueAt ?: 0) > clock.now(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlarmUiState())

    fun complete() {
        viewModelScope.launch { actions.complete(noteId) }
    }

    fun snooze() {
        viewModelScope.launch { actions.snooze(noteId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
