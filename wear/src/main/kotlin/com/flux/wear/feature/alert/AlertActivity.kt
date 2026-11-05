package com.flux.wear.feature.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.flux.core.data.alarm.ActiveAlarm
import com.flux.core.designsystem.sound.AlarmRinger
import com.flux.wear.data.WatchTheme
import com.flux.wear.ui.theme.FluxWearTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Full-screen reminder: title, Posponer and Completar. Vibrates (and rings if enabled) for the
 * configured time, the same as the phone, and closes when the note is handled on either device.
 */
@AndroidEntryPoint
class AlertActivity : ComponentActivity() {
    private val viewModel: AlertViewModel by viewModels()

    @Inject lateinit var activeAlarm: ActiveAlarm

    private val ringer by lazy { AlarmRinger(applicationContext) }
    private var ringJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val silent = intent.getBooleanExtra(EXTRA_SILENT, false)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(state.loading, state.handled) {
                when {
                    state.handled && !state.loading -> finish()
                    !state.loading && !silent && ringJob == null -> ring(state)
                }
            }
            FluxWearTheme(highContrast = state.settings.theme == WatchTheme.HIGH_CONTRAST) {
                AlertScreen(note = state.note, onComplete = viewModel::complete, onSnooze = viewModel::snooze)
            }
        }
    }

    private fun ring(state: AlertUiState) {
        val noteId = state.note?.id ?: return
        activeAlarm.start(noteId)
        ringJob = lifecycleScope.launch {
            withContext(Dispatchers.IO) { ringer.start(state.sound.takeIf { state.settings.soundEnabled }, state.settings.vibration) }
            delay(state.durationSeconds * MILLIS_PER_SECOND)
            stopRinging()
            activeAlarm.stop(noteId)
        }
    }

    private fun stopRinging() {
        ringJob?.cancel()
        ringer.stop()
    }

    override fun onDestroy() {
        stopRinging()
        intent.getStringExtra(EXTRA_NOTE_ID)?.let(activeAlarm::stop)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_NOTE_ID = "noteId"
        private const val EXTRA_SILENT = "silent"
        private const val MILLIS_PER_SECOND = 1_000L

        fun intent(context: Context, noteId: String, silent: Boolean): Intent = Intent(context, AlertActivity::class.java)
            .putExtra(EXTRA_NOTE_ID, noteId)
            .putExtra(EXTRA_SILENT, silent)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
