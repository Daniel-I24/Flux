package com.flux.mobile.sync

import com.flux.core.domain.usecase.ApplyRemoteNoteUseCase
import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.NoteActionType
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.mobile.ai.ReclassificationScheduler
import javax.inject.Inject

/** Notes captured on the watch and reminder actions taken on it. Idempotent by note id. */
class PhoneNoteMessageHandler @Inject constructor(
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val applyRemote: ApplyRemoteNoteUseCase,
    private val complete: CompleteNoteUseCase,
    private val snooze: SnoozeNoteUseCase,
    private val reclassification: ReclassificationScheduler,
) {
    suspend fun handle(nodeId: String, message: SyncMessage) {
        when (message) {
            is SyncMessage.NoteCapture -> {
                val note = message.note.copy(source = DeviceSource.WATCH)
                val isNew = applyRemote(note)
                gateway.send(nodeId, factory.noteAck(note.id))
                if (isNew && note.engine == ClassifierEngine.LOCAL && !note.categoryCorrected) reclassification.schedule(note.id)
            }
            is SyncMessage.NoteAction -> when (message.action) {
                NoteActionType.COMPLETE -> complete(message.noteId)
                NoteActionType.SNOOZE -> snooze(message.noteId, message.snoozeMinutes ?: DEFAULT_SNOOZE)
            }
            else -> Unit
        }
    }

    private companion object {
        const val DEFAULT_SNOOZE = 5
    }
}
