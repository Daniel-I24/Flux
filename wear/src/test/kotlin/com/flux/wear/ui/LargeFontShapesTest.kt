package com.flux.wear.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.flux.wear.feature.home.AlertPreview
import com.flux.wear.feature.home.HomePreview
import com.flux.wear.ui.preview.LARGE_FONT_SCALE
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** With a large system font, the main actions stay visible and inside the smallest round and square screens. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class LargeFontShapesTest {
    @get:Rule val compose = createComposeRule()

    private fun showWithLargeFont(content: @Composable () -> Unit) = compose.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, LARGE_FONT_SCALE)) { content() }
    }

    private fun SemanticsNodeInteraction.assertInsideScreen() {
        assertIsDisplayed()
        val node = getBoundsInRoot()
        val root = compose.onRoot().getBoundsInRoot()
        assertTrue(node.left >= root.left && node.right <= root.right && node.top >= root.top && node.bottom <= root.bottom)
    }

    @Test
    @Config(qualifiers = "es-w192dp-h192dp-round")
    fun `home keeps the dictate button on a small round screen`() {
        showWithLargeFont { HomePreview() }
        compose.onNodeWithContentDescription("Dictar nota").assertInsideScreen()
    }

    @Test
    @Config(qualifiers = "es-w180dp-h180dp-notround")
    fun `home keeps the dictate button on a square screen`() {
        showWithLargeFont { HomePreview() }
        compose.onNodeWithContentDescription("Dictar nota").assertInsideScreen()
    }

    @Test
    @Config(qualifiers = "es-w192dp-h192dp-round")
    fun `the alarm keeps its title on a small round screen`() {
        showWithLargeFont { AlertPreview() }
        compose.onNodeWithText("Cita odontológica").assertInsideScreen()
    }
}
