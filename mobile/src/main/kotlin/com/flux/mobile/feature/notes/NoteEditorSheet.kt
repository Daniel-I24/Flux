package com.flux.mobile.feature.notes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.domain.usecase.NoteEdit
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import com.flux.mobile.feature.home.CategoryDot
import com.flux.mobile.ui.common.DueDateField
import com.flux.mobile.ui.sound.SoundPicker

/** Edit title, text, category, date, alarm and the reminder's own sound. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorSheet(note: Note, categories: List<Category>, onSave: (NoteEdit) -> Unit, onDismiss: () -> Unit) {
    var title by rememberSaveable { mutableStateOf(note.title) }
    var text by rememberSaveable { mutableStateOf(note.text) }
    var categoryId by rememberSaveable { mutableStateOf(note.categoryId) }
    var dueAt by rememberSaveable { mutableStateOf(note.dueAt) }
    var alarm by rememberSaveable { mutableStateOf(note.requiresAlarm) }
    // SoundRef is not Parcelable: kept across recompositions, reset if another note is opened.
    var sound by remember(note.id) { mutableStateOf(note.sound) }
    val titleValid = title.isNotBlank() && title.length <= Note.MAX_TITLE_LENGTH

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = FluxSpacing.lg).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(FluxSpacing.md),
        ) {
            Text(
                stringResource(R.string.editor_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics {
                    heading()
                }
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(Note.MAX_TITLE_LENGTH) },
                label = { Text(stringResource(R.string.editor_note_title)) },
                isError = !titleValid,
                supportingText = { if (!titleValid) Text(stringResource(R.string.editor_title_required)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(Note.MAX_TEXT_LENGTH) },
                label = { Text(stringResource(R.string.editor_note_text)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.editor_category), style = MaterialTheme.typography.titleSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = categoryId == category.id,
                        onClick = { categoryId = category.id },
                        label = { Text(category.displayName()) },
                        leadingIcon = { CategoryDot(category) },
                    )
                }
            }
            DueDateField(dueAt = dueAt, onChange = { dueAt = it })
            AlarmSection(hasDate = dueAt != null, alarm = alarm, onAlarm = { alarm = it }, sound = sound, onSound = { sound = it })
            Button(
                onClick = { onSave(NoteEdit(title, text, categoryId, dueAt, alarm, sound.takeIf { alarm })) },
                enabled = titleValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = FluxSpacing.lg),
            ) { Text(stringResource(R.string.action_save)) }
        }
    }
}

@Composable
private fun AlarmSection(hasDate: Boolean, alarm: Boolean, onAlarm: (Boolean) -> Unit, sound: SoundRef?, onSound: (SoundRef?) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.editor_alarm), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = alarm && hasDate, onCheckedChange = onAlarm, enabled = hasDate)
    }
    if (alarm && hasDate) {
        Text(stringResource(R.string.editor_sound), style = MaterialTheme.typography.titleSmall)
        SoundPicker(selected = sound, onSelect = onSound, defaultLabel = stringResource(R.string.sound_use_category))
    }
}
