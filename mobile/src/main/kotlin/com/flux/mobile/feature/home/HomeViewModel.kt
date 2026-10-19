package com.flux.mobile.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveCategorySummaryUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ObserveUpcomingRemindersUseCase
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.WatchLink
import com.flux.mobile.link.WatchLinkCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryCount(val category: Category, val pending: Int)

data class HomeUiState(
    val userName: String? = null,
    val watch: WatchLink = WatchLink.Searching,
    val upcoming: List<Note> = emptyList(),
    val categories: Map<String, Category> = emptyMap(),
    val summary: List<CategoryCount> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeProfile: ObserveProfileUseCase,
    observeUpcoming: ObserveUpcomingRemindersUseCase,
    observeSummary: ObserveCategorySummaryUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val linkCoordinator: WatchLinkCoordinator,
    clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        observeProfile(),
        linkCoordinator.watchLink,
        observeUpcoming(clock.now(), UPCOMING_LIMIT),
        observeCategories(),
        observeSummary(),
    ) { profile, watch, upcoming, categories, summary ->
        HomeUiState(
            userName = profile?.displayName,
            watch = watch,
            upcoming = upcoming,
            categories = categories.associateBy { it.id },
            summary = categories.mapNotNull { category -> summary[category.id]?.let { CategoryCount(category, it) } },
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    init {
        viewModelScope.launch { linkCoordinator.refresh() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val UPCOMING_LIMIT = 5
    }
}
