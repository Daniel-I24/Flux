package com.flux.core.domain.usecase

import com.flux.core.domain.port.ReclassificationQueue
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import javax.inject.Inject

/** A note typed on the phone: classify (remote → local), save, and queue a remote retry if needed. */
class CaptureNoteUseCase @Inject constructor(
    private val classify: ClassifyNoteUseCase,
    private val create: CreateNoteUseCase,
    private val reclassification: ReclassificationQueue,
) {
    suspend operator fun invoke(text: String): FluxResult<Note> = when (val result = classify(text)) {
        is FluxResult.Failure -> result
        is FluxResult.Success -> {
            val note = create(text.trim(), result.value, DeviceSource.PHONE)
            if (note.engine == ClassifierEngine.LOCAL) reclassification.enqueue(note.id)
            FluxResult.Success(note)
        }
    }
}
