package edu.umg.appcontrolgastos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val sessionManager = UserSessionManager(application)
    private val financeService = (application as ControlGastosApp).financeService

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = sessionManager.getActiveUserId()
            if (userId != -1L) {
                val summary = financeService.getMonthlySummary(userId)
                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    totalIncome = summary.totalIncome,
                    totalExpense = summary.totalExpenses
                )
            } else {
                _uiState.value = AnalyticsUiState(isLoading = false)
            }
        }
    }
}
