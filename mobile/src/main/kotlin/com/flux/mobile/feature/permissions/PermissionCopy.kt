package com.flux.mobile.feature.permissions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhonelinkLock
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.flux.core.data.permissions.AppPermission
import com.flux.mobile.R

/** Title, why FLUX needs it, and what to touch in the system screen. */
data class PermissionCopy(val title: Int, val why: Int, val steps: Int, val icon: ImageVector)

val PERMISSION_COPY: Map<AppPermission, PermissionCopy> = mapOf(
    AppPermission.MICROPHONE to
        PermissionCopy(
            R.string.permission_microphone,
            R.string.permission_microphone_why,
            R.string.permission_microphone_steps,
            Icons.Rounded.Mic
        ),
    AppPermission.NOTIFICATIONS to PermissionCopy(
        R.string.permission_notifications,
        R.string.permission_notifications_why,
        R.string.permission_notifications_steps,
        Icons.Rounded.Notifications,
    ),
    AppPermission.EXACT_ALARMS to PermissionCopy(
        R.string.permission_exact_alarms,
        R.string.permission_exact_alarms_why,
        R.string.permission_exact_alarms_steps,
        Icons.Rounded.Alarm,
    ),
    AppPermission.FULL_SCREEN to PermissionCopy(
        R.string.permission_full_screen,
        R.string.permission_full_screen_why,
        R.string.permission_full_screen_steps,
        Icons.Rounded.Fullscreen,
    ),
    AppPermission.OVERLAY to
        PermissionCopy(
            R.string.permission_overlay,
            R.string.permission_overlay_why,
            R.string.permission_overlay_steps,
            Icons.Rounded.Layers
        ),
    AppPermission.BATTERY to PermissionCopy(
        R.string.permission_battery,
        R.string.permission_battery_why,
        R.string.permission_battery_steps,
        Icons.Rounded.BatteryChargingFull,
    ),
    AppPermission.AUTOSTART to PermissionCopy(
        R.string.permission_autostart,
        R.string.permission_autostart_why,
        R.string.permission_autostart_steps,
        Icons.Rounded.RestartAlt,
    ),
    AppPermission.BRAND_POPUPS to PermissionCopy(
        R.string.permission_popups,
        R.string.permission_popups_why,
        R.string.permission_popups_steps,
        Icons.Rounded.PhonelinkLock,
    ),
)
