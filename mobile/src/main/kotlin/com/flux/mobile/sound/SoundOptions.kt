package com.flux.mobile.sound

import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef

enum class SoundSection { BUNDLED, SYSTEM, IMPORTED }

/** One row of FLUX's tone picker. */
sealed interface SoundOption {
    /** "Sonido de la categoría" / "Sonido predeterminado": inherit the sound (null). */
    data object Inherit : SoundOption

    data object AddFromFiles : SoundOption

    data class Header(val section: SoundSection) : SoundOption

    data class Tone(val sound: SoundRef, val section: SoundSection) : SoundOption
}

/** The single list of the picker, in the order users scan it. Pure, so it is tested. */
object SoundOptions {
    fun build(inherit: Boolean, system: List<SystemTone>, imported: List<ImportedTone>): List<SoundOption> = buildList {
        if (inherit) add(SoundOption.Inherit)
        add(SoundOption.AddFromFiles)
        add(SoundOption.Header(SoundSection.BUNDLED))
        BundledSound.entries.forEach { add(SoundOption.Tone(SoundRef.Bundled(it), SoundSection.BUNDLED)) }
        if (system.isNotEmpty()) {
            add(SoundOption.Header(SoundSection.SYSTEM))
            system.forEach { add(SoundOption.Tone(it.sound, SoundSection.SYSTEM)) }
        }
        if (imported.isNotEmpty()) {
            add(SoundOption.Header(SoundSection.IMPORTED))
            imported.forEach { add(SoundOption.Tone(SoundRef.Custom(it.fileName, it.title), SoundSection.IMPORTED)) }
        }
    }

    /** An imported tone keeps its file when renamed, so it is compared by file; a system tone by URI. */
    fun isSelected(sound: SoundRef, selected: SoundRef?): Boolean = when {
        sound is SoundRef.Custom && selected is SoundRef.Custom -> sound.fileName == selected.fileName
        sound is SoundRef.System && selected is SoundRef.System -> sound.uri == selected.uri
        else -> sound == selected
    }
}
