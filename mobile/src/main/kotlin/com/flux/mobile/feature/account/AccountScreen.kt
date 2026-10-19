package com.flux.mobile.feature.account

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.feature.onboarding.OnboardingViewModel
import com.flux.mobile.ui.common.OnboardingScaffold

@Composable
fun AccountScreen(onBack: () -> Unit, viewModel: AccountViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.ACCOUNT),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(if (state.createAccount) R.string.account_title_sign_up else R.string.account_title_sign_in),
        subtitle = stringResource(if (state.firebaseConfigured) R.string.account_subtitle else R.string.account_local_explanation),
        primaryLabel = stringResource(primaryLabelRes(state)),
        onPrimary = if (state.firebaseConfigured) viewModel::submit else viewModel::continueLocally,
        loading = state.loading,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        if (state.firebaseConfigured) AccountFields(state, viewModel)
        state.error?.let {
            Text(
                text = stringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }

    if (state.askTransfer) {
        TransferDialog(
            needsPassword = state.transferNeedsPassword,
            onConfirm = viewModel::confirmTransfer,
            onDismiss = viewModel::cancelTransfer,
        )
    }
}

private fun primaryLabelRes(state: AccountUiState): Int = when {
    !state.firebaseConfigured -> R.string.account_local
    state.createAccount -> R.string.account_sign_up
    else -> R.string.account_sign_in
}
