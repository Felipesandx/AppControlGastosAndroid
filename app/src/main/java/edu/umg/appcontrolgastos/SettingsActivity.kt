package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.appcontrolgastos.data.AppDatabase
import edu.umg.appcontrolgastos.data.UserEntity
import edu.umg.appcontrolgastos.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: UserSessionManager
    private val userDao by lazy { AppDatabase.getDatabase(this).userDao() }
    private var currentUser: UserEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            if (userId != -1L) {
                currentUser = withContext(Dispatchers.IO) { userDao.getUserById(userId) }
                binding.etName.setText(currentUser?.name ?: "")
            }
        }

        binding.btnUpdateName.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            if (newName.isNotEmpty() && currentUser != null) {
                lifecycleScope.launch {
                    val updatedUser = currentUser!!.copy(name = newName)
                    withContext(Dispatchers.IO) { userDao.update(updatedUser) }
                    Toast.makeText(this@SettingsActivity, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.btnWipeData.setOnClickListener {
            showWipeConfirmationDialog()
        }
    }

    private fun showWipeConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("¿Borrar todos los datos?")
            .setMessage("Esta acción es irreversible. Se eliminará todo tu historial de gastos e ingresos, y se cerrará tu sesión.")
            .setPositiveButton("Sí, borrar todo") { _, _ ->
                wipeAllDataAndExit()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun wipeAllDataAndExit() {
        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            financeService.wipeAllData(userId)
            sessionManager.clearSession()

            val intent = Intent(this@SettingsActivity, UserSelectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}