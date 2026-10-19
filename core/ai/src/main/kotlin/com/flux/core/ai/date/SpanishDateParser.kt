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
)

/**
 * Deterministic Spanish date extraction. Relative offsets win; otherwise the day
 * and the time (clock or routine phrase) are combined. A time without a day that
 * already passed today moves to tomorrow.
 */
class SpanishDateParser(routine: DailyRoutine? = null) {
    private val effectiveRoutine = routine ?: DailyRoutine()
    private val dayParser = DayParser(
        mapOf(
            "manana" to LocalTime.of(MORNING_HOUR, 0),
            "tarde" to LocalTime.of(AFTERNOON_HOUR, 0),
            "noche" to LocalTime.of(NIGHT_HOUR, 0),
        ),
    )
    private val routineParser = RoutinePhraseParser(effectiveRoutine)

    fun parse(input: String, now: ZonedDateTime): ParsedDate? {
        val text = input.normalizeSpanish()
        OffsetParser.parse(text)?.let { return ParsedDate(now.plus(it.duration).withNano(0), listOf(it.text), hasExplicitTime = true) }

        val day = dayParser.parse(text, now.toLocalDate())
        val targetDay = day?.date ?: now.toLocalDate()
        val time = TimeParser.parse(text) ?: routineParser.parse(text, targetDay.dayOfWeek)
        if (day == null && time == null) return null

        val localTime = time?.time ?: day?.impliedTime ?: LocalTime.of(DEFAULT_HOUR, 0)
        var result = now.with(targetDay).with(localTime).withSecond(0).withNano(0)
        if (day == null && !result.isAfter(now)) result = result.plusDays(1)
        return ParsedDate(
            dateTime = result,
            fragments = listOfNotNull(day?.text, time?.text),
            hasExplicitTime = time != null || day?.impliedTime != null,
        )
    }

    private companion object {
        const val DEFAULT_HOUR = 9
        const val MORNING_HOUR = 9
        const val AFTERNOON_HOUR = 16
        const val NIGHT_HOUR = 20
    }
}
