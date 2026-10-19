package com.flux.wear.feature.alert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.GetReminderUseCase
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import com.flux.wear.alarm.ReminderActions
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertUiState(
    val note: Note? = null,
    val settings: WatchSettings = WatchSettings(),
    /** Note sound, then category sound, then the watch's own default. */
    val sound: SoundRef = WatchSettings().sound,
    val handled: Boolean = false,
)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val getReminder: GetReminderUseCase,
    private val store: WatchStateStore,
    private val actions: ReminderActions,
) : ViewModel() {
    private val noteId: String = checkNotNull(savedState[AlertActivity.EXTRA_NOTE_ID])
    private val state = MutableStateFlow(AlertUiState())
    val uiState: StateFlow<AlertUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = store.settings()
            val reminder = getReminder(noteId, settings.sound)
            state.value = AlertUiState(reminder?.note, settings, reminder?.sound ?: settings.sound)
        }
    }

    fun complete() = handle { actions.complete(noteId) }

    fun snooze() = handle { actions.snooze(noteId) }

    private fun handle(action: suspend () -> Unit) {
        viewModelScope.launch {
            action()
            state.value = state.value.copy(handled = true)
        }
    }
}
