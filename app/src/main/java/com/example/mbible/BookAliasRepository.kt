package com.example.mbible

import android.content.ContentValues
import android.content.Context
import com.example.mbible.data.NotesDbHelper
import com.example.mbible.data.BibleBooks

class BookAliasRepository(context: Context) {
    private val helper = NotesDbHelper(context)

    fun getAliasesForBook(book: String): List<String> {
        val db = helper.readableDatabase
        val out = mutableListOf<String>()
        val c = db.rawQuery(
            "SELECT alias FROM book_aliases WHERE canonical_book=? ORDER BY alias",
            arrayOf(book)
        )
        c.use {
            while (it.moveToNext()) out.add(it.getString(0))
        }
        return out
    }

    fun addAlias(book: String, aliasRaw: String): Boolean {
        val alias = normalize(aliasRaw)
        if (alias.isEmpty()) return false

        val db = helper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put("canonical_book", book)
                put("alias", alias)
            }
            db.insertOrThrow("book_aliases", null, values)
            true
        } catch (_: Exception) {
            false // alias already exists (unique index) or other constraint
        }
    }

    fun deleteAlias(aliasRaw: String) {
        val alias = normalize(aliasRaw)
        val db = helper.writableDatabase
        db.delete("book_aliases", "alias=?", arrayOf(alias))
    }

    fun normalize(s: String): String =
        s.trim().lowercase()
            .replace(" ", "")
            .replace(".", "")

    fun resolveBookToken(tokenRaw: String): String? {
        val token = normalize(tokenRaw)
        if (token.isEmpty()) return null

        // 1) try alias table
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT canonical_book FROM book_aliases WHERE alias=? LIMIT 1",
            arrayOf(token)
        )
        c.use { if (it.moveToFirst()) return it.getString(0) }

        // 2) fallback: match canonical book names (john, 1corinthians, songofsolomon...)
        // We use a fixed list because canonical books aren't stored in this DB.
        for (book in CANONICAL_BOOKS) {
            if (normalize(book) == token) return book
        }

        return null
    }

    // Put this inside BookAliasRepository class
    private val CANONICAL_BOOKS = BibleBooks.ALL
}

