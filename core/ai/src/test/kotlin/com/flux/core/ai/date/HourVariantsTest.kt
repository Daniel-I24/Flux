package com.flux.core.ai.date

import com.flux.core.ai.local.LocalNoteClassifier
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.usecase.ClassifyNoteUseCase
import com.flux.core.model.Ambiguity
import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.core.model.DailyRoutine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import com.flux.core.model.Occupation
import com.flux.core.model.TimeOfDay
import com.flux.core.model.TimeRange
import com.flux.core.model.UserProfile
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** End-to-end hour variants through [ClassifyNoteUseCase] with an injected clock (America/Bogota). */
class HourVariantsTest {
    private val zone = ZoneId.of("America/Bogota")

    private class FixedClock(private val at: ZonedDateTime) : Clock {
        override fun now() = at.toInstant().toEpochMilli()
        override fun zone(): ZoneId = at.zone
    }

    private class Categories : CategoryRepository {
        override fun observeCategories() = flowOf(DefaultCategory.all())
        override suspend fun getCategories(): List<Category> = DefaultCategory.all()
        override suspend fun upsert(category: Category) = Unit
        override suspend fun delete(id: String) = Unit
        override suspend fun ensureDefaults() = Unit
    }

    private class Profiles(private val profile: UserProfile?) : ProfileRepository {
        override fun observeProfile() = flowOf(profile)
        override suspend fun getProfile() = profile
        override suspend fun saveProfile(profile: UserProfile) = Unit
        override suspend fun clear() = Unit
    }

    /** Thursday 5 Nov 2026, 10:00. */
    private val thursday = at(2026, 11, 5, 10)

    private fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone)

    private suspend fun classify(text: String, now: ZonedDateTime = thursday, profile: UserProfile? = null): Classification {
        val useCase = ClassifyNoteUseCase(listOf(LocalNoteClassifier()), Categories(), Profiles(profile), FixedClock(now))
        return (useCase(text) as FluxResult.Success).value
    }

    private suspend fun due(text: String, now: ZonedDateTime = thursday) =
        classify(text, now).dueAt?.let { java.time.Instant.ofEpochMilli(it).atZone(zone) }

    @Test
    fun `the two reported notes keep their explicit time`() = runTest {
        val clase = classify("clase para mañana a las 7 a.m de Diseño de Interfaces")
        assertEquals(at(2026, 11, 6, 7).toInstant().toEpochMilli(), clase.dueAt)
        assertEquals("Clase de Diseño de Interfaces", clase.title)
        assertEquals(DefaultCategory.HOMEWORK.id, clase.categoryId)
        val cita = classify("Cita odontológica hoy a las 4:20 p.m")
        assertEquals(at(2026, 11, 5, 16, 20).toInstant().toEpochMilli(), cita.dueAt)
        assertEquals("Cita odontológica", cita.title)
        assertEquals(DefaultCategory.HEALTH.id, cita.categoryId)
        assertTrue(cita.requiresAlarm)
    }

    @Test
    fun `every way of writing a m and p m`() = runTest {
        listOf("a las 9 am", "a las 9 a.m", "a las 9 a.m.", "a las 9 a. m.", "a las 9 a. m.", "a las 9 a. m.")
            .forEach { assertEquals(it, at(2026, 11, 6, 9), due("revisar correo $it")) }
        listOf("hoy a las 3 pm", "hoy a las 3 p.m", "hoy a las 3 p.m.", "hoy a las 3 p. m.", "hoy a las 3 p. m.")
            .forEach { assertEquals(it, at(2026, 11, 5, 15), due("pagar la luz $it")) }
    }

    @Test
    fun `no-break spaces never leak into the title`() = runTest {
        val note = classify("Cita odontológica hoy a las 4:20 p. m.")
        assertEquals("Cita odontológica", note.title)
        assertEquals(at(2026, 11, 5, 16, 20).toInstant().toEpochMilli(), note.dueAt)
    }

    @Test
    fun `parts of the day, noon and midnight`() = runTest {
        assertEquals(at(2026, 11, 5, 20), due("llamar a Ana a las 8 de la noche"))
        assertEquals(at(2026, 11, 6, 10), due("mañana a las 10 de la mañana"))
        assertEquals(at(2026, 11, 5, 16), due("a las 4 de la tarde"))
        assertEquals(at(2026, 11, 6, 2), due("a las 2 de la madrugada"))
        assertEquals(at(2026, 11, 5, 12), due("almorzar al mediodía"))
        assertEquals(at(2026, 11, 5, 12), due("a las 12 del mediodía"))
        assertEquals(at(2026, 11, 6, 0), due("apagar el horno a medianoche"))
        assertEquals(at(2026, 11, 5, 11), due("a las 11 en la mañana"))
    }

    @Test
    fun `minutes, halves, quarters and quarter to`() = runTest {
        assertEquals(at(2026, 11, 5, 16, 20), due("hoy 4:20 p.m."))
        assertEquals(at(2026, 11, 5, 16, 20), due("a las 4 y 20"))
        assertEquals(at(2026, 11, 5, 16, 20), due("a las cuatro y veinte de la tarde"))
        assertEquals(at(2026, 11, 5, 16, 30), due("a las 4 y media"))
        assertEquals(at(2026, 11, 5, 16, 15), due("a las 4 y cuarto"))
        assertEquals(at(2026, 11, 5, 16, 45), due("cuarto para las 5"))
        assertEquals(at(2026, 11, 5, 16, 50), due("diez para las 5 de la tarde"))
        assertEquals(at(2026, 11, 5, 16, 45), due("a las 5 menos cuarto"))
        assertEquals(at(2026, 11, 5, 16, 20), due("cita a las 16:20"))
    }

    @Test
    fun `one to five without a period is the afternoon`() = runTest {
        assertEquals(at(2026, 11, 5, 15), due("dentista a las 3"))
        assertEquals(at(2026, 11, 5, 13), due("a la una"))
        assertTrue(classify("dentista a las 3").ambiguities.isEmpty())
    }

    @Test
    fun `asks only when an hour is really ambiguous`() = runTest {
        val reunion = classify("reunión a las 8")
        val expected = listOf(at(2026, 11, 5, 20), at(2026, 11, 6, 8)).map { it.toInstant().toEpochMilli() }
        assertEquals(listOf(Ambiguity.DueTime(expected)), reunion.ambiguities)
        assertEquals(expected.first(), reunion.dueAt)
        assertEquals(at(2026, 11, 6, 8), due("mañana a las 8"))
        assertTrue(classify("mañana a las 8").ambiguities.isEmpty())
        assertEquals(at(2026, 11, 5, 20), due("hoy a las 8"))
        assertTrue(classify("hoy a las 8").ambiguities.isEmpty())
        assertEquals(at(2026, 11, 5, 20), due("a las 8 esta noche"))
        assertEquals(at(2026, 11, 5, 11), due("a las 11"))
    }

    @Test
    fun `crosses days, months and years`() = runTest {
        assertEquals(at(2027, 1, 1, 10), due("a las 10 de la mañana", at(2026, 12, 31, 23, 30)))
        assertEquals(at(2026, 12, 1, 7), due("mañana a las 7 am", at(2026, 11, 30, 21)))
        assertEquals(at(2027, 1, 1, 0), due("a medianoche", at(2026, 12, 31, 22)))
        assertEquals(at(2026, 3, 1, 18), due("mañana a las 6 pm", at(2026, 2, 28, 20)))
        assertEquals(at(2027, 1, 1, 9), due("el 1 de enero a las 9 am"))
        assertEquals(at(2026, 11, 16, 8), due("el lunes a las 8 am", at(2026, 11, 9, 10)))
        assertEquals(at(2026, 11, 6, 15), due("a las 3 pm", at(2026, 11, 5, 15, 30)))
    }

    @Test
    fun `an explicit hour is never replaced by the profile schedule`() = runTest {
        val routine = DailyRoutine(activityBlocks = mapOf(DayOfWeek.FRIDAY to TimeRange(TimeOfDay.of(9), TimeOfDay.of(17))))
        val profile = UserProfile("Ana", 20, Occupation.STUDENT, routine)
        val note = classify("clase de diseño mañana a las 7 a.m", profile = profile)
        assertEquals(at(2026, 11, 6, 7).toInstant().toEpochMilli(), note.dueAt)
        val afterClass = classify("estudiar después de clase a las 6 pm", profile = profile)
        assertEquals(at(2026, 11, 5, 18).toInstant().toEpochMilli(), afterClass.dueAt)
        assertFalse(afterClass.needsAnswer)
    }
}
