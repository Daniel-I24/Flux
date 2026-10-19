package com.flux.core.sync.protocol

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.model.Note
import javax.inject.Inject

/** Factory: the only place that builds envelopes, so ids, version and timestamps are always set. */
class SyncMessageFactory @Inject constructor(
    private val clock: Clock,
    private val ids: IdGenerator,
) {
    fun envelope(message: SyncMessage): SyncEnvelope =
        SyncEnvelope(version = SyncProtocol.VERSION, id = ids.newId(), sentAt = clock.now(), message = message)

    fun hello(appVersion: Int, linkedAccountId: String?) = envelope(SyncMessage.Hello(appVersion, linkedAccountId))

    fun classifyRequest(text: String): SyncEnvelope =
        envelope(SyncMessage.ClassifyRequest(requestId = ids.newId(), text = text.take(Note.MAX_TEXT_LENGTH)))

    fun noteCapture(note: Note) = envelope(SyncMessage.NoteCapture(note))

    fun noteAck(noteId: String) = envelope(SyncMessage.NoteAck(noteId))

    fun complete(noteId: String) = envelope(SyncMessage.NoteAction(noteId, NoteActionType.COMPLETE))

    fun snooze(noteId: String, minutes: Int) = envelope(SyncMessage.NoteAction(noteId, NoteActionType.SNOOZE, minutes))
}
