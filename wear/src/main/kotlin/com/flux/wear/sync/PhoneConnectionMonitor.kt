package com.flux.wear.sync

import com.flux.core.model.FluxResult
import com.flux.core.model.PhoneConnection
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.core.sync.protocol.WatchOwnership
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

    suspend fun refresh(): PhoneConnection = mutex.withLock {
        evaluate().also { state.value = it }
    }

    fun onStatus(status: SyncMessage.Status) {
        state.value = status.toConnection()
    }

    private suspend fun evaluate(): PhoneConnection {
        if (inbox.versionMismatch.value) return PhoneConnection.UpdateRequired
        val phonesWithApp = gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = false)
        if (phonesWithApp.isEmpty()) {
            return if (gateway.connectedNodes().isEmpty()) PhoneConnection.NoPairedPhone else PhoneConnection.AppNotInstalled
        }
        val phone = gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true).firstOrNull()
        reachablePhone = phone?.id
        if (phone == null) return PhoneConnection.PhoneUnreachable
        val hello = factory.hello(BuildConfig.VERSION_CODE, stateStore.linkedAccount())
        val status = inbox.request<SyncMessage.Status>(SyncInbox.STATUS_CORRELATION, BuildConfig.PHONE_REPLY_TIMEOUT_MS) {
            gateway.send(phone.id, hello) is FluxResult.Success
        }
        return when {
            inbox.versionMismatch.value -> PhoneConnection.UpdateRequired
            status == null -> PhoneConnection.PhoneUnreachable
            else -> status.toConnection()
        }
    }

    private fun SyncMessage.Status.toConnection(): PhoneConnection = when {
        accountState != AccountState.READY -> PhoneConnection.SetupIncomplete
        ownership == WatchOwnership.NOT_LINKED -> PhoneConnection.NotLinked
        ownership == WatchOwnership.LINKED_TO_OTHER -> PhoneConnection.LinkedToOtherAccount
        else -> PhoneConnection.Ready(userName, serverReachable)
    }
}
