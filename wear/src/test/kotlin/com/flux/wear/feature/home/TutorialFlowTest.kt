package com.flux.wear.feature.home

import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import com.flux.wear.feature.settings.WatchSettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TutorialFlowTest {
    private val store = mockk<WatchStateStore>(relaxed = true)

    private fun homeViewModel() = HomeViewModel(
        launcher = mockk(relaxed = true),
        monitor = mockk(relaxed = true),
        linkRequests = mockk(relaxed = true),
        observeSections = mockk(relaxed = true),
        outbox = mockk(relaxed = true),
        store = store,
    )

    private fun settingsViewModel() = WatchSettingsViewModel(store, mockk(relaxed = true), mockk(relaxed = true))

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `leaves the tutorial only after the save finished`() = runTest {
        val saving = CompletableDeferred<Unit>()
        coEvery { store.updateSettings(any()) } coAnswers { saving.await() }
        var left = false
        settingsViewModel().finishTutorial { left = true }
        assertFalse(left)
        saving.complete(Unit)
        assertTrue(left)
    }

    @Test
    fun `tapping twice saves and leaves once`() = runTest {
        var leaves = 0
        val viewModel = settingsViewModel()
        repeat(2) { viewModel.finishTutorial { leaves++ } }
        assertEquals(1, leaves)
        coVerify(exactly = 1) { store.updateSettings(any()) }
    }

    @Test
    fun `home reads the saved value each time it decides`() = runTest {
        coEvery { store.settings() } returnsMany listOf(WatchSettings(tutorialDone = false), WatchSettings(tutorialDone = true))
        val viewModel = homeViewModel()
        assertTrue(viewModel.shouldShowTutorial())
        assertFalse(viewModel.shouldShowTutorial())
    }
}
