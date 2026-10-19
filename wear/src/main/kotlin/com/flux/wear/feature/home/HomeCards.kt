package com.flux.wear.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.flux.core.designsystem.theme.FluxColorTokens
import com.flux.core.model.Note
import com.flux.core.model.PhoneConnection
import com.flux.wear.R
import com.flux.wear.ui.rememberDueDateLabel

/** Small status line under the time: green when ready, amber while checking, red when away. */
@Composable
fun ConnectionLabel(connection: PhoneConnection) {
    val (color, text) = when (connection) {
        PhoneConnection.Checking -> FluxColorTokens.Amber400 to stringResource(R.string.connection_checking)
        is PhoneConnection.Ready -> FluxColorTokens.Green400 to stringResource(
            if (connection.serverReachable) R.string.connection_ready else R.string.connection_ready_local_ai,
        )
        else -> FluxColorTokens.Red400 to stringResource(R.string.connection_offline)
    }
    Row(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun NextReminderCard(note: Note?, pendingCount: Int, onClick: () -> Unit) {
    TitleCard(
        onClick = onClick,
        title = { Text(stringResource(R.string.home_next_reminder)) },
        subtitle = if (pendingCount > 0) {
            { Text(stringResource(R.string.home_pending_queue, pendingCount), color = MaterialTheme.colorScheme.secondary) }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (note == null) {
            Text(stringResource(R.string.home_no_reminders), color = Color.Unspecified)
        } else {
            Text(note.title, maxLines = 2)
            note.dueAt?.let { Text(rememberDueDateLabel(it), style = MaterialTheme.typography.labelSmall) }
        }
    }
}
