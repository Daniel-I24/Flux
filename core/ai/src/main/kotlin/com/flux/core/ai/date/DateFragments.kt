package com.flux.core.ai.date

import com.flux.core.ai.text.SpanishNumbers
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class OffsetMatch(val duration: Duration, val text: String)

/** [weekly] is true for weekdays ("el lunes"): a time that already passed today moves a week ahead. */
data class DayMatch(val date: LocalDate, val impliedTime: LocalTime?, val text: String, val weekly: Boolean = false)

/**
 * A time of day found in the text. [halfDayOpen] is true when the user said an hour from
 * 6 to 11 without "a. m.", "p. m." or a part of the day, so it may be morning or evening.
 */
data class TimeMatch(val time: LocalTime, val text: String, val halfDayOpen: Boolean = false)

/** "en 10 segundos", "dentro de cinco minutos", "en 2 horas", "en 3 dias", "en media hora". */
internal object OffsetParser {
    private val pattern = Regex("\\b(?:en|dentro de)\\s+${SpanishNumbers.TOKEN}\\s+(segundo|minuto|hora|dia)s?\\b")
    private val halfHour = Regex("\\b(?:en|dentro de)\\s+media\\s+hora\\b")

    fun parse(text: String): OffsetMatch? {
        halfHour.find(text)?.let { return OffsetMatch(Duration.ofMinutes(HALF_HOUR_MINUTES), it.value) }
        val match = pattern.find(text) ?: return null
        val amount = SpanishNumbers.parse(match.groupValues[1])?.toLong()?.takeIf { it > 0 } ?: return null
        val duration = when (match.groupValues[2]) {
            "segundo" -> Duration.ofSeconds(amount)
            "minuto" -> Duration.ofMinutes(amount)
            "hora" -> Duration.ofHours(amount)
            else -> Duration.ofDays(amount)
        }
        return OffsetMatch(duration, match.value)
    }

    private const val HALF_HOUR_MINUTES = 30L
}
