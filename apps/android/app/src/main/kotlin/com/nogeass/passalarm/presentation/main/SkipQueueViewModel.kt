package com.nogeass.passalarm.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import com.nogeass.passalarm.domain.usecase.ComputeQueueUseCase
import com.nogeass.passalarm.domain.usecase.SkipDateUseCase
import com.nogeass.passalarm.domain.usecase.UpdateAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkipUiState(
    val queue: List<Occurrence> = emptyList(),
    val isPro: Boolean = false,
    val showProPurchase: Boolean = false,
    val holidayAutoSkip: Boolean = true,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SkipQueueViewModel @Inject constructor(
    private val computeQueueUseCase: ComputeQueueUseCase,
    private val skipDateUseCase: SkipDateUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkipUiState())
    val uiState: StateFlow<SkipUiState> = _uiState

    init {
        load()
        observeProStatus()
    }

    fun load() {
        viewModelScope.launch {
            val settings = appSettingsRepository.get()
            val queue = computeQueueUseCase.execute()
            _uiState.update {
                it.copy(
                    queue = queue,
                    holidayAutoSkip = settings.holidayAutoSkip,
                    isLoading = false,
                )
            }
        }
    }

    fun skip(planId: Long, date: String) {
        viewModelScope.launch {
            skipDateUseCase.execute(planId, date)
            load()
        }
    }

    fun unskip(planId: Long, date: String) {
        viewModelScope.launch {
            skipDateUseCase.unskip(planId, date)
            load()
        }
    }

    fun toggleHolidayAutoSkip(enabled: Boolean) {
        viewModelScope.launch {
            val settings = appSettingsRepository.get()
            updateAppSettingsUseCase.execute(settings.copy(holidayAutoSkip = enabled))
            _uiState.update { it.copy(holidayAutoSkip = enabled) }
            load()
        }
    }

    fun showProPurchase() {
        _uiState.update { it.copy(showProPurchase = true) }
    }

    fun dismissProPurchase() {
        _uiState.update { it.copy(showProPurchase = false) }
    }

    private fun observeProStatus() {
        subscriptionRepository.observeStatus()
            .onEach { status ->
                _uiState.update { it.copy(isPro = status.isPro) }
            }
            .launchIn(viewModelScope)
    }
}
