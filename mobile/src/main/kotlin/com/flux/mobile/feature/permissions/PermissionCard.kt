package com.flux.mobile.feature.permissions

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.data.permissions.AppPermission
import com.flux.core.data.permissions.PermissionStatus
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.designsystem.theme.FluxThemeExtras
import com.flux.mobile.R

/** Numbered step: icon, live state, why, what to touch, and the button that does it. */
@Composable
fun PermissionCard(number: Int, permission: AppPermission, status: PermissionStatus, onGrant: () -> Unit) {
    val copy = PERMISSION_COPY.getValue(permission)
    val done = status == PermissionStatus.GRANTED
    ElevatedCard(Modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
                Icon(copy.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    stringResource(R.string.permission_numbered, number, stringResource(copy.title)),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                StatusChip(status)
            }
            Text(stringResource(copy.why), style = MaterialTheme.typography.bodyMedium)
            if (!done) {
                Text(
                    stringResource(copy.steps),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status == PermissionStatus.UNKNOWN) {
                    OutlinedButton(onClick = onGrant, modifier = Modifier.fillMaxWidth()) { ButtonLabel(R.string.permission_open) }
                } else {
                    Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) { ButtonLabel(R.string.permission_grant) }
                }
            }
        }
    }
}

@Composable
private fun ButtonLabel(text: Int) {
    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = FluxSpacing.sm))
    Text(stringResource(text))
}

@Composable
private fun StatusChip(status: PermissionStatus) {
    val (label, color) = when (status) {
        PermissionStatus.GRANTED -> R.string.permission_granted to FluxThemeExtras.status.success
        PermissionStatus.PENDING -> R.string.permission_pending to MaterialTheme.colorScheme.error
        PermissionStatus.UNKNOWN -> R.string.permission_check to MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        label = { Text(stringResource(label), color = color) },
        leadingIcon = if (status == PermissionStatus.GRANTED) ({ Icon(Icons.Rounded.CheckCircle, null, tint = color) }) else null,
    )
}
