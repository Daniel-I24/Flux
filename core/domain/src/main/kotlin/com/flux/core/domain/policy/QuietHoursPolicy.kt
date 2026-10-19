package com.flux.core.domain.policy

import com.flux.core.model.DailyRoutine
import com.flux.core.model.TimeOfDay

/** FLUX "Do not disturb": silent between the user's sleep and wake-up times (wraps midnight). */
object QuietHoursPolicy {
    fun isQuiet(time: TimeOfDay, routine: DailyRoutine): Boolean {
        val start = routine.sleep
        val end = routine.wakeUp
        return if (start <= end) time >= start && time < end else time >= start || time < end
    }
}
