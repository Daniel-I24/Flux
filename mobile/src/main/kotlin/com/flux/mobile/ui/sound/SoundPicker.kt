package com.flux.mobile.ui.sound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.designsystem.sound.ToneFiles
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import com.flux.mobile.sound.ImportedTone

/**
 * The chosen tone and a "Cambiar" button that opens FLUX's own picker (no system picker: some
 * phones refuse custom tones there). When [defaultLabel] is set, null means "inherit the sound".
 */
@Composable
fun SoundPicker(
    selected: SoundRef?,
    onSelect: (SoundRef?) -> Unit,
    modifier: Modifier = Modifier,
    defaultLabel: String? = null,
    viewModel: SoundPickerViewModel = hiltViewModel(),
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val imported by viewModel.importedTones.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null)
            Text(
                text = soundLabel(selected, imported) ?: defaultLabel.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(horizontal = FluxSpacing.sm),
            )
            TextButton(onClick = { open = true }) { Text(stringResource(R.string.sound_change)) }
        }
        if (selected is SoundRef.Custom && !ToneFiles.exists(context, selected.fileName)) {
            Text(
                stringResource(R.string.sound_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    if (open) {
        SoundPickerSheet(
            selected = selected,
            defaultLabel = defaultLabel,
            onSelect = {
                open = false
                onSelect(it)
            },
            onDismiss = { open = false },
            viewModel = viewModel,
        )
    }
}

/** The name users see: a renamed imported tone shows its new name even in old reminders. */
@Composable
fun soundLabel(sound: SoundRef?, imported: List<ImportedTone>): String? = when (sound) {
    null -> null
    is SoundRef.Bundled -> stringResource(SoundCatalog.nameRes(sound.sound))
    is SoundRef.System -> sound.title.ifBlank { stringResource(R.string.sound_system) }
    is SoundRef.Custom -> imported.firstOrNull { it.fileName == sound.fileName }?.title
        ?: sound.title.ifBlank { stringResource(R.string.sound_custom) }
}
