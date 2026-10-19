package com.flux.wear.feature.alert

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.sound.SoundCatalog
import com.flux.core.model.SoundRef
import com.flux.core.model.VibrationPattern
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchTheme
import com.flux.wear.ui.theme.FluxWearTheme
import dagger.hilt.android.AndroidEntryPoint

/** Full-screen reminder: title, Posponer and Completar; rings until the user acts. */
@AndroidEntryPoint
class AlertActivity : ComponentActivity() {
    private val viewModel: AlertViewModel by viewModels()
    private var ringtone: Ringtone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val silent = intent.getBooleanExtra(EXTRA_SILENT, false)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.note, state.settings) { if (state.note != null && !silent) startRinging(state.settings, state.sound) }
            LaunchedEffect(state.handled) { if (state.handled) finish() }
            FluxWearTheme(highContrast = state.settings.theme == WatchTheme.HIGH_CONTRAST) {
                AlertScreen(note = state.note, onComplete = viewModel::complete, onSnooze = viewModel::snooze)
            }
        }
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    private fun startRinging(settings: WatchSettings, sound: SoundRef) {
        stopRinging()
        if (settings.vibration != VibrationPattern.OFF) {
            getSystemService(Vibrator::class.java)?.vibrate(VibrationEffect.createWaveform(settings.vibration.timingsMs, REPEAT_FROM_START))
        }
        if (settings.soundEnabled) {
            ringtone = RingtoneManager.getRingtone(this, SoundCatalog.uri(this, sound))?.apply {
                audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                isLooping = true
                play()
            }
        }
    }

    private fun stopRinging() {
        getSystemService(Vibrator::class.java)?.cancel()
        ringtone?.stop()
        ringtone = null
    }

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
        private const val EXTRA_SILENT = "silent"
        private const val REPEAT_FROM_START = 0

        fun intent(context: Context, noteId: String, silent: Boolean): Intent = Intent(context, AlertActivity::class.java)
            .putExtra(EXTRA_NOTE_ID, noteId)
            .putExtra(EXTRA_SILENT, silent)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
