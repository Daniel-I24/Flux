package com.flux.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.mobile.feature.home.MainScreen
import com.flux.mobile.feature.onboarding.OnboardingHost
import com.flux.mobile.navigation.AppDestination
import com.flux.mobile.navigation.GateViewModel

/** Root: onboarding until it is done, then the main app. */
@Composable
fun FluxApp(gate: GateViewModel = hiltViewModel()) {
    val destination by gate.destination.collectAsStateWithLifecycle()
    when (val current = destination) {
        AppDestination.Loading -> FluxLoadingState()
        is AppDestination.Onboarding -> OnboardingHost(current.step)
        AppDestination.Main -> MainScreen()
    }
}
