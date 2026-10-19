package com.flux.mobile.feature.link

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.model.WatchLink
import com.flux.mobile.R
import com.flux.mobile.link.LinkProblem
import com.flux.mobile.link.WatchLinkCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LinkUiState(val link: WatchLink = WatchLink.Searching, @param:StringRes val problem: Int? = null)

@HiltViewModel
class LinkViewModel @Inject constructor(private val coordinator: WatchLinkCoordinator) : ViewModel() {
    private val problem = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<LinkUiState> = combine(coordinator.watchLink, coordinator.protocolMismatch, problem) { link, mismatch, issue ->
        LinkUiState(link = if (mismatch) WatchLink.UpdateRequired else link, problem = issue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), LinkUiState())

    init {
        refresh()
    }

    fun refresh() = launchReporting { coordinator.refresh().let { null } }

    fun link(nodeId: String, name: String) = launchReporting { coordinator.link(nodeId, name) }

    fun install(nodeId: String) = launchReporting { coordinator.installOnWatch(nodeId) }

    fun unlink() = launchReporting { coordinator.unlink().let { null } }

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
