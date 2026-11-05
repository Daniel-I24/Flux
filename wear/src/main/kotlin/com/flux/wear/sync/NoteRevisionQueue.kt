package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.model.Note
import javax.inject.Inject

/** Queues the latest revision of a note for the phone and starts delivery (offline-safe, exactly once). */
class NoteRevisionQueue @Inject constructor(
    private val outbox: OutboxRepository,
    private val sender: OutboxSender,
) {
    suspend fun push(note: Note) {
        outbox.enqueue(note)
        sender.trigger()
    }
}
