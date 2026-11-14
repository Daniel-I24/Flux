package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarmLead
import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FakeSettingsRepository
import com.flux.core.domain.FixedClock
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmLeadTest {
    private val clock = FixedClock(nowMillis = NOW)
    private val lead = FakeAlarmLead(minutes = 10)
    private val timing = AlarmTiming(lead, clock)

    private fun note(dueAt: Long, own: Int? = null) = Note(
        id = "n", text = "Cita", title = "Cita", categoryId = "health", dueAt = dueAt, requiresAlarm = true,
        source = DeviceSource.PHONE, engine = ClassifierEngine.LOCAL, alarmLeadMinutes = own, createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `the global lead moves the ring time earlier and a note's own lead wins`() = runTest {
        assertEquals(NOW + HOUR - 10 * MINUTE, timing.next(note(NOW + HOUR), fallbackToDue = false))
        assertEquals(NOW + HOUR - 30 * MINUTE, timing.next(note(NOW + HOUR, own = 30), fallbackToDue = false))
        assertEquals(NOW + HOUR, timing.next(note(NOW + HOUR, own = 0), fallbackToDue = false))
    }

    @Test
    fun `when the lead already passed only a user change rings at the time itself`() = runTest {
        val soon = note(NOW + 3 * MINUTE)
        assertEquals(NOW + 3 * MINUTE, timing.next(soon, fallbackToDue = true))
        // Sync and restore: the early alarm already rang, it must not ring twice.
        assertEquals(null, timing.next(soon, fallbackToDue = false))
    }

    @Test
    fun `changing the global lead reschedules pending alarms`() = runTest {
        val notes = FakeNoteRepository().apply { upsert(note(NOW + HOUR)) }
        val alarms = FakeAlarms()
        val settings = FakeSettingsRepository()
        val set = SetAlarmLeadUseCase(UpdateSettingsUseCase(settings), RescheduleAlarmsUseCase(notes, alarms, clock, timing))
        lead.minutes = 15
        set(15)
        assertEquals(NOW + HOUR - 15 * MINUTE, alarms.scheduled["n"])
        assertTrue(alarms.scheduled.isNotEmpty())
        set(7)
        assertFalse(settings.current.alarmLeadMinutes == 7)
    }

    private companion object {
        const val NOW = 1_760_799_600_000L
        const val MINUTE = 60_000L
        const val HOUR = 60 * MINUTE
    }
}
