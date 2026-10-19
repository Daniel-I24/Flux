package com.flux.mobile.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.usecase.SetOnboardingStepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Moves between onboarding steps; the current step is persisted so the flow resumes after closing the app. */
@HiltViewModel
class OnboardingViewModel @Inject constructor(private val setStep: SetOnboardingStepUseCase) : ViewModel() {
    fun goTo(step: OnboardingStep) {
        viewModelScope.launch { setStep(step) }
    }

    fun next(current: OnboardingStep) = goTo(OnboardingStep.entries[(current.ordinal + 1).coerceAtMost(OnboardingStep.DONE.ordinal)])

    fun back(current: OnboardingStep) = goTo(OnboardingStep.entries[(current.ordinal - 1).coerceAtLeast(0)])

    companion object {
        /** Steps shown with a progress bar (WELCOME … WATCH). */
        val VISIBLE_STEPS = OnboardingStep.DONE.ordinal

        fun positionOf(step: OnboardingStep) = step.ordinal + 1
    }
}
