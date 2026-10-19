package com.flux.mobile.feature.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.component.FluxEmptyState
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** Notas: search, filter, sort, complete, swipe to delete (with "Deshacer"), edit and create. */
@Composable
fun NotesScreen(
    snackbar: SnackbarHostState,
    initialCategoryId: String?,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(initialCategoryId) { viewModel.setCategory(initialCategoryId) }
    NotesEventEffect(event, snackbar, viewModel)

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            NotesFilterBar(
                query = state.query,
                categories = state.categories,
                onText = viewModel::setText,
                onCategory = viewModel::setCategory,
                onSort = viewModel::setSort,
                onIncludeCompleted = viewModel::setIncludeCompleted,
            )
            when {
                state.loading -> FluxLoadingState()
                state.notes.isEmpty() -> FluxEmptyState(
                    icon = Icons.Rounded.SearchOff,
                    title = stringResource(R.string.notes_empty),
                    description = stringResource(R.string.notes_empty_hint),
                )
                else -> LazyColumn(contentPadding = PaddingValues(bottom = FluxSizes.fab + FluxSpacing.xxl)) {
                    items(state.notes, key = { it.id }) { note ->
                        NoteRow(
                            note = note,
                            category = state.categories.firstOrNull { it.id == note.categoryId },
                            onToggle = { viewModel.toggleCompleted(note) },
                            onOpen = { editingId = note.id },
                            onDelete = { viewModel.delete(note) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.notes_new)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(FluxSpacing.lg),
        )
    }

    state.notes.firstOrNull { it.id == editingId }?.let { note ->
        NoteEditorSheet(
            note = note,
            categories = state.categories,
            onSave = { change -> viewModel.save(note.id, change) { editingId = null } },
            onDismiss = { editingId = null },
        )
    }
    if (creating) NewNoteDialog(onCreate = { text -> viewModel.create(text) { creating = false } }, onDismiss = { creating = false })
}

/** Shows "Nota eliminada · Deshacer" and errors in the shared snackbar. */
@Composable
private fun NotesEventEffect(event: NotesEvent?, snackbar: SnackbarHostState, viewModel: NotesViewModel) {
    val context = LocalContext.current
    LaunchedEffect(event) {
        when (event) {
            is NotesEvent.Deleted -> {
                viewModel.consumeEvent()
                val result = snackbar.showSnackbar(context.getString(R.string.notes_deleted), context.getString(R.string.notes_undo))
                if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(event.noteId)
            }
            is NotesEvent.Failed -> {
                viewModel.consumeEvent()
                snackbar.showSnackbar(context.getString(event.message))
            }
            null -> Unit
        }
    }
}
