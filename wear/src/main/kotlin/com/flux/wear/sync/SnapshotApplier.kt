package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.sync.protocol.WatchSnapshot
import com.flux.wear.data.PhoneState
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject

/**
 * Applies the phone's snapshot: categories, agenda (cache) and account state.
 * Watch alarms follow the agenda, so completing on the phone cancels them here.
 */
class SnapshotApplier @Inject constructor(
    private val categories: RoomCategoryRepository,
    private val notes: NoteRepository,
    private val alarms: AlarmScheduler,
    private val stateStore: WatchStateStore,
    private val outbox: OutboxRepository,
    private val clock: Clock,
) {
    suspend fun apply(snapshot: WatchSnapshot) {
        categories.replaceAll(snapshot.categories)
        val incoming = snapshot.agenda.associateBy { it.id }
        // Captures still waiting in the offline queue are not in the phone's agenda yet: keep them.
        val stillQueued = outbox.pending().mapNotNull { notes.getNote(it.noteId) }.filterNot { it.id in incoming }
        val previous = notes.getPendingAlarms().map { it.id }.toSet()
        (previous - incoming.keys - stillQueued.map { it.id }.toSet()).forEach(alarms::cancel)
        notes.clear()
        (snapshot.agenda + stillQueued).forEach { note ->
            notes.upsert(note)
            if (note.hasPendingAlarm) alarms.schedule(note) else alarms.cancel(note.id)
        }
        stateStore.savePhoneState(
            PhoneState(
                accountState = snapshot.accountState,
                linkedAccountId = snapshot.linkedAccountId,
                userName = snapshot.userName,
                snoozeMinutes = snapshot.snoozeMinutes,
                quietHours = snapshot.quietHours,
                receivedAt = clock.now(),
            ),
        )
    }
}
