package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import com.flux.core.sync.protocol.WatchSnapshot
import com.flux.wear.data.PhoneState
import com.flux.wear.data.WatchStateStore
import com.flux.wear.surface.SurfaceRefresher
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
    private val surfaces: SurfaceRefresher,
) {
    suspend fun apply(snapshot: WatchSnapshot) {
        categories.replaceAll(snapshot.categories)
        val queued = outbox.pending().mapNotNull { notes.getNote(it.noteId) }
        val agenda = mergeAgenda(snapshot.agenda, queued)
        val kept = agenda.map { it.id }.toSet()
        notes.getPendingAlarms().map { it.id }.filterNot { it in kept }.forEach(alarms::cancel)
        // Atomic: an alert that is ringing must not see its note vanish for an instant and close itself.
        notes.replaceAll(agenda)
        val now = clock.now()
        agenda.forEach { note ->
            when {
                !note.hasPendingAlarm -> alarms.cancel(note.id)
                // Overdue notes are listed in Vencidos but never ring again (and a ringing one keeps ringing).
                note.dueAt!! > now -> alarms.schedule(note)
            }
        }
        stateStore.savePhoneState(
            PhoneState(
                accountState = snapshot.accountState,
                linkedAccountId = snapshot.linkedAccountId,
                userName = snapshot.userName,
                snoozeMinutes = snapshot.snoozeMinutes,
                quietHours = snapshot.quietHours,
                receivedAt = now,
                alarmDurationSeconds = snapshot.alarmDurationSeconds,
                phoneAppVersion = snapshot.phoneAppVersion,
            ),
        )
        surfaces.refresh()
    }
}

/**
 * The phone's agenda plus what the offline queue still holds: captures it does not know yet, and
 * changes made here (completed, snoozed…) newer than the revision the phone just sent.
 */
internal fun mergeAgenda(phone: List<Note>, queued: List<Note>): List<Note> {
    val local = queued.associateBy { it.id }
    val known = phone.map { it.id }.toSet()
    return phone.map { note -> local[note.id]?.takeIf { it.updatedAt > note.updatedAt } ?: note } + queued.filterNot { it.id in known }
}
