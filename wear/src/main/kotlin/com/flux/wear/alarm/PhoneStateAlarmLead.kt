package com.flux.wear.alarm

import com.flux.core.domain.port.AlarmLeadProvider
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject

/** The watch rings with the lead chosen on the phone (stored from its last snapshot, so it works offline). */
class PhoneStateAlarmLead @Inject constructor(private val store: WatchStateStore) : AlarmLeadProvider {
    override suspend fun globalLeadMinutes(): Int = store.phoneState().alarmLeadMinutes
}
