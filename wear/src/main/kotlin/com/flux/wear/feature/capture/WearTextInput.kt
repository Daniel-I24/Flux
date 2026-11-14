package com.flux.wear.feature.capture

import android.app.RemoteInput
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.wear.input.RemoteInputIntentHelper
import com.flux.core.data.speech.VoiceErrorPolicy

/** The watch's system input screens, used when FLUX cannot listen by itself. */
object WearTextInput {
    const val KEY = "flux_note_text"

    fun dictationIntent(prompt: String): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceErrorPolicy.PRIMARY_LANGUAGE)
        .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)

    /** Wear's input chooser: voice, keyboard or handwriting, whatever the watch offers. */
    fun remoteInputIntent(prompt: String): Intent = RemoteInputIntentHelper.createActionRemoteInputIntent().also {
        RemoteInputIntentHelper.putRemoteInputsExtra(it, listOf(RemoteInput.Builder(KEY).setLabel(prompt).build()))
    }

    /** False when no activity on the watch handles [intent]. */
    fun launch(launcher: (Intent) -> Unit, intent: Intent): Boolean = try {
        launcher(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
