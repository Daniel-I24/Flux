package com.flux.mobile.feature.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
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
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category
import com.flux.mobile.R
import com.flux.mobile.feature.settings.SyncedSettingsViewModel
import com.flux.mobile.launch.CaptureLaunch
import com.flux.mobile.ui.common.SnackbarEventEffect
import com.flux.mobile.ui.common.alarmConfirmation

/** Notas: sections, search, filters, complete and delete with "Deshacer", edit and create (voice or text). */
@Composable
fun NotesScreen(
    snackbar: SnackbarHostState,
    request: NotesRequest?,
    onRequestHandled: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.event.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var newNote by rememberSaveable(stateSaver = NewNote.saver) { mutableStateOf<NewNote?>(null) }
    // Inicio or Categorías ask for a category; a launcher shortcut or quick settings opens a capture.
    LaunchedEffect(request?.token) {
        request ?: return@LaunchedEffect
        if (request.capture == null) viewModel.setCategory(request.categoryId)
        request.capture?.let { newNote = NewNote(listen = it == CaptureLaunch.DICTATE) }
        // Handled once: coming back to the tab keeps the user's filters and opens nothing.
        onRequestHandled()
    }
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
            NotesSectionsList(state, viewModel, onOpen = { editingId = it })
        }
        NewNoteButton(
            category = state.categories.firstOrNull { it.id == state.query.categoryId },
            // The note goes to the category being shown when "Nueva nota" was tapped.
            onClick = { newNote = NewNote(categoryId = state.query.categoryId) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(FluxSpacing.lg),
        )
    }

    state.find(editingId)?.let { note ->
        NoteEditorSheet(
            note = note,
            categories = state.categories,
            onSave = { change -> viewModel.save(note.id, change) { editingId = null } },
            onDismiss = { editingId = null },
            globalLeadMinutes = hiltViewModel<SyncedSettingsViewModel>().settings.collectAsStateWithLifecycle().value.alarmLeadMinutes,
        )
    }
    newNote?.let { NewNoteSheet(it, onClose = { newNote = null }, onAlarmSaved = viewModel::announceAlarm) }
}

/** "Nota eliminada · Deshacer", "Completada · Deshacer" and errors in the shared snackbar. */
@Composable
private fun NotesEventEffect(event: NotesEvent?, snackbar: SnackbarHostState, viewModel: NotesViewModel) {
    val context = LocalContext.current
    SnackbarEventEffect(event, onConsumed = viewModel::consumeEvent) { current ->
        when (current) {
            is NotesEvent.Deleted -> {
                val result = snackbar.showSnackbar(context.getString(R.string.notes_deleted), context.getString(R.string.notes_undo))
                if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(current.noteId)
            }
            is NotesEvent.Completed -> {
                val result = snackbar.showSnackbar(context.getString(R.string.notes_completed), context.getString(R.string.notes_undo))
                if (result == SnackbarResult.ActionPerformed) viewModel.undoComplete(current.noteId)
            }
            is NotesEvent.Failed -> snackbar.showSnackbar(context.getString(current.message))
            is NotesEvent.Scheduled -> snackbar.showSnackbar(context.alarmConfirmation(current.dueAt))
        }
    }
}

/** "Nueva nota", or "Nueva nota en Compras" while a category is shown (the note is saved there). */
@Composable
private fun NewNoteButton(category: Category?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Rounded.Mic, contentDescription = null) },
        text = { Text(category?.let { stringResource(R.string.notes_new_in, it.displayName()) } ?: stringResource(R.string.notes_new)) },
        modifier = modifier,
    )
}
