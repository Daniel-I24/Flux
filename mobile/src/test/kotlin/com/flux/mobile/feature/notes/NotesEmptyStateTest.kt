package com.flux.mobile.feature.notes

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.domain.usecase.NoteQuery
import com.flux.core.domain.usecase.NoteSort
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class NotesEmptyStateTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `search, a category or only pending count as filters, the sort order does not`() {
        assertFalse(NoteQuery().isFiltering())
        assertFalse(NoteQuery(text = "  ", sort = NoteSort.DUE_DATE).isFiltering())
        assertTrue(NoteQuery(text = "pan").isFiltering())
        assertTrue(NoteQuery(categoryId = "shopping").isFiltering())
        assertTrue(NoteQuery(includeCompleted = false).isFiltering())
    }

    @Test
    fun `with filters the empty list asks to change them instead of suggesting a new note`() {
        compose.setContent { FluxTheme { NotesEmptyState(filtered = true) } }
        compose.onNodeWithText("Sin resultados").assertIsDisplayed()
        compose.onNodeWithText("Ninguna nota coincide", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Nueva nota", substring = true).assertDoesNotExist()
    }

    @Test
    fun `without filters the empty list suggests example notes`() {
        compose.setContent { FluxTheme { NotesEmptyState(filtered = false) } }
        compose.onNodeWithText("No hay notas aquí").assertIsDisplayed()
        compose.onNodeWithText("Nueva nota", substring = true).assertIsDisplayed()
    }
}
