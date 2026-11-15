package com.flux.mobile.feature.notes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.flux.core.designsystem.category.color
import com.flux.core.designsystem.motion.pressScale
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.mobile.R

/** A note in the list. Swipe left to delete; TalkBack gets the same action as a custom action. */
@Composable
fun NoteRow(note: Note, category: Category?, actions: NoteRowActions, modifier: Modifier = Modifier) {
    val swipe = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) actions.onDelete()
            false
        },
    )
    val deleteLabel = stringResource(R.string.notes_delete)
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        // The margin is outside the swipe area, so the red "eliminar" background only shows under the card.
        modifier = modifier.padding(horizontal = FluxSpacing.md, vertical = FluxSpacing.xs).semantics {
            customActions = listOf(
                CustomAccessibilityAction(deleteLabel) {
                    actions.onDelete()
                    true
                },
            )
        },
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CardDefaults.elevatedShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = FluxSpacing.lg),
                contentAlignment = Alignment.CenterEnd,
            ) { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer) }
        },
    ) {
        NoteRowContent(note, category, actions)
    }
}

@Composable
private fun NoteRowContent(note: Note, category: Category?, actions: NoteRowActions) {
    val haptics = LocalHapticFeedback.current
    val reduced = rememberReducedMotion()
    // The check "pops" when a note is completed (a still check when animations are reduced).
    val checkScale by animateFloatAsState(
        targetValue = if (note.completed) 1f else CHECK_IDLE_SCALE,
        animationSpec = if (reduced) snap() else spring(dampingRatio = CHECK_BOUNCE),
        label = "check",
    )
    val interaction = remember { MutableInteractionSource() }
    // A card with depth and its category's color on the edge; it sinks a little while pressed.
    ElevatedCard(
        onClick = actions.onOpen,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().pressScale(interaction),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(FluxSizes.accentStripe).fillMaxHeight().background(category?.color ?: MaterialTheme.colorScheme.primary))
            NoteRowBody(note, category, actions, checkScale, onCheck = {
                haptics.performHapticFeedback(if (it) HapticFeedbackType.Confirm else HapticFeedbackType.ContextClick)
                actions.onToggle()
            })
        }
    }
}

@Composable
private fun NoteRowBody(note: Note, category: Category?, actions: NoteRowActions, checkScale: Float, onCheck: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(end = FluxSpacing.md, top = FluxSpacing.sm, bottom = FluxSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxSpacing.xs),
    ) {
        Checkbox(checked = note.completed, onCheckedChange = onCheck, modifier = Modifier.scale(checkScale))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FluxSpacing.xxs)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (note.completed) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (note.text != note.title) {
                Text(
                    note.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            NoteMeta(note, category)
            OverdueChips(actions)
        }
    }
}

private const val CHECK_IDLE_SCALE = 0.92f
private const val CHECK_BOUNCE = 0.4f
