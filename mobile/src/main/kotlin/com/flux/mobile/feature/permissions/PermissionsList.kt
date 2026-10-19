package com.flux.mobile.feature.permissions

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.designsystem.theme.FluxThemeExtras
import com.flux.mobile.R

private data class PermissionCopy(val title: Int, val why: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val COPY = mapOf(
    FluxPermission.NOTIFICATIONS to
        PermissionCopy(R.string.permission_notifications, R.string.permission_notifications_why, Icons.Rounded.Notifications),
    FluxPermission.EXACT_ALARMS to
        PermissionCopy(R.string.permission_exact_alarms, R.string.permission_exact_alarms_why, Icons.Rounded.Alarm),
    FluxPermission.MICROPHONE to PermissionCopy(R.string.permission_microphone, R.string.permission_microphone_why, Icons.Rounded.Mic),
)

/** One card per permission: what it is for, and a button that asks for it in context. */
@SuppressLint("InlinedApi")
@Composable
fun PermissionsList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
        FluxPermission.entries.filter(PermissionChecker::isRequired).forEach { permission ->
            val granted = refresh >= 0 && PermissionChecker.isGranted(context, permission)
            PermissionCard(permission, granted) {
                val runtime = PermissionChecker.runtimePermission(permission)
                if (runtime !=
                    null
                ) {
                    launcher.launch(runtime)
                } else {
                    context.startActivity(PermissionChecker.exactAlarmSettingsIntent(context))
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(permission: FluxPermission, granted: Boolean, onGrant: () -> Unit) {
    val copy = COPY.getValue(permission)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
                Icon(copy.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(copy.title), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (granted) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.permission_granted),
                        tint = FluxThemeExtras.status.success
                    )
                }
            }
            Text(stringResource(copy.why), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!granted) FilledTonalButton(onClick = onGrant) { Text(stringResource(R.string.permission_grant)) }
        }
    }
}
