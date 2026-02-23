package com.nogeass.passalarm.presentation.main

import androidx.lifecycle.ViewModel
import com.nogeass.passalarm.presentation.designsystem.ContentTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MainUiState(
    val selectedTab: ContentTab = ContentTab.LIST,
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun selectTab(tab: ContentTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
