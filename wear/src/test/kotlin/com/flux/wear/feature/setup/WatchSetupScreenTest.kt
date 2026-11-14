package com.flux.wear.feature.setup

import android.app.Application
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.flux.wear.ui.theme.FluxWearTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Small round watch (227 dp), Spanish locale. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "es-w227dp-h227dp-round", application = Application::class)
class WatchSetupScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `lists the watch permissions with their state, the double press and a way out`() {
        var done = false
        compose.setContent { FluxWearTheme { WatchSetupScreen(onDone = { done = true }) } }
        compose.onNodeWithText("Configura FLUX").assertIsDisplayed()
        val list = compose.onNode(hasScrollToNodeAction())
        list.performScrollToNode(hasText("Notificaciones"))
        list.performScrollToNode(hasText("Batería sin restricciones"))
        list.performScrollToNode(hasText("Galaxy Watch", substring = true))
        list.performScrollToNode(hasText("Listo"))
        compose.onNodeWithText("Listo").performClick()
        assertTrue(done)
    }

    @Test
    fun `battery opens a system screen for FLUX`() {
        compose.setContent { FluxWearTheme { WatchSetupScreen(onDone = {}) } }
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Batería sin restricciones"))
        compose.onNodeWithText("Batería sin restricciones").performClick()
        val app = ApplicationProvider.getApplicationContext<Application>()
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, shadowOf(app).nextStartedActivity.action)
    }
}
