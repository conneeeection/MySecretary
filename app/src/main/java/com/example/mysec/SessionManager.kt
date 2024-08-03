package com.example.mysec

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    fun createLoginSession(userId: String) {
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "MySecPrefs"
        private const val KEY_USER_ID = "user_id"
    }
}
