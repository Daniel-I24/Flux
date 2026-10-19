package com.flux.wear.feature.capture

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.wear.ui.theme.FluxWearTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Small round watch (227 dp), Spanish locale. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "es-w227dp-h227dp-round", application = Application::class)
class PreviewUiTest {
    @get:Rule val compose = createComposeRule()

    private val preview = CaptureState.Preview(
        text = "Recuérdame tomar agua",
        classification = Classification("reminders", "Tomar agua", confidence = 0.9, engine = ClassifierEngine.LOCAL),
        secondsLeft = 3,
        corrected = false,
    )

    private fun show(events: MutableList<CaptureEvent>) = compose.setContent {
        FluxWearTheme { PreviewContent(preview, DefaultCategory.all(), onEvent = { events += it }, onEditText = {}) }
    }

    @Test
    fun shows_category_title_countdown_and_actions() {
        val events = mutableListOf<CaptureEvent>()
        show(events)
        compose.onNodeWithText("Recordatorios").assertIsDisplayed()
        compose.onNodeWithText("Tomar agua").assertIsDisplayed()
        compose.onNodeWithText("Se guarda en 3 s").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Guardar").performScrollTo().performClick()
        assertEquals(listOf<CaptureEvent>(CaptureEvent.Confirm), events)
    }

    @Test
    fun category_can_be_changed() {
        val events = mutableListOf<CaptureEvent>()
        show(events)
        compose.onNodeWithText("Recordatorios").performClick()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Salud"))
        compose.onNodeWithText("Salud").performClick()
        assertEquals(CaptureEvent.ChangeCategory("health"), events.single())
    }
}
