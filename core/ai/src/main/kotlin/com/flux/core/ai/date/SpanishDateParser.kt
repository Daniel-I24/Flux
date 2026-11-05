package com.flux.core.ai.date

import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.model.DailyRoutine
import java.time.LocalTime
import java.time.ZonedDateTime

data class ParsedDate(
    val dateTime: ZonedDateTime,
    /** Normalized fragments that produced the date (removed from the title). */
    val fragments: List<String>,
    /** True when the user said a concrete time or offset (worth an exact alarm). */
    val hasExplicitTime: Boolean,
    /** Equally valid readings when the text is truly ambiguous; [dateTime] is the first one. */
    val alternatives: List<ZonedDateTime> = emptyList(),
)

/**
 * Deterministic Spanish date extraction. Relative offsets win; otherwise the day and the
 * time (clock first, then routine phrases, never the other way round) are combined.
 * A time without a day that already passed today moves to tomorrow; a weekday moves a week.
 */
class SpanishDateParser(routine: DailyRoutine? = null) {
    private val dayParser = DayParser(
        mapOf(
            "manana" to LocalTime.of(MORNING_HOUR, 0),
            "tarde" to LocalTime.of(AFTERNOON_HOUR, 0),
            "noche" to LocalTime.of(NIGHT_HOUR, 0),
        ),
    )
    private val routineParser = RoutinePhraseParser(routine ?: DailyRoutine())

    fun parse(input: String, now: ZonedDateTime): ParsedDate? {
        val text = input.normalizeSpanish()
        OffsetParser.parse(text)?.let { return ParsedDate(now.plus(it.duration).withNano(0), listOf(it.text), hasExplicitTime = true) }

        val day = dayParser.parse(text, now.toLocalDate())
        val targetDay = day?.date ?: now.toLocalDate()
        val time = TimeParser.parse(text) ?: routineParser.parse(text, targetDay.dayOfWeek)
        if (day == null && time == null) return null

        val localTime = time?.time ?: day?.impliedTime ?: LocalTime.of(DEFAULT_HOUR, 0)
        val first = now.with(targetDay).with(localTime).withSecond(0).withNano(0)
        val readings = if (time?.halfDayOpen == true) halfDayReadings(first, day, now) else listOf(rolled(first, day, now))
        return ParsedDate(
            dateTime = readings.first(),
            fragments = listOfNotNull(day?.text, time?.text),
            hasExplicitTime = time != null || day?.impliedTime != null,
            alternatives = readings.takeIf { it.size > 1 }.orEmpty(),
        )
    }

    private fun rolled(candidate: ZonedDateTime, day: DayMatch?, now: ZonedDateTime): ZonedDateTime = when {
        candidate.isAfter(now) -> candidate
        day == null -> candidate.plusDays(1)
        day.weekly -> candidate.plusWeeks(1)
        else -> candidate
    }

    /**
     * "a las 8" (6–11 without a. m./p. m.). With "esta tarde/noche" it is the evening; with
     * "hoy" and the morning already gone it is the evening; with no day and the morning gone
     * but the evening still ahead it is really ambiguous: tonight first, tomorrow morning second.
     */
    private fun halfDayReadings(morning: ZonedDateTime, day: DayMatch?, now: ZonedDateTime): List<ZonedDateTime> {
        val evening = morning.plusHours(HALF_DAY_HOURS)
        val impliedEvening = day?.impliedTime?.let { it.hour >= NOON } == true
        return when {
            impliedEvening -> listOf(rolled(evening, day, now))
            morning.isAfter(now) -> listOf(morning)
            !evening.isAfter(now) -> listOf(rolled(morning, day, now))
            day == null -> listOf(evening, morning.plusDays(1))
            day.date == now.toLocalDate() -> listOf(evening)
            else -> listOf(rolled(morning, day, now))
        }
    }

    private companion object {
        const val DEFAULT_HOUR = 9
        const val MORNING_HOUR = 9
        const val AFTERNOON_HOUR = 16
        const val NIGHT_HOUR = 20
        const val NOON = 12
        const val HALF_DAY_HOURS = 12L
    }
}
