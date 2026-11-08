package com.flux.mobile.feature.link

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.model.WatchLink
import com.flux.mobile.R
import com.flux.mobile.link.LinkLaunchRequests
import com.flux.mobile.link.LinkProblem
import com.flux.mobile.link.WatchCandidate
import com.flux.mobile.link.WatchLinkCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LinkUiState(
    val link: WatchLink = WatchLink.Searching,
    val candidates: List<WatchCandidate> = emptyList(),
    /** The list of watches to link (or to switch to) is open. */
    val choosing: Boolean = false,
    @param:StringRes val problem: Int? = null,
)

/** What the watch chooser can do with a watch it lists. */
data class ChooserActions(
    val onLink: (WatchCandidate) -> Unit,
    val onInstall: (WatchCandidate) -> Unit,
    val onSearch: () -> Unit,
)

@HiltViewModel
class LinkViewModel @Inject constructor(
    private val coordinator: WatchLinkCoordinator,
    private val launches: LinkLaunchRequests,
) : ViewModel() {
    private val problem = MutableStateFlow<Int?>(null)
    private val choosing = MutableStateFlow(false)

    val uiState: StateFlow<LinkUiState> = combine(
        coordinator.watchLink,
        coordinator.protocolMismatch,
        coordinator.candidates,
        choosing,
        problem,
    ) { link, mismatch, candidates, open, issue ->
        LinkUiState(if (mismatch) WatchLink.UpdateRequired else link, candidates, open, issue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LinkUiState())

    init {
        refresh()
        viewModelScope.launch { launches.request.filterNotNull().collect { onWatchAsked(it.watchNodeId) } }
    }

    fun refresh() = launchReporting { coordinator.refresh().let { null } }

    /** "Vincular reloj" and "Cambiar de reloj": search again and show every watch found. */
    fun choose() {
        choosing.value = true
        refresh()
    }

    fun closeChooser() {
        choosing.value = false
    }

    fun link(candidate: WatchCandidate) {
        choosing.value = false
        launchReporting { coordinator.link(candidate.nodeId, candidate.displayName) }
    }

    fun install(candidate: WatchCandidate) = launchReporting { coordinator.installOnWatch(candidate.nodeId) }

    fun unlink() = launchReporting { coordinator.unlink().let { null } }

    /** The watch asked for it: link that watch directly, or let the user pick when it is not clear which one. */
    private suspend fun onWatchAsked(watchNodeId: String?) {
        launches.consume()
        coordinator.refresh()
        val withApp = coordinator.candidates.value.filter { it.hasApp }
        val target = withApp.firstOrNull { it.nodeId == watchNodeId } ?: withApp.singleOrNull()?.takeIf { watchNodeId == null }
        if (target != null) link(target) else choosing.value = true
    }

    private fun launchReporting(action: suspend () -> LinkProblem?) {
        viewModelScope.launch {
            problem.value = null
            problem.value = action()?.messageRes()
        }
    }

    private fun LinkProblem.messageRes(): Int = when (this) {
        LinkProblem.REJECTED -> R.string.link_rejected
        LinkProblem.TIMEOUT -> R.string.link_timeout
        LinkProblem.INSTALL_FAILED -> R.string.link_install_failed
        LinkProblem.STORAGE -> R.string.error_storage
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
