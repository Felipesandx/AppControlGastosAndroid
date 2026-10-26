package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.view.View
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
import umg.edu.gt.domain.parser.QuickAddParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: UserSessionManager
    private lateinit var adapter: TransactionAdapter
    private var isDiscreet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = UserSessionManager(this)
        if (!sessionManager.isUserRegistered()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = sessionManager.getUser()
        binding.tvGreeting.text = "Hola, ${user.name}"
        isDiscreet = user.isDiscreetModeEnabled

        // Inicializar adaptador con evento de clic para edición
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
        if (::sessionManager.isInitialized && sessionManager.isUserRegistered()) {
            loadDashboardData()
        }
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

                binding.root.performHapticFeedback(HapticFeedbackConstants.REJECT)

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

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvTransactions)
    }

    private fun toggleDiscreetMode() {
        binding.root.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        isDiscreet = !isDiscreet
        sessionManager.setDiscreetMode(isDiscreet)
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

            // Alternar vista entre la lista y el empty state
            if (transactions.isEmpty()) {
                binding.rvTransactions.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvTransactions.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
            }

            val summary = financeService.getMonthlySummary()

            val calendar = Calendar.getInstance()
            val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val daysRemaining = (totalDaysInMonth - currentDay) + 1

            val safeToSpendToday = financeService.calculateDailySafeToSpend(
                profile = user,
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