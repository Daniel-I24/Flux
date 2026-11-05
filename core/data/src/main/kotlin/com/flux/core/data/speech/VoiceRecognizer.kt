package com.flux.core.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Why listening failed, each with its own message and "Reintentar" in the UI. */
enum class VoiceError { NO_PERMISSION, NO_NETWORK, NOT_UNDERSTOOD, BUSY, UNAVAILABLE }

sealed interface VoiceState {
    data object Idle : VoiceState
    data class Listening(val partial: String = "") : VoiceState
    data class Heard(val text: String) : VoiceState
    data class Failed(val error: VoiceError) : VoiceState
}

/**
 * SpeechRecognizer in Colombian Spanish with Spain Spanish as a fallback language. The voice
 * level (0–1, from onRmsChanged) is a separate flow so the lava can react without recomposing.
 * Must be used from the main thread; call [release] when the screen goes away.
 */
class VoiceRecognizer(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null
    private var languages = LANGUAGES
    private val mutableState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    private val mutableLevel = MutableStateFlow(0f)

    val state: StateFlow<VoiceState> = mutableState.asStateFlow()
    val level: StateFlow<Float> = mutableLevel.asStateFlow()

    /** False when the device has no recognition service: the UI uses RecognizerIntent instead. */
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start() {
        languages = LANGUAGES
        listen(languages.first())
    }

    /** Stops listening and keeps what was heard so far (the recognizer still delivers a result). */
    fun stop() {
        recognizer?.stopListening()
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
        mutableLevel.value = 0f
        mutableState.value = VoiceState.Idle
    }

    private fun listen(language: String) {
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply { setRecognitionListener(listener) }
        mutableState.value = VoiceState.Listening()
        recognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1),
        )
    }

    private fun finish(state: VoiceState) {
        mutableLevel.value = 0f
        mutableState.value = state
        recognizer?.destroy()
        recognizer = null
    }

    private val listener = object : RecognitionListener {
        override fun onRmsChanged(rmsdB: Float) {
            mutableLevel.value = ((rmsdB - RMS_MIN) / (RMS_MAX - RMS_MIN)).coerceIn(0f, 1f)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults.firstText() ?: return
            mutableState.value = VoiceState.Listening(text)
        }

        override fun onResults(results: Bundle?) {
            val text = results.firstText()
            finish(if (text.isNullOrBlank()) VoiceState.Failed(VoiceError.NOT_UNDERSTOOD) else VoiceState.Heard(text))
        }

        override fun onError(error: Int) {
            if (error in LANGUAGE_ERRORS && languages.size > 1) {
                languages = languages.drop(1)
                listen(languages.first())
                return
            }
            finish(VoiceState.Failed(errorOf(error)))
        }

        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() {
            mutableLevel.value = 0f
        }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun Bundle?.firstText(): String? =
        this?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }

    private fun errorOf(code: Int): VoiceError = when (code) {
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceError.NO_PERMISSION
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        SpeechRecognizer.ERROR_SERVER, ERROR_SERVER_DISCONNECTED,
        -> VoiceError.NO_NETWORK
        SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError.NOT_UNDERSTOOD
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY, ERROR_TOO_MANY_REQUESTS -> VoiceError.BUSY
        else -> VoiceError.UNAVAILABLE
    }

    private companion object {
        val LANGUAGES = listOf("es-CO", "es-ES")
        const val RMS_MIN = -2f
        const val RMS_MAX = 10f

        // Constants added in API 31 (kept as literals so the watch, minSdk 30, can use them).
        const val ERROR_TOO_MANY_REQUESTS = 10
        const val ERROR_SERVER_DISCONNECTED = 11
        val LANGUAGE_ERRORS = setOf(12, 13)
    }
}
