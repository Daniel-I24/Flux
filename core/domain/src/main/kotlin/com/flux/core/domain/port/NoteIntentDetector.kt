package com.flux.core.domain.port

/**
 * Tells a note or reminder apart from small talk, greetings, noise or an empty phrase
 * ("Hola, ¿cómo estás?"). Ideas and facts without a date are notes.
 */
fun interface NoteIntentDetector {
    fun isNote(text: String): Boolean
}
