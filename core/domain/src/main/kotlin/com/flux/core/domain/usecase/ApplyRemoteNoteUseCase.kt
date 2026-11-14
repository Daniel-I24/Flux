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
    private val timing: AlarmTiming,
) {
    suspend operator fun invoke(incoming: Note): Boolean {
        // The watch never receives custom tones (their files live on the phone), so its revisions carry no sound.
        val note = if (incoming.sound == null) incoming.copy(sound = notes.getNote(incoming.id)?.sound) else incoming
        val applied = notes.upsertIfNewer(note)
        when {
            !applied -> Unit
            !note.hasPendingAlarm -> alarms.cancel(note.id)
            // Already rang (or was missed) on the other device: it stays silent, overdue ones in Vencidos.
            else -> timing.next(note, fallbackToDue = false)?.let { alarms.schedule(note, it) }
        }
        return applied
    }
}
