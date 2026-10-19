package com.flux.mobile.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.flux.mobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** "Hoy 08:00", "Mañana 08:00" or "vie, 23 oct 09:00" in the device's locale and zone. */
@Composable
@ReadOnlyComposable
fun dueDateLabel(dueAt: Long): String {
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(dueAt).atZone(zone)
    val time = dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    val today = LocalDate.now(zone)
    return when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.date_today_at, time)
        today.plusDays(1) -> stringResource(R.string.date_tomorrow_at, time)
        else -> dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)) + " · " + time
    }
}
