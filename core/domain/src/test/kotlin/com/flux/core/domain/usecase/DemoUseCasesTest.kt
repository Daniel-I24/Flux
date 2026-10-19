package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.SequentialIds
import com.flux.core.domain.note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoUseCasesTest {
    private val notes = FakeNoteRepository()
    private val alarms = FakeAlarms()
    private val clock = FixedClock(nowMillis = 1_000_000)
    private val create = CreateNoteUseCase(notes, alarms, clock, SequentialIds())
    private val load = LoadDemoDataUseCase(create, clock)
    private val clear = ClearDemoDataUseCase(notes, DeleteNoteUseCase(UpdateNoteUseCase(notes, alarms, clock), clock))
    private val examples = listOf(
        DemoNote("comprar leche", "Comprar leche", "shopping"),
        DemoNote("tomar la pastilla", "Tomar la pastilla", "health", dueInSeconds = 10, requiresAlarm = true),
    )

    @Test
    fun `loading the demo creates the examples and schedules the 10 second alarm`() = runTest {
        assertEquals(2, load(examples))
        assertEquals(1_010_000L, alarms.scheduled["demo-2"])
        assertTrue(notes.getNote("demo-1")!!.categoryCorrected)
    }

    @Test
    fun `loading twice replaces the examples instead of duplicating them`() = runTest {
        load(examples)
        load(examples)
        assertEquals(2, notes.observeNotes().first().size)
    }

    @Test
    fun `clearing removes only demo notes and cancels their alarms`() = runTest {
        notes.upsert(note("mine"))
        load(examples)
        assertEquals(2, clear())
        assertEquals(listOf("mine"), notes.observeNotes().first().map { it.id })
        assertTrue("demo-2" in alarms.cancelled)
    }
}
