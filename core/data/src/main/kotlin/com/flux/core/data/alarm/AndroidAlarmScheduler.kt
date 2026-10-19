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

/** Each app tells the scheduler which receiver handles a ringing reminder. */
interface AlarmTarget {
    fun pendingIntent(context: Context, noteId: String): PendingIntent

    /** Stable notification id for a note (used to dismiss it from either device). */
    fun notificationId(noteId: String): Int = noteId.hashCode()
}

/**
 * Exact alarms with AlarmManager (Doze-safe). Falls back to inexact alarms when the
 * user did not grant "Alarmas exactas" on Android 12+, so reminders are never lost.
 */
class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val target: AlarmTarget,
) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(note: Note) {
        val at = note.dueAt ?: return
        val intent = target.pendingIntent(context, note.id)
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, intent)
        }
    }

    override fun cancel(noteId: String) {
        alarmManager.cancel(target.pendingIntent(context, noteId))
        NotificationManagerCompat.from(context).cancel(target.notificationId(noteId))
    }

    private fun canScheduleExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
}
