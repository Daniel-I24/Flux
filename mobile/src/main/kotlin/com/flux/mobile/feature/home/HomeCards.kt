package com.flux.mobile.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.rounded.WatchOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.category.color
import com.flux.core.designsystem.component.CategoryBadge
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.WatchLink
import com.flux.mobile.R
import com.flux.mobile.ui.common.dueDateLabel

/** Linked watch and whether it is in range; any other state invites the user to Ajustes. */
@Composable
fun WatchStatusCard(watch: WatchLink, onOpenSettings: () -> Unit) {
    val linked = watch as? WatchLink.Linked
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(FluxSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md),
        ) {
            Icon(if (linked != null) Icons.Rounded.Watch else Icons.Rounded.WatchOff, contentDescription = null)
            Column(Modifier.weight(1f)) {
                Text(
                    text =
                    linked?.let { stringResource(R.string.link_linked, it.displayName) }
                        ?: stringResource(R.string.home_watch_not_linked),
                    style = MaterialTheme.typography.titleSmall,
                )
                val detail = when {
                    linked == null -> stringResource(R.string.home_watch_not_linked_hint)
                    linked.connected -> stringResource(R.string.link_status_connected)
                    else -> stringResource(R.string.home_watch_offline_hint)
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (linked == null) TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.link_start)) }
        }
    }
}

@Composable
fun UpcomingReminderRow(note: Note, category: Category?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md),
    ) {
        Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
            Text(note.title, style = MaterialTheme.typography.bodyLarge)
            note.dueAt?.let {
                Text(dueDateLabel(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        category?.let { CategoryBadge(it) }
    }
}

@Composable
fun CategoryDot(category: Category) {
    Box(Modifier.size(FluxSizes.categoryDot).background(category.color, CircleShape))
}
