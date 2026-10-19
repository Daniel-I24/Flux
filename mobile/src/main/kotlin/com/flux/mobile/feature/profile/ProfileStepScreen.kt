package com.flux.mobile.feature.profile

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
fun ProfileStepScreen(onDone: () -> Unit, onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.PROFILE),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.profile_title),
        subtitle = stringResource(R.string.profile_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = { viewModel.save(onDone) },
        loading = state.saving,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        ProfileForm(state, viewModel)
    }
}
