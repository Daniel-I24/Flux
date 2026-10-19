package com.flux.mobile.alarm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.flux.core.data.alarm.AlarmTarget
import com.flux.core.designsystem.R as DesignR
import com.flux.core.domain.policy.QuietHoursPolicy
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.Note
import com.flux.core.model.TimeOfDay
import com.flux.mobile.MainActivity
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Reminder notification with Completar / Posponer, category sound and FLUX quiet hours. */
class ReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channels: ReminderChannels,
    private val target: AlarmTarget,
    private val categories: CategoryRepository,
    private val observeSettings: ObserveSettingsUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val clock: Clock,
) {
    @SuppressLint("MissingPermission")
    suspend fun show(note: Note) {
        val notifications = NotificationManagerCompat.from(context)
        if (!notifications.areNotificationsEnabled()) return
        val settings = observeSettings().first()
        val routine = observeProfile().first()?.routine
        val category = categories.getCategories().firstOrNull { it.id == note.categoryId }
        val now = Instant.ofEpochMilli(clock.now()).atZone(clock.zone()).toLocalTime()
        val quiet = settings.quietHoursEnabled && routine != null && QuietHoursPolicy.isQuiet(TimeOfDay.of(now.hour, now.minute), routine)
        val channel = channels.channelFor(category?.sound ?: settings.defaultSound, quiet)
        val id = target.notificationId(note.id)

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(DesignR.drawable.ic_stat_flux)
            .setContentTitle(note.title)
            .setContentText(note.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.text))
            .setColor(category?.colorArgb?.toInt() ?: ContextCompat.getColor(context, DesignR.color.flux_brand))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(if (quiet) NotificationCompat.PRIORITY_LOW else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp(id))
            .addAction(0, context.getString(R.string.reminder_complete), action(note.id, ReminderActionReceiver.ACTION_COMPLETE, id))
            .addAction(
                0,
                context.getString(R.string.reminder_snooze, settings.snoozeMinutes),
                action(note.id, ReminderActionReceiver.ACTION_SNOOZE, id)
            )
            .build()
        notifications.notify(id, notification)
    }

    private fun openApp(requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context,
        requestCode,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun action(noteId: String, action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode + action.hashCode(),
        Intent(context, ReminderActionReceiver::class.java).setAction(action).putExtra(PhoneAlarmTarget.EXTRA_NOTE_ID, noteId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
