package com.flux.wear.sync

import com.flux.core.model.FluxResult
import com.flux.core.model.PhoneConnection
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.LinkLog
import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.wear.BuildConfig
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Works out, step by step, why the watch may not reach FLUX on the phone. Re-evaluated on
 * app start, on capability changes and when the phone pushes a status (never polled).
 */
@Singleton
class PhoneConnectionMonitor @Inject constructor(
    private val gateway: DataLayerGateway,
    private val inbox: SyncInbox,
    private val factory: SyncMessageFactory,
    private val stateStore: WatchStateStore,
) {
    private val state = MutableStateFlow<PhoneConnection>(PhoneConnection.Checking)
    private val mutex = Mutex()
    private var reachablePhone: String? = null

    val connection: StateFlow<PhoneConnection> = state.asStateFlow()

    /** Node id of the phone when it is reachable and ready to answer. */
    fun readyPhoneNode(): String? = reachablePhone.takeIf { state.value is PhoneConnection.Ready }

    /** Node id of a reachable phone with FLUX, even before it is linked (to open FLUX there). */
    fun reachablePhoneNode(): String? = reachablePhone

    suspend fun refresh(): PhoneConnection = mutex.withLock {
        evaluate().also {
            LinkLog.info("watch state=${it.javaClass.simpleName}")
            state.value = it
        }
    }

    fun onStatus(status: SyncMessage.Status) {
        state.value = ConnectionDiagnosis.fromStatus(status)
        LinkLog.info("watch status pushed state=${state.value.javaClass.simpleName}")
    }

    private suspend fun evaluate(): PhoneConnection {
        if (inbox.versionMismatch.value) return PhoneConnection.UpdateRequired
        val nodes = gateway.connectedNodes()
        val withApp = gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = false)
        val reachable = gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true)
        val probe = LinkProbe(nodes.count { it.isNearby }, nodes.count { !it.isNearby }, withApp.size, reachable.size)
        LinkLog.info("watch probe $probe")
        reachablePhone = reachable.firstOrNull()?.id
        ConnectionDiagnosis.fromProbe(probe)?.let { return it }
        val phone = reachablePhone ?: return PhoneConnection.PhoneUnreachable
        val hello = factory.hello(BuildConfig.VERSION_CODE, stateStore.linkedAccount())
        val status = inbox.request<SyncMessage.Status>(SyncInbox.STATUS_CORRELATION, BuildConfig.PHONE_REPLY_TIMEOUT_MS) {
            gateway.send(phone, hello) is FluxResult.Success
        }
        LinkLog.info("watch hello answered=${status != null}")
        return when {
            inbox.versionMismatch.value -> PhoneConnection.UpdateRequired
            status == null -> PhoneConnection.PhoneUnreachable
            else -> ConnectionDiagnosis.fromStatus(status)
        }
    }
}
