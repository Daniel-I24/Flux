package com.flux.wear.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flux.core.data.alarm.ActiveAlarm
import com.flux.core.data.alarm.AlarmLog
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
 * Posts the reminder as an insistent alarm with a full-screen intent: the screen turns on with
 * AlertActivity (Posponer / Completar, the user's vibration) and, if the system only shows the
 * notification, its channel still vibrates until the configured duration ends. The local alarm
 * and the phone's "ringing" message may both arrive: the second one is ignored.
 */
class WatchAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val target: AlarmTarget,
    private val channels: WatchAlarmChannels,
    private val stateStore: WatchStateStore,
    private val activeAlarm: ActiveAlarm,
    private val clock: Clock,
) {
    private val manager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission")
    suspend fun show(note: Note) {
        val id = target.notificationId(note.id)
        if (activeAlarm.noteId.value == note.id || isPosted(id)) return AlarmLog.info("watch alert skipped=duplicate")
        if (!manager.areNotificationsEnabled()) return AlarmLog.info("watch alert blocked=notifications_off")
        channels.ensure()
        val phone = stateStore.phoneState()
        val quiet = isQuiet(phone.quietHours)
        val alert = PendingIntent.getActivity(
            context,
            id,
            AlertActivity.intent(context, note.id, silent = quiet),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, if (quiet) WatchAlarmChannels.QUIET else WatchAlarmChannels.ALARM)
            .setSmallIcon(DesignR.drawable.ic_stat_flux)
            .setContentTitle(note.title)
            .setContentText(context.getString(R.string.alert_subtitle))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(if (quiet) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(alert)
            .setAutoCancel(true)
            .setTimeoutAfter(phone.alarmDurationSeconds * MILLIS_PER_SECOND)
            .addAction(0, context.getString(R.string.alert_complete), action(note.id, WatchReminderActionReceiver.ACTION_COMPLETE, id))
            .addAction(0, context.getString(R.string.alert_snooze), action(note.id, WatchReminderActionReceiver.ACTION_SNOOZE, id))
        if (!quiet) builder.setOngoing(true).setVibrate(WatchAlarmChannels.VIBRATION_PATTERN).setFullScreenIntent(alert, true)
        val notification = builder.build().apply { if (!quiet) flags = flags or Notification.FLAG_INSISTENT }
        manager.notify(id, notification)
        AlarmLog.info("watch alert posted quiet=$quiet fullScreen=${canUseFullScreen()} channelBlocked=${channels.isAlarmChannelBlocked()}")
    }

    private fun isPosted(id: Int): Boolean =
        context.getSystemService(NotificationManager::class.java).activeNotifications.any { it.id == id }

    private fun canUseFullScreen(): Boolean = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

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

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}
