package com.flux.mobile.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.ThemeMode
import com.flux.mobile.R

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
fun AccountSettings(email: String?, localAccount: Boolean, onProfile: () -> Unit, onRoutine: () -> Unit, onSignOut: () -> Unit) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    SettingsSection(stringResource(R.string.settings_account)) {
        Text(
            text = if (localAccount) {
                stringResource(R.string.settings_local_account)
            } else {
                email
                    ?: stringResource(R.string.settings_google_account)
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            OutlinedButton(onClick = onProfile) { Text(stringResource(R.string.settings_profile)) }
            OutlinedButton(onClick = onRoutine) { Text(stringResource(R.string.settings_routine)) }
        }
        TextButton(onClick = { confirming = true }) { Text(stringResource(R.string.settings_sign_out)) }
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.settings_sign_out)) },
            text = { Text(stringResource(R.string.settings_sign_out_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    onSignOut()
                }) { Text(stringResource(R.string.settings_sign_out)) }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(theme: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    val labels = mapOf(
        ThemeMode.SYSTEM to R.string.settings_theme_system,
        ThemeMode.LIGHT to R.string.settings_theme_light,
        ThemeMode.DARK to R.string.settings_theme_dark,
    )
    SettingsSection(stringResource(R.string.settings_appearance)) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == theme,
                    onClick = { onTheme(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                ) { Text(stringResource(labels.getValue(mode))) }
            }
        }
    }
}

@Composable
fun AiSettings(status: AiStatus, onRefresh: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_ai)) {
        val text = when (status) {
            AiStatus.CHECKING -> R.string.settings_ai_checking
            AiStatus.REMOTE -> R.string.settings_ai_remote
            AiStatus.LOCAL -> R.string.settings_ai_local
        }
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRefresh, enabled = status != AiStatus.CHECKING) { Text(stringResource(R.string.settings_ai_refresh)) }
    }
}

@Composable
fun DemoSettings(onLoad: () -> Unit, onClear: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_demo)) {
        Text(
            stringResource(R.string.settings_demo_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            OutlinedButton(onClick = onLoad) { Text(stringResource(R.string.settings_demo_load)) }
            TextButton(onClick = onClear) { Text(stringResource(R.string.settings_demo_clear)) }
        }
    }
}
