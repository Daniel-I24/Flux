package com.flux.wear.feature.agenda

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot
import com.flux.wear.ui.rememberDueDateLabel
import com.flux.wear.ui.rememberHaptics

/** Today and upcoming reminders; tapping the checkbox completes it (synced to the phone). */
@Composable
fun AgendaScreen(viewModel: AgendaViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.agenda_title)) } }
            when {
                state.loading -> item { CircularProgressIndicator() }
                state.today.isEmpty() && state.upcoming.isEmpty() -> item {
                    Text(stringResource(R.string.agenda_empty), textAlign = TextAlign.Center)
                }
                else -> {
                    section(R.string.agenda_today, state.today, state.categories) {
                        haptics.confirm()
                        viewModel.complete(it)
                    }
                    section(R.string.agenda_upcoming, state.upcoming, state.categories) {
                        haptics.confirm()
                        viewModel.complete(it)
                    }
                }
            }
        }
    }
}

private fun TransformingLazyColumnScope.section(
    title: Int,
    notes: List<Note>,
    categories: Map<String, Category>,
    onComplete: (String) -> Unit,
) {
    if (notes.isEmpty()) return
    item { ListHeader { Text(stringResource(title)) } }
    items(notes, key = { it.id }) { note -> AgendaItem(note, categories[note.categoryId], onComplete) }
}

@Composable
private fun AgendaItem(note: Note, category: Category?, onComplete: (String) -> Unit) {
    CheckboxButton(
        checked = note.completed,
        onCheckedChange = { if (it) onComplete(note.id) },
        icon = category?.let { { CategoryIconDot(it) } },
        label = { Text(note.title, maxLines = 2) },
        secondaryLabel = note.dueAt?.let { { Text(rememberDueDateLabel(it)) } },
        modifier = Modifier.fillMaxWidth(),
    )
}
