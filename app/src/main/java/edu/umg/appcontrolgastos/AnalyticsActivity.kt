package edu.umg.appcontrolgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

class AnalyticsActivity : ComponentActivity() {

    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnalyticsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📊 Resumen Financiero",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
        } else {
            TotalSummaryCard(income = state.totalIncome, expense = state.totalExpense)
            Spacer(modifier = Modifier.height(48.dp))
            IncomeVsExpenseBar(income = state.totalIncome, expense = state.totalExpense)
        }
    }
}

@Composable
fun TotalSummaryCard(income: Double, expense: Double) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "GT"))
    val balance = income - expense

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Balance Total", style = MaterialTheme.typography.labelLarge)
            Text(
                text = format.format(balance),
                style = MaterialTheme.typography.displayMedium,
                color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ingresos", color = Color(0xFF2E7D32), style = MaterialTheme.typography.labelMedium)
                    Text(format.format(income), fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gastos", color = Color(0xFFC62828), style = MaterialTheme.typography.labelMedium)
                    Text(format.format(expense), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun IncomeVsExpenseBar(income: Double, expense: Double) {
    val total = income + expense

    if (total == 0.0) {
        Text("Registra movimientos para ver tu gráfica.", color = Color.Gray)
        return
    }

    val incomeWeight = (income / total).toFloat()
    val expenseWeight = (expense / total).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Proporción (Ingresos vs Gastos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.LightGray)
        ) {
            if (incomeWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(incomeWeight)
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50))
                )
            }
            if (expenseWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(expenseWeight)
                        .fillMaxHeight()
                        .background(Color(0xFFF44336))
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${(incomeWeight * 100).toInt()}%", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            Text(text = "${(expenseWeight * 100).toInt()}%", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
        }
    }
}
