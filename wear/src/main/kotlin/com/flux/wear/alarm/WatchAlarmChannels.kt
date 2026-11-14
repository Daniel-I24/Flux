package com.flux.wear.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.flux.wear.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * v1.1 had a silent, vibration-less channel: when the full-screen alert could not open (no
 * notification permission, Samsung freezing the app, full-screen intents denied) the reminder
 * arrived without any vibration. The new channel vibrates by itself and is shown on the lock
 * screen, so the watch always buzzes even if only the notification gets through.
 */
class WatchAlarmChannels @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensure() {
        LEGACY.forEach { if (manager.getNotificationChannel(it) != null) manager.deleteNotificationChannel(it) }
        if (manager.getNotificationChannel(ALARM) == null) {
            manager.createNotificationChannel(
                NotificationChannel(ALARM, context.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH).apply {
                    description = context.getString(R.string.channel_alerts_description)
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setSound(null, null)
                },
            )
        }
        if (manager.getNotificationChannel(QUIET) == null) {
            manager.createNotificationChannel(
                NotificationChannel(QUIET, context.getString(R.string.channel_quiet), NotificationManager.IMPORTANCE_LOW),
            )
        }
    }

    /** True when the user turned the alarm channel off or down in the watch settings. */
    fun isAlarmChannelBlocked(): Boolean =
        (manager.getNotificationChannel(ALARM)?.importance ?: NotificationManager.IMPORTANCE_HIGH) < NotificationManager.IMPORTANCE_HIGH

    companion object {
        const val ALARM = "flux_watch_alarm"
        const val QUIET = "flux_watch_quiet"
        private val LEGACY = listOf("flux_watch_alert")

        /** Off/on pairs in milliseconds: three firm pulses, repeated while the alert is insistent. */
        private const val PULSE_MS = 600L
        private const val GAP_MS = 400L
        val VIBRATION_PATTERN = longArrayOf(0, PULSE_MS, GAP_MS, PULSE_MS, GAP_MS, PULSE_MS)
    }
}
