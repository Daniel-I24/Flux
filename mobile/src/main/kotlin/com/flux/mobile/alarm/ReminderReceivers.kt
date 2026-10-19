package com.flux.mobile.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.domain.usecase.RescheduleAlarmsUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.mobile.sync.ReminderSync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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

/** Alarm fired: show the reminder notification. */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var notifier: ReminderNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync { notifier.show(noteId) }
    }

    companion object {
        const val ACTION_RING = "com.flux.mobile.action.RING"
    }
}

/** "Completar" / "Posponer" from the notification; mirrored to the watch. */
@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var complete: CompleteNoteUseCase

    @Inject lateinit var snooze: SnoozeNoteUseCase

    @Inject lateinit var observeSettings: ObserveSettingsUseCase

    @Inject lateinit var reminderSync: ReminderSync

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync {
            when (intent.action) {
                ACTION_COMPLETE -> {
                    complete(noteId)
                    reminderSync.completed(noteId)
                }
                ACTION_SNOOZE -> {
                    val minutes = observeSettings().first().snoozeMinutes
                    snooze(noteId, minutes)
                    reminderSync.snoozed(noteId, minutes)
                }
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.flux.mobile.action.COMPLETE"
        const val ACTION_SNOOZE = "com.flux.mobile.action.SNOOZE"
    }
}

/** Reboot, app update or time change: AlarmManager lost its alarms. */
@AndroidEntryPoint
class AlarmRestoreReceiver : BroadcastReceiver() {
    @Inject lateinit var reschedule: RescheduleAlarmsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        runAsync { reschedule() }
    }
}
