package com.flux.mobile.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.AppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Full-screen reminder over the lock screen: Posponer and Completar. The alarm service rings. */
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()

    @Inject lateinit var observeSettings: ObserveSettingsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val settings by observeSettings().collectAsStateWithLifecycle(AppSettings())
            LaunchedEffect(state.handled, state.loading) { if (!state.loading && state.handled) finish() }
            FluxTheme(themeMode = settings.themeMode) {
                AlarmScreen(state = state, onComplete = viewModel::complete, onSnooze = viewModel::snooze)
            }
        }
    }

    companion object {
        fun intent(context: Context, noteId: String): Intent = Intent(context, AlarmActivity::class.java)
            .putExtra(PhoneAlarmTarget.EXTRA_NOTE_ID, noteId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }
}
