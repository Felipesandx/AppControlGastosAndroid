package edu.umg.appcontrolgastos

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    suspend fun setActiveUser(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_USER_ID] = userId
        }
    }

    suspend fun getActiveUserId(): Long {
        val preferences = context.dataStore.data.first()
        return preferences[ACTIVE_USER_ID] ?: -1L
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}