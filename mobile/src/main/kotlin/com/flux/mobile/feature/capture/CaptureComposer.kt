package com.flux.mobile.feature.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.flux.core.data.speech.VoiceError
import com.flux.core.designsystem.component.LavaColors
import com.flux.core.designsystem.component.VoiceLava
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Note
import com.flux.mobile.R

/** Microphone over the lava, the editable text and the voice errors with their way out. */
@Composable
fun CaptureComposer(state: PhoneCaptureUi, viewModel: PhoneCaptureViewModel, onMic: () -> Unit, onOpenSettings: () -> Unit) {
    val listening = state.phase == CapturePhase.LISTENING
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(LAVA_HEIGHT).clip(RoundedCornerShape(FluxSpacing.xl)), contentAlignment = Alignment.Center) {
        VoiceLava(
            active = listening,
            level = { viewModel.level.value },
            colors = LavaColors(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer, scheme.surfaceContainer),
            modifier = Modifier.matchParentSize(),
        )
        FilledIconButton(onClick = if (listening) viewModel::stopListening else onMic, modifier = Modifier.size(FluxSizes.fab)) {
            Icon(
                if (listening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = stringResource(if (listening) R.string.capture_stop else R.string.capture_dictate),
            )
        }
    }
    Text(
        stringResource(if (listening) R.string.capture_listening_phone else R.string.notes_new_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = scheme.onSurfaceVariant,
    )
    OutlinedTextField(
        value = state.text,
        onValueChange = { viewModel.setText(it.take(Note.MAX_TEXT_LENGTH)) },
        placeholder = { Text(stringResource(R.string.capture_example_phrase)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
    )
    state.voiceError?.let { error -> VoiceErrorMessage(error, onRetry = onMic, onOpenSettings = onOpenSettings) }
    state.errorMessage?.let { Text(stringResource(it), color = scheme.error, style = MaterialTheme.typography.bodySmall) }
    Button(onClick = viewModel::classify, enabled = state.text.isNotBlank() && !listening, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_continue))
    }
}

@Composable
private fun VoiceErrorMessage(error: VoiceError, onRetry: () -> Unit, onOpenSettings: () -> Unit) {
    val message = when (error) {
        VoiceError.NO_PERMISSION -> R.string.capture_error_permission
        VoiceError.NO_NETWORK -> R.string.capture_error_network
        VoiceError.NOT_UNDERSTOOD -> R.string.capture_error_not_understood
        VoiceError.BUSY, VoiceError.UNAVAILABLE -> R.string.capture_error_unavailable
    }
    Text(stringResource(message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry_voice)) }
    if (error == VoiceError.NO_PERMISSION) TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.capture_open_settings)) }
}

private val LAVA_HEIGHT = FluxSizes.fab * 3
