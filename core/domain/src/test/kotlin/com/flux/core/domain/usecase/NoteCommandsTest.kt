package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.SequentialIds
import com.flux.core.domain.note
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteCommandsTest {
    private val notes = FakeNoteRepository()
    private val alarms = FakeAlarms()
    private val clock = FixedClock(nowMillis = 1_000_000)
    private val update = UpdateNoteUseCase(notes, alarms, clock)

    @Test
    fun `creating a note with an alarm schedules it`() = runTest {
        val classification =
            Classification(
                "reminders",
                "Tomar agua",
                dueAt = 1_010_000,
                requiresAlarm = true,
                confidence = 1.0,
                engine = ClassifierEngine.LOCAL
            )
        val created = CreateNoteUseCase(notes, alarms, clock, SequentialIds())("Recuérdame tomar agua", classification, DeviceSource.WATCH)
        assertEquals("id-1", created.id)
        assertEquals(1_010_000L, alarms.scheduled["id-1"])
        assertEquals(created, notes.getNote("id-1"))
    }

    @Test
    fun `an explicit id makes creation idempotent`() = runTest {
        val classification = Classification("tasks", "x", confidence = 1.0, engine = ClassifierEngine.LOCAL)
        val create = CreateNoteUseCase(notes, alarms, clock, SequentialIds())
        create("x", classification, DeviceSource.WATCH, noteId = "watch-123")
        create("x", classification, DeviceSource.WATCH, noteId = "watch-123")
        assertEquals(1, notes.store.value.size)
    }

    @Test
    fun `completing cancels the alarm and snoozing reschedules it`() = runTest {
        notes.upsert(note("a", dueAt = 1_000_500, alarm = true))
        CompleteNoteUseCase(update)("a")
        assertTrue("a" in alarms.cancelled)
        SnoozeNoteUseCase(update, clock)("a", 5)
        val snoozed = notes.getNote("a")!!
        assertFalse(snoozed.completed)
        assertEquals(1_000_000L + 5 * 60_000, snoozed.dueAt)
        assertEquals(snoozed.dueAt, alarms.scheduled["a"])
    }

    @Test
    fun `updates always produce a newer revision`() = runTest {
        notes.upsert(note("a", updatedAt = 5_000_000))
        val updated = (CompleteNoteUseCase(update)("a") as FluxResult.Success).value
        assertTrue(updated.updatedAt > 5_000_000)
    }

    @Test
    fun `delete is soft and can be undone`() = runTest {
        notes.upsert(note("a"))
        DeleteNoteUseCase(update, clock)("a")
        assertFalse(notes.getNote("a")!!.isActive)
        RestoreNoteUseCase(update)("a")
        assertTrue(notes.getNote("a")!!.isActive)
    }

    @Test
    fun `category correction is remembered`() = runTest {
        notes.upsert(note("a"))
        CorrectCategoryUseCase(update)("a", "health")
        val corrected = notes.getNote("a")!!
        assertEquals("health", corrected.categoryId)
        assertTrue(corrected.categoryCorrected)
    }

    @Test
    fun `editing validates the title`() = runTest {
        notes.upsert(note("a"))
        assertEquals(FluxResult.Failure(AppError.InvalidInput), EditNoteUseCase(update)("a", "  ", "x", null))
        assertEquals(FluxResult.Failure(AppError.NotFound), EditNoteUseCase(update)("zzz", "ok", "x", null))
    }

    @Test
    fun `remote notes merge with last write wins and are idempotent`() = runTest {
        val apply = ApplyRemoteNoteUseCase(notes, alarms)
        assertTrue(apply(note("a", updatedAt = 10, dueAt = 50, alarm = true)))
        assertFalse(apply(note("a", updatedAt = 10, dueAt = 50, alarm = true)))
        assertFalse(apply(note("a", updatedAt = 5)))
        assertEquals(50L, alarms.scheduled["a"])
        assertTrue(apply(note("a", updatedAt = 20).copy(completed = true)))
        assertTrue("a" in alarms.cancelled)
    }
}
