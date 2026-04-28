package com.example.mbible.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.mbible.data.Verse

class BibleRepository(context: Context) {

    private val db: SQLiteDatabase =
        SQLiteDatabase.openDatabase(
            BibleDatabaseHelper.getDatabasePath(context),
            null,
            SQLiteDatabase.OPEN_READONLY
        )

    fun getBooks(testament: String): List<String> {
        val books = mutableListOf<String>()

        val cursor = db.rawQuery(
            """
            SELECT name
            FROM books
            WHERE testament = ?
            ORDER BY book_order
            """.trimIndent(),
            arrayOf(testament)
        )

        cursor.use {
            while (it.moveToNext()) {
                books.add(it.getString(0))
            }
        }

        return books
    }
    fun getChapterCount(bookName: String, testament: String): Int {
        val cursor = db.rawQuery(
            """
        SELECT MAX(v.chapter)
        FROM verses v
        JOIN books b ON b.id = v.book_id
        WHERE b.name = ? AND b.testament = ?
        """.trimIndent(),
            arrayOf(bookName, testament)
        )

        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                return it.getInt(0)
            }
        }
        return 0
    }

    fun getVerses(bookName: String, testament: String, chapter: Int): List<Verse> {
        val verses = mutableListOf<Verse>()

        val cursor = db.rawQuery(
            """
        SELECT v.verse, v.text
        FROM verses v
        JOIN books b ON b.id = v.book_id
        WHERE b.name = ? AND b.testament = ? AND v.chapter = ?
        ORDER BY v.verse
        """.trimIndent(),
            arrayOf(bookName, testament, chapter.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                val verseNum = it.getInt(0)
                val text = it.getString(1)
                verses.add(Verse(verseNum, text))
            }
        }

        return verses
    }
    fun verseExists(bookName: String, chapter: Int, verse: Int): Boolean {
        val c = db.rawQuery(
            """
        SELECT 1
        FROM verses v
        JOIN books b ON b.id = v.book_id
        WHERE b.name = ? AND v.chapter = ? AND v.verse = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(bookName, chapter.toString(), verse.toString())
        )

        return c.use { it.moveToFirst() }
    }
    fun getVerseRange(bookName: String, chapter: Int, startVerse: Int, endVerse: Int): List<Verse> {
        val verses = mutableListOf<Verse>()

        val cursor = db.rawQuery(
            """
        SELECT v.verse, v.text
        FROM verses v
        JOIN books b ON b.id = v.book_id
        WHERE b.name = ? AND v.chapter = ? AND v.verse BETWEEN ? AND ?
        ORDER BY v.verse
        """.trimIndent(),
            arrayOf(bookName, chapter.toString(), startVerse.toString(), endVerse.toString())
        )

        cursor.use {
            while (it.moveToNext()) {
                verses.add(Verse(it.getInt(0), it.getString(1)))
            }
        }

        return verses
    }
    fun getVerseCount(bookName: String, chapter: Int): Int {
        val cursor = db.rawQuery(
            """
        SELECT MAX(v.verse)
        FROM verses v
        JOIN books b ON b.id = v.book_id
        WHERE b.name = ? AND v.chapter = ?
        """.trimIndent(),
            arrayOf(bookName, chapter.toString())
        )
        cursor.use {
            if (it.moveToFirst() && !it.isNull(0)) return it.getInt(0)
        }
        return 0
    }
}
