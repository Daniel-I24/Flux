package com.flux.wear.feature.setup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.flux.core.data.permissions.AppPermission
import com.flux.wear.R

/** Title and what to touch on the watch's own system screen. */
data class WatchPermissionCopy(val title: Int, val steps: Int, val icon: ImageVector)

val WATCH_PERMISSION_COPY: Map<AppPermission, WatchPermissionCopy> = mapOf(
    AppPermission.MICROPHONE to WatchPermissionCopy(R.string.setup_microphone, R.string.setup_microphone_steps, Icons.Rounded.Mic),
    AppPermission.NOTIFICATIONS to
        WatchPermissionCopy(R.string.setup_notifications, R.string.setup_notifications_steps, Icons.Rounded.Notifications),
    AppPermission.EXACT_ALARMS to WatchPermissionCopy(R.string.setup_exact_alarms, R.string.setup_exact_alarms_steps, Icons.Rounded.Alarm),
    AppPermission.FULL_SCREEN to WatchPermissionCopy(
        R.string.setup_full_screen,
        R.string.setup_full_screen_steps,
        Icons.Rounded.Fullscreen
    ),
    AppPermission.BATTERY to
        WatchPermissionCopy(R.string.setup_battery, R.string.setup_battery_steps, Icons.Rounded.BatteryChargingFull),
)
