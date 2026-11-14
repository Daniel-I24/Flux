package com.flux.core.ai.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpanishNoteIntentDetectorTest {
    private val detector = SpanishNoteIntentDetector()

    @Test
    fun `greetings, small talk, noise and empty phrases are not notes`() {
        listOf(
            "Hola, ¿cómo estás?", "hola", "Buenos días", "¿Qué tal?", "Muchas gracias", "jajaja", "mmm este",
            "", "   ", "...", "ok", "probando probando", "uno dos tres", "Hola, ¿qué más? ¿Cómo te va?",
        ).forEach { assertFalse("\"$it\" should not be a note", detector.isNote(it)) }
    }

    @Test
    fun `ideas, facts and reminders are notes, even without a date`() {
        listOf(
            "idea: app para reciclar",
            "Comprar pan",
            "recuérdame llamar a mamá a las 8",
            "La clave del wifi es 4321",
            "Hola, recuérdame pagar el arriendo mañana",
            "examen de cálculo el viernes",
        ).forEach { assertTrue("\"$it\" should be a note", detector.isNote(it)) }
    }
}
