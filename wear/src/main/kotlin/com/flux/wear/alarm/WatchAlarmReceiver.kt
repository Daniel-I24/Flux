package com.flux.wear.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.flux.core.domain.usecase.GetNoteUseCase
import com.flux.core.domain.usecase.RescheduleAlarmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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

/** A reminder is due on the watch: full-screen alert with haptics. */
@AndroidEntryPoint
class WatchAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var getNote: GetNoteUseCase

    @Inject lateinit var alertNotifier: WatchAlertNotifier

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(WatchAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync { getNote(noteId)?.takeIf { it.hasPendingAlarm }?.let { alertNotifier.show(it) } }
    }
}

/** Notification actions (Completar / Posponer) when the alert screen was not opened. */
@AndroidEntryPoint
class WatchReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var actions: ReminderActions

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra(WatchAlarmTarget.EXTRA_NOTE_ID) ?: return
        runAsync {
            when (intent.action) {
                ACTION_COMPLETE -> actions.complete(noteId)
                ACTION_SNOOZE -> actions.snooze(noteId)
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.flux.wear.action.COMPLETE"
        const val ACTION_SNOOZE = "com.flux.wear.action.SNOOZE"
    }
}

@AndroidEntryPoint
class WatchAlarmRestoreReceiver : BroadcastReceiver() {
    @Inject lateinit var reschedule: RescheduleAlarmsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        runAsync { reschedule() }
    }
}
