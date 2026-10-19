package com.flux.mobile.feature

import android.app.Application
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.model.ThemeMode
import com.flux.mobile.feature.settings.AccountSettings
import com.flux.mobile.feature.settings.AiSettings
import com.flux.mobile.feature.settings.AiStatus
import com.flux.mobile.feature.settings.AppearanceSettings
import com.flux.mobile.feature.settings.DemoSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class SettingsUiTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `theme selection reports the chosen mode`() {
        var chosen: ThemeMode? = null
        compose.setContent { FluxTheme { AppearanceSettings(ThemeMode.SYSTEM, onTheme = { chosen = it }) } }
        compose.onNodeWithText("Sistema").assertIsSelected()
        compose.onNodeWithText("Oscuro").performClick()
        assertEquals(ThemeMode.DARK, chosen)
    }

    @Test
    fun `demo mode explains the 10 second alarm and loads the examples`() {
        var loaded = false
        compose.setContent { FluxTheme { DemoSettings(onLoad = { loaded = true }, onClear = {}) } }
        compose.onNodeWithText("suena en 10 segundos", substring = true).assertExists()
        compose.onNodeWithText("Cargar ejemplos").performClick()
        assertTrue(loaded)
    }

    @Test
    fun `the AI section tells when FLUX falls back to the local classifier`() {
        compose.setContent { FluxTheme { AiSettings(AiStatus.LOCAL, onRefresh = {}) } }
        compose.onNodeWithText("IA local", substring = true).assertExists()
    }

    @Test
    fun `signing out asks for confirmation first`() {
        var signedOut = false
        compose.setContent {
            FluxTheme {
                AccountSettings("ana@example.com", localAccount = false, onProfile = {}, onRoutine = {}, onSignOut = {
                    signedOut =
                        true
                })
            }
        }
        compose.onNodeWithText("Cerrar sesión").performClick()
        assertTrue(!signedOut)
        compose.onNodeWithText("Tus notas se quedan", substring = true).assertExists()
    }
}
