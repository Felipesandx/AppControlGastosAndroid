package edu.umg.appcontrolgastos

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
        // 1. Inicializar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configurar el RecyclerView
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        // 3. Cargar datos de la base de datos
        loadDashboardData()

        // 4. Configurar el botón flotante (Próximo paso: crear la pantalla de agregar)
        binding.fabAdd.setOnClickListener {
            // TODO: Abrir Activity/Fragment para guardar una nueva transacción
        }
    }

    private fun loadDashboardData() {
        val financeService = (application as ControlGastosApp).financeService

        // Usamos una corrutina porque la base de datos (Room) exige procesos asíncronos
        lifecycleScope.launch {
            // Asumiendo que tu servicio de finanzas expone métodos para obtener el resumen y las transacciones.
            // Si tu servicio aún no expone esto, coméntalo temporalmente.
            /*
            val transactions = financeService.getAllTransactions()
            adapter.submitList(transactions)

            // Cálculo básico para pintar la tarjeta superior
            val ingresos = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val gastos = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val balance = ingresos - gastos

            binding.tvTotalBalance.text = "Q$balance"
            binding.tvTotalIncomes.text = "↑ Ingresos: Q$ingresos"
            binding.tvTotalExpenses.text = "↓ Gastos: Q$gastos"
            */
        }
    }
}