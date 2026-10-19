package com.flux.wear.feature.capture

import android.app.Activity
import android.app.RemoteInput
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.input.RemoteInputIntentHelper
import com.flux.wear.R

private const val SPEECH_LANGUAGE = "es-CO"
private const val TEXT_KEY = "flux_note_text"

class TextInputLaunchers(val dictate: () -> Unit, val type: () -> Unit)

/**
 * Native dictation (RecognizerIntent, es-CO). When no recognizer is available the
 * keyboard opens instead (RemoteInput with quick replies), so capture never dead-ends.
 */
@Composable
fun rememberTextInputLaunchers(onSpeech: (String?) -> Unit, onText: (String?) -> Unit): TextInputLaunchers {
    val prompt = stringResource(R.string.capture_prompt)
    val quickReplies = arrayOf<CharSequence>(
        stringResource(R.string.capture_quick_remind),
        stringResource(R.string.capture_quick_buy),
        stringResource(R.string.capture_quick_call),
    )
    val keyboard = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.let { RemoteInput.getResultsFromIntent(it)?.getCharSequence(TEXT_KEY)?.toString() }
        onText(text.takeIf { result.resultCode == Activity.RESULT_OK })
    }
    val openKeyboard = {
        val input = RemoteInput.Builder(TEXT_KEY).setLabel(prompt).setChoices(quickReplies).build()
        val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
        RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(input))
        keyboard.launch(intent)
    }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        onSpeech(text.takeIf { result.resultCode == Activity.RESULT_OK })
    }
    val dictate = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, SPEECH_LANGUAGE)
            .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        try {
            speech.launch(intent)
        } catch (_: ActivityNotFoundException) {
            openKeyboard()
        }
    }
    return TextInputLaunchers(dictate = dictate, type = openKeyboard)
}
