package com.flux.core.designsystem.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import com.flux.core.model.SoundRef
import com.flux.core.model.VibrationPattern
import java.io.IOException

/**
 * Rings a reminder: the tone loops with USAGE_ALARM (it ignores the ringer volume and Do Not
 * Disturb rules for alarms) and the vibration pattern repeats. A missing or invalid tone falls
 * back to [SoundCatalog.FALLBACK]. Call [stop] to release the player and the vibrator.
 */
class AlarmRinger(private val context: Context) {
    private val lock = Any()
    private var player: MediaPlayer? = null

    /** Changes on every start and stop, so a start still preparing knows it was cancelled meanwhile. */
    private var generation = 0L
    private val vibrator: Vibrator? = context.getSystemService(Vibrator::class.java)

    /** Blocking (prepares the player): call it off the main thread. [sound] null = vibrate only. */
    fun start(sound: SoundRef?, vibration: VibrationPattern) {
        val ticket = synchronized(lock) {
            release()
            ++generation
        }
        val media = sound?.let { prepare(SoundCatalog.uri(context, SoundCatalog.playable(context, it))) ?: prepareFallback() }
        synchronized(lock) {
            if (ticket != generation) {
                media?.release()
                return
            }
            player = media
            media?.start()
            vibrate(vibration)
        }
    }

    fun stop() = synchronized(lock) {
        generation++
        release()
    }

    private fun release() {
        player?.runCatching {
            if (isPlaying) stop()
            release()
        }
        player = null
        vibrator?.cancel()
    }

    private fun prepareFallback(): MediaPlayer? = prepare(SoundCatalog.uri(context, SoundCatalog.FALLBACK))

    private fun prepare(uri: Uri): MediaPlayer? {
        val media = MediaPlayer()
        return try {
            media.setAudioAttributes(ALARM_AUDIO)
            // Keeps the CPU awake while the alarm rings with the screen off.
            media.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            media.setDataSource(context, uri)
            media.isLooping = true
            media.prepare()
            media
        } catch (_: IOException) {
            media.release()
            null
        } catch (_: IllegalStateException) {
            media.release()
            null
        } catch (_: SecurityException) {
            media.release()
            null
        } catch (_: IllegalArgumentException) {
            media.release()
            null
        }
    }

    private fun vibrate(pattern: VibrationPattern) {
        if (pattern == VibrationPattern.OFF || vibrator?.hasVibrator() != true) return
        // A pause after each pattern so a repeating "double" does not become one long buzz.
        val effect = VibrationEffect.createWaveform(pattern.timingsMs + PAUSE_MS, REPEAT_FROM_START)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(effect, ALARM_AUDIO)
        }
    }

    private companion object {
        const val REPEAT_FROM_START = 0
        const val PAUSE_MS = 800L
        val ALARM_AUDIO: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }
}
