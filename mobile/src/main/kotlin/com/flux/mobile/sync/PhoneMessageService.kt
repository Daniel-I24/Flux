package com.flux.mobile.sync

import com.flux.core.sync.datalayer.SyncInbox
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Woken up by Google Play services when the watch sends a message (no polling, no background service). */
@AndroidEntryPoint
class PhoneMessageService : WearableListenerService() {
    @Inject lateinit var inbox: SyncInbox

    @Inject lateinit var coordinator: PhoneSyncCoordinator

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onMessageReceived(event: MessageEvent) {
        val incoming = inbox.receive(event.data, event.sourceNodeId) ?: return
        scope.launch { coordinator.handle(incoming) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
