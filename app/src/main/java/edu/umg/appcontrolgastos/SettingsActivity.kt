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

        binding.btnDeleteProfile.setOnClickListener {
            showDeleteProfileConfirmationDialog()
        }

        binding.btnWipeData.setOnClickListener {
            showWipeConfirmationDialog()
        }
    }

    private fun showDeleteProfileConfirmationDialog() {
        val name = currentUser?.name ?: "este perfil"
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar perfil \"$name\"?")
            .setMessage("Esta acción es permanente. Se borrarán todos los datos financieros y el perfil.")
            .setPositiveButton("Sí, eliminar perfil") { _, _ ->
                deleteCurrentProfile()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCurrentProfile() {
        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            if (userId != -1L) {
                withContext(Dispatchers.IO) {
                    financeService.wipeAllData(userId)
                    userDao.deleteById(userId)
                }
                sessionManager.clearSession()
                redirectToUserSelection()
            }
        }
    }

    private fun showWipeConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("¿Borrar historial de datos?")
            .setMessage("Se eliminarán todas tus transacciones registradas, pero tu perfil continuará existiendo.")
            .setPositiveButton("Sí, borrar datos") { _, _ ->
                wipeAllData()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun wipeAllData() {
        val financeService = (application as ControlGastosApp).financeService
        lifecycleScope.launch {
            val userId = sessionManager.getActiveUserId()
            financeService.wipeAllData(userId)
            Toast.makeText(this@SettingsActivity, "Historial de datos borrado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun redirectToUserSelection() {
        val intent = Intent(this, UserSelectionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
