package com.flux.mobile.feature.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.component.CategoryBadge
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.mobile.R
import com.flux.mobile.ui.common.dueDateLabel

@Composable
fun NoteMeta(note: Note, category: Category?) {
    Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        category?.let { CategoryBadge(it) }
        note.dueAt?.let { dueAt ->
            if (note.requiresAlarm) {
                Icon(
                    Icons.Rounded.Alarm,
                    stringResource(R.string.notes_has_alarm),
                    Modifier.size(FluxSizes.iconSm)
                )
            }
            Text(
                dueDateLabel(dueAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** "En 1 hora" and "Reprogramar", shown only for overdue notes. */
@Composable
fun OverdueChips(actions: NoteRowActions) {
    if (actions.onInOneHour == null && actions.onReschedule == null) return
    Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        actions.onInOneHour?.let { AssistChip(onClick = it, label = { Text(stringResource(R.string.overdue_in_one_hour)) }) }
        actions.onReschedule?.let { AssistChip(onClick = it, label = { Text(stringResource(R.string.overdue_reschedule)) }) }
    }
}
