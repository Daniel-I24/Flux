package com.flux.core.sync.datalayer

import android.content.Context
import com.flux.core.model.AppError
import com.flux.core.model.FluxResult
import com.flux.core.sync.protocol.DecodeResult
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.core.sync.protocol.WatchSnapshot
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

data class NodeInfo(val id: String, val displayName: String, val isNearby: Boolean)

/**
 * Adapter over the Wearable Data Layer: hides MessageClient, DataClient,
 * CapabilityClient and NodeClient behind suspend functions with FluxResult.
 */
@Singleton
class DataLayerGateway @Inject constructor(
    @ApplicationContext context: Context,
    private val codec: SyncCodec,
) {
    private val messages = Wearable.getMessageClient(context)
    private val data = Wearable.getDataClient(context)
    private val capabilities = Wearable.getCapabilityClient(context)
    private val nodes = Wearable.getNodeClient(context)

    /** Is the Wearable API present at all (Play services / Wear OS app installed)? */
    suspend fun isAvailable(): Boolean = runCatching { nodes.connectedNodes.await() }.isSuccess

    suspend fun connectedNodes(): List<NodeInfo> = runCatching {
        nodes.connectedNodes.await().map { NodeInfo(it.id, it.displayName, it.isNearby) }
    }.getOrDefault(emptyList())

    /** Nodes that installed the other FLUX app; [reachableOnly] = currently connected. */
    suspend fun nodesWithCapability(capability: String, reachableOnly: Boolean): List<NodeInfo> = runCatching {
        val filter = if (reachableOnly) CapabilityClient.FILTER_REACHABLE else CapabilityClient.FILTER_ALL
        capabilities.getCapability(capability, filter).await().nodes.map { NodeInfo(it.id, it.displayName, it.isNearby) }
    }.getOrDefault(emptyList())

    suspend fun send(nodeId: String, envelope: SyncEnvelope): FluxResult<Unit> = try {
        messages.sendMessage(nodeId, SyncProtocol.MESSAGE_PATH, codec.encode(envelope)).await()
        FluxResult.Success(Unit)
    } catch (_: ApiException) {
        FluxResult.Failure(AppError.Network)
    }

    /** Publishes the latest state for the watch (Data Layer keeps it and syncs on reconnection). */
    suspend fun putSnapshot(snapshot: WatchSnapshot): FluxResult<Unit> = try {
        val request = PutDataMapRequest.create(SyncProtocol.SNAPSHOT_PATH).apply {
            dataMap.putByteArray(SyncProtocol.SNAPSHOT_KEY, codec.encode(snapshot))
        }.asPutDataRequest().setUrgent()
        data.putDataItem(request).await()
        FluxResult.Success(Unit)
    } catch (_: ApiException) {
        FluxResult.Failure(AppError.Network)
    }

    suspend fun readSnapshot(): DecodeResult<WatchSnapshot>? = runCatching {
        val buffer = data.dataItems.await()
        try {
            buffer.firstOrNull { it.uri.path == SyncProtocol.SNAPSHOT_PATH }
                ?.let { DataMapItem.fromDataItem(it).dataMap.getByteArray(SyncProtocol.SNAPSHOT_KEY) }
                ?.let(codec::decodeSnapshot)
        } finally {
            buffer.release()
        }
    }.getOrNull()
}
