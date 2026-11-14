package com.flux.mobile.ui.common

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.model.ThemePalette
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class ThemePickerTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `both themes are offered and picking one reports it`() {
        var picked = ThemePalette.ANDEAN_SUNSET
        compose.setContent { FluxTheme(palette = picked) { ThemePicker(picked, { picked = it }, dark = false) } }
        compose.onNode(hasText("Atardecer andino")).assertIsDisplayed()
        compose.onNodeWithText("Selva nocturna").assertIsDisplayed().performClick()
        assertEquals(ThemePalette.NIGHT_JUNGLE, picked)
    }

    @Test
    fun `the current theme is marked as selected`() {
        compose.setContent { FluxTheme { ThemePicker(ThemePalette.NIGHT_JUNGLE, {}, dark = true) } }
        compose.onNode(hasText("Selva nocturna", substring = true), useUnmergedTree = false).assertIsDisplayed()
    }
}
