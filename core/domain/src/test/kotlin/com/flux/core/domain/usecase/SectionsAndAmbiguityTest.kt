package com.flux.core.domain.usecase

import app.cash.turbine.test
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.note
import com.flux.core.domain.port.Clock
import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import java.time.ZoneId
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionsAndAmbiguityTest {
    /** Reads the virtual time of the test, so "now" moves with advanceTimeBy. */
    private class VirtualClock(private val scope: TestScope, private val start: Long) : Clock {
        override fun now() = start + scope.currentTime
        override fun zone(): ZoneId = ZoneId.of("America/Bogota")
    }

    @Test
    fun `notes are split into upcoming, pending, overdue and completed`() {
        val sections = NoteSections.of(
            listOf(
                note("soon", dueAt = 200),
                note("later", dueAt = 900),
                note("late", dueAt = 50),
                note("undated"),
                note("done", dueAt = 10).copy(completed = true),
                note("deleted", dueAt = 300).copy(deletedAt = 1),
            ),
            now = 100,
        )
        assertEquals(listOf("soon", "later"), sections.upcoming.map { it.id })
        assertEquals(listOf("undated"), sections.pending.map { it.id })
        assertEquals(listOf("late"), sections.overdue.map { it.id })
        assertEquals(listOf("done"), sections.completed.map { it.id })
        assertEquals("soon", sections.next?.id)
    }

    @Test
    fun `an upcoming reminder becomes overdue by itself, without reopening the app`() = runTest {
        val notes = FakeNoteRepository()
        notes.upsert(note("a", dueAt = 1_000_000 + 30_000, alarm = true))
        ObserveNoteSectionsUseCase(notes, VirtualClock(this, start = 1_000_000))().test {
            assertEquals("a", awaitItem().next?.id)
            advanceTimeBy(31_000)
            val after = awaitItem()
            assertNull(after.next)
            assertEquals(listOf("a"), after.overdue.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `answering an ambiguity applies it and removes the question`() {
        val resolve = ResolveAmbiguityUseCase()
        val base = Classification(
            categoryId = "shopping",
            title = "Pastillas",
            dueAt = 10,
            confidence = 0.6,
            engine = ClassifierEngine.LOCAL,
            ambiguities = listOf(Ambiguity.DueTime(listOf(10, 20)), Ambiguity.CategoryChoice(listOf("shopping", "health"))),
        )
        val timed = resolve.chooseTime(base, 20)
        assertEquals(20L, timed.dueAt)
        assertEquals(listOf(Ambiguity.CategoryChoice(listOf("shopping", "health"))), timed.ambiguities)
        val done = resolve.chooseCategory(timed, "health")
        assertEquals("health", done.categoryId)
        assertTrue(done.ambiguities.isEmpty())
        assertEquals(base, resolve.chooseTime(base, 99))
        assertTrue(resolve.acceptDefaults(base).ambiguities.isEmpty())
    }
}
