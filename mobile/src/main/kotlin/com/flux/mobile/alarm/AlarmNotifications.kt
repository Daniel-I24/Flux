package com.flux.mobile.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.flux.core.data.alarm.AlarmTarget
import com.flux.core.designsystem.R as DesignR
import com.flux.core.domain.usecase.Reminder
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Every notification of a reminder: ringing (full screen), quiet hours, missed and the fallback. */
class AlarmNotifications @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channels: AlarmChannels,
    private val target: AlarmTarget,
) {
    private val manager = NotificationManagerCompat.from(context)

    fun idOf(noteId: String): Int = target.notificationId(noteId)

    /** Shown while the alarm service rings; opens the full-screen alarm even on the lock screen. */
    fun ringing(reminder: Reminder, snoozeMinutes: Int): Notification = builder(reminder, AlarmChannels.RINGING, snoozeMinutes)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setOngoing(true)
        .setAutoCancel(false)
        .setFullScreenIntent(alarmScreen(reminder.note.id), true)
        .build()

    /** Placeholder required to enter the foreground immediately, before the note is loaded. */
    fun starting(): Notification = NotificationCompat.Builder(context, AlarmChannels.RINGING)
        .setSmallIcon(DesignR.drawable.ic_stat_flux)
        .setContentTitle(context.getString(R.string.alarm_starting))
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setSilent(true)
        .build()

    fun showQuiet(reminder: Reminder, snoozeMinutes: Int) = post(reminder, AlarmChannels.QUIET, snoozeMinutes)

    fun showMissed(reminder: Reminder, snoozeMinutes: Int) = post(reminder, AlarmChannels.MISSED, snoozeMinutes)

    /** Android did not let the service start: a sounding, full-screen notification instead. */
    fun showFallback(reminder: Reminder, snoozeMinutes: Int) {
        channels.ensure()
        val notification = builder(reminder, AlarmChannels.FALLBACK, snoozeMinutes)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(alarmScreen(reminder.note.id), true)
            .build()
        notify(reminder.note.id, notification)
    }

    private fun post(reminder: Reminder, channel: String, snoozeMinutes: Int) {
        channels.ensure()
        notify(reminder.note.id, builder(reminder, channel, snoozeMinutes).build())
    }

    @SuppressLint("MissingPermission")
    private fun notify(noteId: String, notification: Notification) {
        if (manager.areNotificationsEnabled()) manager.notify(idOf(noteId), notification)
    }

    private fun builder(reminder: Reminder, channel: String, snoozeMinutes: Int): NotificationCompat.Builder {
        val note = reminder.note
        val id = idOf(note.id)
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(DesignR.drawable.ic_stat_flux)
            .setContentTitle(note.title)
            .setContentText(note.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.text))
            .setColor(reminder.category?.colorArgb?.toInt() ?: ContextCompat.getColor(context, DesignR.color.flux_brand))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(alarmScreen(note.id))
            .addAction(0, context.getString(R.string.reminder_complete), action(note.id, ReminderActionReceiver.ACTION_COMPLETE, id))
            .addAction(
                0,
                context.getString(R.string.reminder_snooze, snoozeMinutes),
                action(note.id, ReminderActionReceiver.ACTION_SNOOZE, id)
            )
    }

    private fun alarmScreen(noteId: String): PendingIntent = PendingIntent.getActivity(
        context,
        idOf(noteId),
        AlarmActivity.intent(context, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun action(noteId: String, action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode + action.hashCode(),
        Intent(context, ReminderActionReceiver::class.java).setAction(action).putExtra(PhoneAlarmTarget.EXTRA_NOTE_ID, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
