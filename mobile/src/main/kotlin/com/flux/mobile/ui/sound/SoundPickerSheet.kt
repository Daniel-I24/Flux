package com.flux.mobile.ui.sound

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.SoundRef
import com.flux.mobile.R
import com.flux.mobile.sound.SoundOption
import com.flux.mobile.sound.SoundOptions
import com.flux.mobile.sound.SoundSection

/** One list: add from files, FLUX's tones, the phone's tones and the imported ones, each with play. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickerSheet(
    selected: SoundRef?,
    defaultLabel: String?,
    onSelect: (SoundRef?) -> Unit,
    onDismiss: () -> Unit,
    viewModel: SoundPickerViewModel,
) {
    val system by viewModel.systemTones.collectAsStateWithLifecycle()
    val imported by viewModel.importedTones.collectAsStateWithLifecycle()
    val playing by viewModel.playing.collectAsStateWithLifecycle()
    val failed by viewModel.importFailed.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<SoundRef.Custom?>(null) }
    var deleting by remember { mutableStateOf<SoundRef.Custom?>(null) }
    // Leaving the sheet always silences the preview.
    DisposableEffect(Unit) { onDispose(viewModel::stopPreview) }
    val choose = { sound: SoundRef? ->
        viewModel.stopPreview()
        onSelect(sound)
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.import(it, onImported = choose) }
    }
    val options = remember(system, imported, defaultLabel) { SoundOptions.build(defaultLabel != null, system, imported) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Text(
            stringResource(R.string.sound_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = FluxSpacing.lg).semantics { heading() },
        )
        if (failed) ImportError()
        LazyColumn {
            items(options, key = { it.key() }) { option ->
                when (option) {
                    SoundOption.Inherit -> InheritRow(defaultLabel.orEmpty(), selected == null) { choose(null) }
                    SoundOption.AddFromFiles -> AddFromFilesRow { pickFile.launch(AUDIO_TYPES) }
                    is SoundOption.Header -> SectionHeader(option.section)
                    is SoundOption.Tone -> ToneRow(
                        option = option,
                        label = soundLabel(option.sound, imported).orEmpty(),
                        selected = SoundOptions.isSelected(option.sound, selected),
                        playing = playing?.let { SoundOptions.isSelected(option.sound, it) } == true,
                        actions = ToneRowActions(
                            onSelect = { choose(option.sound) },
                            onPlay = { viewModel.togglePreview(option.sound) },
                            onRename = { editing = option.sound as? SoundRef.Custom },
                            onDelete = { deleting = option.sound as? SoundRef.Custom },
                        ),
                    )
                }
            }
        }
    }
    editing?.let { tone ->
        RenameToneDialog(soundLabel(tone, imported).orEmpty(), onRename = { viewModel.rename(tone.fileName, it) }, onDismiss = {
            editing =
                null
        })
    }
    deleting?.let { tone ->
        DeleteToneDialog(soundLabel(tone, imported).orEmpty(), onDelete = {
            viewModel.delete(tone.fileName)
        }, onDismiss = { deleting = null })
    }
}

@Composable
private fun SectionHeader(section: SoundSection) {
    val title = when (section) {
        SoundSection.BUNDLED -> R.string.sound_section_bundled
        SoundSection.SYSTEM -> R.string.sound_section_system
        SoundSection.IMPORTED -> R.string.sound_section_imported
    }
    Text(
        stringResource(title),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = FluxSpacing.lg, end = FluxSpacing.lg, top = FluxSpacing.md).semantics { heading() },
    )
}

private fun SoundOption.key(): String = when (this) {
    SoundOption.Inherit -> "inherit"
    SoundOption.AddFromFiles -> "add"
    is SoundOption.Header -> "header-$section"
    is SoundOption.Tone -> when (val sound = sound) {
        is SoundRef.Bundled -> "bundled-${sound.sound}"
        is SoundRef.System -> "system-${sound.uri}"
        is SoundRef.Custom -> "custom-${sound.fileName}"
    }
}

/** Ogg is listed apart because some providers report WhatsApp audios as application/ogg. */
private val AUDIO_TYPES = arrayOf("audio/*", "application/ogg")
