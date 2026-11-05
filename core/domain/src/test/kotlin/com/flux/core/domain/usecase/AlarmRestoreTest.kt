package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmRestoreTest {
    private val hour = 60L * 60 * 1_000
    private val notes = FakeNoteRepository()
    private val alarms = FakeAlarms()
    private val clock = FixedClock(nowMillis = 100 * hour)
    private val restore = RescheduleAlarmsUseCase(notes, alarms, clock)

    private suspend fun seed() {
        notes.upsert(note("recent", dueAt = clock.nowMillis - 60_000, alarm = true))
        notes.upsert(note("old", dueAt = clock.nowMillis - 7 * hour, alarm = true))
        notes.upsert(note("future", dueAt = clock.nowMillis + hour, alarm = true))
    }

    @Test
    fun `after a reboot only recently missed alarms ring again`() = runTest {
        seed()
        assertEquals(2, restore(ringMissed = true))
        assertEquals(setOf("recent", "future"), alarms.scheduled.keys)
        assertTrue(alarms.scheduled.getValue("recent")!! > clock.nowMillis)
    }

    @Test
    fun `an app update or a clock change never rings an old reminder again`() = runTest {
        seed()
        assertEquals(1, restore())
        assertEquals(setOf("future"), alarms.scheduled.keys)
    }
}
