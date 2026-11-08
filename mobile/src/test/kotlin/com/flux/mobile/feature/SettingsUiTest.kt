package com.flux.mobile.feature

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.model.ThemeMode
import com.flux.mobile.feature.settings.AccountSettings
import com.flux.mobile.feature.settings.AdvancedSettings
import com.flux.mobile.feature.settings.AiSettings
import com.flux.mobile.feature.settings.AiStatus
import com.flux.mobile.feature.settings.AppearanceSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `demo mode is folded under Avanzado and asks before loading`() {
        var loaded = false
        compose.setContent { FluxTheme { AdvancedSettings(onLoadDemo = { loaded = true }, onClearDemo = {}) } }
        compose.onNodeWithText("Cargar ejemplos").assertDoesNotExist()
        compose.onNodeWithText("Avanzado").performClick()
        compose.onNodeWithText("suena en 10 segundos", substring = true).assertExists()
        compose.onNodeWithText("Cargar ejemplos").performClick()
        assertFalse(loaded)
        compose.onAllNodesWithText("Cargar ejemplos").onLast().performClick()
        assertTrue(loaded)
    }

    @Test
    fun `the AI row is short and offers a new check only for the local AI`() {
        var status by mutableStateOf(AiStatus.OFFLINE)
        compose.setContent { FluxTheme { AiSettings(status, onRefresh = {}) } }
        compose.onNodeWithText("IA local (sin servidor)").assertExists()
        compose.onNodeWithText("Comprobar de nuevo").assertExists()
        status = AiStatus.CONNECTING
        compose.onNodeWithText("Conectando con la IA…").assertExists()
        compose.onNodeWithText("Comprobar de nuevo").assertDoesNotExist()
        status = AiStatus.NO_SESSION
        compose.onNodeWithText("IA local (sin sesión)").assertExists()
        status = AiStatus.REMOTE
        compose.onNodeWithText("IA en la nube activa").assertExists()
        compose.onNodeWithText("Comprobar de nuevo").assertDoesNotExist()
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
