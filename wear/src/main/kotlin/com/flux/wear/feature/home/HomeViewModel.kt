package com.flux.wear.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.data.repository.OutboxRepository
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.model.Note
import com.flux.core.model.PhoneConnection
import com.flux.wear.BuildConfig
import com.flux.wear.data.WatchStateStore
import com.flux.wear.sync.LinkRequests
import com.flux.wear.sync.PendingLink
import com.flux.wear.sync.PhoneConnectionMonitor
import com.flux.wear.sync.PhoneLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val connection: PhoneConnection = PhoneConnection.Checking,
    val nextReminder: Note? = null,
    val overdueCount: Int = 0,
    val pendingCount: Int = 0,
    val pendingLink: PendingLink? = null,
    /** The phone runs another FLUX version: features may not match until both are updated. */
    val versionMismatch: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val launcher: PhoneLauncher,
    private val monitor: PhoneConnectionMonitor,
    private val linkRequests: LinkRequests,
    observeSections: ObserveNoteSectionsUseCase,
    outbox: OutboxRepository,
    private val store: WatchStateStore,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        monitor.connection,
        combine(observeSections(), outbox.observePendingCount(), ::Pair),
        linkRequests.pendingLink,
        store.observePhoneState(),
    ) { connection, (sections, pending), link, phone ->
        HomeUiState(
            connection = connection,
            nextReminder = sections.next,
            overdueCount = sections.overdue.size,
            pendingCount = pending,
            pendingLink = link,
            versionMismatch = phone.phoneAppVersion != 0 && phone.phoneAppVersion != BuildConfig.VERSION_CODE,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { monitor.refresh() }
    }

    /** Reads the store, not [uiState]: on returning from the tutorial uiState can still hold the old value. */
    suspend fun shouldShowTutorial(): Boolean = !store.settings().tutorialDone

    fun answerLink(accepted: Boolean) {
        viewModelScope.launch { linkRequests.answer(accepted) }
    }

    /** Opens FLUX's store page, or FLUX itself to link this watch, on the phone: the fix happens there. */
    fun openOnPhone(action: ProblemAction, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(if (action == ProblemAction.OPEN_STORE) launcher.openStore() else launcher.openLink())
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
