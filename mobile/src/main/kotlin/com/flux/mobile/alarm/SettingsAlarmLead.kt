package com.flux.mobile.alarm

import com.flux.core.domain.port.AlarmLeadProvider
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** The phone's global lead comes from its own settings. */
class SettingsAlarmLead @Inject constructor(private val observeSettings: ObserveSettingsUseCase) : AlarmLeadProvider {
    override suspend fun globalLeadMinutes(): Int = observeSettings().first().alarmLeadMinutes
}
