package com.flux.wear

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.flux.wear.feature.agenda.AgendaScreen
import com.flux.wear.feature.capture.CaptureScreen
import com.flux.wear.feature.categories.CategoriesScreen
import com.flux.wear.feature.home.HomeActions
import com.flux.wear.feature.home.HomeScreen
import com.flux.wear.feature.overdue.OverdueScreen
import com.flux.wear.feature.settings.WatchSettingsScreen
import com.flux.wear.feature.settings.WatchSettingsViewModel
import com.flux.wear.feature.tutorial.WatchTutorialScreen

/** Swipe-to-dismiss navigation (the scaffold shows the time); [startRoute] lets the tile open dictation directly. */
@Composable
fun WatchApp(startRoute: String = WatchRoutes.HOME, navController: NavHostController = rememberSwipeDismissableNavController()) {
    AppScaffold {
        SwipeDismissableNavHost(navController = navController, startDestination = startRoute) {
            composable(WatchRoutes.HOME) {
                HomeScreen(
                    HomeActions(
                        dictate = { navController.navigate(WatchRoutes.DICTATE) },
                        type = if (BuildConfig.DEBUG) ({ navController.navigate(WatchRoutes.TYPE) }) else null,
                        agenda = { navController.navigate(WatchRoutes.AGENDA) },
                        overdue = { navController.navigate(WatchRoutes.OVERDUE) },
                        categories = { navController.navigate(WatchRoutes.CATEGORIES) },
                        settings = { navController.navigate(WatchRoutes.SETTINGS) },
                        tutorial = { navController.navigate(WatchRoutes.TUTORIAL) { launchSingleTop = true } },
                    ),
                )
            }
            composable(WatchRoutes.DICTATE) { CaptureScreen(onDone = { navController.popBackOrHome() }) }
            if (BuildConfig.DEBUG) {
                composable(WatchRoutes.TYPE) { CaptureScreen(onDone = { navController.popBackOrHome() }, startWithKeyboard = true) }
            }
            composable(WatchRoutes.AGENDA) { AgendaScreen() }
            composable(WatchRoutes.OVERDUE) { OverdueScreen() }
            composable(WatchRoutes.CATEGORIES) { CategoriesScreen(onDictate = { navController.navigate(WatchRoutes.DICTATE) }) }
            composable(WatchRoutes.SETTINGS) { WatchSettingsScreen(onTutorial = { navController.navigate(WatchRoutes.TUTORIAL) }) }
            composable(WatchRoutes.TUTORIAL) {
                val settings: WatchSettingsViewModel = hiltViewModel()
                WatchTutorialScreen(onFinish = { settings.finishTutorial(onSaved = navController::popBackOrHome) })
            }
        }
    }
}

private fun NavHostController.popBackOrHome() {
    if (!popBackStack()) navigate(WatchRoutes.HOME)
}
