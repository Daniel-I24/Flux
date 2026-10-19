package com.flux.mobile.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.ui.common.OnboardingScaffold

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.WELCOME),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.welcome_title),
        subtitle = stringResource(R.string.welcome_subtitle),
        primaryLabel = stringResource(R.string.welcome_start),
        onPrimary = onStart,
    ) {
        Feature(Icons.Rounded.Watch, stringResource(R.string.welcome_point_capture))
        Feature(Icons.Rounded.AutoAwesome, stringResource(R.string.welcome_point_ai))
        Feature(Icons.Rounded.NotificationsActive, stringResource(R.string.welcome_point_alarms))
    }
}

@Composable
private fun Feature(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(FluxSizes.iconLg))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
