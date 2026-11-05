package com.flux.core.ai.date

import com.flux.core.ai.text.SpanishNumbers
import java.time.LocalTime

/**
 * Clock times on normalized Spanish text: "a las 8", "a las 4:20 p. m.", "4:20pm", "hoy 16:20",
 * "a las 4 y 20", "a las 4 y media", "a las 4 y cuarto", "a las 5 menos cuarto",
 * "cuarto para las 5", "diez para las 5", "al mediodía", "a medianoche".
 */
internal object TimeParser {
    private val num = "(?:${SpanishNumbers.ALTERNATIVES})"
    private val period = "(?:\\s*(${TimePeriods.ANY}))?"

    /** "cuarto para las 5", "diez para las 5 de la tarde". */
    private val before = Regex("\\b(cuarto|$num)\\s+para\\s+(?:las?\\s+)?($num)\\b$period")

    /** "a las 4", "a las 4:20", "a las 4 y 20", "a las 4 y media", "a las 5 menos cuarto", "a eso de las 3". */
    private val clock = Regex(
        "\\b(?:a|para|a eso de)\\s+las?\\s+($num)\\b(?::(\\d{2})|\\s+y\\s+(media|cuarto|$num)\\b|\\s+menos\\s+(cuarto|$num)\\b)?$period",
    )

    /** "4pm", "4:20 p. m." without "a las". */
    private val bareWithPeriod = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(${TimePeriods.AM_PM})")

    /** "16:20", "4:20 de la tarde". */
    private val bareClock = Regex("\\b(\\d{1,2}):(\\d{2})\\b$period")
    private val noon = Regex("\\b(?:al|a|el|para el)\\s+mediodia\\b")
    private val midnight = Regex("\\b(?:a|para)\\s+(?:la\\s+)?medianoche\\b")

    fun parse(text: String): TimeMatch? = parseBefore(text)
        ?: parseClock(text)
        ?: parseBare(bareWithPeriod, text)
        ?: parseBare(bareClock, text)
        ?: noon.find(text)?.let { TimeMatch(LocalTime.NOON, it.value) }
        ?: midnight.find(text)?.let { TimeMatch(LocalTime.MIDNIGHT, it.value) }

    /** Groups: hour, minutes (optional), period. */
    private fun parseBare(pattern: Regex, text: String): TimeMatch? {
        val match = pattern.find(text) ?: return null
        val (hour, minutes, rawPeriod) = match.destructured
        return build(hour, minutes.toIntOrNull() ?: 0, rawPeriod, match.value)
    }

    private fun parseBefore(text: String): TimeMatch? {
        val match = before.find(text) ?: return null
        val minutesBefore = minutesOf(match.groupValues[1]) ?: return null
        val (_, hour, rawPeriod) = match.destructured
        return build(hour, -minutesBefore, rawPeriod, match.value)
    }

    private fun parseClock(text: String): TimeMatch? {
        val match = clock.find(text) ?: return null
        val groups = match.groupValues
        val (digits, plus, minus) = Triple(groups[GROUP_DIGITS], groups[GROUP_PLUS], groups[GROUP_MINUS])
        val minutes = when {
            digits.isNotEmpty() -> digits.toInt()
            plus.isNotEmpty() -> minutesOf(plus) ?: return null
            minus.isNotEmpty() -> -(minutesOf(minus) ?: return null)
            else -> 0
        }
        return build(groups[GROUP_HOUR], minutes, groups[GROUP_PERIOD], match.value)
    }

    private fun minutesOf(token: String): Int? = when (token) {
        "media" -> HALF
        "cuarto" -> QUARTER
        else -> SpanishNumbers.parse(token)
    }?.takeIf { it in 0 until MINUTES_PER_HOUR }

    /** [minutes] may be negative ("menos cuarto", "cuarto para"): the hour moves back. */
    private fun build(hourToken: String, minutes: Int, rawPeriod: String, text: String): TimeMatch? {
        if (minutes >= MINUTES_PER_HOUR) return null
        val rawHour = SpanishNumbers.parse(hourToken)?.takeIf { it <= LAST_HOUR } ?: return null
        val period = TimePeriods.of(rawPeriod)
        val hour = TimePeriods.hour24(rawHour, period) ?: return null
        val total = Math.floorMod(hour * MINUTES_PER_HOUR + minutes, MINUTES_PER_DAY)
        val time = LocalTime.of(total / MINUTES_PER_HOUR, total % MINUTES_PER_HOUR)
        return TimeMatch(time, text.trim(), halfDayOpen = TimePeriods.isHalfDayOpen(rawHour, period))
    }

    private const val GROUP_HOUR = 1
    private const val GROUP_DIGITS = 2
    private const val GROUP_PLUS = 3
    private const val GROUP_MINUS = 4
    private const val GROUP_PERIOD = 5
    private const val HALF = 30
    private const val QUARTER = 15
    private const val LAST_HOUR = 23
    private const val MINUTES_PER_HOUR = 60
    private const val MINUTES_PER_DAY = 24 * 60
}
