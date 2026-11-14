package com.flux.mobile.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.domain.usecase.SetAlarmLeadUseCase
import com.flux.core.domain.usecase.SetThemePaletteUseCase
import com.flux.core.model.AppSettings
import com.flux.core.model.ThemePalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Settings the watch follows: the "Avisarme" lead (which also moves pending alarms) and the theme.
 * Used from Ajustes and from the first-launch flow.
 */
@HiltViewModel
class SyncedSettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val setAlarmLead: SetAlarmLeadUseCase,
    private val setPalette: SetThemePaletteUseCase,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppSettings())

    fun selectLead(minutes: Int?) {
        viewModelScope.launch { setAlarmLead(minutes ?: AppSettings.DEFAULT_ALARM_LEAD_MINUTES) }
    }

    fun selectPalette(palette: ThemePalette) {
        viewModelScope.launch { setPalette(palette) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
