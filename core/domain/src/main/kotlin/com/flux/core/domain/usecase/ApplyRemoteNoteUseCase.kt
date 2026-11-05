package com.flux.core.domain.usecase

import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
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
    private val clock: Clock,
) {
    suspend operator fun invoke(note: Note): Boolean {
        val applied = notes.upsertIfNewer(note)
        when {
            !applied -> Unit
            !note.hasPendingAlarm -> alarms.cancel(note.id)
            // Already overdue: it rang (or was missed) on the other device; it stays in Vencidos silently.
            note.dueAt!! > clock.now() -> alarms.schedule(note)
        }
        return applied
    }
}
