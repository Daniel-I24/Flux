package com.flux.core.ai.date

/** How the user qualified an hour: "a. m.", "p. m.", "de la noche", "del mediodía" or nothing. */
internal enum class Period { NONE, AM, PM, NIGHT, NOON }

/**
 * Spanish period markers on normalized text (lowercase, no accents, ASCII spaces):
 * am, a.m, a.m., a. m., pm, p.m, p. m., de/en/por la mañana/tarde/noche/madrugada, del mediodía.
 */
internal object TimePeriods {
    /** am/pm only; `\b` after the "m" keeps "a mamá" or "a mi casa" out. */
    const val AM_PM = "(?:a\\.?\\s?m\\b\\.?|p\\.?\\s?m\\b\\.?)"
    const val ANY = "(?:$AM_PM|(?:de|en|por) la (?:manana|tarde|noche|madrugada)|del mediodia)"

    fun of(raw: String?): Period {
        val value = raw?.trim().orEmpty()
        return when {
            value.isEmpty() -> Period.NONE
            value.startsWith("p") || value.endsWith("tarde") -> Period.PM
            value.endsWith("noche") -> Period.NIGHT
            value.endsWith("mediodia") -> Period.NOON
            else -> Period.AM
        }
    }

    /**
     * 24-hour value of [rawHour] said with [period], or null when the combination is impossible
     * ("15 a. m."). Without a period, 1–5 means the afternoon (N-049).
     */
    fun hour24(rawHour: Int, period: Period): Int? = when (period) {
        Period.NONE -> if (rawHour in AFTERNOON_BY_DEFAULT) rawHour + HALF_DAY else rawHour
        Period.AM -> when (rawHour) {
            HALF_DAY -> 0
            in 0 until HALF_DAY -> rawHour
            else -> null
        }
        Period.PM -> when (rawHour) {
            in 1 until HALF_DAY -> rawHour + HALF_DAY
            in HALF_DAY..LAST_HOUR -> rawHour
            else -> null
        }
        Period.NIGHT -> when (rawHour) {
            HALF_DAY -> 0
            in EVENING_FIRST until HALF_DAY -> rawHour + HALF_DAY
            in 0..LAST_HOUR -> rawHour
            else -> null
        }
        Period.NOON -> when (rawHour) {
            in AFTERNOON_BY_DEFAULT -> rawHour + HALF_DAY
            in 0..LAST_HOUR -> rawHour
            else -> null
        }
    }?.takeIf { it in 0..LAST_HOUR }

    /** Hours that may be morning or evening when said alone ("a las 8"). */
    fun isHalfDayOpen(rawHour: Int, period: Period): Boolean = period == Period.NONE && rawHour in EVENING_FIRST until HALF_DAY

    private const val HALF_DAY = 12
    private const val LAST_HOUR = 23
    private const val EVENING_FIRST = 6
    private val AFTERNOON_BY_DEFAULT = 1..5
}
