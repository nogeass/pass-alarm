package com.nogeass.passalarm.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.usecase.ComputeQueueUseCase
import com.nogeass.passalarm.domain.usecase.EnablePlanUseCase
import com.nogeass.passalarm.domain.usecase.SkipDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val plan: AlarmPlan? = null,
    val nextOccurrence: Occurrence? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val computeQueueUseCase: ComputeQueueUseCase,
    private val enablePlanUseCase: EnablePlanUseCase,
    private val skipDateUseCase: SkipDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val plans = planRepository.fetchAll()
            var plan = plans.firstOrNull()
            if (plan == null) {
                val defaultPlan = AlarmPlan()
                planRepository.save(defaultPlan)
                plan = planRepository.fetchAll().firstOrNull()
            }
            val queue = computeQueueUseCase.execute()
            val next = queue.firstOrNull { !it.isSkipped }
            _uiState.value = HomeUiState(plan = plan, nextOccurrence = next, isLoading = false)
        }
    }

    fun togglePlan(isEnabled: Boolean) {
        val plan = _uiState.value.plan ?: return
        viewModelScope.launch {
            enablePlanUseCase.execute(plan.id, isEnabled)
            load()
        }
    }

    fun skipToday() {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val today = dateFormat.format(Date())
            skipDateUseCase.execute(today)
            load()
        }
    }
}
