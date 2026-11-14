package com.flux.core.domain.port

/** The global "Avisarme" lead in minutes: the phone's settings, or what the phone sent to the watch. */
fun interface AlarmLeadProvider {
    suspend fun globalLeadMinutes(): Int
}
