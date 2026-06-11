package com.example.mbible

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mbible.BookAliasRepository

class AliasEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK = "extra_book"
    }

    private lateinit var bookTitle: TextView
    private lateinit var aliasInput: EditText
    private lateinit var btnAddAlias: Button
    private lateinit var aliasesList: ListView

    private val aliases = mutableListOf<String>()
    private lateinit var adapter: AliasListAdapter

    private lateinit var aliasRepo: BookAliasRepository
    private lateinit var book: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alias_editor)
        ThemeManager.applyStatusBarIcons(this)

        // init
        aliasRepo = BookAliasRepository(this)
        book = intent.getStringExtra(EXTRA_BOOK) ?: "Book"

        // views
        bookTitle = findViewById(R.id.bookTitle)
        aliasInput = findViewById(R.id.aliasInput)
        btnAddAlias = findViewById(R.id.btnAddAlias)
        aliasesList = findViewById(R.id.aliasesList)

        bookTitle.text = book

        // adapter FIRST
        adapter = AliasListAdapter(
            this,
            aliases,
            onDelete = { alias ->
                AlertDialog.Builder(this)
                    .setTitle("Delete short name?")
                    .setMessage("Remove “$alias” from $book?")
                    .setPositiveButton("Delete") { _, _ ->
                        aliasRepo.deleteAlias(alias)
                        loadAliases()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        aliasesList.adapter = adapter

        // load from DB
        loadAliases()

        // add alias
        btnAddAlias.setOnClickListener {
            val raw = aliasInput.text.toString().trim()
            if (raw.isEmpty()) return@setOnClickListener

            val ok = aliasRepo.addAlias(book, raw)
            if (!ok) {
                Toast.makeText(this, "Alias already exists or invalid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            aliasInput.setText("")
            loadAliases()
        }
    }

    private fun loadAliases() {
        aliases.clear()
        aliases.addAll(aliasRepo.getAliasesForBook(book))
        adapter.notifyDataSetChanged()
    }
}