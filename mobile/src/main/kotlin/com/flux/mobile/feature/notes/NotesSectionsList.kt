package com.flux.mobile.feature.notes

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.component.FluxEmptyState
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Note
import com.flux.mobile.R

/** Próximos, Pendientes, Vencidos and Completados; each list animates its entries (unless motion is reduced). */
@Composable
fun NotesSectionsList(state: NotesUiState, viewModel: NotesViewModel, onOpen: (String) -> Unit) {
    val sections = state.sections
    val reduced = rememberReducedMotion()
    when {
        state.loading -> FluxLoadingState()
        sections.isEmpty -> FluxEmptyState(
            icon = Icons.Rounded.SearchOff,
            title = stringResource(R.string.notes_empty),
            description = stringResource(R.string.notes_empty_examples),
        )
        else -> LazyColumn(contentPadding = PaddingValues(bottom = FluxSizes.fab + FluxSpacing.xxl)) {
            val categories = state.categories.associateBy { it.id }
            val row: @Composable LazyItemScope.(Note, Boolean) -> Unit = { note, overdue ->
                NoteRow(
                    note = note,
                    category = categories[note.categoryId],
                    actions = NoteRowActions(
                        onToggle = { viewModel.toggleCompleted(note) },
                        onOpen = { onOpen(note.id) },
                        onDelete = { viewModel.delete(note) },
                        onInOneHour = if (overdue) ({ viewModel.inOneHour(note.id) }) else null,
                        onReschedule = if (overdue) ({ onOpen(note.id) }) else null,
                    ),
                    modifier = if (reduced) Modifier else Modifier.animateItem(),
                )
            }
            section(R.string.notes_section_upcoming, sections.upcoming) { row(it, false) }
            section(R.string.notes_section_pending, sections.pending) { row(it, false) }
            section(R.string.notes_section_overdue, sections.overdue) { row(it, true) }
            section(R.string.notes_section_completed, sections.completed) { row(it, false) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.section(
    @StringRes title: Int,
    notes: List<Note>,
    content: @Composable LazyItemScope.(Note) -> Unit,
) {
    if (notes.isEmpty()) return
    stickyHeader(key = "header-$title") {
        Text(
            text = stringResource(R.string.notes_section_count, stringResource(title), notes.size),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = FluxSpacing.lg, vertical = FluxSpacing.sm)
                .semantics { heading() },
        )
    }
    items(notes, key = { it.id }) { note -> content(note) }
}
