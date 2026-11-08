package com.flux.mobile.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Ambiguity
import com.flux.mobile.R
import com.flux.mobile.ui.common.DateTimePickerDialogs
import com.flux.mobile.ui.common.dueDateLabel

/** "¿Cuándo te aviso?": the suggestions, "Elegir fecha y hora" and "Guardar sin recordatorio" as chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderQuestion(question: Ambiguity.ReminderTime, onAnswer: (Long?) -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    Text(stringResource(R.string.capture_question_reminder), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        question.options.forEach { option ->
            AssistChip(
                onClick = { onAnswer(option) },
                label = { Text(dueDateLabel(option)) },
                leadingIcon = { Icon(Icons.Rounded.Alarm, contentDescription = null) },
            )
        }
        AssistChip(
            onClick = { picking = true },
            label = { Text(stringResource(R.string.capture_pick_date_time)) },
            leadingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
        )
        AssistChip(
            onClick = { onAnswer(null) },
            label = { Text(stringResource(R.string.capture_save_without_reminder)) },
            leadingIcon = { Icon(Icons.Rounded.NotificationsOff, contentDescription = null) },
        )
    }
    if (picking) {
        DateTimePickerDialogs(
            initial = question.options.firstOrNull(),
            onPicked = {
                picking = false
                onAnswer(it)
            },
            onCancel = { picking = false },
        )
    }
}
