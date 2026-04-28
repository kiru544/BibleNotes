package com.example.mbible


import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity
import com.example.mbible.data.NotesDbHelper
import com.example.mbible.data.BibleBooks

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val booksList = findViewById<ListView>(R.id.booksList)

        val books = BibleBooks.ALL

        val adapter = ArrayAdapter(
            this,
            R.layout.item_book,
            R.id.bookName,
            books
        )

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
}