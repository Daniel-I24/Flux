package com.flux.core.domain.usecase

import com.flux.core.model.AppSettings
import javax.inject.Inject

/** Changes the global "Avisarme" lead and moves every pending alarm that follows it. */
class SetAlarmLeadUseCase @Inject constructor(
    private val updateSettings: UpdateSettingsUseCase,
    private val reschedule: RescheduleAlarmsUseCase,
) {
    suspend operator fun invoke(minutes: Int) {
        if (minutes !in AppSettings.ALARM_LEAD_OPTIONS) return
        updateSettings { it.copy(alarmLeadMinutes = minutes) }
        reschedule()
    }
}
