package com.flux.wear.feature.agenda

import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.NoteSections
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.model.Note
import io.mockk.every
import io.mockk.mockk
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val moment = ZonedDateTime.of(2026, 11, 6, 22, 0, 0, 0, bogota)
    private val sections = MutableStateFlow(NoteSections())
    private val observe = mockk<ObserveNoteSectionsUseCase> { every { this@mockk.invoke(any()) } returns sections }
    private val categories = mockk<CategoryRepository> { every { observeCategories() } returns flowOf(emptyList()) }
    private val clock = mockk<Clock> {
        every { now() } returns moment.toInstant().toEpochMilli()
        every { zone() } returns bogota
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun note(id: String, due: ZonedDateTime) = mockk<Note>(relaxed = true) {
        every { this@mockk.id } returns id
        every { dueAt } returns due.toInstant().toEpochMilli()
        every { isActive } returns true
        every { completed } returns false
    }

    @Test
    fun `overdue reminders are not in the agenda and it splits by the current day`() = runTest {
        val viewModel = AgendaViewModel(observe, categories, mockk(), clock)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        val all = listOf(note("tonight", moment.plusHours(1)), note("tomorrow", moment.plusHours(3)), note("gone", moment.minusHours(1)))
        sections.value = NoteSections.of(all, moment.toInstant().toEpochMilli())
        assertEquals(listOf("tonight"), viewModel.uiState.value.today.map { it.id })
        assertEquals(listOf("tomorrow"), viewModel.uiState.value.upcoming.map { it.id })
    }
}
