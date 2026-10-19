package com.flux.core.ai.text

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{Mn}+")
private val SPACES = Regex("\\s+")

/** Lowercase, accent-free, single-spaced. Keeps "ñ" as "n" (same length for index mapping). */
fun String.normalizeSpanish(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(DIACRITICS, "").replace(SPACES, " ").trim()

/** Spanish number words that appear in dictated times and offsets. */
object SpanishNumbers {
    private val words = mapOf(
        "un" to 1, "uno" to 1, "una" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
        "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10, "once" to 11, "doce" to 12,
        "trece" to 13, "catorce" to 14, "quince" to 15, "veinte" to 20, "treinta" to 30,
        "cuarenta" to 40, "cincuenta" to 50,
    )

    /** Longest words first so "una" is not captured as "un". */
    val TOKEN: String = "(\\d{1,3}|" + words.keys.sortedByDescending { it.length }.joinToString("|") + ")"

    fun parse(token: String?): Int? = token?.let { it.toIntOrNull() ?: words[it] }
}
