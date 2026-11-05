package com.flux.mobile.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Android freezes the sound of a channel once it is created, so v1.1 channels are silent and the
 * alarm service plays the reminder's own tone (category or reminder sound, including custom files).
 * The v1.0 channels (one per sound) are removed once (N-055).
 */
class AlarmChannels @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensure() {
        if (manager.getNotificationChannel(RINGING) != null) return
        manager.notificationChannels.filter { it.id.startsWith(LEGACY_PREFIX) }.forEach { manager.deleteNotificationChannel(it.id) }
        manager.createNotificationChannels(
            listOf(
                silent(RINGING, R.string.channel_alarm, R.string.channel_alarm_description, NotificationManager.IMPORTANCE_HIGH),
                silent(QUIET, R.string.channel_quiet, R.string.channel_quiet_description, NotificationManager.IMPORTANCE_LOW),
                silent(MISSED, R.string.channel_missed, R.string.channel_missed_description, NotificationManager.IMPORTANCE_DEFAULT),
                fallback(),
            ),
        )
    }

    private fun silent(id: String, name: Int, description: Int, importance: Int) =
        NotificationChannel(id, context.getString(name), importance).apply {
            this.description = context.getString(description)
            setSound(null, null)
            enableVibration(false)
            setBypassDnd(false)
        }

    /** Used only if Android refuses to start the alarm service: the notification itself sounds once. */
    private fun fallback() = NotificationChannel(
        FALLBACK,
        context.getString(R.string.channel_alarm_fallback),
        NotificationManager.IMPORTANCE_HIGH,
    ).apply {
        description = context.getString(R.string.channel_alarm_fallback_description)
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        setSound(SoundCatalog.uri(context, SoundCatalog.FALLBACK), attributes)
    }

    companion object {
        const val RINGING = "flux_alarm_ringing"
        const val QUIET = "flux_alarm_quiet"
        const val MISSED = "flux_alarm_missed"
        const val FALLBACK = "flux_alarm_fallback"
        private const val LEGACY_PREFIX = "flux_reminder_"
    }
}
