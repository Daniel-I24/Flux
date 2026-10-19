package com.flux.mobile.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.BuildConfig
import com.flux.mobile.R
import com.flux.mobile.feature.link.WatchLinkPanel

private enum class SettingsPage { MAIN, PROFILE, ROUTINE }

/** Ajustes: every setting of FLUX lives here (profile, account, watch, reminders, theme, AI, demo). */
@Composable
fun SettingsScreen(snackbar: SnackbarHostState, modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(message) {
        message?.let {
            viewModel.consumeMessage()
            snackbar.showSnackbar(context.getString(it))
        }
    }
    BackHandler(enabled = page != SettingsPage.MAIN) { page = SettingsPage.MAIN }

    when {
        page == SettingsPage.PROFILE -> ProfileSettingsPage(onDone = { page = SettingsPage.MAIN }, modifier = modifier)
        page == SettingsPage.ROUTINE -> RoutineSettingsPage(onDone = { page = SettingsPage.MAIN }, modifier = modifier)
        state.loading -> FluxLoadingState(modifier)
        else -> Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FluxSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(FluxSpacing.md),
        ) {
            AccountSettings(
                email = state.email,
                localAccount = state.localAccount,
                onProfile = { page = SettingsPage.PROFILE },
                onRoutine = { page = SettingsPage.ROUTINE },
                onSignOut = viewModel::signOut,
            )
            SettingsSection(stringResource(R.string.settings_watch)) { WatchLinkPanel() }
            ReminderSettings(state.settings, state.profile?.routine, viewModel)
            AppearanceSettings(state.settings.themeMode, viewModel::setTheme)
            AiSettings(state.ai, viewModel::refreshAi)
            DemoSettings(onLoad = viewModel::loadDemo, onClear = viewModel::clearDemo)
            Text(
                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
