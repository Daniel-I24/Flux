package com.flux.mobile.alarm

import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.mobile.sync.ReminderSync
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * "Completar" / "Posponer" on the phone (notification, full-screen alarm or lists): applied
 * locally (which silences the alarm through the scheduler) and mirrored to the watch.
 */
class ReminderActions @Inject constructor(
    private val completeNote: CompleteNoteUseCase,
    private val snoozeNote: SnoozeNoteUseCase,
    private val observeSettings: ObserveSettingsUseCase,
    private val reminderSync: ReminderSync,
) {
    suspend fun complete(noteId: String) {
        completeNote(noteId)
        reminderSync.completed(noteId)
    }

    suspend fun snooze(noteId: String) {
        val minutes = observeSettings().first().snoozeMinutes
        snoozeNote(noteId, minutes)
        reminderSync.snoozed(noteId, minutes)
    }
}
