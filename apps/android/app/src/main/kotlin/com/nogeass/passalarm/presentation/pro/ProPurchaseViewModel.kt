package com.nogeass.passalarm.presentation.pro

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.model.ProPeriod
import com.nogeass.passalarm.domain.model.ProProduct
import com.nogeass.passalarm.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProPurchaseUiState(
    val products: List<ProProduct> = emptyList(),
    val selectedPeriod: ProPeriod = ProPeriod.YEARLY,
    val isPurchasing: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProPurchaseViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProPurchaseUiState())
    val uiState: StateFlow<ProPurchaseUiState> = _uiState

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = subscriptionRepository.fetchProducts()
                _uiState.update { it.copy(products = products) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "商品を取得できませんでした")
                }
            }
        }
    }

    fun selectPeriod(period: ProPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    /**
     * Launch the billing flow for the currently selected period.
     *
     * @param activity Required by Play Billing to present its sheet.
     * @param onSuccess Called when the purchase completes successfully.
     */
    fun purchase(activity: Activity, onSuccess: () -> Unit) {
        val product = _uiState.value.products
            .firstOrNull { it.period == _uiState.value.selectedPeriod }
            ?: return

        _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                subscriptionRepository.purchase(activity, product)
                _uiState.update { it.copy(isPurchasing = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        errorMessage = e.localizedMessage ?: "購入に失敗しました",
                    )
                }
            }
        }
    }

    /**
     * Restore previous purchases via Google Play.
     *
     * @param onSuccess Called when restore finds an active subscription.
     */
    fun restore(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val status = subscriptionRepository.restorePurchases()
                _uiState.update { it.copy(isPurchasing = false) }
                if (status.isPro) {
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(errorMessage = "有効なサブスクリプションが見つかりませんでした")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        errorMessage = e.localizedMessage ?: "復元に失敗しました",
                    )
                }
            }
        }
    }
}
