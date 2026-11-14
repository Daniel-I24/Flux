package com.flux.core.ai.local

import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.domain.port.NoteIntentDetector
import javax.inject.Inject

/**
 * Conservative offline check: a text is not a note only when, after removing greetings,
 * courtesy, small talk and fillers, no meaningful word is left. "idea: app para reciclar"
 * keeps "app" and "reciclar", so it is a note; "hola, ¿cómo estás?" keeps nothing.
 */
class SpanishNoteIntentDetector @Inject constructor() : NoteIntentDetector {
    override fun isNote(text: String): Boolean {
        val normalized = text.normalizeSpanish()
        val stripped = SMALL_TALK.fold(normalized) { rest, phrase -> rest.replace(phrase, " ") }
        return WORD.findAll(stripped).map { it.value }.any { it.length >= MIN_WORD_LENGTH && it !in FILLERS }
    }

    private companion object {
        const val MIN_WORD_LENGTH = 3
        val WORD = Regex("[a-z0-9]+")

        /** Whole phrases first, matched on word boundaries (the text is accent-free and lowercase). */
        val SMALL_TALK = listOf(
            "buenos dias", "buenas tardes", "buenas noches", "como estas", "como esta", "como estan", "como te va",
            "como vas", "que tal", "que mas", "que haces", "que onda", "quien eres", "mucho gusto", "muchas gracias",
            "hasta luego", "hasta manana", "nos vemos", "un momento", "probando probando", "uno dos tres",
        ).map { Regex("\\b$it\\b") }

        val FILLERS = setOf(
            "hola", "holi", "hey", "ola", "buenas", "buenos", "dias", "tardes", "noches", "bien", "mal", "gracias",
            "chao", "adios", "bye", "jaja", "jajaja",
            "jeje", "ajam", "aja", "mmm", "eh", "este", "pues", "bueno", "vale", "okay", "listo", "nada", "todo", "usted",
            "ustedes", "tu", "estas", "esta", "estoy", "muy", "que", "como", "por", "favor", "amigo", "amiga", "senor",
            "senora", "parce", "prueba", "probando", "test", "sip", "nop", "claro", "perfecto", "genial", "hello", "hi",
            "the", "and", "you", "how", "are", "cuentame", "dime", "oye", "mira", "alo", "aqui", "alli", "eso", "esto",
        )
    }
}
