package com.flux.core.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.model.Note
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Each app tells the scheduler which receiver handles a ringing reminder and what opens the app. */
interface AlarmTarget {
    fun pendingIntent(context: Context, noteId: String): PendingIntent

    /** Shown by the system for the next alarm (status bar / lock screen). */
    fun showIntent(context: Context, noteId: String): PendingIntent

    /** Stable notification id for a note (used to dismiss it from either device). */
    fun notificationId(noteId: String): Int = noteId.hashCode()
}

/**
 * Exact, user-visible alarms with AlarmManager.setAlarmClock (not delayed by Doze and allowed to
 * start the alarm service). Falls back to inexact alarms when "Alarmas exactas" was not granted,
 * so reminders are never lost. Any schedule or cancel also silences that reminder if it is ringing.
 */
class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val target: AlarmTarget,
    private val activeAlarm: ActiveAlarm,
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(note: Note) {
        val at = note.dueAt ?: return
        // A rescheduled (snoozed) reminder must stop ringing and leave the screen on this device.
        activeAlarm.stop(note.id)
        NotificationManagerCompat.from(context).cancel(target.notificationId(note.id))
        val intent = target.pendingIntent(context, note.id)
        if (canScheduleExact()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(at, target.showIntent(context, note.id)), intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    override fun cancel(noteId: String) {
        activeAlarm.stop(noteId)
        alarmManager.cancel(target.pendingIntent(context, noteId))
        NotificationManagerCompat.from(context).cancel(target.notificationId(noteId))
    }

    private fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
}
