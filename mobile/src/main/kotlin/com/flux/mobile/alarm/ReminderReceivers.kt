package com.flux.mobile.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.flux.core.domain.usecase.RescheduleAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/** Runs suspend work from a receiver without letting the process die mid-way. */
private fun BroadcastReceiver.runAsync(block: suspend () -> Unit) {
    val pending = goAsync()
    receiverScope.launch {
        try {
            block()
        } finally {
            pending.finish()
        }
    }
}

/** Alarm fired: ring with the alarm service, or a silent notice during FLUX quiet hours. */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var decider: ReminderAlarm

    @Inject lateinit var notifications: AlarmNotifications

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync {
            when (val decision = decider.decide(noteId)) {
                AlarmDecision.Skip -> Unit
                is AlarmDecision.Quiet -> notifications.showQuiet(decision.reminder, decision.settings.snoozeMinutes)
                is AlarmDecision.Ring -> if (!startService(context, noteId)) {
                    notifications.showFallback(decision.reminder, decision.settings.snoozeMinutes)
                }
            }
        }
    }

    /** setAlarmClock alarms may start a foreground service; if Android still refuses, fall back. */
    private fun startService(context: Context, noteId: String): Boolean = try {
        ContextCompat.startForegroundService(context, AlarmService.intent(context, noteId))
        true
    } catch (_: IllegalStateException) {
        // ForegroundServiceStartNotAllowedException (Android 12+) is an IllegalStateException too.
        false
    }

    companion object {
        const val ACTION_RING = "com.flux.mobile.action.RING"
    }
}

/** "Completar" / "Posponer" from a notification; mirrored to the watch. */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: ReminderActions

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync {
            when (intent.action) {
                ACTION_COMPLETE -> actions.complete(noteId)
                ACTION_SNOOZE -> actions.snooze(noteId)
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.flux.mobile.action.COMPLETE"
        const val ACTION_SNOOZE = "com.flux.mobile.action.SNOOZE"
    }
}

/** Reboot, app update (MY_PACKAGE_REPLACED) or time change: AlarmManager lost its alarms. */
@AndroidEntryPoint
class AlarmRestoreReceiver : BroadcastReceiver() {
    @Inject lateinit var reschedule: RescheduleAlarmsUseCase

    @Inject lateinit var channels: AlarmChannels

    override fun onReceive(context: Context, intent: Intent) {
        runAsync {
            channels.ensure()
            reschedule()
        }
    }
}
