package edu.umg.appcontrolgastos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import edu.umg.appcontrolgastos.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import umg.edu.gt.domain.model.UserProfile
import umg.edu.gt.domain.parser.QuickAddParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: UserSessionManager
    private lateinit var adapter: TransactionAdapter
    private var isDiscreet: Boolean = false // Modo discreto manejado en memoria durante la sesión

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        binding.tvGreeting.text = "¡Hola!"

        adapter = TransactionAdapter { transaction ->
            val intent = Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("transaction_id", transaction.id)
                putExtra("transaction_description", transaction.description)
                putExtra("transaction_amount", transaction.amount)
                putExtra("transaction_category", transaction.category.name)
                putExtra("transaction_is_income", transaction.type == TransactionType.INCOME)
                putExtra("transaction_is_impulse", transaction.isImpulse)
                putExtra("transaction_date", transaction.date)
                putExtra("transaction_timestamp", transaction.timestamp)
            }
            openAddTransaction(intent)
        }

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        setupSwipeToDelete()

        binding.fabAdd.setOnClickListener {
            openAddTransaction(Intent(this, AddTransactionActivity::class.java))
        }

        binding.btnToggleDiscreet.setOnClickListener {
            toggleDiscreetMode()
        }

        binding.cardSummary.setOnClickListener {
            toggleDiscreetMode()
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
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val transactionToDelete = adapter.getTransactionAt(position)

                binding.root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                val financeService = (application as ControlGastosApp).financeService
                lifecycleScope.launch {
                    val deleted = financeService.delete(transactionToDelete.id)
                    if (deleted) {
                        loadDashboardData()
                        Snackbar.make(binding.root, "Transacción eliminada", Snackbar.LENGTH_SHORT).show()
                    } else {
                        adapter.notifyItemChanged(position)
                        Toast.makeText(this@MainActivity, "No se pudo eliminar el registro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvTransactions)
    }

    private fun toggleDiscreetMode() {
        binding.root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        isDiscreet = !isDiscreet
        loadDashboardData()
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

        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            if (userId == -1L) return@launch

            val transaction = Transaction(
                id = 0,
                userId = userId, // Parámetro obligatorio V2.0
                amount = parsed.amount,
                description = parsed.description,
                date = dateString,
                timestamp = System.currentTimeMillis(),
                type = TransactionType.EXPENSE,
                category = parsed.category,
                isImpulse = false
            )

            val financeService = (application as ControlGastosApp).financeService
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

        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            if (userId == -1L) {
                startActivity(Intent(this@MainActivity, UserSelectionActivity::class.java))
                finish()
                return@launch
            }

            // Llamadas al core enviando el userId
            val transactions = financeService.listTransactions(userId)
            adapter.submitList(transactions)

            if (transactions.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }

            val summary = financeService.getMonthlySummary(userId)
            val calendar = Calendar.getInstance()
            val daysRemaining = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)) + 1

            // Perfil temporal en memoria para cálculos
            val profile = UserProfile(id = userId, name = "Usuario", isDiscreetModeEnabled = isDiscreet)
            val safeToSpendToday = financeService.calculateDailySafeToSpend(
                profile = profile,
                totalExpensesThisMonth = summary.totalExpenses,
                daysRemainingInMonth = daysRemaining
            )

            if (isDiscreet) {
                binding.tvDailySafeSpend.text = "••••••"
                binding.tvTotalBalance.text = "••••"
                binding.tvTotalExpenses.text = "••••"
                binding.tvImpulseExpenses.text = "••••"
            } else {
                binding.tvDailySafeSpend.text = financeService.formatCurrency(safeToSpendToday)
                binding.tvTotalBalance.text = financeService.formatCurrency(summary.netBalance)
                binding.tvTotalExpenses.text = financeService.formatCurrency(summary.totalExpenses)
                binding.tvImpulseExpenses.text = financeService.formatCurrency(summary.impulseExpensesTotal)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun openAddTransaction(intent: Intent) {
        startActivity(intent)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_OPEN,
                R.anim.slide_in_bottom,
                R.anim.stay
            )
        } else {
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.stay)
        }
    }
}