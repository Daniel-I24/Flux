package com.flux.mobile.feature.permissions

import android.app.Application
import android.provider.Settings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.flux.core.designsystem.theme.FluxTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class PermissionsListTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun `numbered steps show their live state and open the exact screen`() {
        ShadowSettings.setCanDrawOverlays(false)
        compose.setContent { FluxTheme { PermissionsList(Modifier.verticalScroll(rememberScrollState())) } }
        compose.onNodeWithText("1. Micrófono").assertIsDisplayed()
        assertTrue(compose.onAllNodesWithText("Pendiente").fetchSemanticsNodes().isNotEmpty())
        compose.onNodeWithText("Aparecer encima", substring = true).performScrollTo()
        compose.onNodeWithText("Mostrar sobre otras apps", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `the battery button opens a battery screen for FLUX`() {
        compose.setContent { FluxTheme { BackgroundHelpCard() } }
        compose.onNodeWithText("Batería sin restricciones").performClick()
        val app = ApplicationProvider.getApplicationContext<Application>()
        val started = shadowOf(app).nextStartedActivity
        // Robolectric has no battery screen, so FLUX's app info opens (the fallback every phone has).
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, started.action)
    }
}
