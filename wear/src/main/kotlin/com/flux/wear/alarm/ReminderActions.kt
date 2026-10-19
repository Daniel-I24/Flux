package com.flux.wear.alarm

import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject

/** Completar / Posponer on the watch: applied locally and mirrored to the phone. */
class ReminderActions @Inject constructor(
    private val complete: CompleteNoteUseCase,
    private val snooze: SnoozeNoteUseCase,
    private val stateStore: WatchStateStore,
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
) {
    suspend fun complete(noteId: String) {
        complete.invoke(noteId)
        sendToPhone(factory.complete(noteId))
    }

    suspend fun snooze(noteId: String) {
        val minutes = stateStore.phoneState().snoozeMinutes
        snooze.invoke(noteId, minutes)
        sendToPhone(factory.snooze(noteId, minutes))
    }

    private suspend fun sendToPhone(envelope: SyncEnvelope) {
        gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true).forEach { gateway.send(it.id, envelope) }
    }
}
