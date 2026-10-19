package com.flux.core.domain.usecase

import com.flux.core.domain.FakeCategoryRepository
import com.flux.core.domain.FakeProfileRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassifyNoteUseCaseTest {
    private class FakeClassifier(override val engine: ClassifierEngine, private val result: FluxResult<Classification>) : NoteClassifier {
        var lastRequest: ClassificationRequest? = null
        override suspend fun classify(request: ClassificationRequest): FluxResult<Classification> {
            lastRequest = request
            return result
        }
    }

    private fun success(engine: ClassifierEngine) =
        FluxResult.Success(Classification(categoryId = "tasks", title = "x", confidence = 0.9, engine = engine))

    private fun useCase(vararg strategies: NoteClassifier) =
        ClassifyNoteUseCase(strategies.toList(), FakeCategoryRepository(), FakeProfileRepository(), FixedClock())

    @Test
    fun `uses the remote strategy when it succeeds`() = runTest {
        val remote = FakeClassifier(ClassifierEngine.REMOTE, success(ClassifierEngine.REMOTE))
        val local = FakeClassifier(ClassifierEngine.LOCAL, success(ClassifierEngine.LOCAL))
        val result = useCase(remote, local)("  comprar   pan ")
        assertEquals(ClassifierEngine.REMOTE, (result as FluxResult.Success).value.engine)
        assertEquals("comprar pan", remote.lastRequest?.text)
        assertEquals(null, local.lastRequest)
    }

    @Test
    fun `falls back to local when remote fails`() = runTest {
        val remote = FakeClassifier(ClassifierEngine.REMOTE, FluxResult.Failure(AppError.Timeout))
        val local = FakeClassifier(ClassifierEngine.LOCAL, success(ClassifierEngine.LOCAL))
        val result = useCase(remote, local)("comprar pan")
        assertEquals(ClassifierEngine.LOCAL, (result as FluxResult.Success).value.engine)
        assertTrue(local.lastRequest!!.categories.isNotEmpty())
    }

    @Test
    fun `rejects empty text and reports the last error when all fail`() = runTest {
        val failing = FakeClassifier(ClassifierEngine.REMOTE, FluxResult.Failure(AppError.Network))
        assertEquals(FluxResult.Failure(AppError.InvalidInput), useCase(failing)("   "))
        assertEquals(FluxResult.Failure(AppError.Network), useCase(failing)("hola"))
    }
}
