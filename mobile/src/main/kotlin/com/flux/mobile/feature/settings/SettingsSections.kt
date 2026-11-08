package com.flux.mobile.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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

/** A card with its title; [header] replaces the plain title (e.g. a foldable one). */
@Composable
fun SettingsSection(title: String, header: (@Composable () -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            header?.invoke() ?: Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
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
fun TutorialSettings(onRepeat: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_tutorial)) {
        Text(
            stringResource(R.string.settings_tutorial_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onRepeat) { Text(stringResource(R.string.settings_tutorial_repeat)) }
    }
}
