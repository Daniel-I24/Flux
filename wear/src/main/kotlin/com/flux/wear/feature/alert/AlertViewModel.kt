package com.flux.wear.feature.alert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.GetReminderUseCase
import com.flux.core.domain.usecase.ObserveNoteUseCase
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import com.flux.wear.alarm.ReminderActions
import com.flux.wear.data.PhoneState
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertUiState(
    val note: Note? = null,
    val settings: WatchSettings = WatchSettings(),
    /** Note sound, then category sound, then the watch's own default. */
    val sound: SoundRef = WatchSettings().sound,
    val durationSeconds: Int = PhoneState().alarmDurationSeconds,
    val loading: Boolean = true,
    /** Completed, snoozed or deleted here or on the phone: the alert closes and stops vibrating. */
    val handled: Boolean = false,
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeNote: ObserveNoteUseCase,
    private val getReminder: GetReminderUseCase,
    private val store: WatchStateStore,
    private val actions: ReminderActions,
    private val clock: Clock,
) : ViewModel() {
    private val noteId: String = checkNotNull(savedState[AlertActivity.EXTRA_NOTE_ID])
    private val sound = MutableStateFlow<SoundRef?>(null)

    val uiState: StateFlow<AlertUiState> = combine(observeNote(noteId), store.observeSettings(), store.observePhoneState(), sound) {
            note,
            settings,
            phone,
            resolved,
        ->
        AlertUiState(
            note = note,
            settings = settings,
            sound = resolved ?: settings.sound,
            durationSeconds = phone.alarmDurationSeconds,
            loading = resolved == null,
            handled = note == null || !note.isActive || note.completed || (note.dueAt ?: 0) > clock.now(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AlertUiState())

    init {
        viewModelScope.launch {
            val fallback = store.settings().sound
            sound.value = getReminder(noteId, fallback)?.sound ?: fallback
        }
    }

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
