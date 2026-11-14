package com.flux.wear.feature.capture

import android.Manifest
import android.app.Activity
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.flux.core.data.speech.VoiceError
import com.flux.wear.BuildConfig
import com.flux.wear.R

/** [type] is null in release builds: notes are created only by voice there. */
class VoiceInputs(val dictate: () -> Unit, val type: (() -> Unit)?, val openPermissionSettings: () -> Unit)

/**
 * Voice first, with fallbacks so "no disponible" appears only when nothing can listen:
 * 1. SpeechRecognizer inside FLUX (with the lava), es-CO then es-ES;
 * 2. the system dictation screen (RecognizerIntent);
 * 3. Wear's own input screen (RemoteInput, voice or keyboard).
 * The keyboard button exists only in debug builds, for emulators without a microphone.
 */
@Composable
fun rememberVoiceInputs(viewModel: CaptureViewModel): VoiceInputs {
    val context = LocalContext.current
    val prompt = stringResource(R.string.capture_prompt)
    val remoteInput = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(WearTextInput.KEY)?.toString() }
        val heard = result.resultCode == Activity.RESULT_OK && !text.isNullOrBlank()
        viewModel.send(if (heard) CaptureEvent.TextEntered(text.orEmpty()) else CaptureEvent.Cancel)
    }
    val systemDictation = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        viewModel.send(if (result.resultCode == Activity.RESULT_OK) CaptureEvent.SpeechResult(text) else CaptureEvent.Cancel)
    }
    val fallback: (VoiceError) -> Unit = { error ->
        viewModel.beginDictation()
        val launched = WearTextInput.launch(systemDictation::launch, WearTextInput.dictationIntent(prompt)) ||
            WearTextInput.launch(remoteInput::launch, WearTextInput.remoteInputIntent(prompt))
        if (!launched) viewModel.send(CaptureEvent.SpeechFailed(error.toFailure()))
    }
    LaunchedEffect(viewModel) { viewModel.systemDictation.collect(fallback) }
    val startListening = { if (viewModel.canListenInApp()) viewModel.listen() else fallback(VoiceError.UNAVAILABLE) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else viewModel.send(CaptureEvent.SpeechFailed(CaptureFailure.NO_PERMISSION))
    }
    return VoiceInputs(
        dictate = {
            if (context.hasMicrophonePermission()) {
                startListening()
            } else {
                viewModel.beginDictation()
                permission.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        type = if (BuildConfig.DEBUG) {
            { WearTextInput.launch(remoteInput::launch, WearTextInput.remoteInputIntent(prompt)) }
        } else {
            null
        },
        openPermissionSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
            runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        },
    )
}

private fun Context.hasMicrophonePermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
