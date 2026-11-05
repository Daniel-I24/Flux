package com.flux.mobile.ui.sound

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.designsystem.sound.SoundPreviewPlayer
import com.flux.core.designsystem.sound.ToneFiles
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import kotlinx.coroutines.launch

/**
 * Bundled tones, a system ringtone and the user's own audio file, each with a "play" preview.
 * When [defaultLabel] is set, a first option (null) means "use the inherited sound".
 */
@Composable
fun SoundPicker(selected: SoundRef?, onSelect: (SoundRef?) -> Unit, modifier: Modifier = Modifier, defaultLabel: String? = null) {
    val context = LocalContext.current
    val player = remember { SoundPreviewPlayer(context.applicationContext) }
    val importer = remember { ToneImporter(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var importFailed by remember { mutableStateOf(false) }
    DisposableEffect(player) { onDispose { player.stop() } }
    val pickSystem = rememberSystemSoundPicker(context, stringResource(R.string.sound_pick_system)) { tone ->
        onSelect(tone)
        player.play(tone)
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val tone = importer.import(uri)
            importFailed = tone == null
            tone?.let {
                onSelect(it)
                player.play(it)
            }
        }
    }

    Column(modifier.selectableGroup()) {
        if (defaultLabel != null) SoundRow(defaultLabel, selected == null, onSelect = { onSelect(null) }, onPlay = null)
        BundledSound.entries.forEach { bundled ->
            val sound = SoundRef.Bundled(bundled)
            SoundRow(stringResource(SoundCatalog.nameRes(bundled)), selected == sound, { onSelect(sound) }, { player.play(sound) })
        }
        when (selected) {
            is SoundRef.System -> SoundRow(
                selected.title.ifBlank {
                    stringResource(R.string.sound_system)
                },
                true,
                {},
                { player.play(selected) }
            )
            is SoundRef.Custom -> {
                SoundRow(selected.title.ifBlank { stringResource(R.string.sound_custom) }, true, {}, { player.play(selected) })
                if (!ToneFiles.exists(context, selected.fileName)) Warning(stringResource(R.string.sound_missing))
            }
            else -> Unit
        }
        TextButton(onClick = pickSystem) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null)
            Text(stringResource(R.string.sound_pick_system), modifier = Modifier.padding(start = FluxSpacing.sm))
        }
        TextButton(onClick = { pickFile.launch(arrayOf(AUDIO_TYPE)) }) {
            Icon(Icons.Rounded.AudioFile, contentDescription = null)
            Text(stringResource(R.string.sound_pick_file), modifier = Modifier.padding(start = FluxSpacing.sm))
        }
        if (importFailed) Warning(stringResource(R.string.sound_import_failed))
    }
}

@Composable
private fun Warning(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun SoundRow(label: String, checked: Boolean, onSelect: () -> Unit, onPlay: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FluxSizes.minTouchTarget)
            .selectable(selected = checked, onClick = onSelect, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = checked, onClick = null)
        Spacer(Modifier.width(FluxSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (onPlay != null) {
            IconButton(onClick = onPlay) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.sound_preview, label))
            }
        }
    }
}

private const val AUDIO_TYPE = "audio/*"
