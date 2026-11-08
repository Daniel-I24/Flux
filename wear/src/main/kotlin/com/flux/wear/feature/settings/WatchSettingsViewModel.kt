package com.flux.wear.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.model.VibrationPattern
import com.flux.wear.data.PhoneState
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import com.flux.wear.data.WatchTheme
import com.flux.wear.sync.LinkRequests
import com.flux.wear.sync.PhoneLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WatchSettingsUiState(
    val settings: WatchSettings = WatchSettings(),
    val phone: PhoneState = PhoneState(),
    val linkedAccount: String? = null,
)

@HiltViewModel
class WatchSettingsViewModel @Inject constructor(
    private val store: WatchStateStore,
    private val linkRequests: LinkRequests,
    private val launcher: PhoneLauncher,
) : ViewModel() {
    private var finishing = false

    val uiState: StateFlow<WatchSettingsUiState> =
        combine(store.observeSettings(), store.observePhoneState(), store.observeLinkedAccount(), ::WatchSettingsUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), WatchSettingsUiState())

    fun setVibration(pattern: VibrationPattern) = update { it.copy(vibration = pattern) }

    fun setSoundEnabled(enabled: Boolean) = update { it.copy(soundEnabled = enabled) }

    fun setHighContrast(enabled: Boolean) = update { it.copy(theme = if (enabled) WatchTheme.HIGH_CONTRAST else WatchTheme.DEFAULT) }

    /** Saves first and only then leaves: leaving pops this screen and would cancel a save still in progress. */
    fun finishTutorial(onSaved: () -> Unit) {
        if (finishing) return
        finishing = true
        viewModelScope.launch {
            store.updateSettings { it.copy(tutorialDone = true) }
            onSaved()
        }
    }

    /** Opens FLUX on the phone at Ajustes > Reloj, which sends the code to confirm here. */
    fun link(onResult: (Boolean) -> Unit) {
        viewModelScope.launch { onResult(launcher.openLink()) }
    }

    fun unlink() {
        viewModelScope.launch { linkRequests.unlink(notifyPhone = true) }
    }

    private fun update(transform: (WatchSettings) -> WatchSettings) {
        viewModelScope.launch { store.updateSettings(transform) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
