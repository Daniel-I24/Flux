package com.flux.mobile.feature

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.domain.usecase.NoteQuery
import com.flux.core.domain.usecase.NoteSort
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.mobile.feature.notes.NoteRow
import com.flux.mobile.feature.notes.NotesFilterBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class NotesUiTest {
    @get:Rule val compose = createComposeRule()

    private val note = Note(
        id = "n1",
        text = "comprar pan el viernes",
        title = "Comprar pan",
        categoryId = "shopping",
        source = DeviceSource.WATCH,
        engine = ClassifierEngine.LOCAL,
        createdAt = 1,
        updatedAt = 1,
    )

    @Test
    fun `a note row shows its title and category and toggles completion`() {
        var toggled = false
        var opened = false
        compose.setContent {
            FluxTheme {
                NoteRow(note, DefaultCategory.SHOPPING.toCategory(), onToggle = {
                    toggled = true
                }, onOpen = { opened = true }, onDelete = {})
            }
        }
        compose.onNodeWithText("Comprar pan").assertIsDisplayed().performClick()
        compose.onNodeWithText("Compras").assertIsDisplayed()
        assertTrue(opened)
        compose.onNode(isToggleable()).performClick()
        assertTrue(toggled)
    }

    @Test
    fun `the filter bar updates search, pending filter and sort`() {
        var query by mutableStateOf(NoteQuery())
        compose.setContent {
            FluxTheme {
                NotesFilterBar(
                    query = query,
                    categories = DefaultCategory.all(),
                    onText = { query = query.copy(text = it) },
                    onCategory = { query = query.copy(categoryId = it) },
                    onSort = { query = query.copy(sort = it) },
                    onIncludeCompleted = { query = query.copy(includeCompleted = it) },
                )
            }
        }
        compose.onNodeWithText("Buscar notas").performTextInput("pan")
        assertEquals("pan", query.text)
        compose.onNodeWithText("Solo pendientes").performClick().assertIsSelected()
        assertEquals(false, query.includeCompleted)
        compose.onNodeWithContentDescription("Ordenar: Más recientes").performClick()
        compose.onNodeWithText("Por fecha").performClick()
        assertEquals(NoteSort.DUE_DATE, query.sort)
    }
}
