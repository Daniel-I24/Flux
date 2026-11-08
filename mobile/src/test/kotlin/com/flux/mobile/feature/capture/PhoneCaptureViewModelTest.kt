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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneCaptureViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val voice = MutableStateFlow<VoiceState>(VoiceState.Idle)
    private val recognizer = mockk<VoiceRecognizer>(relaxed = true)
    private val capture = mockk<CaptureNoteUseCase>()
    private val categories = mockk<ObserveCategoriesUseCase>()
    private val clear = Classification("shopping", "Comprar pan", confidence = 0.9, engine = ClassifierEngine.LOCAL)
    private lateinit var viewModel: PhoneCaptureViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { recognizer.state } returns voice
        every { recognizer.level } returns MutableStateFlow(0f)
        every { categories() } returns flowOf(DefaultCategory.all())
        coEvery { capture.classify(any()) } returns FluxResult.Success(clear)
        coEvery { capture.save(any(), any(), any()) } returns mockk()
        viewModel = PhoneCaptureViewModel(recognizer, categories, capture, ResolveAmbiguityUseCase())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.classified(text: String = "comprar pan") {
        advanceUntilIdle()
        viewModel.setText(text)
        viewModel.classify()
        advanceTimeBy(1)
    }

    @Test
    fun `without questions it saves by itself when the countdown ends`() = runTest(dispatcher) {
        classified()
        assertEquals(CapturePhase.PREVIEW, viewModel.uiState.value.phase)
        advanceTimeBy(COUNTDOWN_MS)
        advanceTimeBy(1)
        assertEquals(CapturePhase.SAVED, viewModel.uiState.value.phase)
        coVerify(exactly = 1) { capture.save("comprar pan", any(), false) }
    }

    @Test
    fun `a question stops the countdown until it is answered`() = runTest(dispatcher) {
        val question = Ambiguity.CategoryChoice(listOf("shopping", "health"))
        coEvery { capture.classify(any()) } returns FluxResult.Success(clear.copy(ambiguities = listOf(question)))
        classified("comprar medicamentos")
        advanceTimeBy(COUNTDOWN_MS * 2)
        assertFalse(viewModel.uiState.value.counting)
        coVerify(exactly = 0) { capture.save(any(), any(), any()) }
        viewModel.answerCategory("health")
        assertEquals("health", viewModel.uiState.value.classification?.categoryId)
        assertEquals(true, viewModel.uiState.value.corrected)
        assertEquals(true, viewModel.uiState.value.counting)
    }

    @Test
    fun `an incomplete reminder waits for when to ring and then counts down with the alarm`() = runTest(dispatcher) {
        val asked = clear.copy(ambiguities = listOf(Ambiguity.ReminderTime(listOf(AT))))
        coEvery { capture.classify(any()) } returns FluxResult.Success(asked)
        classified("salir de mi casa a las")
        assertFalse(viewModel.uiState.value.counting)
        viewModel.answerReminder(AT)
        assertEquals(AT, viewModel.uiState.value.classification?.dueAt)
        assertEquals(true, viewModel.uiState.value.classification?.requiresAlarm)
        assertEquals(true, viewModel.uiState.value.counting)
    }

    @Test
    fun `tapping save twice stores one note`() = runTest(dispatcher) {
        classified()
        viewModel.save()
        viewModel.save()
        advanceUntilIdle()
        coVerify(exactly = 1) { capture.save(any(), any(), any()) }
    }

    @Test
    fun `closing the sheet cancels the countdown and the next capture starts clean`() = runTest(dispatcher) {
        classified()
        viewModel.reset()
        advanceUntilIdle()
        coVerify(exactly = 0) { capture.save(any(), any(), any()) }
        verify { recognizer.release() }
        assertEquals(PhoneCaptureUi(), viewModel.uiState.value)
    }

    @Test
    fun `after saving and closing, a new note can be captured`() = runTest(dispatcher) {
        classified()
        viewModel.save()
        advanceUntilIdle()
        viewModel.reset()
        classified("otra nota")
        assertEquals(CapturePhase.PREVIEW, viewModel.uiState.value.phase)
        viewModel.save()
        advanceUntilIdle()
        coVerify(exactly = 2) { capture.save(any(), any(), any()) }
    }

    @Test
    fun `what the recognizer heard is classified`() = runTest(dispatcher) {
        advanceUntilIdle()
        voice.value = VoiceState.Heard("llamar a mamá")
        advanceTimeBy(1)
        assertEquals("llamar a mamá", viewModel.uiState.value.text)
        assertEquals(CapturePhase.PREVIEW, viewModel.uiState.value.phase)
    }

    private companion object {
        const val COUNTDOWN_MS = PhoneCaptureUi.AUTO_SAVE_SECONDS * 1_000L
        const val AT = 1_762_380_000_000L
    }
}
