package com.flux.core.data.repository

import com.flux.core.data.local.FluxDatabase
import com.flux.core.data.local.entity.OutboxEntity
import com.flux.core.domain.port.Clock
import com.flux.core.model.Note
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncMessageFactory
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Watch-side offline queue. Keyed by note id, so re-queuing the same note replaces it
 * instead of duplicating it; the phone also de-duplicates by note id (idempotency).
 */
class OutboxRepository @Inject constructor(
    database: FluxDatabase,
    private val codec: SyncCodec,
    private val factory: SyncMessageFactory,
    private val clock: Clock,
) {
    private val dao = database.outbox()

    fun observePendingCount(): Flow<Int> = dao.observeCount()

    suspend fun enqueue(note: Note) {
        dao.upsert(OutboxEntity(note.id, codec.encode(factory.noteCapture(note)), clock.now(), attempts = 0))
    }

    suspend fun pending(): List<OutboxEntity> = dao.pending()

    suspend fun markSent(noteId: String) = dao.remove(noteId)

    suspend fun markFailed(item: OutboxEntity) = dao.upsert(item.copy(attempts = item.attempts + 1))
}
