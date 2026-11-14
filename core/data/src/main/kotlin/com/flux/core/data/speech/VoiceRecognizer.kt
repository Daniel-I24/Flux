package com.flux.core.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface VoiceState {
    data object Idle : VoiceState
    data class Listening(val partial: String = "") : VoiceState
    data class Heard(val text: String) : VoiceState
    data class Failed(val error: VoiceError) : VoiceState
}

/**
 * SpeechRecognizer in Colombian Spanish, then Spain Spanish ([VoiceErrorPolicy]). Each session
 * has its own listener: callbacks of a destroyed session are ignored, and [SpeechSessionGate]
 * guarantees one open session at a time. The voice level (0–1) is a separate flow so the lava
 * reacts without recomposing. Main thread only; call [release] when the screen goes away.
 */
class VoiceRecognizer @Inject constructor(@param:ApplicationContext private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var session = 0
    private var busyRetries = 0
    private val mutableState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    private val mutableLevel = MutableStateFlow(0f)

    val state: StateFlow<VoiceState> = mutableState.asStateFlow()
    val level: StateFlow<Float> = mutableLevel.asStateFlow()

    /** False when the device has no recognition service: the UI uses the system dictation instead. */
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        busyRetries = 0
        open(VoiceErrorPolicy.PRIMARY_LANGUAGE)
    }

    /** Stops listening and keeps what was heard so far (the recognizer still delivers a result). */
    fun stop() {
        recognizer?.stopListening()
    }

    fun release() {
        session++
        handler.removeCallbacksAndMessages(null)
        SpeechSessionGate.close(recognizer)
        recognizer = null
        mutableLevel.value = 0f
        mutableState.value = VoiceState.Idle
    }

    private fun open(language: String) {
        val id = ++session
        handler.removeCallbacksAndMessages(null)
        SpeechSessionGate.close(recognizer)
        recognizer = null
        mutableState.value = VoiceState.Listening()
        handler.postDelayed({ if (id == session) listen(id, language) }, SpeechSessionGate.claim())
    }

    private fun listen(id: Int, language: String) {
        val created = runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
            ?: return finish(id, VoiceState.Failed(VoiceError.UNAVAILABLE))
        recognizer = created
        SpeechSessionGate.opened(created)
        created.setRecognitionListener(Listener(id, language))
        created.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
                // Online recognition is allowed, so a missing offline pack is never an error by itself.
                .putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1),
        )
    }

    private fun finish(id: Int, state: VoiceState) {
        if (id != session) return
        session++
        SpeechSessionGate.close(recognizer)
        recognizer = null
        mutableLevel.value = 0f
        mutableState.value = state
    }

    private inner class Listener(private val id: Int, private val language: String) : RecognitionListener {
        private val current get() = id == session

        override fun onRmsChanged(rmsdB: Float) {
            if (current) mutableLevel.value = ((rmsdB - RMS_MIN) / (RMS_MAX - RMS_MIN)).coerceIn(0f, 1f)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults.firstText() ?: return
            if (current) mutableState.value = VoiceState.Listening(text)
        }

        override fun onResults(results: Bundle?) {
            val text = results.firstText()
            finish(id, if (text == null) VoiceState.Failed(VoiceError.NOT_UNDERSTOOD) else VoiceState.Heard(text))
        }

        override fun onError(error: Int) {
            if (!current) return
            when (val recovery = VoiceErrorPolicy.recover(error, language, busyRetries)) {
                is VoiceRecovery.Restart -> {
                    if (recovery.language == language) busyRetries++
                    open(recovery.language)
                }
                is VoiceRecovery.Fail -> finish(id, VoiceState.Failed(recovery.error))
            }
        }

        override fun onEndOfSpeech() {
            if (current) mutableLevel.value = 0f
        }

        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun Bundle?.firstText(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }

    private companion object {
        const val RMS_MIN = -2f
        const val RMS_MAX = 10f
    }
}
