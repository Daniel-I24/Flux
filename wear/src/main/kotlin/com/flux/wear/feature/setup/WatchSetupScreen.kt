package com.flux.wear.feature.setup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.core.data.permissions.AppPermission
import com.flux.core.data.permissions.PermissionChecks
import com.flux.core.data.permissions.PermissionStatus
import com.flux.wear.R

/**
 * First launch (after the tutorial) and Ajustes: every permission the watch alarms and dictation
 * need, with its live state (refreshed when the user comes back) and the steps on the watch;
 * then how to open FLUX with a double press of the button.
 */
@Composable
fun WatchSetupScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val checks = remember(context) { PermissionChecks(context, isWatch = true) }
    var refresh by remember { mutableIntStateOf(0) }
    var asked by remember { mutableStateOf(emptySet<AppPermission>()) }
    LifecycleResumeEffect(Unit) {
        refresh++
        onPauseOrDispose { }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }
    val grant: (AppPermission) -> Unit = { permission ->
        val runtime = checks.runtimePermission(permission)
        if (runtime != null && permission !in asked) {
            asked = asked + permission
            launcher.launch(runtime)
        } else {
            runCatching { context.startActivity(checks.settingsIntent(permission)) }
        }
    }
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.setup_title)) } }
            item { Text(stringResource(R.string.setup_intro), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
            items(checks.applicable(), key = { it.name }) { permission ->
                val status = refresh.let { checks.status(permission) }
                PermissionChip(permission, status, onClick = { grant(permission) })
            }
            doublePressGuide()
            item {
                Button(onClick = onDone, label = { Text(stringResource(R.string.setup_done)) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** Live state of [permission]: the steps while pending, "Listo" once granted. */
@Composable
fun PermissionChip(permission: AppPermission, status: PermissionStatus, onClick: () -> Unit) {
    val copy = WATCH_PERMISSION_COPY.getValue(permission)
    val granted = status == PermissionStatus.GRANTED
    Button(
        onClick = onClick,
        enabled = !granted,
        icon = { Icon(copy.icon, contentDescription = null) },
        label = { Text(stringResource(copy.title)) },
        secondaryLabel = { Text(stringResource(if (granted) R.string.setup_granted else copy.steps)) },
        modifier = Modifier.fillMaxWidth(),
    )
}
