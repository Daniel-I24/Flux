package com.flux.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.theme.FluxTheme
import com.flux.mobile.launch.CaptureLaunchRequests
import com.flux.mobile.launch.CaptureRequest
import com.flux.mobile.link.LinkLaunchRequests
import com.flux.mobile.ui.common.ThemedSystemBars
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    @Inject lateinit var linkLaunch: LinkLaunchRequests

    @Inject lateinit var captureLaunch: CaptureLaunchRequests

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        linkLaunch.handle(intent)
        captureLaunch.handle(intent)
        setContent {
            val theme by appViewModel.theme.collectAsStateWithLifecycle()
            ThemedSystemBars(theme.mode)
            val linkRequest by linkLaunch.request.collectAsStateWithLifecycle()
            val captureRequest by captureLaunch.request.collectAsStateWithLifecycle()
            FluxTheme(themeMode = theme.mode, palette = theme.palette) {
                FluxApp(linkRequest = linkRequest, capture = CaptureRequest(captureRequest, captureLaunch::consume))
            }
        }
    }

    /** singleTask: a "Vincular" from the watch while FLUX is open arrives here. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        linkLaunch.handle(intent)
        captureLaunch.handle(intent)
    }
}
