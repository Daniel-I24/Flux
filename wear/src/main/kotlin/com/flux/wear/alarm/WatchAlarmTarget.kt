package com.flux.wear.alarm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.flux.core.data.alarm.AlarmTarget
import javax.inject.Inject

class WatchAlarmTarget @Inject constructor() : AlarmTarget {
    override fun pendingIntent(context: Context, noteId: String): PendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId(noteId),
        Intent(context, WatchAlarmReceiver::class.java).putExtra(EXTRA_NOTE_ID, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
    }
}
