package com.example.mbible

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mbible.data.BibleRepository

class ChaptersActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapters)

        val bookName = intent.getStringExtra(EXTRA_BOOK_NAME) ?: return
        val testament = intent.getStringExtra(EXTRA_TESTAMENT) ?: return

        findViewById<TextView>(R.id.chapterTitle).text = bookName

        val repo = BibleRepository(this)
        val count = repo.getChapterCount(bookName, testament)

        val recycler = findViewById<RecyclerView>(R.id.chaptersRecycler)
        recycler.layoutManager = GridLayoutManager(this, 6) // 6 squares per row
        recycler.adapter = ChapterAdapter((1..count).toList()) { chapter ->
            // Next step: open verses reader
            // For now, just show which chapter was clicked (optional)
        }
    }

    companion object {
        const val EXTRA_BOOK_NAME = "book_name"
        const val EXTRA_TESTAMENT = "testament"
    }
}