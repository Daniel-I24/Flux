package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeCategoryRepository
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FakeProfileRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.SequentialIds
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureNoteUseCaseTest {
    private val notes = FakeNoteRepository()
    private val queued = mutableListOf<String>()
    private val clock = FixedClock()

    private fun capture(engine: ClassifierEngine): CaptureNoteUseCase {
        val classifier = object : NoteClassifier {
            override val engine = engine
            override suspend fun classify(request: ClassificationRequest) =
                FluxResult.Success(Classification(categoryId = "shopping", title = "Comprar pan", confidence = 0.8, engine = engine))
        }
        val classify = ClassifyNoteUseCase(listOf(classifier), FakeCategoryRepository(), FakeProfileRepository(), clock)
        return CaptureNoteUseCase(classify, CreateNoteUseCase(notes, FakeAlarms(), clock, SequentialIds()), queued::add)
    }

    @Test
    fun `a locally classified note is saved and queued for the remote AI`() = runTest {
        val note = (capture(ClassifierEngine.LOCAL)("  comprar pan ") as FluxResult.Success).value
        assertEquals(DeviceSource.PHONE, note.source)
        assertEquals("comprar pan", note.text)
        assertEquals(listOf(note.id), queued)
        assertTrue(notes.getNote(note.id) != null)
    }

    @Test
    fun `a remotely classified note is not queued`() = runTest {
        capture(ClassifierEngine.REMOTE)("comprar pan")
        assertTrue(queued.isEmpty())
    }

    @Test
    fun `blank text is rejected`() = runTest {
        assertEquals(FluxResult.Failure(AppError.InvalidInput), capture(ClassifierEngine.LOCAL)("   "))
    }
}
