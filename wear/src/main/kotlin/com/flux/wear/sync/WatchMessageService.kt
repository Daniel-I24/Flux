package com.flux.wear.sync

import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.DecodeResult
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncProtocol
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Event-driven entry point on the watch: messages, snapshot changes and phone
 * (re)connections. The offline queue is flushed as soon as the phone is reachable again.
 */
@AndroidEntryPoint
class WatchMessageService : WearableListenerService() {
    @Inject lateinit var inbox: SyncInbox

    @Inject lateinit var codec: SyncCodec

    @Inject lateinit var coordinator: WatchSyncCoordinator

    @Inject lateinit var snapshotApplier: SnapshotApplier

    @Inject lateinit var monitor: PhoneConnectionMonitor

    @Inject lateinit var outboxSender: OutboxSender

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onMessageReceived(event: MessageEvent) {
        val incoming = inbox.receive(event.data, event.sourceNodeId) ?: return
        scope.launch { coordinator.handle(incoming) }
    }

    override fun onDataChanged(events: DataEventBuffer) {
        val payloads = events
            .filter { it.type == DataEvent.TYPE_CHANGED && it.dataItem.uri.path == SyncProtocol.SNAPSHOT_PATH }
            .mapNotNull { DataMapItem.fromDataItem(it.dataItem).dataMap.getByteArray(SyncProtocol.SNAPSHOT_KEY) }
        events.release()
        payloads.lastOrNull()?.let { bytes ->
            val decoded = codec.decodeSnapshot(bytes)
            if (decoded is DecodeResult.Valid) scope.launch { snapshotApplier.apply(decoded.value) }
        }
    }

    override fun onCapabilityChanged(info: CapabilityInfo) {
        scope.launch {
            monitor.refresh()
            outboxSender.trigger()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
