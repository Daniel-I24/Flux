package com.flux.mobile.sync

import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import javax.inject.Inject

/**
 * Mirrors "Completar" / "Posponer" (and a reminder that starts ringing) to the watch.
 * If the watch is out of reach, the next snapshot (agenda) carries the same change.
 */
class ReminderSync @Inject constructor(
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
) {
    suspend fun completed(noteId: String) = broadcast(factory.complete(noteId))

    suspend fun snoozed(noteId: String, minutes: Int) = broadcast(factory.snooze(noteId, minutes))

    /** The phone started ringing: the watch alerts too, even if its own alarm was delayed or frozen. */
    suspend fun ringing(noteId: String) = broadcast(factory.reminderDue(noteId))

    private suspend fun broadcast(envelope: SyncEnvelope) {
        gateway.nodesWithCapability(SyncProtocol.WATCH_CAPABILITY, reachableOnly = true).forEach { gateway.send(it.id, envelope) }
    }
}
