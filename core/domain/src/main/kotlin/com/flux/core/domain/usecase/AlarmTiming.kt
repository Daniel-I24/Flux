package com.flux.core.domain.usecase

import com.flux.core.domain.port.AlarmLeadProvider
import com.flux.core.domain.port.Clock
import com.flux.core.model.Note
import javax.inject.Inject

/**
 * When a reminder rings: its own lead (or the global one) before its time. If that moment already
 * passed, a new, edited or snoozed note rings at its time instead ([fallbackToDue]); sync and
 * restore never do that, so a reminder that already rang early does not ring a second time.
 */
class AlarmTiming @Inject constructor(private val lead: AlarmLeadProvider, private val clock: Clock) {
    suspend fun leadMinutes(note: Note): Int = note.alarmLeadMinutes ?: lead.globalLeadMinutes()

    /** The planned ring time, past or future; null without a pending alarm. */
    suspend fun ringsAt(note: Note): Long? =
        note.dueAt?.takeIf { note.hasPendingAlarm }?.minus(leadMinutes(note) * MILLIS_PER_MINUTE)

    /** The next ring time after now, or null when nothing should be scheduled. */
    suspend fun next(note: Note, fallbackToDue: Boolean): Long? {
        val early = ringsAt(note) ?: return null
        val now = clock.now()
        return when {
            early > now -> early
            fallbackToDue && note.dueAt!! > now -> note.dueAt
            else -> null
        }
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
