package com.flux.wear.feature.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.flux.core.data.repository.OutboxRepository
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.ObserveUpcomingRemindersUseCase
import com.flux.core.model.Note
import com.flux.core.model.PhoneConnection
import com.flux.wear.BuildConfig
import com.flux.wear.sync.LinkRequests
import com.flux.wear.sync.PendingLink
import com.flux.wear.sync.PhoneConnectionMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

data class HomeUiState(
    val connection: PhoneConnection = PhoneConnection.Checking,
    val nextReminder: Note? = null,
    val pendingCount: Int = 0,
    val pendingLink: PendingLink? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val monitor: PhoneConnectionMonitor,
    private val linkRequests: LinkRequests,
    observeUpcoming: ObserveUpcomingRemindersUseCase,
    outbox: OutboxRepository,
    clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        monitor.connection,
        observeUpcoming(clock.now(), limit = 1).map { it.firstOrNull() },
        outbox.observePendingCount(),
        linkRequests.pendingLink,
    ) { connection, next, pending, link -> HomeUiState(connection, next, pending, link) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { monitor.refresh() }
    }

    fun answerLink(accepted: Boolean) {
        viewModelScope.launch { linkRequests.answer(accepted) }
    }

    /** Opens FLUX (or its Play Store page) on the phone so the user can fix the problem there. */
    fun openOnPhone(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse(BuildConfig.PLAY_STORE_URL))
            val opened =
                runCatching { RemoteActivityHelper(context, Executors.newSingleThreadExecutor()).startRemoteActivity(intent).await() }
            onResult(opened.isSuccess)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
