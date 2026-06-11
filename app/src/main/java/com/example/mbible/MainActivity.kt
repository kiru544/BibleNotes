package com.example.mbible

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make the status-bar icons dark on the light theme and light on the dark
        // theme, so they're always readable.
        val lightIcons = !ThemeManager.isDark(this)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = lightIcons

        // 3. Initialize bottomNav FIRST
        bottomNav = findViewById(R.id.bottomNav)

        // 4. Load default fragment
        if (savedInstanceState == null) {
            switchFragment(BibleFragment.newInstance("Old"))
            bottomNav.selectedItemId = R.id.nav_ot
        }

        // 5. Tab listener
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_ot -> {
                    switchFragment(BibleFragment.newInstance("Old"))
                }
                R.id.nav_nt -> {
                    switchFragment(BibleFragment.newInstance("New"))
                }
                R.id.nav_docs -> {
                    switchFragment(DocsFragment())
                }
                R.id.nav_notes -> {
                    switchFragment(NotesFragment())
                }
            }
            true
        }

        // 6. Back button
        onBackPressedDispatcher.addCallback(this) {
            val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (current is BibleFragment && current.onBackPressed()) return@addCallback
            if (current is NotesFragment) {
                val handled = current.childFragmentManager.popBackStackImmediate()
                if (handled) return@addCallback
            }
            if (bottomNav.selectedItemId != R.id.nav_ot) {
                bottomNav.selectedItemId = R.id.nav_ot
            } else {
                finish()
            }
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}