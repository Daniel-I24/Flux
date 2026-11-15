package com.flux.core.data.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/** Which permissions apply to this device, their live state and the exact system screen for each. */
@SuppressLint("InlinedApi", "NewApi", "BatteryLife")
class PermissionChecks(private val context: Context, private val isWatch: Boolean) {
    private val sdk = Build.VERSION.SDK_INT

    fun applicable(): List<AppPermission> = AppPermission.entries.filter(::isRequired)

    fun isRequired(permission: AppPermission): Boolean = when (permission) {
        AppPermission.MICROPHONE, AppPermission.NOTIFICATIONS, AppPermission.BATTERY -> true
        AppPermission.EXACT_ALARMS -> sdk in Build.VERSION_CODES.S..Build.VERSION_CODES.S_V2
        AppPermission.FULL_SCREEN -> sdk >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        AppPermission.OVERLAY -> !isWatch
        AppPermission.AUTOSTART -> !isWatch && OemSettings.autostartIntent(context) != null
        AppPermission.BRAND_POPUPS -> !isWatch && OemSettings.popupsIntent(context) != null
    }

    fun status(permission: AppPermission): PermissionStatus = when (permission) {
        AppPermission.AUTOSTART, AppPermission.BRAND_POPUPS -> PermissionStatus.UNKNOWN
        else -> if (isGranted(permission)) PermissionStatus.GRANTED else PermissionStatus.PENDING
    }

    /** True when everything the alarms need is granted (checked live, not only during setup). */
    fun alarmsReady(): Boolean = ALARM_CRITICAL.filter(::isRequired).all(::isGranted)

    /** Runtime permission asked with the system dialog, or null when it is granted in a settings screen. */
    fun runtimePermission(permission: AppPermission): String? = when {
        permission == AppPermission.MICROPHONE -> Manifest.permission.RECORD_AUDIO
        permission == AppPermission.NOTIFICATIONS && sdk >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.POST_NOTIFICATIONS
        else -> null
    }

    /** The exact system (or brand) screen for [permission]; FLUX's app info when nothing better exists. */
    fun settingsIntent(permission: AppPermission): Intent {
        val forApp = "package:${context.packageName}".toUri()
        val intent = when (permission) {
            AppPermission.NOTIFICATIONS -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            AppPermission.EXACT_ALARMS -> Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, forApp)
            AppPermission.FULL_SCREEN -> Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, forApp)
            AppPermission.OVERLAY -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, forApp)
            AppPermission.BATTERY -> Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, forApp)
            AppPermission.AUTOSTART -> OemSettings.autostartIntent(context)
            AppPermission.BRAND_POPUPS -> OemSettings.popupsIntent(context)
            AppPermission.MICROPHONE -> null
        }?.takeIf(::opensRealScreen)
        return (intent ?: Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, forApp)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun isGranted(permission: AppPermission): Boolean = when (permission) {
        AppPermission.MICROPHONE -> granted(Manifest.permission.RECORD_AUDIO)
        AppPermission.NOTIFICATIONS -> NotificationManagerCompat.from(context).areNotificationsEnabled()
        AppPermission.EXACT_ALARMS -> context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        AppPermission.FULL_SCREEN -> context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        AppPermission.OVERLAY -> Settings.canDrawOverlays(context)
        AppPermission.BATTERY -> context.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(context.packageName)
        AppPermission.AUTOSTART, AppPermission.BRAND_POPUPS -> false
    }

    /** Wear OS answers some settings actions with a placeholder that closes at once: those count as missing. */
    private fun opensRealScreen(intent: Intent): Boolean {
        val activity = context.packageManager.resolveActivity(intent, 0)?.activityInfo ?: return false
        return !activity.name.endsWith(PLACEHOLDER_ACTIVITY)
    }

    private fun granted(name: String) = ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED

    private companion object {
        const val PLACEHOLDER_ACTIVITY = "FakeSettingsActivity"
        val ALARM_CRITICAL = listOf(AppPermission.NOTIFICATIONS, AppPermission.EXACT_ALARMS, AppPermission.FULL_SCREEN)
    }
}
