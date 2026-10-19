package com.flux.mobile.feature.link

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.feature.onboarding.OnboardingViewModel
import com.flux.mobile.ui.common.OnboardingScaffold

/** Last onboarding step: link the watch now or later from Ajustes. */
@Composable
fun WatchLinkStepScreen(onDone: () -> Unit, onBack: () -> Unit) {
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.WATCH),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.watch_step_title),
        subtitle = stringResource(R.string.watch_step_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = onDone,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        WatchLinkPanel()
    }
}
