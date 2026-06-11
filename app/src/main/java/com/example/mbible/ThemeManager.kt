package com.example.mbible

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Central place for theme (day/night) logic.
 *
 * How it works:
 *  - Android picks colors from res/values/ (light "Sunrise Orange") or
 *    res/values-night/ (dark "Midnight Blue") based on the current night mode.
 *  - AppCompatDelegate.setDefaultNightMode(...) sets that mode for the whole app
 *    and automatically recreates any AppCompatActivity so the new colors apply.
 *  - We save the user's choice in SharedPreferences so it survives app restarts.
 */
object ThemeManager {
    private const val PREFS = "theme_prefs"
    private const val KEY_MODE = "night_mode"

    /** Call once at app startup so the saved theme is applied before any UI shows. */
    fun applySavedTheme(context: Context) {
        val mode = prefs(context).getInt(
            KEY_MODE,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM // first launch: follow the device
        )
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /** Flip between dark and light, then remember it. The current activity recreates itself. */
    fun toggleTheme(context: Context) {
        val newMode = if (isDark(context)) {
            AppCompatDelegate.MODE_NIGHT_NO   // -> Sunrise Orange
        } else {
            AppCompatDelegate.MODE_NIGHT_YES  // -> Midnight Blue
        }
        AppCompatDelegate.setDefaultNightMode(newMode)
        prefs(context).edit().putInt(KEY_MODE, newMode).apply()
    }

    /** True if the app is currently showing the dark theme (accounts for "follow system"). */
    fun isDark(context: Context): Boolean {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // Following the system: read what the system is actually doing right now.
                val uiMode = context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK
                uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}