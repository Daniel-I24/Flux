package com.flux.mobile.sync

import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.datalayer.SyncWorkScope
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Woken up by Google Play services when the watch sends a message (no polling, no background service). */
@AndroidEntryPoint
class PhoneMessageService : WearableListenerService() {
    @Inject lateinit var inbox: SyncInbox

    @Inject lateinit var coordinator: PhoneSyncCoordinator

    @Inject lateinit var scope: SyncWorkScope

    override fun onMessageReceived(event: MessageEvent) {
        val incoming = inbox.receive(event.data, event.sourceNodeId) ?: return
        scope.launch { coordinator.handle(incoming) }
    }
}
