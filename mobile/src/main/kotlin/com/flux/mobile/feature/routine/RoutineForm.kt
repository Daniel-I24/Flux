package com.flux.mobile.feature.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.ui.common.TimeField
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

private val MEALS_AND_SLEEP = listOf(
    RoutineTime.WAKE_UP to R.string.routine_wake_up,
    RoutineTime.BREAKFAST to R.string.routine_breakfast,
    RoutineTime.LUNCH to R.string.routine_lunch,
    RoutineTime.DINNER to R.string.routine_dinner,
    RoutineTime.SLEEP to R.string.routine_sleep,
)

/** Reused by onboarding and by Ajustes > Horarios. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineForm(state: RoutineUiState, viewModel: RoutineViewModel, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FluxSpacing.xs)) {
        MEALS_AND_SLEEP.forEach { (field, label) ->
            TimeField(stringResource(label), state.times.getValue(field), { viewModel.setTime(field, it) })
        }
        HorizontalDivider(Modifier.padding(vertical = FluxSpacing.sm))
        Text(stringResource(R.string.routine_activity), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.routine_activity_days), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.xs)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day in state.activityDays,
                    onClick = { viewModel.toggleDay(day) },
                    label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) },
                )
            }
        }
        TimeField(stringResource(R.string.routine_activity_start), state.times.getValue(RoutineTime.ACTIVITY_START), {
            viewModel.setTime(RoutineTime.ACTIVITY_START, it)
        })
        TimeField(stringResource(R.string.routine_activity_end), state.times.getValue(RoutineTime.ACTIVITY_END), {
            viewModel.setTime(RoutineTime.ACTIVITY_END, it)
        })
        if (state.invalid) Text(stringResource(R.string.routine_invalid), color = MaterialTheme.colorScheme.error)
    }
}
