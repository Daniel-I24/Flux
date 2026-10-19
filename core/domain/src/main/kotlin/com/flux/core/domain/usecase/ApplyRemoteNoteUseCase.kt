package com.flux.core.domain.usecase

import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import javax.inject.Inject

/**
 * Merges a revision received from the other device (last-write-wins on updatedAt).
 * Idempotent: receiving the same revision twice changes nothing.
 */
class ApplyRemoteNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
) {
    suspend operator fun invoke(note: Note): Boolean {
        val applied = notes.upsertIfNewer(note)
        if (applied) {
            if (note.hasPendingAlarm) alarms.schedule(note) else alarms.cancel(note.id)
        }
        return applied
    }
}
