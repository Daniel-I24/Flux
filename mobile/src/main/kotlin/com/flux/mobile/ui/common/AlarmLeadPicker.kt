package com.flux.mobile.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.AppSettings
import com.flux.mobile.R

/** "A la hora", "5 min antes"… "1 h antes". */
@Composable
fun alarmLeadLabel(minutes: Int): String = when (minutes) {
    0 -> stringResource(R.string.alarm_lead_exact)
    MINUTES_PER_HOUR -> stringResource(R.string.alarm_lead_hour)
    else -> stringResource(R.string.alarm_lead_minutes, minutes)
}

/**
 * The reminder's "Avisarme" lead as chips (they wrap on narrow phones and with large text).
 * With [globalMinutes] a first chip keeps the note on the global setting (value null).
 */
@Composable
fun AlarmLeadPicker(selected: Int?, onSelect: (Int?) -> Unit, modifier: Modifier = Modifier, globalMinutes: Int? = null) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        globalMinutes?.let { global ->
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.alarm_lead_default, alarmLeadLabel(global))) },
            )
        }
        AppSettings.ALARM_LEAD_OPTIONS.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(alarmLeadLabel(option)) },
            )
        }
    }
}

private const val MINUTES_PER_HOUR = 60
