package edu.umg.appcontrolgastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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

@Composable
fun AppControlGastosComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF6EA8FE),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2B2B2B),
            onBackground = Color(0xFFF8F9FA),
            onSurface = Color(0xFFF8F9FA),
            onSurfaceVariant = Color(0xFFADB5BD)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0D6EFD),
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF1F3F5),
            onBackground = Color(0xFF212529),
            onSurface = Color(0xFF212529),
            onSurfaceVariant = Color(0xFF6C757D)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

class AnalyticsActivity : ComponentActivity() {

    private val viewModel: AnalyticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppControlGastosComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnalyticsScreen(
                        viewModel = viewModel,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, onBackClick: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "← Regresar",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "📊 Resumen Financiero",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
        } else {
            TotalSummaryCard(income = state.totalIncome, expense = state.totalExpense)

            Spacer(modifier = Modifier.height(32.dp))

            IncomeVsExpenseBar(income = state.totalIncome, expense = state.totalExpense)

            WeeklyHeatMap(heatMapData = state.weeklyHeatMap)
        }
    }
}

@Composable
fun TotalSummaryCard(income: Double, expense: Double) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "GT"))
    val balance = income - expense
    val isDark = isSystemInDarkTheme()

    val greenColor = if (isDark) Color(0xFF75B798) else Color(0xFF28A745)
    val redColor = if (isDark) Color(0xFFEA868F) else Color(0xFFDC3545)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Balance Total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = format.format(balance),
                style = MaterialTheme.typography.displayMedium,
                color = if (balance >= 0) greenColor else redColor,
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
                    Text("Ingresos", color = greenColor, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = format.format(income),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gastos", color = redColor, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = format.format(expense),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeVsExpenseBar(income: Double, expense: Double) {
    val total = income + expense
    val isDark = isSystemInDarkTheme()

    val greenColor = if (isDark) Color(0xFF75B798) else Color(0xFF28A745)
    val redColor = if (isDark) Color(0xFFEA868F) else Color(0xFFDC3545)

    if (total == 0.0) {
        Text(
            text = "Registra movimientos para ver tu gráfica.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val incomeWeight = (income / total).toFloat()
    val expenseWeight = (expense / total).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Proporción (Ingresos vs Gastos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (isDark) Color(0xFF343A40) else Color(0xFFE9ECEF))
        ) {
            if (incomeWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(incomeWeight)
                        .fillMaxHeight()
                        .background(greenColor)
                )
            }
            if (expenseWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(expenseWeight)
                        .fillMaxHeight()
                        .background(redColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${(incomeWeight * 100).toInt()}%", color = greenColor, fontWeight = FontWeight.Bold)
            Text(text = "${(expenseWeight * 100).toInt()}%", color = redColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeeklyHeatMap(heatMapData: List<DailyExpense>) {
    if (heatMapData.isEmpty()) return

    val maxExpense = heatMapData.maxOfOrNull { it.amount } ?: 0.0
    val isDark = isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Text(
            text = "Actividad de Gastos (Últimos 7 días)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            heatMapData.forEach { dayData ->
                val intensity = if (maxExpense > 0) (dayData.amount / maxExpense).toFloat() else 0f

                val boxColor = when {
                    dayData.amount == 0.0 -> if (isDark) Color(0xFF2B2B2B) else Color(0xFFEEEEEE)
                    intensity < 0.3f -> if (isDark) Color(0xFF623035) else Color(0xFFFFCDD2)
                    intensity < 0.7f -> if (isDark) Color(0xFFC62828) else Color(0xFFEF5350)
                    else -> if (isDark) Color(0xFFFF5252) else Color(0xFFB71C1C)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boxColor)
                    )
                    Text(
                        text = dayData.dayName.take(3),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
