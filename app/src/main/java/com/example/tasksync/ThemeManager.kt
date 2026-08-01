package com.example.tasksync.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val PREF_NAME = "theme_pref"
    private const val KEY_THEME = "theme"

    fun saveTheme(context: Context, mode: Int) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putInt(KEY_THEME, mode).apply()
    }

    fun getTheme(context: Context): Int {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getInt(
            KEY_THEME,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }

    fun applyTheme(context: Context) {
        AppCompatDelegate.setDefaultNightMode(getTheme(context))
    }
}