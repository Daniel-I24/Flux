package com.flux.mobile.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.feature.profile.ProfileForm
import com.flux.mobile.feature.profile.ProfileViewModel
import com.flux.mobile.feature.routine.RoutineForm
import com.flux.mobile.feature.routine.RoutineViewModel

/** Same forms as onboarding, reused from Ajustes. */
@Composable
fun ProfileSettingsPage(onDone: () -> Unit, modifier: Modifier = Modifier, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsSubpage(
        title = stringResource(R.string.settings_profile),
        onBack = onDone,
        onSave = { viewModel.save(onDone) },
        saving = state.saving,
        modifier = modifier,
    ) {
        ProfileForm(state, viewModel)
    }
}

@Composable
fun RoutineSettingsPage(onDone: () -> Unit, modifier: Modifier = Modifier, viewModel: RoutineViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsSubpage(
        title = stringResource(R.string.settings_routine),
        onBack = onDone,
        onSave = { viewModel.save(onDone) },
        saving = state.saving,
        modifier = modifier,
    ) {
        RoutineForm(state, viewModel)
    }
}

@Composable
private fun SettingsSubpage(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FluxSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FluxSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back)) }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        content()
        Button(onClick = onSave, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_save)) }
    }
}
