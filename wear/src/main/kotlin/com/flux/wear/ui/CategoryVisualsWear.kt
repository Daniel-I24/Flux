package com.flux.wear.ui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.category.color
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.category.toImageVector
import com.flux.core.model.Category
import com.flux.wear.R
import java.time.LocalDate
import java.time.ZoneId

/** Category pill in its own color with icon and name (tap to change it). */
@Composable
fun CategoryChip(category: Category?, onClick: () -> Unit) {
    category ?: return
    Button(
        onClick = onClick,
        icon = { Icon(category.icon.toImageVector(), contentDescription = null, tint = category.color) },
        label = { Text(category.displayName(), maxLines = 1) },
        secondaryLabel = { Text(stringResource(R.string.capture_change_category), maxLines = 1) },
        colors = ButtonDefaults.filledTonalButtonColors(),
    )
}

@Composable
fun CategoryIconDot(category: Category) {
    Icon(category.icon.toImageVector(), contentDescription = null, tint = category.color, modifier = Modifier.size(20.dp))
}

@Composable
fun rememberDueDateLabel(dueAt: Long): String {
    val zone = ZoneId.systemDefault()
    return DueDateText.format(
        dueAt = dueAt,
        today = LocalDate.now(zone),
        zone = zone,
        todayLabel = stringResource(R.string.agenda_today),
        tomorrowLabel = stringResource(R.string.date_tomorrow),
    )
}
