package com.example.mbible

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.mbible.data.BibleBooks

class SettingsActivity : AppCompatActivity() {

    private lateinit var adapter: SettingsBookAdapter
    private val books = BibleBooks.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ThemeManager.applyStatusBarIcons(this)

        val booksList = findViewById<ListView>(R.id.booksList)
        adapter = SettingsBookAdapter(this, books, BookAliasRepository(this))
        booksList.adapter = adapter

        booksList.setOnItemClickListener { _, _, position, _ ->
            val book = books[position]
            startActivity(
                android.content.Intent(this, AliasEditorActivity::class.java).apply {
                    putExtra(AliasEditorActivity.EXTRA_BOOK, book)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh tags after returning from the alias editor.
        adapter.notifyDataSetChanged()
    }
}