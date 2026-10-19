package com.flux.mobile.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.usecase.ObserveOnboardingStepUseCase
import com.flux.core.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface AppDestination {
    data object Loading : AppDestination
    data class Onboarding(val step: OnboardingStep) : AppDestination
    data object Main : AppDestination
}

/** Decides the first screen: onboarding step, sign-in again, or the main app. */
@HiltViewModel
class GateViewModel @Inject constructor(
    observeSession: ObserveSessionUseCase,
    observeStep: ObserveOnboardingStepUseCase,
) : ViewModel() {
    val destination: StateFlow<AppDestination> = combine(observeSession(), observeStep()) { session, step ->
        when {
            session == null && step > OnboardingStep.WELCOME -> AppDestination.Onboarding(OnboardingStep.ACCOUNT)
            step != OnboardingStep.DONE -> AppDestination.Onboarding(step)
            else -> AppDestination.Main
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), AppDestination.Loading)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
