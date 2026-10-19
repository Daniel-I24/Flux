package com.flux.wear.feature.home

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.wear.R
import com.flux.wear.feature.link.LinkConfirmContent
import com.flux.wear.ui.rememberHaptics

/** Glanceable home: link status, a big dictate button, next reminder and shortcuts (rotary scroll). */
@Composable
fun HomeScreen(actions: HomeActions, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()
    val context = LocalContext.current
    val opened = stringResource(R.string.problem_opened_on_phone)
    state.pendingLink?.let { pending ->
        LinkConfirmContent(pending, onAnswer = { accepted ->
            if (accepted) haptics.confirm() else haptics.reject()
            viewModel.answerLink(accepted)
        })
        return
    }
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(onClick = {
                haptics.tap()
                actions.dictate()
            }, buttonSize = EdgeButtonSize.Large) {
                Icon(Icons.Rounded.Mic, contentDescription = stringResource(R.string.home_dictate))
            }
        },
    ) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ConnectionLabel(state.connection) }
            item {
                ConnectionProblemCard(state.connection, onAction = { action ->
                    haptics.tap()
                    when (action) {
                        ProblemAction.RETRY -> viewModel.refresh()
                        ProblemAction.OPEN_ON_PHONE -> viewModel.openOnPhone { ok ->
                            if (ok) Toast.makeText(context, opened, Toast.LENGTH_SHORT).show()
                        }
                        ProblemAction.OPEN_SETTINGS -> actions.settings()
                        ProblemAction.NONE -> Unit
                    }
                })
            }
            item { NextReminderCard(state.nextReminder, state.pendingCount, onClick = actions.agenda) }
            item { Shortcut(Icons.Rounded.Keyboard, stringResource(R.string.home_type), actions.type) }
            item { Shortcut(Icons.AutoMirrored.Rounded.EventNote, stringResource(R.string.home_agenda), actions.agenda) }
            item { Shortcut(Icons.Rounded.Category, stringResource(R.string.home_categories), actions.categories) }
            item { Shortcut(Icons.Rounded.Settings, stringResource(R.string.home_settings), actions.settings) }
        }
    }
}

@Composable
private fun Shortcut(icon: ImageVector, label: String, onClick: () -> Unit) {
    val haptics = rememberHaptics()
    FilledTonalButton(
        onClick = {
            haptics.tap()
            onClick()
        },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
    )
}
