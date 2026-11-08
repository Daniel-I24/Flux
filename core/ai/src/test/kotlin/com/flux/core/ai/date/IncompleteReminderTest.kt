package com.flux.core.ai.date

import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.DailyRoutine
import com.flux.core.model.Occupation
import com.flux.core.model.TimeOfDay
import com.flux.core.model.UserProfile
import java.time.Instant
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Thursday 5 Nov 2026, 10:00: incomplete reminders become "¿Cuándo te aviso?" with suggestions. */
class IncompleteReminderTest : HourVariantsHarness() {
    private fun Classification.options(): List<ZonedDateTime>? = ambiguities.filterIsInstance<Ambiguity.ReminderTime>()
        .firstOrNull()?.options?.map { Instant.ofEpochMilli(it).atZone(zone) }

    private fun assertAsks(classification: Classification) {
        assertNull(classification.dueAt)
        assertFalse(classification.requiresAlarm)
        assertTrue(classification.ambiguities.first() is Ambiguity.ReminderTime)
        assertTrue(classification.needsAnswer)
    }

    @Test
    fun `a las without an hour asks with today's and tomorrow's suggestions`() = runTest {
        val result = classify("salir de mi casa a las")
        assertAsks(result)
        assertEquals(listOf(at(2026, 11, 5, 16), at(2026, 11, 5, 18), at(2026, 11, 6, 8)), result.options())
    }

    @Test
    fun `manana a las suggests times tomorrow`() = runTest {
        val result = classify("mañana a las")
        assertAsks(result)
        assertEquals(listOf(at(2026, 11, 6, 8), at(2026, 11, 6, 16), at(2026, 11, 6, 18)), result.options())
    }

    @Test
    fun `a weekday without a time suggests times that day`() = runTest {
        val result = classify("pagar el arriendo el viernes")
        assertAsks(result)
        assertEquals(at(2026, 11, 6, 8), result.options()!!.first())
    }

    @Test
    fun `a part of the day without the hour asks`() = runTest {
        assertAsks(classify("llamar al médico a las de la tarde"))
    }

    @Test
    fun `a cut day, a dictation with a period and a bare reminder ask`() = runTest {
        assertAsks(classify("llamar a Juan el"))
        assertAsks(classify("salir de mi casa a las."))
        assertAsks(classify("recuérdame llamar a mamá"))
    }

    @Test
    fun `an explicit time always wins over suggestions`() = runTest {
        val result = classify("salir de mi casa a las 6 de la tarde")
        assertNull(result.options())
        assertEquals(at(2026, 11, 5, 18).toInstant().toEpochMilli(), result.dueAt)
        assertTrue(result.requiresAlarm)
        assertNull(classify("el viernes a las 3 pm").options())
    }

    @Test
    fun `notes without a reminder are not questioned`() = runTest {
        assertTrue(classify("comprar pan").ambiguities.isEmpty())
        assertTrue(classify("ir a la tienda").ambiguities.isEmpty())
        assertTrue(classify("llevar a las niñas al parque").ambiguities.isEmpty())
    }

    @Test
    fun `suggestions come from the profile's routine`() = runTest {
        val routine = DailyRoutine(breakfast = TimeOfDay.of(6), dinner = TimeOfDay.of(20))
        val profile = UserProfile("Ana", age = 20, occupation = Occupation.STUDENT, routine = routine)
        val result = classify("mañana a las", profile = profile)
        assertEquals(listOf(at(2026, 11, 6, 7), at(2026, 11, 6, 16), at(2026, 11, 6, 18, 30)), result.options())
    }
}
