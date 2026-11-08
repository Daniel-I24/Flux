package com.flux.wear.feature.capture

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.DatePicker
import androidx.wear.compose.material3.TimePicker
import androidx.wear.compose.material3.TimePickerType
import com.flux.core.designsystem.text.FluxLocale
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * "Elegir fecha y hora" on the watch: the day (from today on), then the time in 24 h, in Spanish.
 * Back returns to the question without changing anything.
 */
// FLUX ships APKs with every language, so no Play language split is ever missing. Only the pickers' locale changes.
@SuppressLint("AppBundleLocaleChanges")
@Composable
fun ReminderDateTimePicker(initial: Long?, onPicked: (Long) -> Unit, onCancel: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val start = remember(initial) { initial?.let { Instant.ofEpochMilli(it).atZone(zone) } }
    var pickedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    BackHandler {
        if (pickedDay != null) pickedDay = null else onCancel()
    }
    val base = LocalConfiguration.current
    val context = LocalContext.current
    val spanish = remember(base) { Configuration(base).apply { setLocale(FluxLocale.SPANISH) } }
    val spanishContext = remember(context, spanish) { context.createConfigurationContext(spanish) }
    CompositionLocalProvider(LocalConfiguration provides spanish, LocalContext provides spanishContext) {
        val day = pickedDay
        if (day == null) {
            val today = LocalDate.now(zone)
            DatePicker(
                initialDate = start?.toLocalDate()?.takeIf { !it.isBefore(today) } ?: today,
                onDatePicked = { pickedDay = it.toEpochDay() },
                minValidDate = today,
            )
        } else {
            TimePicker(
                initialTime = start?.toLocalTime() ?: DEFAULT_TIME,
                onTimePicked = { onPicked(LocalDate.ofEpochDay(day).atTime(it).atZone(zone).toInstant().toEpochMilli()) },
                timePickerType = TimePickerType.HoursMinutes24H,
            )
        }
    }
}

private val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)
