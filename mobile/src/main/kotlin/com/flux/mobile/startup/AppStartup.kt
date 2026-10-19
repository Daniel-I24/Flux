package com.flux.mobile.startup

import com.flux.core.domain.repository.CategoryRepository
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
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch { categories.ensureDefaults() }
        accountGuard.start(scope)
        snapshotPublisher.start(scope)
    }
}
