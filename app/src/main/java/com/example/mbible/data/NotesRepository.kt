package com.example.mbible.data

import android.content.ContentValues
import android.content.Context

class NotesRepository(context: Context) {
    private val helper = NotesDbHelper(context)

    fun getAll(): List<Note> {
        val db = helper.readableDatabase
        val list = mutableListOf<Note>()

        val c = db.rawQuery(
            "SELECT id, title, body, updated_at FROM notes ORDER BY updated_at DESC",
            null
        )

        c.use {
            while (it.moveToNext()) {
                list.add(
                    Note(
                        id = it.getLong(0),
                        title = it.getString(1),
                        body = it.getString(2),
                        updatedAt = it.getLong(3)
                    )
                )
            }
        }
        return list
    }

    fun create(title: String): Long {
        val db = helper.writableDatabase
        val now = System.currentTimeMillis()

        val values = ContentValues().apply {
            put("title", title)
            put("body", "")
            put("updated_at", now)
        }
        return db.insert("notes", null, values)
    }
    fun getById(id: Long): Note? {
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT id, title, body, updated_at FROM notes WHERE id = ? LIMIT 1",
            arrayOf(id.toString())
        )

        c.use {
            if (!it.moveToFirst()) return null
            return Note(
                id = it.getLong(0),
                title = it.getString(1),
                body = it.getString(2),
                updatedAt = it.getLong(3)
            )
        }
    }
    fun update(id: Long, title: String, body: String) {
        val db = helper.writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("body", body)
            put("updated_at", System.currentTimeMillis())
        }
        db.update("notes", values, "id=?", arrayOf(id.toString()))
    }
    fun delete(id: Long) {
        val db = helper.writableDatabase
        db.delete("notes", "id=?", arrayOf(id.toString()))
    }
}
