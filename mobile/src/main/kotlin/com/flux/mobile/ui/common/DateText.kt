package com.flux.mobile.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.text.FluxLocale
import com.flux.mobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** "Hoy · 8:00", "Mañana · 8:00" or "vie 23 oct · 9:00", always in Spanish, in the device's time zone. */
@Composable
@ReadOnlyComposable
fun dueDateLabel(dueAt: Long): String {
    val locale = FluxLocale.SPANISH
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
