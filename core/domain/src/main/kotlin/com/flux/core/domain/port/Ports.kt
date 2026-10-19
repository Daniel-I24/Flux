package com.flux.core.domain.port

import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.core.model.UserProfile
import java.time.ZoneId

/** Time source injected everywhere so tests control "now". */
interface Clock {
    fun now(): Long
    fun zone(): ZoneId
}

fun interface IdGenerator {
    fun newId(): String
}

data class ClassificationRequest(
    val text: String,
    val nowMillis: Long,
    val zone: ZoneId,
    val categories: List<Category>,
    val profile: UserProfile?,
)

/** Strategy contract: remote (Gemini through our server) or local NLP. */
interface NoteClassifier {
    val engine: ClassifierEngine
    suspend fun classify(request: ClassificationRequest): FluxResult<Classification>
}

/** Exact alarms (AlarmManager on Android); rescheduled after reboot by the app. */
interface AlarmScheduler {
    fun schedule(note: Note)
    fun cancel(noteId: String)
}

/** Stable id of this app installation (used for the one-phone-per-account rule). */
fun interface InstallationIdProvider {
    suspend fun installationId(): String
}

/** Notes classified offline are queued to be reclassified by the remote AI when there is internet. */
fun interface ReclassificationQueue {
    fun enqueue(noteId: String)
}
