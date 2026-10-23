package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.umg.appcontrolgastos.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import umg.edu.gt.domain.model.TransactionType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = TransactionAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        // Abre la pantalla de agregar al presionar el FAB
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
    }

    // onResume se ejecuta cada vez que la pantalla vuelve a estar visible
    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val financeService = (application as ControlGastosApp).financeService

        lifecycleScope.launch {
            // 1. Usamos listTransactions() en lugar de getAllTransactions()
            val transactions = financeService.listTransactions()
            adapter.submitList(transactions)

            // 2. Usamos tu excelente getMonthlySummary() para los totales
            val summary = financeService.getMonthlySummary()

            binding.tvTotalBalance.text = financeService.formatCurrency(summary.netBalance)
            binding.tvTotalIncomes.text = "↑ Ingresos: ${financeService.formatCurrency(summary.totalIncome)}"
            binding.tvTotalExpenses.text = "↓ Gastos: ${financeService.formatCurrency(summary.totalExpenses)}"
        }
    }
}