package com.flux.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import com.flux.wear.data.WatchTheme
import com.flux.wear.ui.theme.FluxWearTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var stateStore: WatchStateStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val startRoute = if (intent?.action == ACTION_DICTATE) WatchRoutes.DICTATE else WatchRoutes.HOME
        setContent {
            val settings by stateStore.observeSettings().collectAsStateWithLifecycle(WatchSettings())
            FluxWearTheme(highContrast = settings.theme == WatchTheme.HIGH_CONTRAST) {
                WatchApp(startRoute = startRoute)
            }
        }
    }

    companion object {
        /** Used by the tile and the complication to open dictation directly. */
        const val ACTION_DICTATE = "com.flux.wear.action.DICTATE"
    }
}
