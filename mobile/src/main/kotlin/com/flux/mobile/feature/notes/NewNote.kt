package com.flux.mobile.feature.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.platform.LocalFocusManager
import com.flux.mobile.feature.capture.CaptureSheet

/** An open "Nueva nota": the category it is saved in (the one shown, or none) and whether to listen right away. */
data class NewNote(val categoryId: String? = null, val listen: Boolean = false) {
    companion object {
        val saver: Saver<NewNote?, Any> = listSaver(
            save = { note -> note?.let { listOf(it.categoryId.orEmpty(), it.listen) }.orEmpty() },
            restore = { values -> NewNote((values[0] as String).ifEmpty { null }, values[1] as Boolean) },
        )
    }
}

/** The capture sheet for [newNote]; closing it clears the focus so the search field does not open the keyboard. */
@Composable
fun NewNoteSheet(newNote: NewNote, onClose: () -> Unit, onAlarmSaved: (Long) -> Unit) {
    val focus = LocalFocusManager.current
    CaptureSheet(
        onDismiss = {
            onClose()
            focus.clearFocus()
        },
        onAlarmSaved = onAlarmSaved,
        listenOnOpen = newNote.listen,
        fixedCategoryId = newNote.categoryId,
    )
}
