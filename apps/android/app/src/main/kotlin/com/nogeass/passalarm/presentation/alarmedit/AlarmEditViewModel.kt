package com.nogeass.passalarm.presentation.alarmedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.usecase.CreatePlanUseCase
import com.nogeass.passalarm.domain.usecase.DeletePlanUseCase
import com.nogeass.passalarm.domain.usecase.UpdatePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmEditUiState(
    val isNew: Boolean = true,
    val timeHHmm: String = "07:00",
    val label: String = "",
    val weekdaysMask: Int = 0b00011111, // Mon-Fri
    val repeatCount: Int = 10,
    val intervalMin: Int = 5,
    val soundId: String = "default",
    val isEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val saveError: String? = null,
)

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: AlarmPlanRepository,
    private val createPlanUseCase: CreatePlanUseCase,
    private val updatePlanUseCase: UpdatePlanUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
) : ViewModel() {

    private val planId: Long? = savedStateHandle.get<String>("planId")
        ?.takeIf { it != "new" }
        ?.toLongOrNull()

    private var existingPlan: AlarmPlan? = null

    private val _uiState = MutableStateFlow(AlarmEditUiState())
    val uiState: StateFlow<AlarmEditUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            if (planId != null) {
                val plan = planRepository.fetchById(planId)
                if (plan != null) {
                    existingPlan = plan
                    _uiState.update {
                        it.copy(
                            isNew = false,
                            timeHHmm = plan.timeHHmm,
                            label = plan.label,
                            weekdaysMask = plan.weekdaysMask,
                            repeatCount = plan.repeatCount,
                            intervalMin = plan.intervalMin,
                            soundId = plan.soundId,
                            isEnabled = plan.isEnabled,
                            isLoading = false,
                        )
                    }
                    return@launch
                }
            }
            // New plan defaults
            _uiState.update { it.copy(isNew = true, isLoading = false) }
        }
    }

    fun updateTime(time: String) {
        _uiState.update { it.copy(timeHHmm = time) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun updateWeekdaysMask(mask: Int) {
        _uiState.update { it.copy(weekdaysMask = mask) }
    }

    fun updateRepeatCount(count: Int) {
        _uiState.update { it.copy(repeatCount = count.coerceIn(1, 20)) }
    }

    fun updateIntervalMin(min: Int) {
        _uiState.update { it.copy(intervalMin = min.coerceIn(1, 30)) }
    }

    fun updateSoundId(soundId: String) {
        _uiState.update { it.copy(soundId = soundId) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.isNew) {
                val plan = AlarmPlan(
                    label = state.label,
                    timeHHmm = state.timeHHmm,
                    weekdaysMask = state.weekdaysMask,
                    repeatCount = state.repeatCount,
                    intervalMin = state.intervalMin,
                    soundId = state.soundId,
                    isEnabled = state.isEnabled,
                )
                val success = createPlanUseCase.execute(plan)
                if (success) {
                    _uiState.update { it.copy(isSaved = true) }
                } else {
                    _uiState.update { it.copy(saveError = "Proプランにアップグレードしてアラームを追加しよう") }
                }
            } else {
                val existing = existingPlan ?: return@launch
                val updated = existing.copy(
                    label = state.label,
                    timeHHmm = state.timeHHmm,
                    weekdaysMask = state.weekdaysMask,
                    repeatCount = state.repeatCount,
                    intervalMin = state.intervalMin,
                    soundId = state.soundId,
                    isEnabled = state.isEnabled,
                )
                updatePlanUseCase.execute(updated)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }

    fun delete() {
        val id = planId ?: return
        viewModelScope.launch {
            deletePlanUseCase.execute(id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }
}
