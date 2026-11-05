package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.component.LavaColors
import com.flux.core.designsystem.component.VoiceLava
import com.flux.wear.R
import com.flux.wear.ui.rememberWatchInsets

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(rememberWatchInsets()), contentAlignment = Alignment.Center) { content() }
}

/** The lava reacts to the voice; "Listo" stops listening and keeps what was said. */
@Composable
fun ListeningContent(level: () -> Float, partial: String, onDone: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        VoiceLava(
            active = true,
            level = level,
            colors = LavaColors(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer, scheme.background),
            modifier = Modifier.fillMaxSize(),
        )
        Centered {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Mic, contentDescription = null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                Text(stringResource(R.string.capture_listening), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = partial.ifBlank { stringResource(R.string.capture_listening_hint) },
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    style = MaterialTheme.typography.bodySmall,
                )
                CompactButton(onClick = onDone, label = { Text(stringResource(R.string.capture_done_speaking)) })
            }
        }
    }
}

@Composable
fun ProcessingContent(text: String) = Centered {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.capture_processing), style = MaterialTheme.typography.titleSmall)
        Text(text, maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SavedContent(queued: Boolean) = Centered {
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

private fun CaptureFailure.message(): Int = when (this) {
    CaptureFailure.NOT_UNDERSTOOD -> R.string.capture_error_not_understood
    CaptureFailure.NO_PERMISSION -> R.string.capture_error_permission
    CaptureFailure.NO_NETWORK -> R.string.capture_error_network
    CaptureFailure.UNAVAILABLE -> R.string.capture_error_unavailable
    CaptureFailure.NOT_SAVED -> R.string.capture_error_not_saved
}

/** A clear reason and the action that fixes it: retry, or open settings for the microphone permission. */
@Composable
fun ErrorContent(failure: CaptureFailure, onRetry: () -> Unit, onOpenSettings: () -> Unit, onType: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(rememberWatchInsets()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Icon(Icons.Rounded.MicOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(stringResource(failure.message()), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onRetry, label = { Text(stringResource(R.string.capture_try_again)) }, modifier = Modifier.fillMaxWidth())
        if (failure == CaptureFailure.NO_PERMISSION) {
            FilledTonalButton(onClick = onOpenSettings, label = {
                Text(stringResource(R.string.capture_open_settings))
            }, modifier = Modifier.fillMaxWidth())
        }
        onType?.let { type ->
            FilledTonalButton(onClick = type, label = {
                Text(stringResource(R.string.capture_type_debug))
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}
