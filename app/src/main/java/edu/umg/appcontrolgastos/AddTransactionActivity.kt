package edu.umg.appcontrolgastos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.appcontrolgastos.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.launch
import umg.edu.gt.domain.model.Category
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun saveTransaction() {
        val description = binding.etDescription.text.toString()
        val amountText = binding.etAmount.text.toString()

        if (description.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val type = if (binding.rbExpense.isChecked) TransactionType.EXPENSE else TransactionType.INCOME
        val amount = amountText.toDouble()
        val dateString = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        // Creamos la transacción usando tu modelo de dominio puro
        val newTransaction = Transaction(
            id = 0, // 0 porque la base de datos (Room) generará el ID automáticamente
            amount = amount,
            description = description,
            date = dateString,
            timestamp = System.currentTimeMillis(),
            type = type,
            category = Category.OTHER, // Por ahora usamos una categoría genérica
            isImpulse = false
        )

        // Guardamos de forma asíncrona a través del servicio
        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val error = financeService.register(newTransaction) // 👈 Usamos register()

            if (error == null) {
                finish() // Si no hay error, cierra la pantalla y vuelve al Dashboard
            } else {
                Toast.makeText(this@AddTransactionActivity, error, Toast.LENGTH_LONG).show() // Muestra la validación
            }
        }
    }
}