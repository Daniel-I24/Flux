package com.flux.core.domain.usecase

import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.AppError
import com.flux.core.model.AppSettings
import com.flux.core.model.Classification
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import javax.inject.Inject

/** Persists a classified note and schedules its alarm when needed. */
class CreateNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val clock: Clock,
    private val ids: IdGenerator,
    private val timing: AlarmTiming,
) {
    suspend operator fun invoke(
        text: String,
        classification: Classification,
        source: DeviceSource,
        noteId: String? = null,
        categoryCorrected: Boolean = false,
        alarmLeadMinutes: Int? = null,
    ): Note {
        val now = clock.now()
        val note = Note(
            id = noteId ?: ids.newId(),
            text = text.take(Note.MAX_TEXT_LENGTH),
            title = classification.title.take(Note.MAX_TITLE_LENGTH),
            categoryId = classification.categoryId,
            dueAt = classification.dueAt,
            priority = classification.priority,
            requiresAlarm = classification.requiresAlarm,
            source = source,
            engine = classification.engine,
            categoryCorrected = categoryCorrected,
            alarmLeadMinutes = alarmLeadMinutes,
            createdAt = now,
            updatedAt = now,
        )
        notes.upsert(note)
        timing.next(note, fallbackToDue = true)?.let { alarms.schedule(note, it) }
        return note
    }
}

/** Applies a change to an existing note, stamps it and keeps its alarm in sync. */
class UpdateNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val clock: Clock,
    private val timing: AlarmTiming,
) {
    suspend operator fun invoke(id: String, change: (Note) -> Note): FluxResult<Note> {
        val current = notes.getNote(id) ?: return FluxResult.Failure(AppError.NotFound)
        val updated = change(current).copy(updatedAt = maxOf(clock.now(), current.updatedAt + 1))
        notes.upsert(updated)
        // An overdue note keeps no alarm: editing it must not make it ring again right away.
        timing.next(updated, fallbackToDue = true)?.let { alarms.schedule(updated, it) } ?: alarms.cancel(id)
        return FluxResult.Success(updated)
    }
}

class CompleteNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String, completed: Boolean = true) = update(id) { it.copy(completed = completed) }
}

class SnoozeNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase, private val clock: Clock) {
    suspend operator fun invoke(id: String, minutes: Int) =
        update(id) { it.copy(dueAt = clock.now() + minutes * MILLIS_PER_MINUTE, completed = false, requiresAlarm = true) }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

/** Soft delete so the UI can offer "Deshacer". */
class DeleteNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase, private val clock: Clock) {
    suspend operator fun invoke(id: String) = update(id) { it.copy(deletedAt = clock.now()) }
}

class RestoreNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String) = update(id) { it.copy(deletedAt = null) }
}

/** "Reprogramar" from Vencidos: a new date keeps (or turns on) the alarm. */
class RescheduleNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String, dueAt: Long) = update(id) { it.copy(dueAt = dueAt, completed = false, requiresAlarm = true) }
}

/** Everything the phone's note editor can change. */
data class NoteEdit(
    val title: String,
    val text: String,
    val categoryId: String,
    val dueAt: Long?,
    val requiresAlarm: Boolean,
    val sound: SoundRef?,
    /** Own "Avisarme" lead; null = the global one. */
    val alarmLeadMinutes: Int? = null,
)

class EditNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String, edit: NoteEdit): FluxResult<Note> {
        val cleanTitle = edit.title.trim()
        if (cleanTitle.isEmpty() || cleanTitle.length > Note.MAX_TITLE_LENGTH) return FluxResult.Failure(AppError.InvalidInput)
        return update(id) { current ->
            current.copy(
                title = cleanTitle,
                text = edit.text.trim().take(Note.MAX_TEXT_LENGTH),
                categoryId = edit.categoryId,
                categoryCorrected = current.categoryCorrected || edit.categoryId != current.categoryId,
                dueAt = edit.dueAt,
                requiresAlarm = edit.requiresAlarm && edit.dueAt != null,
                sound = edit.sound,
                alarmLeadMinutes = edit.alarmLeadMinutes?.takeIf { it in AppSettings.ALARM_LEAD_OPTIONS },
            )
        }
    }
}
