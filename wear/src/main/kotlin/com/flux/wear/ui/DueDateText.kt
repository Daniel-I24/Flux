package com.flux.wear.ui

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Short, glanceable due date: "Hoy 08:00", "Mañana 08:00" or "vie 23 oct 09:00". */
object DueDateText {
    fun format(
        dueAt: Long,
        today: LocalDate,
        zone: ZoneId,
        todayLabel: String,
        tomorrowLabel: String,
        locale: Locale = Locale.getDefault()
    ): String {
        val dateTime = Instant.ofEpochMilli(dueAt).atZone(zone)
        val time = dateTime.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        return when (dateTime.toLocalDate()) {
            today -> "$todayLabel $time"
            today.plusDays(1) -> "$tomorrowLabel $time"
            else -> dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)) + " $time"
        }
    }
}
