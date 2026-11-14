package com.flux.mobile.feature.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.flux.core.data.permissions.AppPermission
import com.flux.core.data.permissions.PermissionChecks
import com.flux.core.designsystem.theme.FluxSpacing

/**
 * One card per permission that applies to this phone, with its live state (refreshed when the
 * user comes back from the system screen) and a button that asks for it or opens that screen.
 * A runtime permission denied before opens its settings screen instead of the dialog.
 */
@Composable
fun PermissionsList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val checks = remember(context) { PermissionChecks(context, isWatch = false) }
    var refresh by remember { mutableIntStateOf(0) }
    var asked by remember { mutableStateOf(emptySet<AppPermission>()) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
        checks.applicable().forEachIndexed { index, permission ->
            val status = refresh.let { checks.status(permission) }
            PermissionCard(index + 1, permission, status) {
                val runtime = checks.runtimePermission(permission)
                if (runtime != null && permission !in asked) {
                    asked = asked + permission
                    launcher.launch(runtime)
                } else {
                    runCatching { context.startActivity(checks.settingsIntent(permission)) }
                }
            }
        }
    }
}
