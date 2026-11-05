package com.flux.core.domain.usecase

import com.flux.core.domain.port.ReclassificationQueue
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import javax.inject.Inject

/**
 * A note dictated or typed on the phone, in two steps like on the watch: [classify] feeds the
 * preview (with questions when something is ambiguous) and [save] stores what the user confirmed.
 */
class CaptureNoteUseCase @Inject constructor(
    private val classifyNote: ClassifyNoteUseCase,
    private val create: CreateNoteUseCase,
    private val reclassification: ReclassificationQueue,
) {
    suspend fun classify(text: String): FluxResult<Classification> = classifyNote(text)

    suspend fun save(text: String, classification: Classification, categoryCorrected: Boolean = false): Note {
        val note = create(text.trim(), classification, DeviceSource.PHONE, categoryCorrected = categoryCorrected)
        if (note.engine == ClassifierEngine.LOCAL && !categoryCorrected) reclassification.enqueue(note.id)
        return note
    }

    /** Classify and save at once: the first option of any ambiguity is kept. */
    suspend operator fun invoke(text: String): FluxResult<Note> = when (val result = classify(text)) {
        is FluxResult.Failure -> result
        is FluxResult.Success -> FluxResult.Success(save(text, result.value))
    }
}
