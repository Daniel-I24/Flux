package com.flux.mobile.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.SetAlarmLeadUseCase
import com.flux.core.model.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Global "Avisarme" lead: changing it also moves every pending alarm that follows it. */
@HiltViewModel
class AlarmLeadViewModel @Inject constructor(private val setAlarmLead: SetAlarmLeadUseCase) : ViewModel() {
    fun select(minutes: Int?) {
        viewModelScope.launch { setAlarmLead(minutes ?: AppSettings.DEFAULT_ALARM_LEAD_MINUTES) }
    }
}
