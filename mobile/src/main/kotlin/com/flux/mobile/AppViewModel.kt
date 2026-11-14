package com.flux.mobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.ThemeMode
import com.flux.core.model.ThemePalette
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ThemeChoice(val mode: ThemeMode = ThemeMode.SYSTEM, val palette: ThemePalette = ThemePalette.ANDEAN_SUNSET)

/** App-wide UI preferences (theme) read at the activity level. */
@HiltViewModel
class AppViewModel @Inject constructor(observeSettings: ObserveSettingsUseCase) : ViewModel() {
    val theme: StateFlow<ThemeChoice> = observeSettings()
        .map { ThemeChoice(it.themeMode, it.palette) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeChoice())
}
