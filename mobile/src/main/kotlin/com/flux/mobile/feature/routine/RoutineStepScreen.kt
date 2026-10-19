package com.flux.mobile.feature.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.feature.onboarding.OnboardingViewModel
import com.flux.mobile.ui.common.OnboardingScaffold

@Composable
fun RoutineStepScreen(onDone: () -> Unit, onBack: () -> Unit, viewModel: RoutineViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.ROUTINE),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.routine_title),
        subtitle = stringResource(R.string.routine_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = { viewModel.save(onDone) },
        loading = state.saving,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        RoutineForm(state, viewModel)
    }
}
