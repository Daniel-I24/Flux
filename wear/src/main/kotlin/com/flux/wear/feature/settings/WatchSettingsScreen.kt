package com.flux.wear.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import com.flux.core.model.VibrationPattern
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
fun WatchSettingsScreen(viewModel: WatchSettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.settings_title)) } }
            item { ListHeader { Text(stringResource(R.string.settings_vibration)) } }
            items(VibrationPattern.entries, key = { it.name }) { pattern ->
                RadioButton(
                    selected = state.settings.vibration == pattern,
                    onSelect = {
                        haptics.tap()
                        viewModel.setVibration(pattern)
                    },
                    label = { Text(stringResource(pattern.labelRes())) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SwitchButton(
                    checked = state.settings.soundEnabled,
                    onCheckedChange = viewModel::setSoundEnabled,
                    label = { Text(stringResource(R.string.settings_sound)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SwitchButton(
                    checked = state.settings.theme == WatchTheme.HIGH_CONTRAST,
                    onCheckedChange = viewModel::setHighContrast,
                    label = { Text(stringResource(R.string.settings_theme_contrast)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { ListHeader { Text(stringResource(R.string.settings_link)) } }
            item {
                val label = when {
                    state.linkedAccount == null -> stringResource(R.string.settings_not_linked)
                    state.phone.userName != null -> stringResource(R.string.settings_linked_to, state.phone.userName!!)
                    else -> stringResource(R.string.settings_linked_unknown)
                }
                Text(label)
            }
            if (state.linkedAccount != null) {
                item {
                    OutlinedButton(
                        onClick = {
                            haptics.confirm()
                            viewModel.unlink()
                        },
                        label = { Text(stringResource(R.string.settings_unlink)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
