package com.flux.mobile.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.alarm.ActiveAlarm
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveNoteUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.alertHandled
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
) : ViewModel() {
    /** The reminder's time when this alert opened; a different time later means it was snoozed or moved. */
    private var openedDueAt: Long? = null

    private fun ringingDueAt(note: Note?): Long? {
        if (openedDueAt == null) openedDueAt = note?.dueAt
        return openedDueAt
    }

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
            handled = note.alertHandled(ringingDueAt(note)),
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
