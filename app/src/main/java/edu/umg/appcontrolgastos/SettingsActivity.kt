package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.umg.appcontrolgastos.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        // Cargar nombre actual
        binding.etName.setText(sessionManager.getUser().name)

        binding.btnUpdateName.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            if (newName.isNotEmpty()) {
                sessionManager.updateUserName(newName)
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                finish()
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
            // 1. Borrar base de datos usando el Core
            financeService.wipeAllData()

            // 2. Limpiar sesión (SharedPreferences)
            sessionManager.clearSession()

            // 3. Reiniciar la app mandando al Onboarding
            val intent = Intent(this@SettingsActivity, OnboardingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}