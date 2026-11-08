package com.flux.mobile.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.model.SoundRef
import kotlinx.coroutines.flow.StateFlow

/**
 * Plays a tone inside FLUX with MediaPlayer. The old preview used Ringtone, which may play out of
 * process and cannot read FLUX's private copy of an imported tone, so imported tones were silent.
 * A tone that cannot be decoded plays [SoundCatalog.FALLBACK], as the alarm does.
 */
class TonePreview(private val context: Context) {
    private var player: MediaPlayer? = null
    private val toggle = PreviewToggle<SoundRef>(start = ::start, halt = ::release)

    val playing: StateFlow<SoundRef?> = toggle.playing

    fun toggle(sound: SoundRef) = toggle.toggle(sound)

    fun stop() = toggle.stop()

    private fun start(sound: SoundRef) {
        open(SoundCatalog.playable(context, sound), sound)
    }

    private fun open(source: SoundRef, shown: SoundRef) {
        val media = MediaPlayer()
        player = media
        runCatching {
            media.setAudioAttributes(PREVIEW_AUDIO)
            media.setDataSource(context, SoundCatalog.uri(context, source))
            media.setOnPreparedListener { if (it === player) it.start() }
            media.setOnCompletionListener {
                toggle.finished(shown)
                if (it === player) release()
            }
            media.setOnErrorListener { _, _, _ ->
                fallback(source, shown)
                true
            }
            media.prepareAsync()
        }.onFailure { fallback(source, shown) }
    }

    private fun fallback(failed: SoundRef, shown: SoundRef) {
        release()
        if (failed != SoundCatalog.FALLBACK) open(SoundCatalog.FALLBACK, shown) else toggle.finished(shown)
    }

    private fun release() {
        player?.runCatching { release() }
        player = null
    }

    private companion object {
        val PREVIEW_AUDIO: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }
}
