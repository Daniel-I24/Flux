package com.flux.mobile.feature.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.repository.AuthSession
import com.flux.core.domain.usecase.CheckRemoteAiUseCase
import com.flux.core.domain.usecase.ClearDemoDataUseCase
import com.flux.core.domain.usecase.LoadDemoDataUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ObserveSessionUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.domain.usecase.SignOutUseCase
import com.flux.core.domain.usecase.UpdateSettingsUseCase
import com.flux.core.model.AppSettings
import com.flux.core.model.SoundRef
import com.flux.core.model.ThemeMode
import com.flux.core.model.UserProfile
import com.flux.core.model.VibrationPattern
import com.flux.mobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AiStatus { CHECKING, REMOTE, LOCAL }

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val profile: UserProfile? = null,
    val email: String? = null,
    val localAccount: Boolean = false,
    val ai: AiStatus = AiStatus.CHECKING,
    val loading: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    observeProfile: ObserveProfileUseCase,
    observeSession: ObserveSessionUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val checkRemoteAi: CheckRemoteAiUseCase,
    private val signOut: SignOutUseCase,
    private val loadDemo: LoadDemoDataUseCase,
    private val clearDemo: ClearDemoDataUseCase,
    private val demoContent: DemoContent,
) : ViewModel() {
    private val ai = MutableStateFlow(AiStatus.CHECKING)
    private val messages = MutableStateFlow<Int?>(null)

    /** One-shot message for the snackbar. */
    val message: StateFlow<Int?> = messages.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(observeSettings(), observeProfile(), observeSession(), ai, ::toUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    init {
        refreshAi()
    }

    fun refreshAi() {
        ai.value = AiStatus.CHECKING
        viewModelScope.launch { ai.value = if (checkRemoteAi()) AiStatus.REMOTE else AiStatus.LOCAL }
    }

    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun setSnooze(minutes: Int) = update { it.copy(snoozeMinutes = minutes) }

    fun setVibration(pattern: VibrationPattern) = update { it.copy(vibration = pattern) }

    fun setDefaultSound(sound: SoundRef) = update { it.copy(defaultSound = sound) }

    fun setQuietHours(enabled: Boolean) = update { it.copy(quietHoursEnabled = enabled) }

    fun signOut() {
        viewModelScope.launch { signOut.invoke() }
    }

    fun loadDemo() = launchWithMessage(R.string.settings_demo_loaded) { loadDemo(demoContent.examples()) }

    fun clearDemo() = launchWithMessage(R.string.settings_demo_cleared) { clearDemo.invoke() }

    fun consumeMessage() {
        messages.value = null
    }

    private fun toUiState(settings: AppSettings, profile: UserProfile?, session: AuthSession?, aiStatus: AiStatus) =
        SettingsUiState(settings, profile, session?.email, session?.isLocal == true, aiStatus, loading = false)

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }

    private fun launchWithMessage(@StringRes message: Int, block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
            messages.value = message
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
