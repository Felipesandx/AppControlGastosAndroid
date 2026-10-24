package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.umg.appcontrolgastos.databinding.ActivityOnboardingBinding
import umg.edu.gt.domain.model.UserProfile

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = UserSessionManager(this)

        binding.btnContinue.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val budgetStr = binding.etBudget.text.toString().trim()

            if (name.isEmpty() || budgetStr.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = budgetStr.toDoubleOrNull() ?: 0.0
            sessionManager.saveUser(UserProfile(name = name, monthlyBudget = budget))

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}