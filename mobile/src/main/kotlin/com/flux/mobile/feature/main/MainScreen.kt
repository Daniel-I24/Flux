package com.flux.mobile.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.mobile.feature.categories.CategoriesScreen
import com.flux.mobile.feature.home.HomeScreen
import com.flux.mobile.feature.notes.NotesScreen
import com.flux.mobile.feature.settings.SettingsScreen

/** Main area after onboarding: four tabs sharing one snackbar host (used for "Deshacer"). */
@Composable
fun MainScreen() {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var notesCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = {
                            notesCategory = null
                            tab = item
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.label)) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            MainTab.HOME -> HomeScreen(
                onOpenNotes = { categoryId ->
                    notesCategory = categoryId
                    tab = MainTab.NOTES
                },
                onOpenSettings = { tab = MainTab.SETTINGS },
                modifier = modifier,
            )
            MainTab.NOTES -> NotesScreen(snackbar = snackbar, initialCategoryId = notesCategory, modifier = modifier)
            MainTab.CATEGORIES -> CategoriesScreen(snackbar = snackbar, modifier = modifier)
            MainTab.SETTINGS -> SettingsScreen(modifier = modifier)
        }
    }
}
