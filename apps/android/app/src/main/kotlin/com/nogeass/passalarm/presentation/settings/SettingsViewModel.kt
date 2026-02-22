package com.nogeass.passalarm.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.usecase.UpdatePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val timeHHmm: String = "07:00",
    val weekdaysMask: Int = 0b00011111,
    val repeatCount: Int = 10,
    val intervalMin: Int = 5,
    val holidayAutoSkip: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val updatePlanUseCase: UpdatePlanUseCase
) : ViewModel() {

    private var plan: AlarmPlan? = null
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val plans = planRepository.fetchAll()
            plan = plans.firstOrNull()
            plan?.let { p ->
                _uiState.value = SettingsUiState(
                    timeHHmm = p.timeHHmm,
                    weekdaysMask = p.weekdaysMask,
                    repeatCount = p.repeatCount,
                    intervalMin = p.intervalMin,
                    holidayAutoSkip = p.holidayAutoSkip,
                    isLoading = false
                )
            }
        }
    }

    fun updateRepeatCount(count: Int) {
        _uiState.value = _uiState.value.copy(repeatCount = count)
    }

    fun updateIntervalMin(min: Int) {
        _uiState.value = _uiState.value.copy(intervalMin = min)
    }

    fun updateTime(time: String) {
        _uiState.value = _uiState.value.copy(timeHHmm = time)
    }

    fun updateWeekdaysMask(mask: Int) {
        _uiState.value = _uiState.value.copy(weekdaysMask = mask)
    }

    fun updateHolidayAutoSkip(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(holidayAutoSkip = enabled)
    }

    fun save() {
        val current = plan ?: return
        val state = _uiState.value
        viewModelScope.launch {
            updatePlanUseCase.execute(
                current.copy(
                    timeHHmm = state.timeHHmm,
                    weekdaysMask = state.weekdaysMask,
                    repeatCount = state.repeatCount,
                    intervalMin = state.intervalMin,
                    holidayAutoSkip = state.holidayAutoSkip
                )
            )
        }
    }
}
