package com.flux.core.ai.local

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.model.Category
import com.flux.core.model.CategoryIcon
import com.flux.core.model.Classification
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import com.flux.core.model.Priority
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalNoteClassifierTest {
    private val zone = ZoneId.of("America/Bogota")
    private val now = ZonedDateTime.of(2026, 10, 18, 10, 0, 0, 0, zone)
    private val classifier = LocalNoteClassifier()

    private suspend fun classify(text: String, categories: List<Category> = DefaultCategory.all()): Classification {
        val result = classifier.classify(ClassificationRequest(text, now.toInstant().toEpochMilli(), zone, categories, profile = null))
        return (result as FluxResult.Success).value
    }

    @Test
    fun `reminder with time gets an alarm and a clean title`() = runTest {
        val result = classify("Recuérdame entregar el proyecto de bases de datos mañana a las 8 am")
        assertEquals(DefaultCategory.REMINDERS.id, result.categoryId)
        assertEquals("Entregar el proyecto de bases de datos", result.title)
        assertEquals(now.plusDays(1).withHour(8).toInstant().toEpochMilli(), result.dueAt)
        assertTrue(result.requiresAlarm)
    }

    @Test
    fun `fast reminder in seconds`() = runTest {
        val result = classify("Recuérdame tomar agua en 10 segundos")
        assertEquals(DefaultCategory.REMINDERS.id, result.categoryId)
        assertEquals("Tomar agua", result.title)
        assertEquals(now.plusSeconds(10).toInstant().toEpochMilli(), result.dueAt)
    }

    @Test
    fun `classifies the default categories`() = runTest {
        assertEquals(DefaultCategory.SHOPPING.id, classify("Tengo que comprar los cables HDMI para el laboratorio").categoryId)
        assertEquals(DefaultCategory.HOMEWORK.id, classify("Estudiar para el examen de cálculo").categoryId)
        assertEquals(DefaultCategory.IDEAS.id, classify("Se me ocurrió una idea: una app para reciclar").categoryId)
        assertEquals(DefaultCategory.MEALS.id, classify("Cocinar arroz con pollo para la cena").categoryId)
        assertEquals(DefaultCategory.HEALTH.id, classify("Pedir cita con el odontólogo").categoryId)
        assertEquals(DefaultCategory.INFO.id, classify("El código del laboratorio es 4829").categoryId)
        assertEquals(DefaultCategory.TASKS.id, classify("Llamar a mamá").categoryId)
    }

    @Test
    fun `an action with a date keeps its specific category`() = runTest {
        val result = classify("Comprar café para la reunión del viernes")
        assertEquals(DefaultCategory.SHOPPING.id, result.categoryId)
        assertEquals("Comprar café para la reunión", result.title)
        assertNotNull(result.dueAt)
        assertFalse(result.requiresAlarm)
    }

    @Test
    fun `detects priority`() = runTest {
        assertEquals(Priority.HIGH, classify("Es urgente pagar el arriendo").priority)
        assertEquals(Priority.LOW, classify("Algún día aprender a tocar guitarra").priority)
    }

    @Test
    fun `custom categories win when named`() = runTest {
        val gym = Category(id = "gym", customName = "Gimnasio", colorArgb = 0xFF000000, icon = CategoryIcon.SPORT)
        assertEquals("gym", classify("Rutina de gimnasio: piernas", DefaultCategory.all() + gym).categoryId)
    }

    @Test
    fun `unknown text falls back to info without a date`() = runTest {
        val result = classify("Hola")
        assertEquals(DefaultCategory.INFO.id, result.categoryId)
        assertNull(result.dueAt)
    }
}
