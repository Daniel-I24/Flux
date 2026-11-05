package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.core.model.Ambiguity
import com.flux.core.model.Category
import com.flux.wear.R
import com.flux.wear.ui.CategoryChip
import com.flux.wear.ui.rememberDueDateLabel

/**
 * Category (color + icon), title, date, questions with large buttons when something is ambiguous,
 * and the 5 s countdown ring when nothing is. Scrolls with the crown on small round screens.
 */
@Composable
fun PreviewContent(preview: CaptureState.Preview, categories: List<Category>, onEvent: (CaptureEvent) -> Unit, onEditText: (() -> Unit)?) {
    var picking by rememberSaveable { mutableStateOf(false) }
    if (picking) {
        CategoryPicker(categories, selectedId = preview.classification.categoryId) { id ->
            picking = false
            onEvent(CaptureEvent.ChangeCategory(id))
        }
        return
    }
    val category = categories.firstOrNull { it.id == preview.classification.categoryId }
    val question = preview.classification.ambiguities.firstOrNull()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        if (question == null) {
            CircularProgressIndicator(
                progress = { if (preview.paused) 1f else preview.secondsLeft / CaptureState.AUTO_SAVE_SECONDS.toFloat() },
                modifier = Modifier.fillMaxSize(),
            )
        }
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { CategoryChip(category, onClick = { picking = true }) }
            item {
                Text(
                    text = preview.classification.title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Text(
                    text = preview.classification.dueAt?.let { rememberDueDateLabel(it) } ?: stringResource(R.string.capture_no_date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (question != null) {
                ambiguityQuestion(question, categories, onEvent)
            } else {
                item { CountdownLabel(preview) }
            }
            item { PreviewActions(onEvent, onEditText) }
        }
    }
}

@Composable
private fun CountdownLabel(preview: CaptureState.Preview) {
    Text(
        text = if (preview.paused) {
            stringResource(
                R.string.capture_paused
            )
        } else {
            stringResource(R.string.capture_saving_in, preview.secondsLeft)
        },
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Cancel, re-dictate (or type, debug only) and save. */
@Composable
private fun PreviewActions(onEvent: (CaptureEvent) -> Unit, onEditText: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(onClick = { onEvent(CaptureEvent.Cancel) }) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.capture_cancel))
        }
        if (onEditText != null) {
            IconButton(onClick = onEditText) { Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.capture_edit)) }
        } else {
            IconButton(onClick = { onEvent(CaptureEvent.StartDictation) }) {
                Icon(Icons.Rounded.Mic, contentDescription = stringResource(R.string.capture_redictate))
            }
        }
        FilledIconButton(onClick = { onEvent(CaptureEvent.Confirm) }) {
            Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.capture_save))
        }
    }
}

/** Ambiguity options as large buttons; answering removes the question and starts the countdown. */
internal fun Ambiguity.answers(): List<CaptureEvent> = when (this) {
    is Ambiguity.DueTime -> options.map(CaptureEvent::AnswerTime)
    is Ambiguity.CategoryChoice -> options.map(CaptureEvent::AnswerCategory)
}
