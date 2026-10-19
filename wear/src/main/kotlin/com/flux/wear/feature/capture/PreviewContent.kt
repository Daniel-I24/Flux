package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.core.model.Category
import com.flux.wear.R
import com.flux.wear.ui.CategoryChip
import com.flux.wear.ui.rememberDueDateLabel

/** Category (color + icon), title, date, 5 s circular countdown and the four actions. */
@Composable
fun PreviewContent(preview: CaptureState.Preview, categories: List<Category>, onEvent: (CaptureEvent) -> Unit, onEditText: () -> Unit) {
    var picking by rememberSaveable { mutableStateOf(false) }
    if (picking) {
        CategoryPicker(categories, selectedId = preview.classification.categoryId) { id ->
            picking = false
            onEvent(CaptureEvent.ChangeCategory(id))
        }
        return
    }
    val category = categories.firstOrNull { it.id == preview.classification.categoryId }
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            progress = { if (preview.paused) 1f else preview.secondsLeft / CaptureState.AUTO_SAVE_SECONDS.toFloat() },
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CategoryChip(category, onClick = { picking = true })
            Text(
                text = preview.classification.title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 3,
            )
            Text(
                text = preview.classification.dueAt?.let { rememberDueDateLabel(it) } ?: stringResource(R.string.capture_no_date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = if (preview.paused) {
                    stringResource(
                        R.string.capture_paused
                    )
                } else {
                    stringResource(R.string.capture_saving_in, preview.secondsLeft)
                },
                style = MaterialTheme.typography.labelSmall,
            )
            PreviewActions(onEvent, onEditText)
        }
    }
}

@Composable
private fun PreviewActions(onEvent: (CaptureEvent) -> Unit, onEditText: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(onClick = { onEvent(CaptureEvent.Cancel) }) {
            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.capture_cancel))
        }
        IconButton(onClick = onEditText) {
            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.capture_edit))
        }
        FilledIconButton(onClick = { onEvent(CaptureEvent.Confirm) }) {
            Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.capture_save))
        }
    }
}
