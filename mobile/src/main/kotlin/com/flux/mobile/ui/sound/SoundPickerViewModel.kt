package com.flux.mobile.ui.sound

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.model.SoundRef
import com.flux.mobile.sound.ImportedTone
import com.flux.mobile.sound.SystemTone
import com.flux.mobile.sound.ToneLibrary
import com.flux.mobile.sound.TonePreview
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What a row of the picker can do with its tone. */
data class ToneRowActions(val onSelect: () -> Unit, val onPlay: () -> Unit, val onRename: () -> Unit, val onDelete: () -> Unit)

/** FLUX's tone picker: lists, imports, renames and deletes tones, and plays one preview at a time. */
@HiltViewModel
class SoundPickerViewModel @Inject constructor(
    @param:ApplicationContext context: Context,
    private val library: ToneLibrary,
) : ViewModel() {
    private val preview = TonePreview(context)
    private val failed = MutableStateFlow(false)

    val systemTones: StateFlow<List<SystemTone>> = library.systemTones
    val importedTones: StateFlow<List<ImportedTone>> = library.importedTones
    val playing: StateFlow<SoundRef?> = preview.playing

    /** The last import was not usable audio. */
    val importFailed: StateFlow<Boolean> = failed.asStateFlow()

    init {
        viewModelScope.launch { library.load() }
    }

    fun togglePreview(sound: SoundRef) = preview.toggle(sound)

    fun stopPreview() = preview.stop()

    fun import(uri: Uri, onImported: (SoundRef.Custom) -> Unit) {
        viewModelScope.launch {
            val tone = library.import(uri)
            failed.value = tone == null
            tone?.let { onImported(SoundRef.Custom(it.fileName, it.title)) }
        }
    }

    fun rename(fileName: String, title: String) {
        viewModelScope.launch { library.rename(fileName, title) }
    }

    fun delete(fileName: String) {
        preview.stop()
        viewModelScope.launch { library.delete(fileName) }
    }

    override fun onCleared() {
        preview.stop()
    }
}
