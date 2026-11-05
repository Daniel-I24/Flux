package com.flux.wear.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.flux.core.designsystem.component.LavaColors
import com.flux.core.designsystem.component.VoiceLava
import com.flux.core.designsystem.theme.FluxColorTokens
import com.flux.core.model.Note
import com.flux.core.model.PhoneConnection
import com.flux.wear.R
import com.flux.wear.ui.rememberDueDateLabel

/** Discreet status: a colored dot and two words (green ready, amber checking, red away). */
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
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** The protagonist: a large round microphone over a still lava of the theme colors. */
@Composable
fun VoiceHeroButton(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(R.string.home_dictate)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(HERO_WIDTH)
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .semantics {
                    role = Role.Button
                    contentDescription = label
                },
            contentAlignment = Alignment.Center,
        ) {
            VoiceLava(
                active = false,
                level = { 0f },
                colors = LavaColors(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer, scheme.primaryContainer),
                modifier = Modifier.fillMaxSize(),
            )
            Icon(Icons.Rounded.Mic, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
        }
        Text(label, style = MaterialTheme.typography.titleSmall)
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
            Text(stringResource(R.string.home_no_reminders))
        } else {
            Text(note.title, maxLines = 2)
            note.dueAt?.let { Text(rememberDueDateLabel(it), style = MaterialTheme.typography.labelSmall) }
        }
    }
}

private const val HERO_WIDTH = 0.55f
