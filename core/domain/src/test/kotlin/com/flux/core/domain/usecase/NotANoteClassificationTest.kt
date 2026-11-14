package com.flux.core.domain.usecase

import com.flux.core.domain.FakeCategoryRepository
import com.flux.core.domain.FakeProfileRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.domain.port.NoteIntentDetector
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotANoteClassificationTest {
    private fun classifier(isNote: Boolean) = object : NoteClassifier {
        override val engine = ClassifierEngine.REMOTE
        override suspend fun classify(request: ClassificationRequest) = FluxResult.Success(
            Classification(
                categoryId = "tasks",
                title = "Hola",
                dueAt = DUE,
                requiresAlarm = true,
                confidence = 0.9,
                engine = engine,
                isNote = isNote
            ),
        )
    }

    private fun useCase(aiSaysNote: Boolean, detector: NoteIntentDetector) = ClassifyNoteUseCase(
        listOf(classifier(aiSaysNote)),
        FakeCategoryRepository(),
        FakeProfileRepository(),
        FixedClock(),
        AskReminderTimeUseCase({ _, _ -> null }, FixedClock()),
        detector,
    )

    private suspend fun ClassifyNoteUseCase.result(text: String) = (invoke(text) as FluxResult.Success).value

    @Test
    fun `small talk caught on the device is not a note, whatever the AI says`() = runTest {
        val result = useCase(aiSaysNote = true) { false }.result("Hola, ¿cómo estás?")
        assertFalse(result.isNote)
        assertNull(result.dueAt)
        assertFalse(result.requiresAlarm)
    }

    @Test
    fun `small talk caught by the AI is not a note`() = runTest {
        assertFalse(useCase(aiSaysNote = false) { true }.result("¿qué más, parcero?").isNote)
    }

    @Test
    fun `a real note keeps its date and alarm`() = runTest {
        val result = useCase(aiSaysNote = true) { true }.result("llamar a mamá a las 8")
        assertTrue(result.isNote)
        assertEquals(DUE, result.dueAt)
    }

    private companion object {
        const val DUE = 1_000_000L
    }
}
