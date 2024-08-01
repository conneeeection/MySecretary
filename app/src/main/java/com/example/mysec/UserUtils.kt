package com.example.mysec

import android.content.Context
import android.content.SharedPreferences

object UserUtils {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: String) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getCurrentUserId(context: Context): String? {
        val prefs = getPreferences(context)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearUserId(context: Context) {
        val prefs = getPreferences(context)
        val editor = prefs.edit()
        editor.remove(KEY_USER_ID)
        editor.apply()
    }
}
