package com.flux.wear.feature.capture

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.flux.core.model.Ambiguity
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

    private fun show(events: MutableList<CaptureEvent>, state: CaptureState.Preview = preview) = compose.setContent {
        FluxWearTheme { PreviewContent(state, DefaultCategory.all(), onEvent = { events += it }, onEditText = null) }
    }

    @Test
    fun shows_category_title_countdown_and_actions() {
        val events = mutableListOf<CaptureEvent>()
        show(events)
        compose.onNodeWithText("Recordatorios").assertIsDisplayed()
        compose.onNodeWithText("Tomar agua").assertIsDisplayed()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Se guarda en 3 s"))
        compose.onNodeWithText("Se guarda en 3 s").assertIsDisplayed()
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription("Guardar"))
        compose.onNodeWithContentDescription("Guardar").performClick()
        assertEquals(listOf<CaptureEvent>(CaptureEvent.Confirm), events)
    }

    @Test
    fun release_builds_offer_dictating_again_instead_of_typing() {
        show(mutableListOf())
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription("Dictar de nuevo"))
        compose.onNodeWithContentDescription("Dictar de nuevo").assertIsDisplayed()
    }

    @Test
    fun an_ambiguous_category_is_asked_with_large_buttons() {
        val events = mutableListOf<CaptureEvent>()
        val question = preview.copy(
            classification = preview.classification.copy(
                categoryId = "shopping",
                ambiguities = listOf(Ambiguity.CategoryChoice(listOf("shopping", "health"))),
            ),
            paused = true,
        )
        show(events, question)
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("¿En qué categoría?"))
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Salud"))
        compose.onNodeWithText("Salud").performClick()
        assertEquals(CaptureEvent.AnswerCategory("health"), events.single())
    }

    @Test
    fun an_incomplete_reminder_offers_suggestions_another_time_or_none() {
        val events = mutableListOf<CaptureEvent>()
        val question = preview.copy(
            classification = preview.classification.copy(ambiguities = listOf(Ambiguity.ReminderTime(listOf(System.currentTimeMillis())))),
            paused = true,
        )
        show(events, question)
        listOf("¿Cuándo te aviso?", "Elegir fecha y hora", "Sin recordatorio").forEach {
            compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(it))
        }
        compose.onNodeWithText("Sin recordatorio").performClick()
        assertEquals(CaptureEvent.AnswerReminder(null), events.single())
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
