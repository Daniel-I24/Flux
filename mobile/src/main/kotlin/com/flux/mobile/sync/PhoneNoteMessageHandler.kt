package com.flux.mobile.sync

import com.flux.core.domain.port.ReclassificationQueue
import com.flux.core.domain.usecase.ApplyRemoteNoteUseCase
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.core.model.AppSettings
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.Note
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.NoteActionType
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import javax.inject.Inject

/** Notes captured on the watch and reminder actions taken on it. Idempotent by note id. */
class PhoneNoteMessageHandler @Inject constructor(
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val applyRemote: ApplyRemoteNoteUseCase,
    private val complete: CompleteNoteUseCase,
    private val snooze: SnoozeNoteUseCase,
    private val reclassification: ReclassificationQueue,
) {
    suspend fun handle(nodeId: String, message: SyncMessage) {
        when (message) {
            // A capture or an edit made on the watch (complete, snooze, reschedule, delete), merged by revision.
            is SyncMessage.NoteCapture -> {
                val note = message.note
                val applied = applyRemote(note)
                gateway.send(nodeId, factory.noteAck(note.id, note.updatedAt))
                if (applied && note.wantsRemoteClassification()) reclassification.enqueue(note.id)
            }
            is SyncMessage.NoteAction -> when (message.action) {
                NoteActionType.COMPLETE -> complete(message.noteId)
                NoteActionType.SNOOZE -> snooze(message.noteId, message.snoozeMinutes ?: DEFAULT_SNOOZE)
            }
            else -> Unit
        }
    }

    /** A brand-new capture (not an edit) classified offline that the user did not correct. */
    private fun Note.wantsRemoteClassification(): Boolean =
        createdAt == updatedAt && engine == ClassifierEngine.LOCAL && !categoryCorrected

    private companion object {
        const val DEFAULT_SNOOZE = AppSettings.DEFAULT_SNOOZE_MINUTES
    }
}
