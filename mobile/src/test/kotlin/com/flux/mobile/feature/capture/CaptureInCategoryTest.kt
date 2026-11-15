package com.flux.mobile.feature.capture

import com.flux.core.data.speech.VoiceRecognizer
import com.flux.core.data.speech.VoiceState
import com.flux.core.domain.usecase.CaptureNoteUseCase
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ResolveAmbiguityUseCase
import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Emulator finding: inside a category the category question was gone but the countdown never started. */
@OptIn(ExperimentalCoroutinesApi::class)
class CaptureInCategoryTest {
    private val dispatcher = StandardTestDispatcher()
    private val recognizer = mockk<VoiceRecognizer>(relaxed = true)
    private val capture = mockk<CaptureNoteUseCase>()
    private val categories = mockk<ObserveCategoriesUseCase>()
    private lateinit var viewModel: PhoneCaptureViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { recognizer.state } returns MutableStateFlow<VoiceState>(VoiceState.Idle)
        every { recognizer.level } returns MutableStateFlow(0f)
        every { categories() } returns flowOf(DefaultCategory.all())
        val question = Ambiguity.CategoryChoice(listOf("shopping", "tasks"))
        coEvery { capture.classify(any()) } returns FluxResult.Success(
            Classification("shopping", "Comprar aceite", confidence = 0.6, engine = ClassifierEngine.LOCAL, ambiguities = listOf(question)),
        )
        coEvery { capture.save(any(), any(), any(), any()) } returns mockk()
        viewModel = PhoneCaptureViewModel(recognizer, categories, capture, ResolveAmbiguityUseCase())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a note created inside a category saves itself there when the countdown ends`() = runTest(dispatcher) {
        advanceUntilIdle()
        viewModel.fixCategory("tasks")
        viewModel.setText("comprar aceite")
        viewModel.classify()
        advanceTimeBy(1)
        assertEquals(true, viewModel.uiState.value.counting)
        advanceTimeBy(COUNTDOWN_MS)
        advanceTimeBy(1)
        assertEquals(CapturePhase.SAVED, viewModel.uiState.value.phase)
        coVerify(exactly = 1) { capture.save("comprar aceite", match { it.categoryId == "tasks" }, true, null) }
    }

    private companion object {
        const val COUNTDOWN_MS = PhoneCaptureUi.AUTO_SAVE_SECONDS * 1_000L
    }
}
