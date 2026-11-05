package com.flux.core.designsystem.sound

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.flux.core.designsystem.R
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import java.io.File

/** Maps the model's sound references to playable URIs (bundled tones live in res/raw). */
object SoundCatalog {
    /** Played when the chosen tone was deleted, moved or cannot be decoded. */
    val FALLBACK: SoundRef = SoundRef.Bundled(BundledSound.CHIME)

    @RawRes
    fun rawRes(sound: BundledSound): Int = when (sound) {
        BundledSound.CHIME -> R.raw.flux_chime
        BundledSound.PULSE -> R.raw.flux_pulse
        BundledSound.BELL -> R.raw.flux_bell
        BundledSound.SOFT -> R.raw.flux_soft
    }

    @StringRes
    fun nameRes(sound: BundledSound): Int = when (sound) {
        BundledSound.CHIME -> R.string.sound_chime
        BundledSound.PULSE -> R.string.sound_pulse
        BundledSound.BELL -> R.string.sound_bell
        BundledSound.SOFT -> R.string.sound_soft
    }

    fun uri(context: Context, sound: SoundRef): Uri = when (sound) {
        is SoundRef.Bundled -> Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(context.packageName)
            .appendPath(context.resources.getResourceTypeName(rawRes(sound.sound)))
            .appendPath(context.resources.getResourceEntryName(rawRes(sound.sound)))
            .build()
        is SoundRef.System -> Uri.parse(sound.uri)
        is SoundRef.Custom -> Uri.fromFile(ToneFiles.file(context, sound.fileName))
    }

    /** A custom tone whose file vanished is replaced by [FALLBACK] before anything tries to play it. */
    fun playable(context: Context, sound: SoundRef): SoundRef =
        if (sound is SoundRef.Custom && !ToneFiles.exists(context, sound.fileName)) FALLBACK else sound
}

/** Custom tones are copied into the app's private storage, so they keep working if the original is moved. */
object ToneFiles {
    private const val DIRECTORY = "tones"

    fun directory(context: Context): File = File(context.filesDir, DIRECTORY).apply { mkdirs() }

    /** Only plain file names are accepted: a stored name can never point outside [directory]. */
    fun file(context: Context, name: String): File = File(directory(context), File(name).name)

    fun exists(context: Context, name: String): Boolean = file(context, name).let { it.isFile && it.length() > 0 }
}
