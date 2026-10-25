package edu.umg.appcontrolgastos

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ArrayAdapter
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
    private val categories = Category.values()
    private var editingTransactionId: Long = 0L
    private var existingTimestamp: Long = 0L
    private var existingDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCategoryDropdown()
        setupTypeListeners()
        checkEditMode()

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupCategoryDropdown() {
        val categoryNames = categories.map { it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.titlecase() } }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(categoryNames.first(), false)
    }

    private fun setupTypeListeners() {
        binding.rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbIncome) {
                binding.switchImpulse.isChecked = false
                binding.switchImpulse.visibility = View.GONE
            } else {
                binding.switchImpulse.visibility = View.VISIBLE
            }
        }
    }

    private fun checkEditMode() {
        editingTransactionId = intent.getLongExtra("transaction_id", 0L)
        if (editingTransactionId != 0L) {
            binding.tvTitle.text = "Editar Transacción"
            binding.btnSave.text = "Actualizar Cambios"

            binding.etDescription.setText(intent.getStringExtra("transaction_description"))
            binding.etAmount.setText(intent.getDoubleExtra("transaction_amount", 0.0).toString())

            val categoryName = intent.getStringExtra("transaction_category") ?: Category.OTHER.name
            val formattedCategory = categoryName.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
            binding.actvCategory.setText(formattedCategory, false)

            val isIncome = intent.getBooleanExtra("transaction_is_income", false)
            if (isIncome) {
                binding.rbIncome.isChecked = true
                binding.switchImpulse.visibility = View.GONE
            } else {
                binding.rbExpense.isChecked = true
                binding.switchImpulse.visibility = View.VISIBLE
                binding.switchImpulse.isChecked = intent.getBooleanExtra("transaction_is_impulse", false)
            }

            existingTimestamp = intent.getLongExtra("transaction_timestamp", System.currentTimeMillis())
            existingDate = intent.getStringExtra("transaction_date").orEmpty()
        }
    }

    private fun saveTransaction() {
        val description = binding.etDescription.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()

        if (description.isEmpty() || amountText.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, "Ingresa un monto válido mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        binding.root.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

        val type = if (binding.rbExpense.isChecked) TransactionType.EXPENSE else TransactionType.INCOME
        val selectedIndex = categories.map { it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.titlecase() } }
            .indexOf(binding.actvCategory.text.toString())
        val category = if (selectedIndex >= 0) categories[selectedIndex] else Category.OTHER
        val isImpulse = type == TransactionType.EXPENSE && binding.switchImpulse.isChecked

        val dateString = if (editingTransactionId != 0L && existingDate.isNotEmpty()) {
            existingDate
        } else {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        }

        val timestamp = if (editingTransactionId != 0L && existingTimestamp != 0L) {
            existingTimestamp
        } else {
            System.currentTimeMillis()
        }

        val transaction = Transaction(
            id = editingTransactionId,
            amount = amount,
            description = description,
            date = dateString,
            timestamp = timestamp,
            type = type,
            category = category,
            isImpulse = isImpulse
        )

        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val error = if (editingTransactionId != 0L) {
                financeService.update(transaction)
            } else {
                financeService.register(transaction)
            }

            if (error == null) {
                finish()
            } else {
                Toast.makeText(this@AddTransactionActivity, error, Toast.LENGTH_LONG).show()
            }
        }
    }
    @Suppress("DEPRECATION")
    override fun finish() {
        super.finish()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                R.anim.stay,
                R.anim.slide_out_bottom
            )
        } else {
            overridePendingTransition(R.anim.stay, R.anim.slide_out_bottom)
        }
    }
}