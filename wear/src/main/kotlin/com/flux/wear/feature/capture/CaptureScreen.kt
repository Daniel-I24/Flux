package com.flux.wear.feature.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.wear.ui.rememberHaptics
import kotlinx.coroutines.delay

/** Capture flow driven by [CaptureState]; [startWithKeyboard] (debug only) opens the keyboard instead of voice. */
@Composable
fun CaptureScreen(onDone: () -> Unit, startWithKeyboard: Boolean = false, viewModel: CaptureViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val partial by viewModel.partial.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var started by rememberSaveable { mutableStateOf(false) }
    var leftIdle by rememberSaveable { mutableStateOf(false) }
    val inputs = rememberVoiceInputs(viewModel)

    LaunchedEffect(Unit) {
        if (started) return@LaunchedEffect
        started = true
        val type = inputs.type
        if (startWithKeyboard && type != null) type() else inputs.dictate()
    }
    LaunchedEffect(state) {
        if (state != CaptureState.Idle) leftIdle = true
        when (state) {
            CaptureState.Idle -> if (leftIdle) onDone()
            is CaptureState.Saved -> {
                haptics.confirm()
                delay(SAVED_SCREEN_MS)
                onDone()
            }
            is CaptureState.Error -> haptics.reject()
            else -> Unit
        }
    }

    when (val current = state) {
        CaptureState.Idle, CaptureState.Listening -> ListeningContent(
            level = { viewModel.level.value },
            partial = partial,
            onDone = viewModel::stopListening,
        )
        is CaptureState.Processing -> ProcessingContent(current.text)
        is CaptureState.Preview -> PreviewContent(
            preview = current,
            categories = categories,
            onEvent = { event ->
                haptics.tap()
                if (event == CaptureEvent.StartDictation) inputs.dictate() else viewModel.send(event)
            },
            onEditText = inputs.type,
        )
        is CaptureState.Saving -> ProcessingContent(current.preview.text)
        is CaptureState.Saved -> SavedContent(current.queued)
        is CaptureState.Error -> ErrorContent(
            failure = current.failure,
            onRetry = inputs.dictate,
            onOpenSettings = inputs.openPermissionSettings,
            onType = inputs.type,
        )
    }
}

private const val SAVED_SCREEN_MS = 1_500L
