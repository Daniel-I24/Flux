package com.flux.core.domain.usecase

import com.flux.core.domain.policy.ReminderSoundPolicy
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetNoteUseCase @Inject constructor(private val notes: NoteRepository) {
    suspend operator fun invoke(id: String): Note? = notes.getNote(id)
}

data class Reminder(val note: Note, val category: Category?, val sound: SoundRef)

/** A ringing reminder with its category and the sound it must play ([ReminderSoundPolicy]). */
class GetReminderUseCase @Inject constructor(private val notes: NoteRepository, private val categories: CategoryRepository) {
    suspend operator fun invoke(id: String, defaultSound: SoundRef): Reminder? {
        val note = notes.getNote(id) ?: return null
        val category = categories.getCategories().firstOrNull { it.id == note.categoryId }
        return Reminder(note, category, ReminderSoundPolicy.resolve(note, category, defaultSound))
    }
}

/** After a reboot (or time change) AlarmManager forgets everything: schedule pending reminders again. */
class RescheduleAlarmsUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Int {
        val now = clock.now()
        // Missed recently (device off or restarting): ring right away. Older ones just stay in Vencidos.
        val pending = notes.getPendingAlarms().filter { (it.dueAt ?: 0) > now - MISSED_WINDOW_MS }
        pending.forEach { note ->
            val due = note.dueAt?.coerceAtLeast(now + RING_DELAY_MS) ?: return@forEach
            alarms.schedule(note.copy(dueAt = due))
        }
        return pending.size
    }

    private companion object {
        const val RING_DELAY_MS = 5_000L
        const val MISSED_WINDOW_MS = 6 * 60 * 60 * 1_000L
    }
}

/** One note, live (the full-screen alarm closes itself when the note is handled elsewhere). */
class ObserveNoteUseCase @Inject constructor(private val notes: NoteRepository) {
    operator fun invoke(id: String): Flow<Note?> = notes.observeNote(id)
}
