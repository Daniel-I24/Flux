package com.flux.core.domain.usecase

import com.flux.core.model.DailyRoutine
import com.flux.core.model.TimeOfDay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * When to ring a reminder the user left without a time: the morning, the end of the day's
 * activity and the evening, taken from the profile's routine (08:00, 16:00 and 18:00 by default).
 * Only future times are offered; without a day, today's are followed by tomorrow morning.
 */
object ReminderSuggestions {
    const val MAX_OPTIONS = 3

    fun of(day: LocalDate?, now: ZonedDateTime, routine: DailyRoutine): List<Long> {
        val today = now.toLocalDate()
        val days = if (day == null) listOf(today, today.plusDays(1)) else listOf(day)
        val earliest = now.plusMinutes(MIN_LEAD_MINUTES)
        return days.flatMap { date -> slots(date, routine).map { now.with(date).with(it).withSecond(0).withNano(0) } }
            .filter { it.isAfter(earliest) }
            .map { it.toInstant().toEpochMilli() }
            .distinct()
            .take(MAX_OPTIONS)
    }

    private fun slots(date: LocalDate, routine: DailyRoutine): List<LocalTime> {
        val morning = routine.breakfast.toLocalTime().plusMinutes(AFTER_BREAKFAST_MINUTES)
        val afternoon = routine.activityBlocks[date.dayOfWeek]?.end?.toLocalTime()
            ?: routine.lunch.toLocalTime().plusMinutes(AFTER_LUNCH_MINUTES)
        val evening = routine.dinner.toLocalTime().minusMinutes(BEFORE_DINNER_MINUTES)
        return listOf(morning, afternoon, evening).distinct().sorted()
    }

    private fun TimeOfDay.toLocalTime(): LocalTime = LocalTime.of(hour, minute)

    private const val MIN_LEAD_MINUTES = 15L
    private const val AFTER_BREAKFAST_MINUTES = 60L
    private const val AFTER_LUNCH_MINUTES = 120L
    private const val BEFORE_DINNER_MINUTES = 90L
}
