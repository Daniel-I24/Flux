package com.flux.wear

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.DecodeResult
import com.flux.wear.sync.OutboxSender
import com.flux.wear.sync.PhoneConnectionMonitor
import com.flux.wear.sync.SnapshotApplier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FluxWearApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var categories: CategoryRepository

    @Inject lateinit var gateway: DataLayerGateway

    @Inject lateinit var snapshotApplier: SnapshotApplier

    @Inject lateinit var monitor: PhoneConnectionMonitor

    @Inject lateinit var outboxSender: OutboxSender

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    /** On start: defaults, latest snapshot from the Data Layer, connection check and queue flush. */
    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            categories.ensureDefaults()
            (gateway.readSnapshot() as? DecodeResult.Valid)?.let { snapshotApplier.apply(it.value) }
            monitor.refresh()
            outboxSender.trigger()
        }
    }
}
