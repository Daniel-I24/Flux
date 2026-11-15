package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.flux.core.model.AppSettings
import com.flux.wear.R

/** Every choice in order: the phone's setting (null), then each lead. One tap moves to the next. */
private val LEAD_CHOICES: List<Int?> = listOf<Int?>(null) + AppSettings.ALARM_LEAD_OPTIONS

fun nextLead(current: Int?): Int? = LEAD_CHOICES[(LEAD_CHOICES.indexOf(current) + 1) % LEAD_CHOICES.size]

@Composable
private fun leadLabel(minutes: Int?): String = when (minutes) {
    null -> stringResource(R.string.lead_phone)
    0 -> stringResource(R.string.lead_exact)
    MINUTES_PER_HOUR -> stringResource(R.string.lead_hour)
    else -> stringResource(R.string.lead_minutes, minutes)
}

/** "Avisar · a la hora", "Avisar · 10 min antes"…: a small screen has room for one button, not a list. */
@Composable
fun LeadChip(minutes: Int?, onChange: (Int?) -> Unit) {
    FilledTonalButton(
        onClick = { onChange(nextLead(minutes)) },
        icon = { Icon(Icons.Rounded.NotificationsActive, contentDescription = null) },
        label = { Text(stringResource(R.string.lead_title)) },
        secondaryLabel = { Text(leadLabel(minutes)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private const val MINUTES_PER_HOUR = 60
