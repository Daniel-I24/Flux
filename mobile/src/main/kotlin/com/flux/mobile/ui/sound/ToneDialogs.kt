package com.flux.mobile.ui.sound

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.flux.mobile.R
import com.flux.mobile.sound.ToneFormats

@Composable
fun RenameToneDialog(current: String, onRename: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(current) }
    val valid = ToneFormats.cleanTitle(name) != null
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sound_rename_title)) },
        text = {
            OutlinedTextField(value = name, onValueChange = {
                name = it
            }, label = { Text(stringResource(R.string.sound_name)) }, singleLine = true)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRename(name)
                    onDismiss()
                },
                enabled = valid,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

/** Reminders that used the tone keep working: they ring the fallback tone. */
@Composable
fun DeleteToneDialog(name: String, onDelete: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sound_delete_title, name)) },
        text = { Text(stringResource(R.string.sound_delete_message)) },
        confirmButton = {
            TextButton(onClick = {
                onDelete()
                onDismiss()
            }) { Text(stringResource(R.string.sound_delete)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
