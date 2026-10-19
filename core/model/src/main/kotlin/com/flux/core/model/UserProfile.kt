package com.flux.core.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/** Minutes since midnight; avoids timezone surprises when stored or synced. */
@JvmInline
@Serializable
value class TimeOfDay(val minutes: Int) : Comparable<TimeOfDay> {
    init {
        require(minutes in 0 until MINUTES_PER_DAY) { "Invalid time of day: $minutes" }
    }

    val hour: Int get() = minutes / 60
    val minute: Int get() = minutes % 60

    override fun compareTo(other: TimeOfDay): Int = minutes.compareTo(other.minutes)

    companion object {
        const val MINUTES_PER_DAY = 24 * 60
        fun of(hour: Int, minute: Int = 0): TimeOfDay = TimeOfDay(hour * 60 + minute)
    }
}

@Serializable
data class TimeRange(val start: TimeOfDay, val end: TimeOfDay)

/** Habitual schedule used by the AI ("después de clase", "antes de dormir") and quiet hours. */
@Serializable
data class DailyRoutine(
    val wakeUp: TimeOfDay = TimeOfDay.of(6, 30),
    val sleep: TimeOfDay = TimeOfDay.of(22, 30),
    val breakfast: TimeOfDay = TimeOfDay.of(7),
    val lunch: TimeOfDay = TimeOfDay.of(12, 30),
    val dinner: TimeOfDay = TimeOfDay.of(19, 30),
    val activityBlocks: Map<DayOfWeek, TimeRange> = DayOfWeek.entries
        .filter { it != DayOfWeek.SATURDAY && it != DayOfWeek.SUNDAY }
        .associateWith { TimeRange(TimeOfDay.of(8), TimeOfDay.of(16)) },
)

@Serializable
enum class Occupation { STUDENT, WORKER, BOTH, OTHER }

@Serializable
data class UserProfile(
    val displayName: String,
    val age: Int?,
    val occupation: Occupation,
    val routine: DailyRoutine = DailyRoutine(),
) {
    companion object {
        const val MAX_NAME_LENGTH = 40
        val AGE_RANGE = 5..120
    }
}
