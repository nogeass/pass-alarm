package com.nogeass.passalarm.presentation.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.Occurrence
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import com.nogeass.passalarm.domain.usecase.ComputeQueueUseCase
import com.nogeass.passalarm.domain.usecase.SkipDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QueueUiState(
    val queue: List<Occurrence> = emptyList(),
    val isLoading: Boolean = true,
    val isPro: Boolean = false,
    val showProPurchase: Boolean = false,
)

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val computeQueueUseCase: ComputeQueueUseCase,
    private val skipDateUseCase: SkipDateUseCase,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState

    init {
        load()
        observeProStatus()
    }

    fun load() {
        viewModelScope.launch {
            val queue = computeQueueUseCase.execute()
            _uiState.update { it.copy(queue = queue, isLoading = false) }
        }
    }

    fun skip(date: String) {
        viewModelScope.launch {
            skipDateUseCase.execute(date)
            load()
        }
    }

    fun unskip(date: String) {
        viewModelScope.launch {
            skipDateUseCase.unskip(date)
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
