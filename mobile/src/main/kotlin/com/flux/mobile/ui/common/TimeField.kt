package com.flux.mobile.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun TimeOfDay.formatted(): String = LocalTime.of(hour, minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

/** Label + current time; tapping opens the Material 3 time picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeField(label: String, value: TimeOfDay, onChange: (TimeOfDay) -> Unit, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = { open = true }) { Text(value.formatted(), style = MaterialTheme.typography.titleMedium) }
    }
    if (open) {
        val pickerState = rememberTimePickerState(initialHour = value.hour, initialMinute = value.minute)
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text(stringResource(R.string.routine_pick_time)) },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onChange(TimeOfDay.of(pickerState.hour, pickerState.minute))
                    open = false
                }) { Text(stringResource(R.string.action_accept)) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}
