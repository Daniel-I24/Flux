package com.flux.mobile.feature.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Permissions FLUX may ask for, each requested only when its benefit is explained. */
enum class FluxPermission { NOTIFICATIONS, EXACT_ALARMS, FULL_SCREEN }

@SuppressLint("InlinedApi", "NewApi")
object PermissionChecker {
    fun isRequired(permission: FluxPermission): Boolean = when (permission) {
        FluxPermission.NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        FluxPermission.EXACT_ALARMS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        FluxPermission.FULL_SCREEN -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    fun isGranted(context: Context, permission: FluxPermission): Boolean = when (permission) {
        FluxPermission.NOTIFICATIONS -> !isRequired(permission) || granted(context, Manifest.permission.POST_NOTIFICATIONS)
        FluxPermission.EXACT_ALARMS -> !isRequired(permission) ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        FluxPermission.FULL_SCREEN -> !isRequired(permission) ||
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    }

    /** True when every permission the alarms need is granted (checked at run time, not only in onboarding). */
    fun alarmsReady(context: Context): Boolean = FluxPermission.entries.all { isGranted(context, it) }

    /** Runtime permission name, or null when it is granted from system settings instead. */
    fun runtimePermission(permission: FluxPermission): String? = when (permission) {
        FluxPermission.NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
        FluxPermission.EXACT_ALARMS, FluxPermission.FULL_SCREEN -> null
    }

    /** System screen where a non-runtime permission is granted. */
    fun settingsIntent(context: Context, permission: FluxPermission): Intent {
        val action = if (permission == FluxPermission.FULL_SCREEN) {
            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT
        } else {
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
        }
        return Intent(action, Uri.parse("package:${context.packageName}"))
    }

    private fun granted(context: Context, name: String) =
        ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
}
