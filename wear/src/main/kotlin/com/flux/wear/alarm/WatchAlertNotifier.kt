package com.flux.wear.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flux.core.data.alarm.AlarmTarget
import com.flux.core.designsystem.R as DesignR
import com.flux.core.domain.policy.QuietHoursPolicy
import com.flux.core.domain.port.Clock
import com.flux.core.model.Note
import com.flux.core.model.TimeOfDay
import com.flux.core.sync.protocol.QuietWindow
import com.flux.wear.R
import com.flux.wear.data.WatchStateStore
import com.flux.wear.feature.alert.AlertActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

/**
 * Posts the reminder with a full-screen intent (AlertActivity handles sound and the
 * user's vibration pattern). During FLUX quiet hours it is delivered silently.
 */
class WatchAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val target: AlarmTarget,
    private val stateStore: WatchStateStore,
    private val clock: Clock,
) {
    private val manager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission")
    suspend fun show(note: Note) {
        if (!manager.areNotificationsEnabled()) return
        val quiet = isQuiet(stateStore.phoneState().quietHours)
        ensureChannels()
        val id = target.notificationId(note.id)
        val alert = PendingIntent.getActivity(
            context,
            id,
            AlertActivity.intent(context, note.id, silent = quiet),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, if (quiet) QUIET_CHANNEL else ALERT_CHANNEL)
            .setSmallIcon(DesignR.drawable.ic_stat_flux)
            .setContentTitle(note.title)
            .setContentText(context.getString(R.string.alert_subtitle))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(if (quiet) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MAX)
            .setContentIntent(alert)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.alert_complete), action(note.id, WatchReminderActionReceiver.ACTION_COMPLETE, id))
            .addAction(0, context.getString(R.string.alert_snooze), action(note.id, WatchReminderActionReceiver.ACTION_SNOOZE, id))
        if (!quiet) builder.setFullScreenIntent(alert, true)
        manager.notify(id, builder.build())
    }

    private fun isQuiet(window: QuietWindow?): Boolean {
        window ?: return false
        val time = Instant.ofEpochMilli(clock.now()).atZone(clock.zone()).toLocalTime()
        return QuietHoursPolicy.isQuiet(TimeOfDay.of(time.hour, time.minute), TimeOfDay(window.startMinutes), TimeOfDay(window.endMinutes))
    }

    private fun action(noteId: String, action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode + action.hashCode(),
        Intent(context, WatchReminderActionReceiver::class.java).setAction(action).putExtra(WatchAlarmTarget.EXTRA_NOTE_ID, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun ensureChannels() {
        val system = context.getSystemService(NotificationManager::class.java)
        if (system.getNotificationChannel(ALERT_CHANNEL) == null) {
            // Sound and vibration are produced by AlertActivity with the user's watch settings.
            system.createNotificationChannel(
                NotificationChannel(ALERT_CHANNEL, context.getString(R.string.channel_alerts), NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(null, null)
                    enableVibration(false)
                },
            )
        }
        if (system.getNotificationChannel(QUIET_CHANNEL) == null) {
            system.createNotificationChannel(
                NotificationChannel(QUIET_CHANNEL, context.getString(R.string.channel_quiet), NotificationManager.IMPORTANCE_LOW),
            )
        }
    }

    private companion object {
        const val ALERT_CHANNEL = "flux_watch_alert"
        const val QUIET_CHANNEL = "flux_watch_quiet"
    }
}
