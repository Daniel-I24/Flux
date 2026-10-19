package com.flux.mobile.feature

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.SaveProfileUseCase
import com.flux.core.model.UserProfile
import com.flux.mobile.feature.onboarding.WelcomeScreen
import com.flux.mobile.feature.profile.ProfileForm
import com.flux.mobile.feature.profile.ProfileViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OnboardingUiTest {
    @get:Rule val compose = createComposeRule()

    private class InMemoryProfiles : ProfileRepository {
        val stored = MutableStateFlow<UserProfile?>(null)
        override fun observeProfile() = stored
        override suspend fun getProfile() = stored.value
        override suspend fun saveProfile(profile: UserProfile) {
            stored.value = profile
        }
        override suspend fun clear() {
            stored.value = null
        }
    }

    @Test
    fun welcome_explains_the_app_and_starts() {
        var started = false
        compose.setContent { FluxTheme { WelcomeScreen(onStart = { started = true }) } }
        compose.onNodeWithText("Tus ideas, al instante").assertIsDisplayed()
        compose.onNodeWithText("Dicta una nota desde el reloj en segundos").assertIsDisplayed()
        compose.onNodeWithText("Comenzar").performClick()
        assertTrue(started)
    }

    @Test
    fun profile_form_validates_then_saves() {
        val profiles = InMemoryProfiles()
        val viewModel = ProfileViewModel(ObserveProfileUseCase(profiles), SaveProfileUseCase(profiles))
        var saved = false
        compose.setContent { ObservedProfileForm(viewModel) }

        viewModel.save { saved = true }
        compose.onNodeWithText("Escribe tu nombre (máximo 40 caracteres).").assertIsDisplayed()

        viewModel.onNameChange("Daniela")
        viewModel.save { saved = true }
        compose.waitUntil(timeoutMillis = 5_000) { saved }
        assertEquals("Daniela", profiles.stored.value?.displayName)
    }

    @Test
    fun profile_form_accepts_typed_name() {
        val profiles = InMemoryProfiles()
        val viewModel = ProfileViewModel(ObserveProfileUseCase(profiles), SaveProfileUseCase(profiles))
        compose.setContent { ObservedProfileForm(viewModel) }
        compose.onNodeWithText("¿Cómo te llamas?").performTextInput("Ana")
        assertEquals("Ana", viewModel.uiState.value.name)
    }

    @Composable
    private fun ObservedProfileForm(viewModel: ProfileViewModel) {
        val state by viewModel.uiState.collectAsState()
        FluxTheme { ProfileForm(state, viewModel) }
    }
}
