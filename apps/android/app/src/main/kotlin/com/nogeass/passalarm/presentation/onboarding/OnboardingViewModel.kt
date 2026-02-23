package com.nogeass.passalarm.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.usecase.SeedDefaultAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val seedDefaultAlarmsUseCase: SeedDefaultAlarmsUseCase,
) : ViewModel() {

    fun seedDefaultAlarms() {
        viewModelScope.launch {
            seedDefaultAlarmsUseCase.execute()
        }
    }
}
