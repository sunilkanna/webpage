package com.simats.genecare.data

import android.content.Context
import android.content.SharedPreferences
import com.simats.genecare.data.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "GeneCareSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_USER_TYPE = "userType"
        private const val KEY_VERIFICATION_STATUS = "verificationStatus"
    }

    fun saveUserSession(user: User) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_FULL_NAME, user.fullName)
        editor.putString(KEY_USER_TYPE, user.userType)
        editor.putString(KEY_VERIFICATION_STATUS, user.verificationStatus)
        editor.apply()
    }

    fun getUserSession(): User? {
        if (!isLoggedIn()) return null
        val id = prefs.getInt(KEY_USER_ID, -1)
        val fullName = prefs.getString(KEY_FULL_NAME, null)
        val userType = prefs.getString(KEY_USER_TYPE, null)
        val verificationStatus = prefs.getString(KEY_VERIFICATION_STATUS, null)

        return if (id != -1 && fullName != null && userType != null) {
            User(id, fullName, userType, verificationStatus)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
