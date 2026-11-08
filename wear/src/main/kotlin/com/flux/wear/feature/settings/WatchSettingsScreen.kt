package com.flux.wear.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.flux.core.model.VibrationPattern
import com.flux.wear.BuildConfig
import com.flux.wear.R
import com.flux.wear.data.WatchTheme
import com.flux.wear.ui.rememberHaptics

private fun VibrationPattern.labelRes(): Int = when (this) {
    VibrationPattern.SHORT -> R.string.vibration_short
    VibrationPattern.DOUBLE -> R.string.vibration_double
    VibrationPattern.LONG -> R.string.vibration_long
    VibrationPattern.HEARTBEAT -> R.string.vibration_heartbeat
    VibrationPattern.OFF -> R.string.vibration_off
}

/** Vibration, sound, theme and link status, all inside the watch app. */
@Composable
fun WatchSettingsScreen(onTutorial: () -> Unit, viewModel: WatchSettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val opened = stringResource(R.string.problem_opened_on_phone)
    val failed = stringResource(R.string.problem_open_failed)
    WatchSettingsContent(
        state,
        WatchSettingsActions(
            vibration = {
                haptics.tap()
                viewModel.setVibration(it)
            },
            sound = viewModel::setSoundEnabled,
            highContrast = viewModel::setHighContrast,
            tutorial = onTutorial,
            link = {
                haptics.tap()
                viewModel.link { ok -> Toast.makeText(context, if (ok) opened else failed, Toast.LENGTH_SHORT).show() }
            },
            unlink = {
                haptics.confirm()
                viewModel.unlink()
            },
        ),
    )
}

data class WatchSettingsActions(
    val vibration: (VibrationPattern) -> Unit,
    val sound: (Boolean) -> Unit,
    val highContrast: (Boolean) -> Unit,
    val tutorial: () -> Unit,
    val link: () -> Unit,
    val unlink: () -> Unit,
)

/** Stateless, so every watch shape can be previewed. Vertical list only: nothing competes with swipe-back. */
@Composable
fun WatchSettingsContent(state: WatchSettingsUiState, actions: WatchSettingsActions) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.settings_title)) } }
            item { ListHeader { Text(stringResource(R.string.settings_vibration)) } }
            items(VibrationPattern.entries, key = { it.name }) { pattern ->
                RadioButton(
                    selected = state.settings.vibration == pattern,
                    onSelect = { actions.vibration(pattern) },
                    label = { Text(stringResource(pattern.labelRes())) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SwitchButton(
                    checked = state.settings.soundEnabled,
                    onCheckedChange = actions.sound,
                    label = { Text(stringResource(R.string.settings_sound)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SwitchButton(
                    checked = state.settings.theme == WatchTheme.HIGH_CONTRAST,
                    onCheckedChange = actions.highContrast,
                    label = { Text(stringResource(R.string.settings_theme_contrast)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedButton(onClick = actions.tutorial, label = {
                    Text(stringResource(R.string.settings_tutorial))
                }, modifier = Modifier.fillMaxWidth())
            }
            linkSection(state, onLink = actions.link, onUnlink = actions.unlink)
            item {
                Text(
                    stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private fun TransformingLazyColumnScope.linkSection(state: WatchSettingsUiState, onLink: () -> Unit, onUnlink: () -> Unit) {
    item { ListHeader { Text(stringResource(R.string.settings_link)) } }
    item {
        val label = when {
            state.linkedAccount == null -> stringResource(R.string.settings_not_linked)
            state.phone.userName != null -> stringResource(R.string.settings_linked_to, state.phone.userName)
            else -> stringResource(R.string.settings_linked_unknown)
        }
        Text(label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
    item {
        val linked = state.linkedAccount != null
        OutlinedButton(
            onClick = if (linked) onUnlink else onLink,
            label = { Text(stringResource(if (linked) R.string.settings_unlink else R.string.settings_link_phone)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
