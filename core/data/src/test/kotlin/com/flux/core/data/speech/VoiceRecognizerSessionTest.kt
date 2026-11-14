package com.flux.core.data.speech

import android.app.Application
import android.os.Bundle
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.test.core.app.ApplicationProvider
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowSpeechRecognizer

/** The log of the Galaxy Watch4: a second session opened while the first was alive ("capacity is full"). */
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(sdk = [35], application = Application::class)
class VoiceRecognizerSessionTest {
    private val recognizer = VoiceRecognizer(ApplicationProvider.getApplicationContext())

    @Before
    fun clean() = ShadowSpeechRecognizer.reset()

    private fun settle() = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(SpeechSessionGate.SETTLE_MS))

    private fun latest() = ShadowSpeechRecognizer.getLatestSpeechRecognizer()

    @Test
    fun `a missing language pack destroys the session before opening the Spain Spanish one`() {
        recognizer.start()
        settle()
        val first = latest()
        shadowOf(first).triggerOnError(LANGUAGE_NOT_SUPPORTED)
        assertTrue(shadowOf(first).isDestroyed)
        settle()
        val second = latest()
        assertNotSame(first, second)
        assertEquals("es-ES", shadowOf(second).lastRecognizerIntent.getStringExtra(RecognizerIntent.EXTRA_LANGUAGE))
        assertEquals(VoiceState.Listening(), recognizer.state.value)
    }

    @Test
    fun `starting again destroys the previous session and ignores its late callbacks`() {
        recognizer.start()
        settle()
        val first = latest()
        recognizer.start()
        assertTrue(shadowOf(first).isDestroyed)
        shadowOf(first).triggerOnError(SpeechRecognizer.ERROR_NO_MATCH)
        assertEquals(VoiceState.Listening(), recognizer.state.value)
        settle()
        val results = Bundle().apply { putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, arrayListOf("comprar pan")) }
        shadowOf(latest()).triggerOnResults(results)
        assertEquals(VoiceState.Heard("comprar pan"), recognizer.state.value)
    }

    @Test
    fun `no speech is reported as not understood`() {
        recognizer.start()
        settle()
        shadowOf(latest()).triggerOnError(SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
        assertEquals(VoiceState.Failed(VoiceError.NOT_UNDERSTOOD), recognizer.state.value)
    }

    private companion object {
        const val LANGUAGE_NOT_SUPPORTED = 12
    }
}
