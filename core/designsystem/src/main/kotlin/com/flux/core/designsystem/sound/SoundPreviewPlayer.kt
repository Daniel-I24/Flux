package com.flux.core.designsystem.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import com.flux.core.model.SoundRef

/** Plays one sound at a time so the user can hear it before choosing it. */
class SoundPreviewPlayer(private val context: Context) {
    private var current: Ringtone? = null

    fun play(sound: SoundRef) {
        stop()
        current = RingtoneManager.getRingtone(context, SoundCatalog.uri(context, sound))?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            play()
        }
    }

    fun stop() {
        current?.stop()
        current = null
    }
}
