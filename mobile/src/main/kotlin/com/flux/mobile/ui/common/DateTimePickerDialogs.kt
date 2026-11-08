package com.flux.mobile.ui.common

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.text.FluxLocale
import com.flux.mobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * The date first, then the time (24 h), both in Spanish. Cancelling either step calls [onCancel]
 * and nothing changes. Past days cannot be picked: a reminder there would never ring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialogs(initial: Long?, onPicked: (Long) -> Unit, onCancel: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val start = remember(initial) { initial?.let { Instant.ofEpochMilli(it).atZone(zone) } }
    var pickedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val cancel = @Composable { TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) } }
    InSpanish {
        val day = pickedDay
        if (day == null) {
            val today = LocalDate.now(zone)
            // The Material date picker works in UTC midnight millis.
            val state = rememberDatePickerState(
                initialSelectedDateMillis = (start?.toLocalDate()?.takeIf { !it.isBefore(today) } ?: today).utcMillis(),
                selectableDates = remember(today) { FromDay(today.utcMillis()) },
            )
            DatePickerDialog(
                onDismissRequest = onCancel,
                confirmButton = {
                    TextButton(
                        onClick = { state.selectedDateMillis?.let { pickedDay = it.utcDay() } },
                        enabled = state.selectedDateMillis != null,
                    ) { Text(stringResource(R.string.picker_next)) }
                },
                dismissButton = cancel,
            ) { DatePicker(state) }
        } else {
            val time = start?.toLocalTime() ?: DEFAULT_TIME
            val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
            AlertDialog(
                onDismissRequest = onCancel,
                title = { Text(stringResource(R.string.picker_time_title)) },
                text = { TimePicker(state = state) },
                confirmButton = {
                    TextButton(onClick = {
                        val at = LocalDate.ofEpochDay(day).atTime(state.hour, state.minute).atZone(zone)
                        onPicked(at.toInstant().toEpochMilli())
                    }) { Text(stringResource(R.string.action_accept)) }
                },
                dismissButton = cancel,
            )
        }
    }
}

/** Month and weekday names of the pickers follow the configuration's locale: always Spanish in FLUX. */
// FLUX ships APKs with every language, so no Play language split is ever missing. Only the pickers' locale changes.
@SuppressLint("AppBundleLocaleChanges")
@Composable
private fun InSpanish(content: @Composable () -> Unit) {
    val base = LocalConfiguration.current
    val spanish = remember(base) { Configuration(base).apply { setLocale(FluxLocale.SPANISH) } }
    CompositionLocalProvider(LocalConfiguration provides spanish, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
private class FromDay(private val firstUtcMillis: Long) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= firstUtcMillis
}

private fun LocalDate.utcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.utcDay(): Long = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

private val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)
