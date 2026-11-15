package com.flux.mobile.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.motion.fluxTabTransition
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.mobile.feature.categories.CategoriesScreen
import com.flux.mobile.feature.home.HomeScreen
import com.flux.mobile.feature.notes.NotesRequest
import com.flux.mobile.feature.notes.NotesScreen
import com.flux.mobile.feature.settings.SettingsScreen
import com.flux.mobile.launch.CaptureRequest
import com.flux.mobile.link.LinkLaunch

/** Main area after onboarding: four tabs sharing one snackbar host (used for "Deshacer"). */
@Composable
fun MainScreen(linkRequest: LinkLaunch? = null, capture: CaptureRequest = CaptureRequest()) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    // "Vincular" on the watch: Ajustes > Reloj takes it from here.
    LaunchedEffect(linkRequest) { if (linkRequest != null) tab = MainTab.SETTINGS }
    var notesRequest by remember { mutableStateOf<NotesRequest?>(null) }
    // Launcher shortcut or quick settings: Notas opens the capture sheet.
    LaunchedEffect(capture.launch) {
        val launch = capture.launch ?: return@LaunchedEffect
        notesRequest = NotesRequest(categoryId = null, token = System.nanoTime(), capture = launch)
        tab = MainTab.NOTES
        capture.consume()
    }
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { MainNavigationBar(tab, onSelect = { tab = it }) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        val reduced = rememberReducedMotion()
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fluxTabTransition(reduced, forward = targetState.ordinal > initialState.ordinal) },
            label = "tab",
        ) { current ->
            when (current) {
                MainTab.HOME -> HomeScreen(
                    onOpenNotes = { categoryId ->
                        notesRequest = NotesRequest(categoryId, System.nanoTime())
                        tab = MainTab.NOTES
                    },
                    onOpenSettings = { tab = MainTab.SETTINGS },
                    modifier = modifier,
                )
                MainTab.NOTES -> NotesScreen(
                    snackbar = snackbar,
                    request = notesRequest,
                    onRequestHandled = { notesRequest = null },
                    modifier = modifier,
                )
                MainTab.CATEGORIES -> CategoriesScreen(
                    snackbar = snackbar,
                    onOpenNotes = { categoryId ->
                        notesRequest = NotesRequest(categoryId, System.nanoTime())
                        tab = MainTab.NOTES
                    },
                    modifier = modifier,
                )
                MainTab.SETTINGS -> SettingsScreen(snackbar = snackbar, modifier = modifier)
            }
        }
    }
}

@Composable
private fun MainNavigationBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar {
        MainTab.entries.forEach { item ->
            NavigationBarItem(
                selected = selected == item,
                onClick = { onSelect(item) },
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.label)) },
            )
        }
    }
}
