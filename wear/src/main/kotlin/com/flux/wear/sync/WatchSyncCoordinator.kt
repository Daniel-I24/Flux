package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.core.sync.datalayer.Incoming
import com.flux.core.sync.protocol.NoteActionType
import com.flux.core.sync.protocol.SyncMessage
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject
import javax.inject.Singleton

/** Watch side of the protocol: link requests, acks, reminder actions and status pushes. */
@Singleton
class WatchSyncCoordinator @Inject constructor(
    private val linkRequests: LinkRequests,
    private val monitor: PhoneConnectionMonitor,
    private val outbox: OutboxRepository,
    private val stateStore: WatchStateStore,
    private val complete: CompleteNoteUseCase,
    private val snooze: SnoozeNoteUseCase,
) {
    suspend fun handle(incoming: Incoming) {
        when (val message = incoming.envelope.message) {
            is SyncMessage.LinkRequest -> linkRequests.onRequest(message, incoming.sourceNodeId)
            is SyncMessage.Unlink -> if (message.accountId == stateStore.linkedAccount()) linkRequests.unlink(notifyPhone = false)
            is SyncMessage.NoteAck -> outbox.markSent(message.noteId)
            is SyncMessage.NoteAction -> when (message.action) {
                NoteActionType.COMPLETE -> complete(message.noteId)
                NoteActionType.SNOOZE -> snooze(message.noteId, message.snoozeMinutes ?: stateStore.phoneState().snoozeMinutes)
            }
            is SyncMessage.Status -> monitor.onStatus(message)
            else -> Unit
        }
    }
}
