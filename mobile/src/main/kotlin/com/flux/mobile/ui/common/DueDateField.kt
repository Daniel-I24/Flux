package com.flux.mobile.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.mobile.R

/** "Fecha": "Sin fecha" or the date and time (tap to change it) and "Quitar". The pickers are shown by the caller. */
@Composable
fun DueDateField(dueAt: Long?, onPick: () -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.editor_date), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onPick) { Text(dueAt?.let { dueDateLabel(it) } ?: stringResource(R.string.editor_no_date)) }
        if (dueAt != null) TextButton(onClick = onClear) { Text(stringResource(R.string.editor_clear_date)) }
    }
}
