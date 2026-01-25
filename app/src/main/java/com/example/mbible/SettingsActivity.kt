package com.example.mbible

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val booksList = findViewById<ListView>(R.id.booksList)

        val books = listOf(
            "Genesis","Exodus","Leviticus","Numbers","Deuteronomy","Joshua","Judges","Ruth",
            "1 Samuel","2 Samuel","1 Kings","2 Kings","1 Chronicles","2 Chronicles","Ezra","Nehemiah",
            "Esther","Job","Psalms","Proverbs","Ecclesiastes","Song of Solomon","Isaiah","Jeremiah",
            "Lamentations","Ezekiel","Daniel","Hosea","Joel","Amos","Obadiah","Jonah","Micah","Nahum",
            "Habakkuk","Zephaniah","Haggai","Zechariah","Malachi",
            "Matthew","Mark","Luke","John","Acts","Romans","1 Corinthians","2 Corinthians","Galatians",
            "Ephesians","Philippians","Colossians","1 Thessalonians","2 Thessalonians","1 Timothy",
            "2 Timothy","Titus","Philemon","Hebrews","James","1 Peter","2 Peter","1 John","2 John",
            "3 John","Jude","Revelation"
        )

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