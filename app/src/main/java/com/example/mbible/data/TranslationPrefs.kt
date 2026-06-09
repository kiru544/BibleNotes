package com.example.mbible.data

import android.content.Context

class TranslationPrefs(context: Context) {

    private val prefs =
        context.applicationContext
            .getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var activeTranslationId: String
        get() = prefs.getString(KEY_ACTIVE, Translations.KJV.id) ?: Translations.KJV.id
        set(value) {
            prefs.edit().putString(KEY_ACTIVE, value).apply()
        }

    val activeTranslation: Translation
        get() = Translations.byId(activeTranslationId)

    companion object {
        private const val FILE = "mbible_translation_prefs"
        private const val KEY_ACTIVE = "active_translation_id"
    }
}