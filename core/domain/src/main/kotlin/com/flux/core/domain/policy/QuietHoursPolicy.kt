package com.flux.core.domain.policy

import com.flux.core.model.DailyRoutine
import com.flux.core.model.TimeOfDay

/** FLUX "Do not disturb": silent between the user's sleep and wake-up times (wraps midnight). */
object QuietHoursPolicy {
    fun isQuiet(time: TimeOfDay, routine: DailyRoutine): Boolean = isQuiet(time, start = routine.sleep, end = routine.wakeUp)

    fun isQuiet(time: TimeOfDay, start: TimeOfDay, end: TimeOfDay): Boolean =
        if (start <= end) time >= start && time < end else time >= start || time < end
}
