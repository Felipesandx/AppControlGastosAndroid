package edu.umg.appcontrolgastos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
                holder.name.text = user.name
                holder.itemView.setOnClickListener { loginWithUser(user.id) }
            }

            override fun getItemCount() = users.size
        }
    }

    private fun showCreateProfileDialog(isCancelable: Boolean = true) {
        val input = EditText(this).apply { hint = "Ej. Personal, Negocio" }
        AlertDialog.Builder(this)
            .setTitle("Nuevo Perfil")
            .setView(input)
            .setCancelable(isCancelable)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) createNewUser(name)
            }
            .apply { if (isCancelable) setNegativeButton("Cancelar", null) }
            .show()
    }

    private fun createNewUser(name: String) {
        lifecycleScope.launch {
            val newUserId = withContext(Dispatchers.IO) { userDao.insert(UserEntity(name = name)) }
            loginWithUser(newUserId)
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
    }
}