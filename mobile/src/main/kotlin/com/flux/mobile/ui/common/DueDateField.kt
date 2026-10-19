package com.flux.mobile.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.model.TimeOfDay
import com.flux.mobile.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Optional date + time. "Sin fecha" clears it; picking a date keeps the previous time (09:00 by default). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDateField(dueAt: Long?, onChange: (Long?) -> Unit, modifier: Modifier = Modifier) {
    val zone = ZoneId.systemDefault()
    val current = dueAt?.let { Instant.ofEpochMilli(it).atZone(zone) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Column(modifier) {
        Row(Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.editor_date), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { pickingDate = true }) {
                Text(dueAt?.let { dueDateLabel(it) } ?: stringResource(R.string.editor_no_date))
            }
            if (dueAt != null) TextButton(onClick = { onChange(null) }) { Text(stringResource(R.string.editor_clear_date)) }
        }
        if (current != null) {
            TimeField(
                label = stringResource(R.string.editor_time),
                value = TimeOfDay.of(current.hour, current.minute),
                onChange = { time -> onChange(current.with(LocalTime.of(time.hour, time.minute)).toInstant().toEpochMilli()) },
            )
        }
    }

    if (pickingDate) {
        val initialDate = current?.toLocalDate() ?: LocalDate.now(zone)
        // The Material date picker works in UTC midnight millis.
        val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utc ->
                        val date = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        val time = current?.toLocalTime() ?: DEFAULT_TIME
                        onChange(date.atTime(time).atZone(zone).toInstant().toEpochMilli())
                    }
                    pickingDate = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text(stringResource(R.string.action_cancel)) } },
        ) { DatePicker(state) }
    }
}

private val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)
