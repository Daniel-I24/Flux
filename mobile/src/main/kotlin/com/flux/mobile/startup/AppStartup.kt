package com.flux.mobile.startup

import com.flux.core.domain.port.RemoteAiStatus
import com.flux.core.domain.repository.CategoryRepository
import com.flux.mobile.alarm.AlarmChannels
import com.flux.mobile.link.AccountGuard
import com.flux.mobile.sync.SnapshotPublisher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Long-lived, event-driven work started once per process (no polling). */
@Singleton
class AppStartup @Inject constructor(
    private val categories: CategoryRepository,
    private val accountGuard: AccountGuard,
    private val snapshotPublisher: SnapshotPublisher,
    private val alarmChannels: AlarmChannels,
    private val remoteAi: RemoteAiStatus,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch { categories.ensureDefaults() }
        scope.launch { alarmChannels.ensure() }
        accountGuard.start(scope)
        snapshotPublisher.start(scope)
        // Wakes the free-hosted server early, so the first note is more likely classified by the remote AI.
        scope.launch { remoteAi.check(patient = true) }
    }
}
