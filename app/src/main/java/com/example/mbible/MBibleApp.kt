package com.example.mbible

import android.app.Application
import com.youversion.platform.core.YouVersionPlatformConfiguration

class MBibleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply the user's saved theme (light/dark) before any screen is shown.
        ThemeManager.applySavedTheme(this)
        YouVersionPlatformConfiguration.configure(
            context = this,
            appKey = BuildConfig.YOUVERSION_APP_KEY
        )
    }
}