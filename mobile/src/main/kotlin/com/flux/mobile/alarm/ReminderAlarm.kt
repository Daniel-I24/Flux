package com.flux.mobile.alarm

import com.flux.core.domain.policy.QuietHoursPolicy
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.GetReminderUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.domain.usecase.Reminder
import com.flux.core.model.AppSettings
import com.flux.core.model.TimeOfDay
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** What to do when a reminder is due: nothing (already handled), a silent notice, or ring. */
sealed interface AlarmDecision {
    data object Skip : AlarmDecision
    data class Quiet(val reminder: Reminder, val settings: AppSettings) : AlarmDecision
    data class Ring(val reminder: Reminder, val settings: AppSettings) : AlarmDecision
}

/** Loads the reminder with its sound and applies FLUX quiet hours (N-035, quiet hours policy). */
class ReminderAlarm @Inject constructor(
    private val getReminder: GetReminderUseCase,
    private val observeSettings: ObserveSettingsUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val clock: Clock,
) {
    suspend fun decide(noteId: String): AlarmDecision {
        val settings = observeSettings().first()
        val reminder = getReminder(noteId, settings.defaultSound)?.takeIf { it.note.hasPendingAlarm } ?: return AlarmDecision.Skip
        val routine = observeProfile().first()?.routine
        val now = Instant.ofEpochMilli(clock.now()).atZone(clock.zone()).toLocalTime()
        val quiet = settings.quietHoursEnabled && routine != null && QuietHoursPolicy.isQuiet(TimeOfDay.of(now.hour, now.minute), routine)
        return if (quiet) AlarmDecision.Quiet(reminder, settings) else AlarmDecision.Ring(reminder, settings)
    }
}
