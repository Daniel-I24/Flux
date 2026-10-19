package com.flux.mobile.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.component.FluxEmptyState
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** Inicio: greeting, watch state, upcoming reminders and pending notes per category. */
@Composable
fun HomeScreen(
    onOpenNotes: (categoryId: String?) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.loading) {
        FluxLoadingState(modifier)
        return
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FluxSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FluxSpacing.lg),
    ) {
        Text(
            text = state.userName?.let { stringResource(R.string.home_greeting, it) } ?: stringResource(R.string.home_greeting_anonymous),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        WatchStatusCard(state.watch, onOpenSettings)
        SectionTitle(stringResource(R.string.home_upcoming))
        if (state.upcoming.isEmpty()) {
            FluxEmptyState(
                icon = Icons.Rounded.EventAvailable,
                title = stringResource(R.string.home_no_upcoming),
                description = stringResource(R.string.home_no_upcoming_hint),
            )
        } else {
            state.upcoming.forEach { note -> UpcomingReminderRow(note, state.categories[note.categoryId]) }
            TextButton(onClick = { onOpenNotes(null) }) { Text(stringResource(R.string.home_see_all)) }
        }
        if (state.summary.isNotEmpty()) {
            SectionTitle(stringResource(R.string.home_by_category))
            CategorySummary(state.summary, onOpen = onOpenNotes)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySummary(summary: List<CategoryCount>, onOpen: (String) -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        summary.forEach { (category, pending) ->
            AssistChip(
                onClick = { onOpen(category.id) },
                label = { Text(stringResource(R.string.home_category_count, category.displayName(), pending)) },
                leadingIcon = { CategoryDot(category) },
            )
        }
    }
}
