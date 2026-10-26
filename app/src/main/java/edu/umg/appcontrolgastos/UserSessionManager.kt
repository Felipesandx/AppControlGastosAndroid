package edu.umg.appcontrolgastos

import android.content.Context
import android.content.SharedPreferences
import umg.edu.gt.domain.model.UserProfile

class UserSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "user_name"
        private const val KEY_BUDGET = "monthly_budget"
        private const val KEY_DISCREET = "discreet_mode"
    }

    fun isUserRegistered(): Boolean = prefs.contains(KEY_NAME)

    fun saveUser(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putFloat(KEY_BUDGET, profile.monthlyBudget.toFloat())
            .putBoolean(KEY_DISCREET, profile.isDiscreetModeEnabled)
            .apply()
    }

    fun getUser(): UserProfile {
        return UserProfile(
            name = prefs.getString(KEY_NAME, "Usuario") ?: "Usuario",
            monthlyBudget = prefs.getFloat(KEY_BUDGET, 0f).toDouble(),
            isDiscreetModeEnabled = prefs.getBoolean(KEY_DISCREET, false)
        )
    }

    fun setDiscreetMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DISCREET, enabled).apply()
    }

    fun updateUserName(newName: String) {
        val currentUser = getUser()
        val updatedUser = currentUser.copy(name = newName)
        // Reutiliza el método actual para guardar el usuario
        saveUser(updatedUser)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}