package com.flux.wear.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.flux.core.designsystem.motion.FluxMotion
import com.flux.wear.ui.theme.FluxWearTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "es-w227dp-h227dp-round", application = Application::class)
class WatchIntroTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `the opening animation is announced and then shows the home screen`() {
        var done = false
        compose.mainClock.autoAdvance = false
        compose.setContent { FluxWearTheme { WatchIntro(onDone = { done = true }) } }
        compose.onNodeWithContentDescription("FLUX se está abriendo").assertExists()
        compose.mainClock.advanceTimeBy(FluxMotion.MEDIUM_MS.toLong())
        assertFalse(done)
        compose.mainClock.advanceTimeBy(FluxMotion.LONG_MS.toLong())
        assertTrue(done)
    }
}
