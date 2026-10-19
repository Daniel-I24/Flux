package com.flux.core.domain.usecase

import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import javax.inject.Inject

/** Persists a classified note and schedules its alarm when needed. */
class CreateNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val clock: Clock,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(
        text: String,
        classification: Classification,
        source: DeviceSource,
        noteId: String? = null,
        categoryCorrected: Boolean = false,
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
            createdAt = now,
            updatedAt = now,
        )
        notes.upsert(note)
        if (note.hasPendingAlarm) alarms.schedule(note)
        return note
    }
}

/** Applies a change to an existing note, stamps it and keeps its alarm in sync. */
class UpdateNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: String, change: (Note) -> Note): FluxResult<Note> {
        val current = notes.getNote(id) ?: return FluxResult.Failure(AppError.NotFound)
        val updated = change(current).copy(updatedAt = maxOf(clock.now(), current.updatedAt + 1))
        notes.upsert(updated)
        if (updated.hasPendingAlarm) alarms.schedule(updated) else alarms.cancel(id)
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

/** The user fixed the AI's category in the preview: remembered so it is never reclassified. */
class CorrectCategoryUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String, categoryId: String) =
        update(id) { it.copy(categoryId = categoryId, categoryCorrected = true) }
}

class EditNoteUseCase @Inject constructor(private val update: UpdateNoteUseCase) {
    suspend operator fun invoke(id: String, title: String, text: String, dueAt: Long?): FluxResult<Note> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || cleanTitle.length > Note.MAX_TITLE_LENGTH) return FluxResult.Failure(AppError.InvalidInput)
        return update(id) { it.copy(title = cleanTitle, text = text.trim().take(Note.MAX_TEXT_LENGTH), dueAt = dueAt) }
    }
}
