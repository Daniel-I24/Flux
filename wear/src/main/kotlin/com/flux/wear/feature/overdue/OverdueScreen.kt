package com.flux.wear.feature.overdue

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot
import com.flux.wear.ui.rememberDueDateLabel
import com.flux.wear.ui.rememberHaptics

/** Vencidos: tap a reminder to see its actions (Completar, En 1 hora, Mañana, Eliminar). */
@Composable
fun OverdueScreen(viewModel: OverdueViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.overdue_title)) } }
            if (!state.loading && state.notes.isEmpty()) {
                item { Text(stringResource(R.string.overdue_empty), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
            }
            items(state.notes, key = { it.id }) { note ->
                TitleCard(
                    onClick = { selected = note.id.takeUnless { it == selected } },
                    title = { Text(note.title, maxLines = 2) },
                    subtitle = note.dueAt?.let { { Text(rememberDueDateLabel(it)) } },
                    time = state.categories[note.categoryId]?.let { { CategoryIconDot(it) } },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (selected == note.id) {
                        val act: (() -> Unit) -> Unit = { action ->
                            haptics.confirm()
                            selected = null
                            action()
                        }
                        Action(Icons.Rounded.Check, R.string.agenda_complete) { act { viewModel.complete(note.id) } }
                        Action(Icons.Rounded.Schedule, R.string.overdue_in_one_hour) { act { viewModel.inOneHour(note.id) } }
                        Action(Icons.Rounded.WbSunny, R.string.overdue_tomorrow) { act { viewModel.tomorrowMorning(note.id) } }
                        Action(Icons.Rounded.Delete, R.string.overdue_delete) { act { viewModel.delete(note.id) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun Action(icon: ImageVector, label: Int, onClick: () -> Unit) {
    CompactButton(
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(stringResource(label)) },
        modifier = Modifier.fillMaxWidth(),
    )
}
