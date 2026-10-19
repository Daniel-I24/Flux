package com.flux.mobile.feature.permissions

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Permissions FLUX may ask for, each requested only when its benefit is explained. */
enum class FluxPermission { NOTIFICATIONS, EXACT_ALARMS, MICROPHONE }

object PermissionChecker {
    fun isRequired(permission: FluxPermission): Boolean = when (permission) {
        FluxPermission.NOTIFICATIONS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        FluxPermission.EXACT_ALARMS -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        FluxPermission.MICROPHONE -> true
    }

    fun isGranted(context: Context, permission: FluxPermission): Boolean = when (permission) {
        FluxPermission.NOTIFICATIONS -> !isRequired(permission) || granted(context, Manifest.permission.POST_NOTIFICATIONS)
        FluxPermission.EXACT_ALARMS -> !isRequired(permission) ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        FluxPermission.MICROPHONE -> granted(context, Manifest.permission.RECORD_AUDIO)
    }

    /** Runtime permission name, or null when it is granted from system settings instead. */
    fun runtimePermission(permission: FluxPermission): String? = when (permission) {
        FluxPermission.NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
        FluxPermission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
        FluxPermission.EXACT_ALARMS -> null
    }

    fun exactAlarmSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    private fun granted(context: Context, name: String) =
        ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
}
