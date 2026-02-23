package com.nogeass.passalarm.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.AlarmPlan
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.domain.repository.AlarmPlanRepository
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import com.nogeass.passalarm.domain.usecase.ComputeQueueUseCase
import com.nogeass.passalarm.domain.usecase.DeletePlanUseCase
import com.nogeass.passalarm.domain.usecase.SkipDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tutorial step within the interactive onboarding flow.
 */
enum class TutorialStep {
    CreateAlarm,
    SkipAlarm,
    DeleteAlarm,
}

/**
 * UI state for the tutorial steps (after permission is granted).
 */
data class TutorialUiState(
    val currentStep: TutorialStep = TutorialStep.CreateAlarm,
    val plans: List<AlarmPlan> = emptyList(),
    val queue: List<Occurrence> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val planRepository: AlarmPlanRepository,
    private val computeQueueUseCase: ComputeQueueUseCase,
    private val skipDateUseCase: SkipDateUseCase,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _tutorialState = MutableStateFlow(TutorialUiState())
    val tutorialState: StateFlow<TutorialUiState> = _tutorialState

    /**
     * Infer which tutorial step the user should be on, based on current data.
     * Enables mid-tutorial recovery (e.g. after app kill).
     */
    fun inferStep() {
        viewModelScope.launch {
            val plans = planRepository.fetchAll()
            if (plans.isEmpty()) {
                _tutorialState.update {
                    it.copy(
                        currentStep = TutorialStep.CreateAlarm,
                        plans = plans,
                        isLoading = false,
                    )
                }
                return@launch
            }

            val queue = computeQueueUseCase.execute()
            val hasSkipped = queue.any { it.isSkipped }

            val step = when {
                !hasSkipped -> TutorialStep.SkipAlarm
                else -> TutorialStep.DeleteAlarm
            }

            _tutorialState.update {
                it.copy(
                    currentStep = step,
                    plans = plans,
                    queue = queue,
                    isLoading = false,
                )
            }
        }
    }

    /**
     * Reload plans from repository (used after returning from alarm edit).
     */
    fun loadPlans() {
        viewModelScope.launch {
            val plans = planRepository.fetchAll()
            _tutorialState.update { it.copy(plans = plans) }
        }
    }

    /**
     * Reload the skip queue from the compute use case.
     */
    fun loadQueue() {
        viewModelScope.launch {
            val queue = computeQueueUseCase.execute()
            _tutorialState.update { it.copy(queue = queue) }
        }
    }

    /**
     * Skip a single occurrence (tutorial step 2).
     */
    fun skipOccurrence(planId: Long, date: String) {
        viewModelScope.launch {
            skipDateUseCase.execute(planId, date)
            loadQueue()
        }
    }

    /**
     * Delete a plan (tutorial step 3).
     */
    fun deletePlan(planId: Long) {
        viewModelScope.launch {
            deletePlanUseCase.execute(planId)
            val plans = planRepository.fetchAll()
            _tutorialState.update { it.copy(plans = plans) }
        }
    }

    /**
     * Advance to the given tutorial step.
     */
    fun advanceTo(step: TutorialStep) {
        _tutorialState.update { it.copy(currentStep = step) }
        // Reload relevant data for the new step
        when (step) {
            TutorialStep.SkipAlarm -> loadQueue()
            TutorialStep.DeleteAlarm -> loadPlans()
            else -> {}
        }
    }

    /**
     * Mark the tutorial as completed and persist to AppSettings.
     */
    fun completeTutorial(onDone: () -> Unit) {
        viewModelScope.launch {
            val settings = appSettingsRepository.get()
            appSettingsRepository.save(settings.copy(tutorialCompleted = true))
            onDone()
        }
    }
}
