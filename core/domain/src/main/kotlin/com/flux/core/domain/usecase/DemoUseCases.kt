package com.flux.core.domain.usecase

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.RemoteAiStatus
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Priority
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** One example note; [dueInSeconds] is relative to the moment the demo is loaded. */
data class DemoNote(
    val text: String,
    val title: String,
    val categoryId: String,
    val dueInSeconds: Long? = null,
    val requiresAlarm: Boolean = false,
    val priority: Priority = Priority.NORMAL,
)

/**
 * Demo mode: loads example notes (already classified, no AI needed) with stable ids so
 * loading twice replaces them instead of duplicating, and they can be removed later.
 */
class LoadDemoDataUseCase @Inject constructor(private val create: CreateNoteUseCase, private val clock: Clock) {
    suspend operator fun invoke(examples: List<DemoNote>): Int {
        val now = clock.now()
        examples.forEachIndexed { index, demo ->
            val classification = Classification(
                categoryId = demo.categoryId,
                title = demo.title,
                dueAt = demo.dueInSeconds?.let { now + it * MILLIS_PER_SECOND },
                priority = demo.priority,
                requiresAlarm = demo.requiresAlarm && demo.dueInSeconds != null,
                confidence = 1.0,
                engine = ClassifierEngine.LOCAL,
            )
            create(demo.text, classification, DeviceSource.PHONE, noteId = demoId(index), categoryCorrected = true)
        }
        return examples.size
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L
    }
}

class ClearDemoDataUseCase @Inject constructor(private val notes: NoteRepository, private val delete: DeleteNoteUseCase) {
    suspend operator fun invoke(): Int {
        val demo = notes.observeNotes().first().filter { it.id.startsWith(DEMO_PREFIX) }
        demo.forEach { delete(it.id) }
        return demo.size
    }
}

class CheckRemoteAiUseCase @Inject constructor(private val status: RemoteAiStatus) {
    suspend operator fun invoke(): Boolean = status.isAvailable()
}

private const val DEMO_PREFIX = "demo-"

internal fun demoId(index: Int) = "$DEMO_PREFIX${index + 1}"
