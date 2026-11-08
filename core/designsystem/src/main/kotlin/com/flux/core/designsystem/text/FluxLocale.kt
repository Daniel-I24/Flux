package com.flux.core.designsystem.text

import java.time.format.DateTimeFormatter
import java.util.Locale

/** FLUX is Spanish-only: dates and weekdays use Spanish even on a device set to another language. */
object FluxLocale {
    val SPANISH: Locale = Locale.forLanguageTag("es")

    /** Times are always 24 h with two digits ("08:00", "18:30"), on the phone and on the watch. */
    val TIME_24H: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", SPANISH)
}
