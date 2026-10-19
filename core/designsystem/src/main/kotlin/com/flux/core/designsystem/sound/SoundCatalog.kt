package com.flux.core.designsystem.sound

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.flux.core.designsystem.R
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef

/** Maps the model's sound references to playable URIs (bundled tones live in res/raw). */
object SoundCatalog {
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
    }

    /** Short stable key used to build one notification channel per sound. */
    fun key(sound: SoundRef): String = when (sound) {
        is SoundRef.Bundled -> sound.sound.name.lowercase()
        is SoundRef.System -> "sys_" + Integer.toHexString(sound.uri.hashCode())
    }
}
