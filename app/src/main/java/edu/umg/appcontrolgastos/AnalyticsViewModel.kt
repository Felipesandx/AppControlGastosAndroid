package edu.umg.appcontrolgastos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import umg.edu.gt.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers

// Estructura para cada día del mapa de calor
data class DailyExpense(val dayName: String, val amount: Double, val dateKey: String)

data class AnalyticsState(
    val isLoading: Boolean = true,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val weeklyHeatMap: List<DailyExpense> = emptyList() // NUEVA LISTA
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val financeService = (application as ControlGastosApp).financeService
    private val sessionManager = UserSessionManager(application)

    private val _uiState = MutableStateFlow(AnalyticsState())
    val uiState: StateFlow<AnalyticsState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = sessionManager.getActiveUserId()
                if (userId != -1L) {
                    val transactions = financeService.listTransactions(userId)

                    val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                    val totalExpense = expenses.sumOf { it.amount }

                    // --- LÓGICA DEL MAPA DE CALOR ---
                    val cal = Calendar.getInstance()
                    val sdfName = SimpleDateFormat("EEE", Locale("es", "GT"))
                    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    val heatMapList = mutableListOf<DailyExpense>()

                    cal.add(Calendar.DAY_OF_YEAR, -6)
                    // repeat(7) ejecuta el código 7 veces sin pedirte variables
                    repeat(7) {
                        val dName = sdfName.format(cal.time).replaceFirstChar { it.uppercase() }
                        val dKey = sdfKey.format(cal.time)
                        heatMapList.add(DailyExpense(dName, 0.0, dKey))
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    expenses.forEach { exp ->
                        // Usamos exp.timestamp (Long) en lugar de exp.date (String)
                        val expTimestamp = if (exp.timestamp > 0) exp.timestamp else System.currentTimeMillis()
                        val expDate = Date(expTimestamp)
                        val expKey = sdfKey.format(expDate)

                        val index = heatMapList.indexOfFirst { it.dateKey == expKey }
                        if (index != -1) {
                            heatMapList[index] = heatMapList[index].copy(
                                amount = heatMapList[index].amount + exp.amount
                            )
                        }
                    }

                    _uiState.value = AnalyticsState(
                        isLoading = false,
                        totalIncome = income,
                        totalExpense = totalExpense,
                        weeklyHeatMap = heatMapList
                    )
                } else {
                    // Si no hay usuario activo, apagamos la carga
                    _uiState.value = AnalyticsState(isLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // APAGAMOS LA CARGA SI HAY ERROR para que no se quede trabado
                _uiState.value = AnalyticsState(isLoading = false)
            }
        }
    }
}