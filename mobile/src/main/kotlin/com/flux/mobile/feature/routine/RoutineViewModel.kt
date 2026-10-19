package com.flux.mobile.feature.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.SaveRoutineUseCase
import com.flux.core.model.DailyRoutine
import com.flux.core.model.FluxResult
import com.flux.core.model.TimeOfDay
import com.flux.core.model.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoutineTime { WAKE_UP, SLEEP, BREAKFAST, LUNCH, DINNER, ACTIVITY_START, ACTIVITY_END }

data class RoutineUiState(
    val times: Map<RoutineTime, TimeOfDay> = defaultTimes(DailyRoutine()),
    val activityDays: Set<DayOfWeek> = DailyRoutine().activityBlocks.keys,
    val invalid: Boolean = false,
    val saving: Boolean = false,
)

private fun defaultTimes(routine: DailyRoutine): Map<RoutineTime, TimeOfDay> {
    val block = routine.activityBlocks.values.firstOrNull() ?: DailyRoutine().activityBlocks.values.first()
    return mapOf(
        RoutineTime.WAKE_UP to routine.wakeUp,
        RoutineTime.SLEEP to routine.sleep,
        RoutineTime.BREAKFAST to routine.breakfast,
        RoutineTime.LUNCH to routine.lunch,
        RoutineTime.DINNER to routine.dinner,
        RoutineTime.ACTIVITY_START to block.start,
        RoutineTime.ACTIVITY_END to block.end,
    )
}

/** One activity block (same hours) for the selected days keeps the form simple. */
@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val saveRoutine: SaveRoutineUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            observeProfile().first()?.routine?.let { routine ->
                state.update { it.copy(times = defaultTimes(routine), activityDays = routine.activityBlocks.keys) }
            }
        }
    }

    fun setTime(field: RoutineTime, time: TimeOfDay) = state.update { it.copy(times = it.times + (field to time), invalid = false) }

    fun toggleDay(day: DayOfWeek) = state.update {
        it.copy(activityDays = if (day in it.activityDays) it.activityDays - day else it.activityDays + day)
    }

    fun save(onSaved: () -> Unit) {
        val current = state.value
        val time = { field: RoutineTime -> current.times.getValue(field) }
        val block = TimeRange(time(RoutineTime.ACTIVITY_START), time(RoutineTime.ACTIVITY_END))
        val routine = DailyRoutine(
            wakeUp = time(RoutineTime.WAKE_UP),
            sleep = time(RoutineTime.SLEEP),
            breakfast = time(RoutineTime.BREAKFAST),
            lunch = time(RoutineTime.LUNCH),
            dinner = time(RoutineTime.DINNER),
            activityBlocks = current.activityDays.associateWith { block },
        )
        viewModelScope.launch {
            state.update { it.copy(saving = true) }
            when (saveRoutine(routine)) {
                is FluxResult.Success -> onSaved()
                is FluxResult.Failure -> state.update { it.copy(invalid = true) }
            }
            state.update { it.copy(saving = false) }
        }
    }
}
