package com.flux.wear.feature.capture

import android.Manifest
import android.app.Activity
import android.app.RemoteInput
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.wear.input.RemoteInputIntentHelper
import com.flux.core.data.speech.VoiceRecognizer
import com.flux.wear.BuildConfig
import com.flux.wear.R

private const val TEXT_KEY = "flux_note_text"

/** [type] is null in release builds: notes are created only by voice there. */
class VoiceInputs(val dictate: () -> Unit, val type: (() -> Unit)?, val openPermissionSettings: () -> Unit)

/**
 * Voice first: SpeechRecognizer inside FLUX (with the lava) after asking for the microphone;
 * the system dictation screen (RecognizerIntent) when the watch has no recognition service.
 * The keyboard exists only in debug builds, for emulators without a microphone.
 */
@Composable
fun rememberVoiceInputs(viewModel: CaptureViewModel): VoiceInputs {
    val context = LocalContext.current
    val prompt = stringResource(R.string.capture_prompt)
    val systemDictation = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        viewModel.send(if (result.resultCode == Activity.RESULT_OK) CaptureEvent.SpeechResult(text) else CaptureEvent.Cancel)
    }
    val startListening = {
        if (viewModel.canListenInApp()) {
            viewModel.listen()
        } else {
            viewModel.beginDictation()
            if (!launchSystemDictation(prompt) { systemDictation.launch(it) }) {
                viewModel.send(CaptureEvent.SpeechFailed(CaptureFailure.UNAVAILABLE))
            }
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startListening() else viewModel.send(CaptureEvent.SpeechFailed(CaptureFailure.NO_PERMISSION))
    }
    val keyboard = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(TEXT_KEY)?.toString() }
        viewModel.send(
            if (result.resultCode == Activity.RESULT_OK &&
                !text.isNullOrBlank()
            ) {
                CaptureEvent.TextEntered(text)
            } else {
                CaptureEvent.Cancel
            }
        )
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
            {
                val input = RemoteInput.Builder(TEXT_KEY).setLabel(prompt).build()
                val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
                RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(input))
                keyboard.launch(intent)
            }
        } else {
            null
        },
        openPermissionSettings = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        },
    )
}

private fun Context.hasMicrophonePermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

/** False when nothing on the watch can listen at all (the error screen explains it). */
private fun launchSystemDictation(prompt: String, launch: (Intent) -> Unit): Boolean {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceRecognizer.PRIMARY_LANGUAGE)
        .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
    return try {
        launch(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
