package com.flux.mobile.feature.permissions

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.flux.core.data.permissions.AppPermission
import com.flux.core.data.permissions.PermissionChecks
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

private val STEPS = listOf(R.string.background_help_battery, R.string.background_help_autostart, R.string.background_help_sleeping)

/**
 * Battery savers of some brands stop FLUX and the watch app in the background, so reminders arrive
 * late. Each step has a button that opens its exact screen when this phone has it.
 */
@Composable
fun BackgroundHelpCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val checks = remember(context) { PermissionChecks(context, isWatch = false) }
    val open: (Intent) -> Unit = { intent -> runCatching { context.startActivity(intent) } }
    ElevatedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Text(stringResource(R.string.background_help_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.background_help_intro), style = MaterialTheme.typography.bodyMedium)
            STEPS.forEach { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium) }
            Text(
                stringResource(R.string.background_help_where),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HelpButton(Icons.Rounded.BatteryChargingFull, R.string.permission_battery) {
                open(checks.settingsIntent(AppPermission.BATTERY))
            }
            if (checks.isRequired(AppPermission.AUTOSTART)) {
                HelpButton(Icons.Rounded.RestartAlt, R.string.permission_autostart) { open(checks.settingsIntent(AppPermission.AUTOSTART)) }
            }
            OutlinedButton(onClick = { open(appInfo(context.packageName)) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.padding(end = FluxSpacing.sm))
                Text(stringResource(R.string.background_help_open))
            }
        }
    }
}

@Composable
private fun HelpButton(icon: ImageVector, label: Int, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = FluxSpacing.sm))
        Text(stringResource(label))
    }
}

private fun appInfo(packageName: String) =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
