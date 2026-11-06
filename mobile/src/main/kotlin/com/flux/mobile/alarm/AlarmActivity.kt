package com.flux.mobile.alarm

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
        showOverLockScreen()
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

    /** Android 8.0 (the minimum) only has the window flags; the Activity methods arrived in 8.1. */
    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    companion object {
        fun intent(context: Context, noteId: String): Intent = Intent(context, AlarmActivity::class.java)
            .putExtra(PhoneAlarmTarget.EXTRA_NOTE_ID, noteId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }
}
