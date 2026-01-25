package com.example.mbible.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NotesDbHelper(context: Context) : SQLiteOpenHelper(context, "notes.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                body TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS book_aliases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                canonical_book TEXT NOT NULL,
                alias TEXT NOT NULL
            )
        """.trimIndent())

                db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_alias_unique
            ON book_aliases(alias)
        """.trimIndent())

                db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_alias_by_book
            ON book_aliases(canonical_book)
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS book_aliases (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                canonical_book TEXT NOT NULL,
                alias TEXT NOT NULL
            )
        """.trimIndent())

                db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_alias_unique
            ON book_aliases(alias)
        """.trimIndent())

                db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_alias_by_book
            ON book_aliases(canonical_book)
        """.trimIndent())
    }
}
