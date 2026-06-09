package com.example.mbible.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists chapters fetched from a remote provider (YouVersion Platform).
 * Chapters the user has read while online become available offline next time.
 *
 * Schema: one row per (translation, book USFM, chapter) holding a JSON-encoded
 * list of verses. JSON is used (rather than per-verse rows) so a chapter is
 * written atomically — no partially-cached chapters on a mid-fetch crash.
 *
 * Cached chapters are kept indefinitely until manually cleared
 * (see [clearTranslation]).
 */
class ChapterCache(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no-op for now; if schema changes, drop & recreate is fine since this is a cache
    }

    fun get(versionId: Int, bookUsfm: String, chapter: Int): List<Verse>? {
        readableDatabase.rawQuery(
            "SELECT verses_json FROM chapter_cache WHERE version_id=? AND book_usfm=? AND chapter=?",
            arrayOf(versionId.toString(), bookUsfm, chapter.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return parseVerses(c.getString(0))
        }
    }

    fun put(versionId: Int, bookUsfm: String, chapter: Int, verses: List<Verse>) {
        val arr = JSONArray()
        for (v in verses) {
            arr.put(JSONObject().put("n", v.verse).put("t", v.text))
        }
        val values = ContentValues().apply {
            put("version_id", versionId)
            put("book_usfm", bookUsfm)
            put("chapter", chapter)
            put("verses_json", arr.toString())
            put("fetched_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict(
            "chapter_cache", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun has(versionId: Int, bookUsfm: String, chapter: Int): Boolean {
        readableDatabase.rawQuery(
            "SELECT 1 FROM chapter_cache WHERE version_id=? AND book_usfm=? AND chapter=? LIMIT 1",
            arrayOf(versionId.toString(), bookUsfm, chapter.toString())
        ).use { return it.moveToFirst() }
    }

    fun clearTranslation(versionId: Int) {
        writableDatabase.delete(
            "chapter_cache", "version_id=?", arrayOf(versionId.toString())
        )
    }

    private fun parseVerses(json: String): List<Verse> {
        val arr = JSONArray(json)
        val out = ArrayList<Verse>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Verse(o.getInt("n"), o.getString("t")))
        }
        return out
    }

    companion object {
        private const val DB_NAME = "remote_bible_cache.db"
        private const val DB_VERSION = 1
        private const val CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS chapter_cache (
                version_id INTEGER NOT NULL,
                book_usfm TEXT NOT NULL,
                chapter INTEGER NOT NULL,
                verses_json TEXT NOT NULL,
                fetched_at INTEGER NOT NULL,
                PRIMARY KEY (version_id, book_usfm, chapter)
            )
        """
    }
}