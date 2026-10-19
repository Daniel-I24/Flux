package com.flux.wear

import androidx.compose.runtime.Composable
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
import com.flux.wear.feature.settings.WatchSettingsScreen

/** Swipe-to-dismiss navigation; [startRoute] lets the tile open dictation directly. */
@Composable
fun WatchApp(startRoute: String = WatchRoutes.HOME, navController: NavHostController = rememberSwipeDismissableNavController()) {
    AppScaffold {
        SwipeDismissableNavHost(navController = navController, startDestination = startRoute) {
            composable(WatchRoutes.HOME) {
                HomeScreen(
                    HomeActions(
                        dictate = { navController.navigate(WatchRoutes.DICTATE) },
                        type = { navController.navigate(WatchRoutes.TYPE) },
                        agenda = { navController.navigate(WatchRoutes.AGENDA) },
                        categories = { navController.navigate(WatchRoutes.CATEGORIES) },
                        settings = { navController.navigate(WatchRoutes.SETTINGS) },
                    ),
                )
            }
            composable(WatchRoutes.DICTATE) { CaptureScreen(onDone = { navController.popBackOrHome() }) }
            composable(WatchRoutes.TYPE) { CaptureScreen(onDone = { navController.popBackOrHome() }, startWithKeyboard = true) }
            composable(WatchRoutes.AGENDA) { AgendaScreen() }
            composable(WatchRoutes.CATEGORIES) { CategoriesScreen(onDictate = { navController.navigate(WatchRoutes.DICTATE) }) }
            composable(WatchRoutes.SETTINGS) { WatchSettingsScreen() }
        }
    }
}

private fun NavHostController.popBackOrHome() {
    if (!popBackStack()) navigate(WatchRoutes.HOME)
}
