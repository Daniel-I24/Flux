package com.flux.mobile.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.flux.core.designsystem.component.CategoryBadge
import com.flux.core.designsystem.component.LavaColors
import com.flux.core.designsystem.component.VoiceLava
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.ui.common.dueDateLabel

/** Big, glanceable alarm: the lava pulses while it rings; two large actions. */
@Composable
fun AlarmScreen(state: AlarmUiState, onComplete: () -> Unit, onSnooze: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        VoiceLava(
            active = state.ringing,
            level = { PULSE_LEVEL },
            colors = LavaColors(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer, scheme.background),
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(FluxSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(FluxSpacing.lg, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(FluxSizes.iconXl)
            )
            Text(
                text = state.note?.title ?: stringResource(R.string.alarm_title_fallback),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            state.note?.dueAt?.let { Text(dueDateLabel(it), style = MaterialTheme.typography.titleMedium, color = scheme.onSurfaceVariant) }
            state.category?.let { CategoryBadge(it) }
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.fab),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Text(stringResource(R.string.reminder_complete), modifier = Modifier.padding(start = FluxSpacing.sm))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onSnooze()
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.fab),
            ) {
                Icon(Icons.Rounded.Snooze, contentDescription = null)
                Text(stringResource(R.string.reminder_snooze, state.snoozeMinutes), modifier = Modifier.padding(start = FluxSpacing.sm))
            }
        }
    }
}

/** The alarm screen has no microphone: the lava "breathes" at a fixed medium level. */
private const val PULSE_LEVEL = 0.4f
