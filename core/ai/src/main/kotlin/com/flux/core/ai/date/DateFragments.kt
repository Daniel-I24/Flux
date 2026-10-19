package com.flux.core.ai.date

import com.flux.core.ai.text.SpanishNumbers
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class OffsetMatch(val duration: Duration, val text: String)

data class DayMatch(val date: LocalDate, val impliedTime: LocalTime?, val text: String)

data class TimeMatch(val time: LocalTime, val text: String)

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

/** "a las 8", "a las 8:30 pm", "a las ocho y media de la noche", "al mediodía", "a medianoche". */
internal object TimeParser {
    private const val PERIOD = "(a\\.?\\s?m\\.?|p\\.?\\s?m\\.?|de la manana|de la tarde|de la noche|de la madrugada)"
    private val clock = Regex(
        "\\ba\\s+las?\\s+${SpanishNumbers.TOKEN}(?::(\\d{2}))?(\\s+y media|\\s+y cuarto|\\s+menos cuarto)?(?:\\s*$PERIOD)?",
    )
    private val noon = Regex("\\bal? mediodia\\b")
    private val midnight = Regex("\\ba (?:la )?medianoche\\b")
    private val afternoon = Regex("p\\.?\\s?m|tarde|noche")
    private val morning = Regex("a\\.?\\s?m|manana|madrugada")

    fun parse(text: String): TimeMatch? {
        noon.find(text)?.let { return TimeMatch(LocalTime.NOON, it.value) }
        midnight.find(text)?.let { return TimeMatch(LocalTime.MIDNIGHT, it.value) }
        val match = clock.find(text) ?: return null
        val rawHour = SpanishNumbers.parse(match.groupValues[GROUP_HOUR])?.takeIf { it <= MAX_HOUR } ?: return null
        val period = match.groupValues[GROUP_PERIOD]
        var hour = when {
            afternoon.containsMatchIn(period) && rawHour < NOON_HOUR -> rawHour + NOON_HOUR
            morning.containsMatchIn(period) && rawHour == NOON_HOUR -> 0
            else -> rawHour
        }
        var minutes = (match.groupValues[GROUP_MINUTES].toIntOrNull() ?: 0) + when (match.groupValues[GROUP_FRACTION].trim()) {
            "y media" -> HALF
            "y cuarto" -> QUARTER
            "menos cuarto" -> -QUARTER
            else -> 0
        }
        if (minutes < 0) {
            minutes += MINUTES_PER_HOUR
            hour = (hour + HOURS_PER_DAY - 1) % HOURS_PER_DAY
        }
        if (minutes >= MINUTES_PER_HOUR) return null
        return TimeMatch(LocalTime.of(hour, minutes), match.value.trim())
    }

    private const val GROUP_HOUR = 1
    private const val GROUP_MINUTES = 2
    private const val GROUP_FRACTION = 3
    private const val GROUP_PERIOD = 4
    private const val MAX_HOUR = 23
    private const val NOON_HOUR = 12
    private const val HALF = 30
    private const val QUARTER = 15
    private const val MINUTES_PER_HOUR = 60
    private const val HOURS_PER_DAY = 24
}
