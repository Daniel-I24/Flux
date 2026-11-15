package com.flux.mobile.feature.onboarding

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.domain.repository.OnboardingStep
import com.flux.mobile.R
import com.flux.mobile.feature.settings.SyncedSettingsViewModel
import com.flux.mobile.ui.common.AlarmLeadPicker
import com.flux.mobile.ui.common.OnboardingScaffold
import com.flux.mobile.ui.common.ThemePicker

/** Before the first note: how early reminders ring and the theme (both shared with the watch). */
@Composable
fun PreferencesStepScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    synced: SyncedSettingsViewModel = hiltViewModel(),
) {
    val settings by synced.settings.collectAsStateWithLifecycle()
    val dark = settings.themeMode.isDark(isSystemInDarkTheme())
    OnboardingScaffold(
        step = OnboardingViewModel.positionOf(OnboardingStep.PREFERENCES),
        totalSteps = OnboardingViewModel.VISIBLE_STEPS,
        title = stringResource(R.string.preferences_title),
        subtitle = stringResource(R.string.preferences_subtitle),
        primaryLabel = stringResource(R.string.action_continue),
        onPrimary = onDone,
        secondaryLabel = stringResource(R.string.action_back),
        onSecondary = onBack,
    ) {
        Text(stringResource(R.string.alarm_lead_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.alarm_lead_hint), style = MaterialTheme.typography.bodySmall)
        AlarmLeadPicker(settings.alarmLeadMinutes, synced::selectLead)
        Text(stringResource(R.string.preferences_theme), style = MaterialTheme.typography.titleMedium)
        ThemePicker(settings.palette, synced::selectPalette, dark = dark)
    }
}
