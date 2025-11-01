package com.example.safedinneres.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.safedinneres.utils.Constants

/**
 * Maneja todas las operaciones con SharedPreferences
 */
class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREF_NAME,
        Context.MODE_PRIVATE
    )

    // User ID
    var userId: String
        get() = prefs.getString(Constants.PREF_USER_ID, "") ?: ""
        set(value) = prefs.edit().putString(Constants.PREF_USER_ID, value).apply()

    // User Name
    var userName: String
        get() = prefs.getString(Constants.PREF_USER_NAME, "Usuario") ?: "Usuario"
        set(value) = prefs.edit().putString(Constants.PREF_USER_NAME, value).apply()

    // Selected Month
    var selectedMonth: String?
        get() = prefs.getString(Constants.PREF_SELECTED_MONTH, null)
        set(value) = prefs.edit().putString(Constants.PREF_SELECTED_MONTH, value).apply()

    /**
     * Limpia todas las preferencias (útil al cerrar sesión)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    /**
     * Guarda los datos del usuario
     */
    fun saveUserData(userId: String, userName: String) {
        prefs.edit()
            .putString(Constants.PREF_USER_ID, userId)
            .putString(Constants.PREF_USER_NAME, userName)
            .apply()
    }
}