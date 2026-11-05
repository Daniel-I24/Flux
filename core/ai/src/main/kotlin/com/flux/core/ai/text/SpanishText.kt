package com.flux.core.ai.text

import java.text.Normalizer

private val DIACRITICS = Regex("\\p{Mn}+")

/**
 * Any Unicode space. Speech recognizers and Android formatters write "p. m." with a
 * no-break (U+00A0) or narrow no-break (U+202F) space, which `\s` does not match.
 */
private const val SPACE_CLASS = "[\\s\\u00A0\\u2007\\u202F]"
private val ANY_SPACE = Regex("$SPACE_CLASS+")
private val ONE_SPACE = Regex(SPACE_CLASS)

/** Collapses every kind of whitespace (including no-break spaces) to one ASCII space. */
fun String.normalizeSpaces(): String = replace(ANY_SPACE, " ").trim()

/** Lowercase, accent-free, single-spaced. Keeps "ñ" as "n" (same length for index mapping). */
fun String.normalizeSpanish(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD).replace(DIACRITICS, "").normalizeSpaces()

/** Lowercase, accent-free and with ASCII spaces, but the same length as the input so indexes map back. */
fun String.foldAccentsKeepingLength(): String = map { char ->
    if (ONE_SPACE.matches(char.toString())) {
        ' '
    } else {
        Normalizer.normalize(char.lowercaseChar().toString(), Normalizer.Form.NFD).replace(DIACRITICS, "").firstOrNull() ?: char
    }
}.joinToString("")

/** Spanish number words that appear in dictated times and offsets. */
object SpanishNumbers {
    private val words = mapOf(
        "un" to 1, "uno" to 1, "una" to 1, "dos" to 2, "tres" to 3, "cuatro" to 4, "cinco" to 5,
        "seis" to 6, "siete" to 7, "ocho" to 8, "nueve" to 9, "diez" to 10, "once" to 11, "doce" to 12,
        "trece" to 13, "catorce" to 14, "quince" to 15, "dieciseis" to 16, "diecisiete" to 17, "dieciocho" to 18,
        "diecinueve" to 19, "veinte" to 20, "veintiuno" to 21, "veintidos" to 22, "veintitres" to 23,
        "veinticuatro" to 24, "veinticinco" to 25, "veintiseis" to 26, "veintisiete" to 27, "veintiocho" to 28,
        "veintinueve" to 29, "treinta" to 30, "cuarenta" to 40, "cincuenta" to 50,
    )

    /** Digits or words, longest words first so "una" is not captured as "un" (no capturing group). */
    val ALTERNATIVES: String = "\\d{1,3}|" + words.keys.sortedByDescending { it.length }.joinToString("|")

    /** [ALTERNATIVES] as one capturing group. */
    val TOKEN: String = "($ALTERNATIVES)"

    fun parse(token: String?): Int? = token?.trim()?.let { it.toIntOrNull() ?: words[it] }
}
