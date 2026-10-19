package com.flux.mobile.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.feature.account.AccountScreen
import com.flux.mobile.feature.link.WatchLinkStepScreen
import com.flux.mobile.feature.permissions.PermissionsList
import com.flux.mobile.feature.profile.ProfileStepScreen
import com.flux.mobile.feature.routine.RoutineStepScreen
import com.flux.mobile.ui.common.OnboardingScaffold

/** Shows the persisted onboarding step; finishing the last one sets DONE and the gate opens the app. */
@Composable
fun OnboardingHost(step: OnboardingStep, viewModel: OnboardingViewModel = hiltViewModel()) {
    BackHandler(enabled = step > OnboardingStep.WELCOME) { viewModel.back(step) }
    AnimatedContent(targetState = step, label = "onboarding") { current ->
        when (current) {
            OnboardingStep.WELCOME -> WelcomeScreen(onStart = { viewModel.next(current) })
            OnboardingStep.ACCOUNT -> AccountScreen(onBack = { viewModel.back(current) })
            OnboardingStep.PROFILE -> ProfileStepScreen(onDone = { viewModel.next(current) }, onBack = { viewModel.back(current) })
            OnboardingStep.ROUTINE -> RoutineStepScreen(onDone = { viewModel.next(current) }, onBack = { viewModel.back(current) })
            OnboardingStep.PERMISSIONS -> PermissionsStepScreen(onDone = { viewModel.next(current) }, onBack = { viewModel.back(current) })
            OnboardingStep.WATCH -> WatchLinkStepScreen(onDone = { viewModel.next(current) }, onBack = { viewModel.back(current) })
            OnboardingStep.DONE -> Unit
        }
    }
}

@Composable
private fun PermissionsStepScreen(onDone: () -> Unit, onBack: () -> Unit) {
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.PERMISSIONS),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.permissions_title),
        subtitle = stringResource(R.string.permissions_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = onDone,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        PermissionsList()
    }
}
