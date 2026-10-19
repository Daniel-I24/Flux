package com.flux.mobile.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Android fixes the sound per notification channel, so FLUX creates one channel per
 * selected sound (category sounds included) plus a silent one for quiet hours.
 */
class ReminderChannels @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun channelFor(sound: SoundRef, quiet: Boolean): String = if (quiet) ensureQuiet() else ensureSound(sound)

    private fun ensureSound(sound: SoundRef): String {
        val id = "$PREFIX${SoundCatalog.key(sound)}"
        if (manager.getNotificationChannel(id) == null) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val channel = NotificationChannel(
                id,
                context.getString(R.string.channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_reminders_description)
                setSound(SoundCatalog.uri(context, sound), attributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
        return id
    }

    private fun ensureQuiet(): String {
        if (manager.getNotificationChannel(QUIET_ID) == null) {
            val channel = NotificationChannel(
                QUIET_ID,
                context.getString(R.string.channel_quiet),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_quiet_description)
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }
        return QUIET_ID
    }

    private companion object {
        const val PREFIX = "flux_reminder_"
        const val QUIET_ID = "flux_reminder_quiet"
    }
}
