package com.flux.mobile.ui.sound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.designsystem.sound.SoundPreviewPlayer
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import com.flux.mobile.R

/**
 * Bundled tones and the chosen system tone, each with a "play" preview. When
 * [defaultLabel] is set, a first option (null) means "use the inherited sound".
 */
@Composable
fun SoundPicker(selected: SoundRef?, onSelect: (SoundRef?) -> Unit, modifier: Modifier = Modifier, defaultLabel: String? = null) {
    val context = LocalContext.current
    val player = remember { SoundPreviewPlayer(context) }
    DisposableEffect(player) { onDispose { player.stop() } }
    val pickSystem = rememberSystemSoundPicker(context, stringResource(R.string.sound_pick_system)) { tone ->
        onSelect(tone)
        player.play(tone)
    }

    Column(modifier.selectableGroup()) {
        if (defaultLabel != null) SoundRow(defaultLabel, selected == null, onSelect = { onSelect(null) }, onPlay = null)
        BundledSound.entries.forEach { bundled ->
            val sound = SoundRef.Bundled(bundled)
            SoundRow(stringResource(SoundCatalog.nameRes(bundled)), selected == sound, { onSelect(sound) }, { player.play(sound) })
        }
        (selected as? SoundRef.System)?.let { system ->
            SoundRow(system.title.ifBlank { stringResource(R.string.sound_system) }, true, { onSelect(system) }, { player.play(system) })
        }
        TextButton(onClick = pickSystem) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null)
            Text(stringResource(R.string.sound_pick_system))
        }
    }
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
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (onPlay != null) {
            IconButton(onClick = onPlay) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.sound_preview, label))
            }
        }
    }
}
