package com.flux.core.ai.date

import com.flux.core.model.DailyRoutine
import com.flux.core.model.TimeOfDay
import com.flux.core.model.TimeRange
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanishDateParserTest {
    private val zone = ZoneId.of("America/Bogota")

    // Sunday 18 Oct 2026, 10:00.
    private val now = ZonedDateTime.of(2026, 10, 18, 10, 0, 0, 0, zone)
    private val parser = SpanishDateParser()

    private fun at(day: Int, hour: Int, minute: Int = 0, month: Int = 10) = ZonedDateTime.of(2026, month, day, hour, minute, 0, 0, zone)

    private fun parse(text: String) = parser.parse(text, now)?.dateTime

    @Test
    fun `returns null without dates`() {
        assertNull(parse("El código del laboratorio es 4829"))
    }

    @Test
    fun `parses relative offsets`() {
        assertEquals(now.plusSeconds(10), parse("recuérdame tomar agua en 10 segundos"))
        assertEquals(now.plusMinutes(20), parse("en veinte minutos llamar a Ana"))
        assertEquals(now.plusHours(2), parse("dentro de 2 horas"))
        assertEquals(now.plusMinutes(30), parse("en media hora"))
        assertEquals(now.plusDays(3), parse("en tres dias"))
    }

    @Test
    fun `parses tomorrow at a time`() {
        assertEquals(at(19, 8), parse("entregar el proyecto mañana a las 8"))
        assertEquals(at(19, 8), parse("mañana a las 8 am"))
    }

    @Test
    fun `does not confuse de la mañana with tomorrow`() {
        assertEquals(at(18, 11), parse("a las 11 de la mañana"))
    }

    @Test
    fun `moves a past time without a day to tomorrow`() {
        assertEquals(at(19, 7), parse("a las 7 de la mañana"))
    }

    @Test
    fun `understands pm, words and fractions`() {
        assertEquals(at(18, 15), parse("hoy a las 3 pm"))
        assertEquals(at(18, 20, 30), parse("a las ocho y media de la noche"))
        assertEquals(at(19, 13), parse("mañana a la una de la tarde"))
        assertEquals(at(18, 15, 45), parse("a las 15:45"))
        assertEquals(at(18, 18, 45), parse("a las siete menos cuarto de la tarde"))
    }

    @Test
    fun `parses weekdays and dates`() {
        assertEquals(at(23, 9), parse("el viernes"))
        assertEquals(at(19, 10), parse("el lunes a las 10"))
        assertEquals(at(25, 9), parse("el próximo domingo"))
        assertEquals(at(25, 9, month = 12), parse("el 25 de diciembre"))
    }

    @Test
    fun `parses parts of the day`() {
        assertEquals(at(20, 16), parse("pasado mañana en la tarde"))
        assertEquals(at(18, 20), parse("esta noche"))
        assertEquals(at(19, 16), parse("mañana en la tarde"))
        assertEquals(at(18, 12), parse("al mediodía"))
    }

    @Test
    fun `uses the user routine for habit phrases`() {
        val routine = DailyRoutine(
            lunch = TimeOfDay.of(13),
            sleep = TimeOfDay.of(23),
            activityBlocks = mapOf(DayOfWeek.MONDAY to TimeRange(TimeOfDay.of(7), TimeOfDay.of(14))),
        )
        val withRoutine = SpanishDateParser(routine)
        assertEquals(at(18, 13), withRoutine.parse("a la hora del almuerzo", now)?.dateTime)
        assertEquals(at(18, 22, 30), withRoutine.parse("antes de dormir", now)?.dateTime)
        assertEquals(at(19, 14), withRoutine.parse("el lunes después de clase", now)?.dateTime)
    }

    @Test
    fun `reports fragments and explicit time`() {
        val parsed = parser.parse("mañana a las 8 am", now)
        assertEquals(listOf("manana", "a las 8 am"), parsed?.fragments)
        assertTrue(parsed!!.hasExplicitTime)
        assertFalse(parser.parse("el viernes", now)!!.hasExplicitTime)
    }
}
