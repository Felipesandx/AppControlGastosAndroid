package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import umg.edu.gt.domain.parser.QuickAddParser
import umg.edu.gt.domain.parser.ParsedTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import edu.umg.appcontrolgastos.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: UserSessionManager
    private val adapter = TransactionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Verificar si hay usuario registrado
        sessionManager = UserSessionManager(this)
        if (!sessionManager.isUserRegistered()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        binding.etQuickAdd.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                processQuickAdd()
                true
            } else {
                false
            }
        }

        binding.layoutQuickAdd.setEndIconOnClickListener {
            processQuickAdd()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sessionManager.isInitialized && sessionManager.isUserRegistered()) {
            loadDashboardData()
        }
    }

    private fun processQuickAdd() {
        val text = binding.etQuickAdd.text?.toString().orEmpty()
        val parsed = QuickAddParser.parse(text)

        if (parsed == null) {
            Toast.makeText(this, "Escribe concepto y monto (ej. Café 25)", Toast.LENGTH_SHORT).show()
            return
        }

        binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

        val dateString = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val transaction = Transaction(
            id = 0,
            amount = parsed.amount,
            description = parsed.description,
            date = dateString,
            timestamp = System.currentTimeMillis(),
            type = TransactionType.EXPENSE,
            category = parsed.category,
            isImpulse = false
        )

        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val error = financeService.register(transaction)
            if (error == null) {
                binding.etQuickAdd.text?.clear()
                binding.etQuickAdd.clearFocus()
                loadDashboardData()
            } else {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDashboardData() {
        val financeService = (application as ControlGastosApp).financeService
        val user = sessionManager.getUser()

        lifecycleScope.launch {
            val transactions = financeService.listTransactions()
            adapter.submitList(transactions)

            val summary = financeService.getMonthlySummary()

            // Días restantes en el mes actual
            val calendar = Calendar.getInstance()
            val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = (totalDaysInMonth - currentDay) + 1

            // Regla de negocio de la versión 1.1.0
            val safeToSpendToday = financeService.calculateDailySafeToSpend(
                profile = user,
                totalExpensesThisMonth = summary.totalExpenses,
                daysRemainingInMonth = daysRemaining
            )

            binding.tvTotalBalance.text = financeService.formatCurrency(summary.netBalance)
            binding.tvTotalIncomes.text = "↑ Ingresos: ${financeService.formatCurrency(summary.totalIncome)}"
            binding.tvTotalExpenses.text = "Disponible hoy: ${financeService.formatCurrency(safeToSpendToday)}"
        }
    }
}