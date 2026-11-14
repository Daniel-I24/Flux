package com.flux.core.data.speech

import android.os.SystemClock
import android.speech.SpeechRecognizer

/**
 * One recognition session per process. Wear's recognition service accepts a single client:
 * a second startListening while the first is still shutting down is ignored ("capacity is
 * full") and the user saw "no disponible". Opening a session first destroys any other one and
 * waits until the service had time to release it. Main thread only.
 */
internal object SpeechSessionGate {
    /** Time the recognition service needs to close a destroyed session. */
    const val SETTLE_MS = 600L

    private var active: SpeechRecognizer? = null
    private var releasedAt = 0L

    /** Destroys whatever session is open and returns how long to wait before starting a new one. */
    fun claim(): Long {
        close(active)
        val elapsed = SystemClock.elapsedRealtime() - releasedAt
        return (SETTLE_MS - elapsed).coerceIn(0L, SETTLE_MS)
    }

    fun opened(recognizer: SpeechRecognizer) {
        active = recognizer
    }

    fun close(recognizer: SpeechRecognizer?) {
        recognizer ?: return
        runCatching { recognizer.cancel() }
        runCatching { recognizer.destroy() }
        if (recognizer === active) active = null
        releasedAt = SystemClock.elapsedRealtime()
    }
}
