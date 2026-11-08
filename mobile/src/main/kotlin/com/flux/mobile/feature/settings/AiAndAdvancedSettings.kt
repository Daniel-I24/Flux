package com.flux.mobile.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** One compact row: a colored dot, a short state and one line of help. "Comprobar de nuevo" only for the local AI. */
@Composable
fun AiSettings(status: AiStatus, onRefresh: () -> Unit) {
    SettingsSection(stringResource(R.string.settings_ai)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Box(Modifier.size(FluxSizes.statusDot).background(status.dotColor(), CircleShape))
            Text(stringResource(status.label()), style = MaterialTheme.typography.bodyLarge)
        }
        status.help()?.let {
            Text(stringResource(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (status.isLocal) TextButton(onClick = onRefresh) { Text(stringResource(R.string.settings_ai_refresh)) }
    }
}

/** "Avanzado", folded by default: the demo mode with a confirmation before loading the examples. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedSettings(onLoadDemo: () -> Unit, onClearDemo: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    SettingsSection(stringResource(R.string.settings_advanced), header = {
        Row(
            Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget).toggleable(open, role = Role.Button) { open = it },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.settings_advanced),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null)
        }
    }) {
        AnimatedVisibility(open) {
            Column(verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
                Text(stringResource(R.string.settings_demo), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.settings_demo_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
                    OutlinedButton(onClick = { confirming = true }) { Text(stringResource(R.string.settings_demo_load)) }
                    TextButton(onClick = onClearDemo) { Text(stringResource(R.string.settings_demo_clear)) }
                }
            }
        }
    }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.settings_demo_confirm_title)) },
            text = { Text(stringResource(R.string.settings_demo_description)) },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    onLoadDemo()
                }) { Text(stringResource(R.string.settings_demo_load)) }
            },
            dismissButton = { TextButton(onClick = { confirming = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun AiStatus.dotColor(): Color = when (this) {
    AiStatus.REMOTE -> MaterialTheme.colorScheme.primary
    AiStatus.CHECKING, AiStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.outline
}

private fun AiStatus.label(): Int = when (this) {
    AiStatus.CHECKING -> R.string.settings_ai_checking
    AiStatus.CONNECTING -> R.string.settings_ai_connecting
    AiStatus.REMOTE -> R.string.settings_ai_remote
    AiStatus.OFFLINE -> R.string.settings_ai_local
    AiStatus.NO_SESSION -> R.string.settings_ai_no_session
    AiStatus.NOT_CONFIGURED -> R.string.settings_ai_not_configured
}

private fun AiStatus.help(): Int? = when (this) {
    AiStatus.CHECKING -> null
    AiStatus.CONNECTING -> R.string.settings_ai_connecting_help
    AiStatus.REMOTE -> R.string.settings_ai_remote_help
    AiStatus.OFFLINE -> R.string.settings_ai_local_help
    AiStatus.NO_SESSION -> R.string.settings_ai_no_session_help
    AiStatus.NOT_CONFIGURED -> R.string.settings_ai_not_configured_help
}
