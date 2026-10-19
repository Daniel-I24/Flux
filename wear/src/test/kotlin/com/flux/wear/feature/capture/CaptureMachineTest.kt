package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureMachineTest {
    private val classification = Classification("reminders", "Tomar agua", confidence = 0.9, engine = ClassifierEngine.LOCAL)

    private class FakeEffects(private val saveOk: Boolean = true) : CaptureEffects {
        var saved: Triple<String, Classification, Boolean>? = null
        override suspend fun classify(
            text: String
        ) = Classification("reminders", "Tomar agua", confidence = 0.9, engine = ClassifierEngine.LOCAL)
        override suspend fun save(text: String, classification: Classification, corrected: Boolean): SaveResult {
            saved = Triple(text, classification, corrected)
            val note = Note(
                id = "n1",
                text = text,
                title = classification.title,
                categoryId = classification.categoryId,
                source = DeviceSource.WATCH,
                engine = classification.engine,
                createdAt = 1,
                updatedAt = 1,
            )
            return SaveResult(note.takeIf { saveOk }, queued = true)
        }
    }

    private fun TestScope.machine(effects: FakeEffects = FakeEffects()) = CaptureMachine(backgroundScope, effects)

    @Test
    fun `pure transitions follow the capture flow`() {
        val listening = CaptureState.Idle.on(CaptureEvent.StartDictation)
        assertEquals(CaptureState.Listening, listening)
        assertEquals(CaptureState.Error(CaptureFailure.NO_SPEECH), listening.on(CaptureEvent.SpeechResult("  ")))
        val processing = listening.on(CaptureEvent.SpeechResult("tomar agua"))
        assertEquals(CaptureState.Processing("tomar agua"), processing)
        val preview = processing.on(CaptureEvent.Classified(classification)) as CaptureState.Preview
        assertEquals(CaptureState.AUTO_SAVE_SECONDS, preview.secondsLeft)
        assertEquals(CaptureState.Idle, preview.on(CaptureEvent.Cancel))
        assertTrue(preview.on(CaptureEvent.Confirm) is CaptureState.Saving)
    }

    @Test
    fun `impossible events keep the current state`() {
        assertEquals(CaptureState.Idle, CaptureState.Idle.on(CaptureEvent.Confirm))
        assertEquals(CaptureState.Listening, CaptureState.Listening.on(CaptureEvent.Tick))
    }

    @Test
    fun `changing the category marks the correction and pauses auto-save`() {
        val preview = CaptureState.Preview("x", classification, 3, corrected = false)
        val changed = preview.on(CaptureEvent.ChangeCategory("health")) as CaptureState.Preview
        assertEquals("health", changed.classification.categoryId)
        assertTrue(changed.corrected && changed.paused)
        assertEquals(changed, changed.on(CaptureEvent.Tick))
    }

    @Test
    fun `auto-saves after five seconds`() = runTest {
        val effects = FakeEffects()
        val machine = machine(effects)
        machine.send(CaptureEvent.TextEntered("tomar agua"))
        runCurrent()
        assertTrue(machine.state.value is CaptureState.Preview)
        advanceTimeBy(4_500)
        assertTrue(machine.state.value is CaptureState.Preview)
        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(
            CaptureState.Saved(
                effects.saved!!.let { _ ->
                    (machine.state.value as CaptureState.Saved).note
                },
                queued = true
            ),
            machine.state.value
        )
        assertEquals("tomar agua", effects.saved?.first)
    }

    @Test
    fun `confirming saves immediately with the user's correction`() = runTest {
        val effects = FakeEffects()
        val machine = machine(effects)
        machine.send(CaptureEvent.TextEntered("tomar agua"))
        runCurrent()
        machine.send(CaptureEvent.ChangeCategory("health"))
        machine.send(CaptureEvent.Confirm)
        runCurrent()
        assertEquals("health", effects.saved?.second?.categoryId)
        assertEquals(true, effects.saved?.third)
    }

    @Test
    fun `a failed save ends in an error state`() = runTest {
        val machine = machine(FakeEffects(saveOk = false))
        machine.send(CaptureEvent.TextEntered("tomar agua"))
        runCurrent()
        machine.send(CaptureEvent.Confirm)
        runCurrent()
        assertEquals(CaptureState.Error(CaptureFailure.NOT_SAVED), machine.state.value)
    }
}
