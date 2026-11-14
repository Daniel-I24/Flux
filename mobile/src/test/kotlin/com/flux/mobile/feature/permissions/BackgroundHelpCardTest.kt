package com.flux.mobile.feature.permissions

import android.app.Application
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.flux.core.designsystem.theme.FluxTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class BackgroundHelpCardTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `explains battery and autostart for any brand and opens FLUX's app info`() {
        compose.setContent { FluxTheme { BackgroundHelpCard() } }
        compose.onNodeWithText("Si los recordatorios llegan tarde").assertIsDisplayed()
        compose.onNodeWithText("la app de tu reloj (Galaxy Wearable, Pixel Watch u otra)", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Sin restricciones", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Inicio automático", substring = true).assertIsDisplayed()

        compose.onNodeWithText("Abrir ajustes de FLUX").performClick()

        val app = ApplicationProvider.getApplicationContext<Application>()
        val started = shadowOf(app).nextStartedActivity
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started.action)
        assertEquals("package:${app.packageName}", started.dataString)
    }
}
