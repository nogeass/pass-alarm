package com.nogeass.passalarm.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.usecase.DeletePlanUseCase
import com.nogeass.passalarm.domain.usecase.EnablePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmListUiState(
    val plans: List<AlarmPlan> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val enablePlanUseCase: EnablePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmListUiState())
    val uiState: StateFlow<AlarmListUiState> = _uiState

    init {
        load()
        observePlans()
    }

    fun load() {
        viewModelScope.launch {
            val plans = planRepository.fetchAll()
            _uiState.update { it.copy(plans = plans, isLoading = false) }
        }
    }

    fun togglePlan(planId: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            enablePlanUseCase.execute(planId, isEnabled)
            load()
        }
    }

    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            deletePlanUseCase.execute(planId)
            load()
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            planRepository.observeAll().collect { plans ->
                _uiState.update { it.copy(plans = plans, isLoading = false) }
            }
        }
    }
}
