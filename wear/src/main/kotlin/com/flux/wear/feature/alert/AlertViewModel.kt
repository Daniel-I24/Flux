package com.flux.wear.feature.alert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.GetNoteUseCase
import com.flux.core.model.Note
import com.flux.wear.alarm.ReminderActions
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertUiState(val note: Note? = null, val settings: WatchSettings = WatchSettings(), val handled: Boolean = false)

@HiltViewModel
class AlertViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val getNote: GetNoteUseCase,
    private val store: WatchStateStore,
    private val actions: ReminderActions,
) : ViewModel() {
    private val noteId: String = checkNotNull(savedState[AlertActivity.EXTRA_NOTE_ID])
    private val state = MutableStateFlow(AlertUiState())
    val uiState: StateFlow<AlertUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { state.value = AlertUiState(getNote(noteId), store.settings()) }
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
