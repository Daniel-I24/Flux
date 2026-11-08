package com.flux.mobile.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.text.FluxLocale
import com.flux.mobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** "Hoy · 08:00", "Mañana · 08:00" or "vie 23 oct · 09:00", always in Spanish, in the device's time zone. */
@Composable
@ReadOnlyComposable
fun dueDateLabel(dueAt: Long): String {
    val locale = FluxLocale.SPANISH
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(dueAt).atZone(zone)
    val time = dateTime.format(FluxLocale.TIME_24H)
    val today = LocalDate.now(zone)
    return when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.date_today_at, time)
        today.plusDays(1) -> stringResource(R.string.date_tomorrow_at, time)
        else -> dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)) + " · " + time
    }
}

/** "Sonará hoy a las 18:00", "Sonará mañana a las 08:00" or "Sonará el vie 23 oct a las 09:00". */
fun Context.alarmConfirmation(dueAt: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val dateTime = Instant.ofEpochMilli(dueAt).atZone(zone)
    val time = dateTime.format(FluxLocale.TIME_24H)
    val today = LocalDate.now(zone)
    return when (dateTime.toLocalDate()) {
        today -> getString(R.string.alarm_rings_today, time)
        today.plusDays(1) -> getString(R.string.alarm_rings_tomorrow, time)
        else -> getString(R.string.alarm_rings_on, dateTime.format(DateTimeFormatter.ofPattern("EEE d MMM", FluxLocale.SPANISH)), time)
    }
}

@Composable
fun alarmConfirmation(dueAt: Long): String = LocalContext.current.alarmConfirmation(dueAt)
