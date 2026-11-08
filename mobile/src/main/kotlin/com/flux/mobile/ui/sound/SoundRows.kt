package com.flux.mobile.ui.sound

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.sound.SoundOption
import com.flux.mobile.sound.SoundSection

private val rowModifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget).padding(horizontal = FluxSpacing.md)

@Composable
fun ToneRow(option: SoundOption.Tone, label: String, selected: Boolean, playing: Boolean, actions: ToneRowActions) {
    Row(
        modifier = Modifier.selectable(selected = selected, onClick = actions.onSelect, role = Role.RadioButton).then(rowModifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(FluxSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = actions.onPlay) {
            Icon(
                if (playing) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = stringResource(if (playing) R.string.sound_stop else R.string.sound_preview, label),
            )
        }
        if (option.section == SoundSection.IMPORTED) ImportedMenu(label, actions)
    }
}

@Composable
private fun ImportedMenu(label: String, actions: ToneRowActions) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = {
            open = true
        }) { Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.sound_more, label)) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.sound_rename)) }, onClick = {
                open = false
                actions.onRename()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.sound_delete)) }, onClick = {
                open = false
                actions.onDelete()
            })
        }
    }
}

@Composable
fun InheritRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.selectable(selected = selected, onClick = onSelect, role = Role.RadioButton).then(rowModifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(FluxSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AddFromFilesRow(onClick: () -> Unit) {
    Row(Modifier.clickable(onClick = onClick, role = Role.Button).then(rowModifier), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(FluxSpacing.sm))
        Text(
            stringResource(R.string.sound_add_files),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ImportError() {
    Text(
        stringResource(R.string.sound_import_failed),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = FluxSpacing.lg, vertical = FluxSpacing.sm),
    )
}
