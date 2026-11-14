package com.flux.core.data.speech

/** Why listening failed, each with its own message in the UI. */
enum class VoiceError {
    NO_PERMISSION,
    NO_NETWORK,
    NOT_UNDERSTOOD,
    BUSY,

    /** No recognizer for Spanish: the UI tries the system dictation before saying so. */
    LANGUAGE,

    /** No usable recognizer: the UI tries the system dictation before saying so. */
    UNAVAILABLE,
    ;

    /** True when the system dictation screen (and on Wear its voice input) should be tried first. */
    val trySystemDictation: Boolean get() = this == LANGUAGE || this == UNAVAILABLE
}

/** What to do after a SpeechRecognizer error. */
sealed interface VoiceRecovery {
    /** Open a fresh session after the service released the previous one. */
    data class Restart(val language: String) : VoiceRecovery
    data class Fail(val error: VoiceError) : VoiceRecovery
}

/**
 * Pure decision table for SpeechRecognizer errors (codes from android.speech.SpeechRecognizer,
 * kept as literals because several arrived after API 30). A missing es-CO pack moves on to
 * es-ES, and a busy service gets one retry: neither is shown to the user.
 */
object VoiceErrorPolicy {
    const val PRIMARY_LANGUAGE = "es-CO"
    const val FALLBACK_LANGUAGE = "es-ES"
    val LANGUAGES = listOf(PRIMARY_LANGUAGE, FALLBACK_LANGUAGE)
    const val MAX_BUSY_RETRIES = 1

    private const val NETWORK_TIMEOUT = 1
    private const val NETWORK = 2
    private const val AUDIO = 3
    private const val SERVER = 4
    private const val CLIENT = 5
    private const val SPEECH_TIMEOUT = 6
    private const val NO_MATCH = 7
    private const val RECOGNIZER_BUSY = 8
    private const val INSUFFICIENT_PERMISSIONS = 9
    private const val TOO_MANY_REQUESTS = 10
    private const val SERVER_DISCONNECTED = 11
    private const val LANGUAGE_NOT_SUPPORTED = 12
    private const val LANGUAGE_UNAVAILABLE = 13

    fun recover(code: Int, language: String, busyRetries: Int): VoiceRecovery = when (code) {
        LANGUAGE_NOT_SUPPORTED, LANGUAGE_UNAVAILABLE -> nextLanguage(language)?.let(VoiceRecovery::Restart)
            ?: VoiceRecovery.Fail(VoiceError.LANGUAGE)
        RECOGNIZER_BUSY, CLIENT, TOO_MANY_REQUESTS, AUDIO -> if (busyRetries < MAX_BUSY_RETRIES) {
            VoiceRecovery.Restart(language)
        } else {
            VoiceRecovery.Fail(VoiceError.BUSY)
        }
        else -> VoiceRecovery.Fail(errorOf(code))
    }

    fun errorOf(code: Int): VoiceError = when (code) {
        INSUFFICIENT_PERMISSIONS -> VoiceError.NO_PERMISSION
        NETWORK, NETWORK_TIMEOUT, SERVER, SERVER_DISCONNECTED -> VoiceError.NO_NETWORK
        NO_MATCH, SPEECH_TIMEOUT -> VoiceError.NOT_UNDERSTOOD
        RECOGNIZER_BUSY, CLIENT, TOO_MANY_REQUESTS, AUDIO -> VoiceError.BUSY
        LANGUAGE_NOT_SUPPORTED, LANGUAGE_UNAVAILABLE -> VoiceError.LANGUAGE
        else -> VoiceError.UNAVAILABLE
    }

    private fun nextLanguage(language: String): String? = LANGUAGES.getOrNull(LANGUAGES.indexOf(language) + 1)
}
