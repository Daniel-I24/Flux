package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.wear.R
import com.flux.wear.ui.rememberHaptics
import kotlinx.coroutines.delay

/** Capture flow driven by [CaptureState]; [startWithKeyboard] opens the keyboard instead of dictation. */
@Composable
fun CaptureScreen(onDone: () -> Unit, startWithKeyboard: Boolean = false, viewModel: CaptureViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    var started by rememberSaveable { mutableStateOf(false) }
    var leftIdle by rememberSaveable { mutableStateOf(false) }
    val inputs = rememberTextInputLaunchers(
        onSpeech = { text -> viewModel.send(if (text == null) CaptureEvent.Cancel else CaptureEvent.SpeechResult(text)) },
        onText = { text ->
            if (text.isNullOrBlank()) viewModel.send(CaptureEvent.Cancel) else viewModel.send(CaptureEvent.TextEntered(text))
        },
    )

    LaunchedEffect(Unit) {
        if (started) return@LaunchedEffect
        started = true
        viewModel.send(CaptureEvent.StartDictation)
        if (startWithKeyboard) inputs.type() else inputs.dictate()
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
        CaptureState.Idle, CaptureState.Listening -> Centered { Text(stringResource(R.string.capture_listening)) }
        is CaptureState.Processing -> ProcessingContent(current.text)
        is CaptureState.Preview -> PreviewContent(
            preview = current,
            categories = categories,
            onEvent = { event ->
                haptics.tap()
                viewModel.send(event)
                if (event == CaptureEvent.StartDictation) inputs.dictate()
            },
            onEditText = inputs.type,
        )
        is CaptureState.Saving -> ProcessingContent(current.preview.text)
        is CaptureState.Saved -> SavedContent(current.queued)
        is CaptureState.Error -> ErrorContent(current.failure) {
            viewModel.send(CaptureEvent.StartDictation)
            inputs.dictate()
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun ProcessingContent(text: String) = Centered {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.capture_processing), style = MaterialTheme.typography.titleSmall)
        Text(text, maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SavedContent(queued: Boolean) = Centered {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = if (queued) Icons.Rounded.CloudQueue else Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(stringResource(R.string.capture_saved), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(if (queued) R.string.capture_saved_queued else R.string.capture_saved_sent),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ErrorContent(failure: CaptureFailure, onRetry: () -> Unit) = Centered {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.MicOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = stringResource(
                if (failure ==
                    CaptureFailure.NO_SPEECH
                ) {
                    R.string.capture_error_no_speech
                } else {
                    R.string.capture_error_not_saved
                }
            ),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = onRetry, label = { Text(stringResource(R.string.capture_try_again)) })
    }
}

private const val SAVED_SCREEN_MS = 1_500L
