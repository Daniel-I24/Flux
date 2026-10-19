package com.flux.wear.feature.capture

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.domain.usecase.CreateNoteUseCase
import com.flux.core.model.Classification
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.wear.sync.OutboxSender
import javax.inject.Inject

/** Saves on the watch first (agenda + alarm), then queues it for the phone (offline-safe). */
class WatchNoteSaver @Inject constructor(
    private val createNote: CreateNoteUseCase,
    private val outbox: OutboxRepository,
    private val outboxSender: OutboxSender,
) {
    suspend fun save(text: String, classification: Classification, corrected: Boolean): Note {
        val note = createNote(text, classification, DeviceSource.WATCH, categoryCorrected = corrected)
        outbox.enqueue(note)
        outboxSender.trigger()
        return note
    }
}
