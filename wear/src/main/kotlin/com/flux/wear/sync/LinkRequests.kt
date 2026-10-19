package com.flux.wear.sync

import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PendingLink(val code: String, val accountId: String, val userName: String?, val phoneNodeId: String)

/**
 * Link requests from the phone. A watch linked to another account rejects them
 * automatically; otherwise the user must confirm the 6-digit code on the watch.
 */
@Singleton
class LinkRequests @Inject constructor(
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val stateStore: WatchStateStore,
    private val monitor: PhoneConnectionMonitor,
    private val notifier: LinkRequestNotifier,
) {
    private val pending = MutableStateFlow<PendingLink?>(null)
    val pendingLink: StateFlow<PendingLink?> = pending.asStateFlow()

    suspend fun onRequest(request: SyncMessage.LinkRequest, phoneNodeId: String) {
        val owner = stateStore.linkedAccount()
        if (owner != null && owner != request.accountId) {
            gateway.send(phoneNodeId, factory.envelope(SyncMessage.LinkResponse(request.code, accepted = false, ownerAccountId = owner)))
            return
        }
        pending.value = PendingLink(request.code, request.accountId, request.userName, phoneNodeId)
        notifier.show(request.code)
    }

    suspend fun answer(accepted: Boolean) {
        val request = pending.value ?: return
        pending.value = null
        notifier.dismiss()
        if (accepted) stateStore.setLinkedAccount(request.accountId)
        val owner = if (accepted) request.accountId else stateStore.linkedAccount()
        gateway.send(request.phoneNodeId, factory.envelope(SyncMessage.LinkResponse(request.code, accepted, owner)))
        monitor.refresh()
    }

    /** Unlinking from the watch (Ajustes) or from the phone. */
    suspend fun unlink(notifyPhone: Boolean) {
        val owner = stateStore.linkedAccount() ?: return
        stateStore.setLinkedAccount(null)
        if (notifyPhone) monitor.readyPhoneNode()?.let { gateway.send(it, factory.envelope(SyncMessage.Unlink(owner))) }
        monitor.refresh()
    }
}
