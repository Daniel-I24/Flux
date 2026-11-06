package com.flux.core.data.alarm

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The reminder ringing right now in this process (at most one). The alarm service and the
 * full-screen screen observe it; any change to that note (completed, snoozed, deleted, edited,
 * here or on the other device) goes through the scheduler, which calls [stop]. That is how
 * "Posponer" or "Completar" on one device silences both.
 */
@Singleton
class ActiveAlarm @Inject constructor() {
    private val ringing = MutableStateFlow<String?>(null)
    val noteId: StateFlow<String?> = ringing.asStateFlow()

    fun start(noteId: String) {
        ringing.value = noteId
    }

    /** Stops only if [noteId] is the one ringing (another reminder may have taken over). */
    fun stop(noteId: String) {
        ringing.compareAndSet(noteId, null)
    }

    /** True when [noteId] stops ringing because someone acted on it; false when [timeoutMillis] runs out first. */
    suspend fun awaitHandled(noteId: String, timeoutMillis: Long): Boolean =
        withTimeoutOrNull(timeoutMillis) {
            ringing.first { it != noteId }
            true
        } ?: false
}
