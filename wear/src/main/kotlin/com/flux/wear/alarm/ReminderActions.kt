package com.flux.wear.alarm

import com.flux.core.domain.usecase.CompleteNoteUseCase
import com.flux.core.domain.usecase.DeleteNoteUseCase
import com.flux.core.domain.usecase.RescheduleNoteUseCase
import com.flux.core.domain.usecase.SnoozeNoteUseCase
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.wear.data.WatchStateStore
import com.flux.wear.surface.SurfaceRefresher
import com.flux.wear.sync.NoteRevisionQueue
import javax.inject.Inject

/**
 * Completar / Posponer / Reprogramar / Eliminar on the watch: applied locally (which silences the
 * alarm), queued as the new revision of the note (delivered exactly once, even offline) and, for
 * reminder actions, also sent right away so the phone stops ringing.
 */
class ReminderActions @Inject constructor(
    private val complete: CompleteNoteUseCase,
    private val snooze: SnoozeNoteUseCase,
    private val reschedule: RescheduleNoteUseCase,
    private val delete: DeleteNoteUseCase,
    private val stateStore: WatchStateStore,
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val revisions: NoteRevisionQueue,
    private val surfaces: SurfaceRefresher,
) {
    suspend fun complete(noteId: String) {
        queue(complete.invoke(noteId))
        sendNow(factory.complete(noteId))
    }

    suspend fun snooze(noteId: String) {
        val minutes = stateStore.phoneState().snoozeMinutes
        queue(snooze.invoke(noteId, minutes))
        sendNow(factory.snooze(noteId, minutes))
    }

    suspend fun reschedule(noteId: String, dueAt: Long) = queue(reschedule.invoke(noteId, dueAt))

    suspend fun delete(noteId: String) = queue(delete.invoke(noteId))

    private suspend fun queue(result: FluxResult<Note>) {
        (result as? FluxResult.Success)?.value?.let { revisions.push(it) }
        surfaces.refresh()
    }

    private suspend fun sendNow(envelope: SyncEnvelope) {
        gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true).forEach { gateway.send(it.id, envelope) }
    }
}
