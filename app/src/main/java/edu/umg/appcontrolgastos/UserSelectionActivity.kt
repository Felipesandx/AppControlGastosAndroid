package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import edu.umg.appcontrolgastos.data.AppDatabase
import edu.umg.appcontrolgastos.data.UserEntity
import edu.umg.appcontrolgastos.databinding.ActivityUserSelectionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import umg.edu.gt.domain.model.Category
import umg.edu.gt.domain.model.Transaction
import umg.edu.gt.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserSelectionBinding
    private lateinit var sessionManager: UserSessionManager
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = UserSessionManager(this)

        binding.btnAddProfile.setOnClickListener { showCreateProfileDialog() }
        loadProfiles()
    }

    private fun loadProfiles() {
        lifecycleScope.launch {
            val users = withContext(Dispatchers.IO) { userDao.getAllUsers() }
            if (users.isEmpty()) {
                showCreateProfileDialog(isCancelable = false)
            } else {
                setupRecyclerView(users)
            }
        }
    }

    private fun setupRecyclerView(users: List<UserEntity>) {
        binding.rvUsers.adapter = object : RecyclerView.Adapter<UserViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_profile, parent, false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
                val user = users[position]
                val format = NumberFormat.getCurrencyInstance(Locale("es", "GT"))

                holder.name.text = user.name
                holder.initialAmount.text = "Saldo Inicial: ${format.format(user.initialAmount)}"

                holder.itemView.setOnClickListener { loginWithUser(user.id) }
                holder.btnDelete.setOnClickListener { showDeleteConfirmationDialog(user) }
            }

            override fun getItemCount() = users.size
        }
    }

    private fun showCreateProfileDialog(isCancelable: Boolean = true) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val etName = EditText(this).apply {
            hint = "Nombre del perfil (ej. Personal, Trabajo)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        val etInitialAmount = EditText(this).apply {
            hint = "Monto/Saldo Inicial (Q. opcional)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        layout.addView(etName)
        layout.addView(etInitialAmount)

        AlertDialog.Builder(this)
            .setTitle("Nuevo Perfil")
            .setView(layout)
            .setCancelable(isCancelable)
            .setPositiveButton("Crear") { _, _ ->
                val name = etName.text.toString().trim()
                val amount = etInitialAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) createNewUser(name, amount)
            }
            .apply { if (isCancelable) setNegativeButton("Cancelar", null) }
            .show()
    }

    private fun createNewUser(name: String, initialAmount: Double) {
        lifecycleScope.launch {
            val newUserId = withContext(Dispatchers.IO) {
                userDao.insert(UserEntity(name = name, initialAmount = initialAmount))
            }

            // Si se asignó un saldo inicial, registrar la transacción inicial automáticamente
            if (initialAmount > 0) {
                val financeService = (application as ControlGastosApp).financeService
                val dateString = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                val initTransaction = Transaction(
                    id = 0,
                    userId = newUserId,
                    amount = initialAmount,
                    description = "Saldo Inicial",
                    date = dateString,
                    timestamp = System.currentTimeMillis(),
                    type = TransactionType.INCOME,
                    category = Category.OTHER,
                    isImpulse = false
                )
                financeService.register(initTransaction)
            }

            loginWithUser(newUserId)
        }
    }

    private fun showDeleteConfirmationDialog(user: UserEntity) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar perfil?")
            .setMessage("Se eliminará el perfil \"${user.name}\" y todos sus registros financieros de forma permanente.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(user: UserEntity) {
        lifecycleScope.launch {
            val financeService = (application as ControlGastosApp).financeService
            withContext(Dispatchers.IO) {
                financeService.wipeAllData(user.id)
                userDao.deleteById(user.id)
            }

            val activeUserId = sessionManager.getActiveUserId()
            if (activeUserId == user.id) {
                sessionManager.clearSession()
            }

            loadProfiles()
        }
    }

    private fun loginWithUser(userId: Long) {
        lifecycleScope.launch {
            sessionManager.setActiveUser(userId)
            startActivity(Intent(this@UserSelectionActivity, MainActivity::class.java))
            finish()
        }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvUserName)
        val initialAmount: TextView = view.findViewById(R.id.tvInitialAmount)
        val btnDelete: ImageButton = view.findViewById(R.id.ibDeleteUser)
    }
}
