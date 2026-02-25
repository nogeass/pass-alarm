package com.nogeass.passalarm.presentation.redeem

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nogeass.passalarm.domain.repository.ServerEntitlementRepository
import com.nogeass.passalarm.domain.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RedeemStep {
    Loading,
    Disabled,
    NeedsAuth,
    ReadyToClaim,
    Claiming,
    Success,
    Error,
}

data class RedeemUiState(
    val step: RedeemStep = RedeemStep.Loading,
    val token: String = "",
    val errorMessage: String? = null,
    val userEmail: String? = null,
)

@HiltViewModel
class RedeemViewModel @Inject constructor(
    private val authService: AuthService,
    private val serverEntitlementRepository: ServerEntitlementRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemUiState())
    val uiState: StateFlow<RedeemUiState> = _uiState

    init {
        val navToken = savedStateHandle.get<String>("token")
        if (!navToken.isNullOrBlank()) {
            _uiState.update { it.copy(token = navToken) }
        }
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            try {
                val disabled = serverEntitlementRepository.isRedeemDisabled()
                if (disabled) {
                    _uiState.update { it.copy(step = RedeemStep.Disabled) }
                    return@launch
                }

                val user = authService.currentUser
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            step = RedeemStep.ReadyToClaim,
                            userEmail = user.email,
                        )
                    }
                } else {
                    _uiState.update { it.copy(step = RedeemStep.NeedsAuth) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        step = RedeemStep.Error,
                        errorMessage = e.localizedMessage ?: "初期化に失敗しました",
                    )
                }
            }
        }
    }

    fun updateToken(newToken: String) {
        _uiState.update { it.copy(token = newToken) }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(step = RedeemStep.Loading) }
                val user = authService.signInWithGoogle(activity)
                _uiState.update {
                    it.copy(
                        step = RedeemStep.ReadyToClaim,
                        userEmail = user.email,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        step = RedeemStep.NeedsAuth,
                        errorMessage = e.localizedMessage ?: "サインインに失敗しました",
                    )
                }
            }
        }
    }

    fun claimToken() {
        val token = _uiState.value.token.trim()
        if (token.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "トークンを入力してください")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(step = RedeemStep.Claiming, errorMessage = null) }
            try {
                serverEntitlementRepository.claimToken(token)
                _uiState.update { it.copy(step = RedeemStep.Success) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        step = RedeemStep.Error,
                        errorMessage = e.localizedMessage ?: "特典の取得に失敗しました",
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.update { it.copy(errorMessage = null) }
        checkInitialState()
    }
}
