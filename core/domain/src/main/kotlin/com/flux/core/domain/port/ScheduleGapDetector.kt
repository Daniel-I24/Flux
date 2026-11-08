package com.flux.core.domain.port

import java.time.LocalDate
import java.time.ZonedDateTime

/** A reminder in the text whose time (and maybe its day) is missing; [day] is null when no day was said either. */
data class ScheduleGap(val day: LocalDate?)

/**
 * Finds reminders left incomplete: "salir a las", "mañana a las", "el viernes", "recuérdame llamar".
 * Returns null when the user said an explicit time, because that time always wins.
 */
fun interface ScheduleGapDetector {
    fun detect(text: String, now: ZonedDateTime): ScheduleGap?
}
