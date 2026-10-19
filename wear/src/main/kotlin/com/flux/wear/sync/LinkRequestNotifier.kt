package com.flux.wear.sync

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.flux.core.designsystem.R as DesignR
import com.flux.wear.MainActivity
import com.flux.wear.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** If FLUX is not open on the watch, a notification brings the user to the confirmation screen. */
class LinkRequestNotifier @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager = NotificationManagerCompat.from(context)

    @SuppressLint("MissingPermission")
    fun show(code: String) {
        if (!manager.areNotificationsEnabled()) return
        ensureChannel()
        val open = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(DesignR.drawable.ic_stat_flux)
            .setContentTitle(context.getString(R.string.link_confirm_title))
            .setContentText(context.getString(R.string.link_confirm_notification, code))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() = manager.cancel(NOTIFICATION_ID)

    private fun ensureChannel() {
        val system = context.getSystemService(NotificationManager::class.java)
        if (system.getNotificationChannel(CHANNEL_ID) == null) {
            system.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_link), NotificationManager.IMPORTANCE_HIGH),
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "flux_link"
        const val NOTIFICATION_ID = 4_200
    }
}
