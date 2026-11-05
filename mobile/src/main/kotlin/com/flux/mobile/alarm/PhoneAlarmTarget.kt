package com.flux.mobile.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.flux.core.data.alarm.AlarmTarget
import com.flux.mobile.MainActivity
import javax.inject.Inject

/** Phone alarms are delivered to [ReminderReceiver]; the system "next alarm" opens FLUX. */
class PhoneAlarmTarget @Inject constructor() : AlarmTarget {
    override fun pendingIntent(context: Context, noteId: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId(noteId),
        Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_RING).putExtra(EXTRA_NOTE_ID, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    override fun showIntent(context: Context, noteId: String): PendingIntent = PendingIntent.getActivity(
        context,
        notificationId(noteId),
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
    }
}
