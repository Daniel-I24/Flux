package com.flux.mobile.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.flux.core.data.alarm.ActiveAlarm
import com.flux.core.designsystem.sound.AlarmRinger
import com.flux.core.domain.usecase.Reminder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Foreground service (type mediaPlayback) that rings a reminder in a loop with USAGE_ALARM until
 * the user acts on either device or the configured time runs out; then it becomes "missed".
 */
@AndroidEntryPoint
class AlarmService : Service() {
    @Inject lateinit var decider: ReminderAlarm

    @Inject lateinit var notifications: AlarmNotifications

    @Inject lateinit var channels: AlarmChannels

    @Inject lateinit var activeAlarm: ActiveAlarm

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ringer by lazy { AlarmRinger(this) }
    private var ringJob: Job? = null
    private var current: Pair<Reminder, Int>? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        channels.ensure()
        val noteId = intent?.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID)
        // Must enter the foreground right away; the reminder's notification replaces this one (same id).
        val id = noteId?.let(notifications::idOf) ?: STARTING_ID
        ServiceCompat.startForeground(this, id, notifications.starting(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        if (noteId == null) finish() else ring(noteId)
        return START_NOT_STICKY
    }

    private fun ring(noteId: String) {
        ringJob?.cancel()
        ringer.stop()
        // Another reminder was ringing: it stays available as a missed one.
        current?.let { (reminder, snooze) ->
            activeAlarm.stop(reminder.note.id)
            notifications.showMissed(reminder, snooze)
        }
        current = null
        ringJob = scope.launch {
            val decision = decider.decide(noteId) as? AlarmDecision.Ring ?: return@launch finish()
            val (reminder, settings) = decision
            current = reminder to settings.snoozeMinutes
            ServiceCompat.startForeground(
                this@AlarmService,
                notifications.idOf(noteId),
                notifications.ringing(reminder, settings.snoozeMinutes),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
            activeAlarm.start(noteId)
            withContext(Dispatchers.IO) { ringer.start(reminder.sound, settings.vibration) }
            val handled = withTimeoutOrNull(settings.alarmDurationSeconds * MILLIS_PER_SECOND) {
                activeAlarm.noteId.first { it != noteId }
            } != null
            ringer.stop()
            current = null
            if (!handled) activeAlarm.stop(noteId)
            ServiceCompat.stopForeground(this@AlarmService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            if (!handled) notifications.showMissed(reminder, settings.snoozeMinutes)
            finish()
        }
    }

    private fun finish() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        ringer.stop()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val STARTING_ID = 4_100
        private const val MILLIS_PER_SECOND = 1_000L

        fun intent(context: Context, noteId: String): Intent =
            Intent(context, AlarmService::class.java).putExtra(PhoneAlarmTarget.EXTRA_NOTE_ID, noteId)
    }
}
