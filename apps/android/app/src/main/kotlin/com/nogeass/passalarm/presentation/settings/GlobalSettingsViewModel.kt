package com.nogeass.passalarm.presentation.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.repository.AppSettingsRepository
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import com.nogeass.passalarm.domain.usecase.UpdateAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GlobalSettingsUiState(
    val holidayAutoSkip: Boolean = true,
    val isPro: Boolean = false,
    val showProPurchase: Boolean = false,
    val isLoading: Boolean = true,
    val restoreMessage: String? = null,
)

@HiltViewModel
class GlobalSettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val updateAppSettingsUseCase: UpdateAppSettingsUseCase,
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSettingsUiState())
    val uiState: StateFlow<GlobalSettingsUiState> = _uiState

    init {
        load()
        observeProStatus()
    }

    private fun load() {
        viewModelScope.launch {
            val settings = appSettingsRepository.get()
            _uiState.update {
                it.copy(
                    holidayAutoSkip = settings.holidayAutoSkip,
                    isLoading = false,
                )
            }
        }
    }

    fun toggleHolidayAutoSkip(enabled: Boolean) {
        viewModelScope.launch {
            val settings = appSettingsRepository.get()
            updateAppSettingsUseCase.execute(settings.copy(holidayAutoSkip = enabled))
            _uiState.update { it.copy(holidayAutoSkip = enabled) }
        }
    }

    fun showProPurchase() {
        _uiState.update { it.copy(showProPurchase = true) }
    }

    fun dismissProPurchase() {
        _uiState.update { it.copy(showProPurchase = false) }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                val status = subscriptionRepository.restorePurchases()
                if (status.isPro) {
                    _uiState.update {
                        it.copy(isPro = true, restoreMessage = "復元しました")
                    }
                } else {
                    _uiState.update {
                        it.copy(restoreMessage = "有効なサブスクリプションが見つかりませんでした")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(restoreMessage = e.localizedMessage ?: "復元に失敗しました")
                }
            }
        }
    }

    fun clearRestoreMessage() {
        _uiState.update { it.copy(restoreMessage = null) }
    }

    private fun observeProStatus() {
        subscriptionRepository.observeStatus()
            .onEach { status ->
                _uiState.update { it.copy(isPro = status.isPro) }
            }
            .launchIn(viewModelScope)
    }
}
