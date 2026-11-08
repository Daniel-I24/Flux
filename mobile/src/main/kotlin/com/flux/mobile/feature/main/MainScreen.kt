package com.flux.mobile.feature.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import com.flux.core.designsystem.motion.FluxMotion
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.mobile.feature.categories.CategoriesScreen
import com.flux.mobile.feature.home.HomeScreen
import com.flux.mobile.feature.notes.NotesRequest
import com.flux.mobile.feature.notes.NotesScreen
import com.flux.mobile.feature.settings.SettingsScreen
import com.flux.mobile.link.LinkLaunch

/** Main area after onboarding: four tabs sharing one snackbar host (used for "Deshacer"). */
@Composable
fun MainScreen(linkRequest: LinkLaunch? = null) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    // "Vincular" on the watch: Ajustes > Reloj takes it from here.
    LaunchedEffect(linkRequest) { if (linkRequest != null) tab = MainTab.SETTINGS }
    var notesRequest by remember { mutableStateOf<NotesRequest?>(null) }
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = { tab = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.label)) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        val reduced = rememberReducedMotion()
        Crossfade(targetState = tab, animationSpec = tween(if (reduced) 0 else FluxMotion.MEDIUM_MS), label = "tab") { current ->
            when (current) {
                MainTab.HOME -> HomeScreen(
                    onOpenNotes = { categoryId ->
                        notesRequest = NotesRequest(categoryId, System.nanoTime())
                        tab = MainTab.NOTES
                    },
                    onOpenSettings = { tab = MainTab.SETTINGS },
                    modifier = modifier,
                )
                MainTab.NOTES -> NotesScreen(snackbar = snackbar, request = notesRequest, modifier = modifier)
                MainTab.CATEGORIES -> CategoriesScreen(snackbar = snackbar, modifier = modifier)
                MainTab.SETTINGS -> SettingsScreen(snackbar = snackbar, modifier = modifier)
            }
        }
    }
}
