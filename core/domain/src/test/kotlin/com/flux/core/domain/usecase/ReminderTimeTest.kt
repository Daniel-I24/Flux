package com.flux.core.domain.usecase

import com.flux.core.domain.FixedClock
import com.flux.core.domain.port.ScheduleGap
import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DailyRoutine
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTimeTest {
    private val zone = ZoneId.of("America/Bogota")
    private val now = ZonedDateTime.of(2026, 11, 5, 17, 0, 0, 0, zone)
    private val clock = FixedClock(now.toInstant().toEpochMilli())
    private val resolve = ResolveAmbiguityUseCase()
    private val guessed = Classification(
        categoryId = "tasks",
        title = "Salir",
        dueAt = 1L,
        requiresAlarm = true,
        confidence = 0.5,
        engine = ClassifierEngine.LOCAL,
        ambiguities = listOf(Ambiguity.DueTime(listOf(1L, 2L))),
    )
    private fun millis(day: Int, hour: Int) = ZonedDateTime.of(2026, 11, day, hour, 0, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun `a gap drops the guessed date and asks first`() {
        val asked = AskReminderTimeUseCase({ _, _ -> ScheduleGap(null) }, clock)("salir a las", guessed, null)
        assertNull(asked.dueAt)
        assertFalse(asked.requiresAlarm)
        assertEquals(listOf(Ambiguity.ReminderTime(listOf(millis(5, 18), millis(6, 8), millis(6, 16)))), asked.ambiguities)
    }

    @Test
    fun `no gap leaves the classification untouched`() {
        assertEquals(guessed, AskReminderTimeUseCase({ _, _ -> null }, clock)("salir a las 6", guessed, null))
    }

    @Test
    fun `late at night only tomorrow is suggested`() {
        val late = now.withHour(23)
        val options = ReminderSuggestions.of(null, late, DailyRoutine()).map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
        assertTrue(options.all { it == late.toLocalDate().plusDays(1) })
    }

    @Test
    fun `a picked time turns the alarm on and save without reminder keeps none`() {
        val asked = guessed.copy(dueAt = null, requiresAlarm = false, ambiguities = listOf(Ambiguity.ReminderTime(listOf(millis(5, 18)))))
        val custom = resolve.chooseReminder(asked, millis(9, 7))
        assertEquals(millis(9, 7), custom.dueAt)
        assertTrue(custom.requiresAlarm)
        assertFalse(custom.needsAnswer)
        val none = resolve.chooseReminder(asked, null)
        assertNull(none.dueAt)
        assertFalse(none.requiresAlarm)
        assertFalse(none.needsAnswer)
        assertNull(resolve.acceptDefaults(asked).dueAt)
    }
}
