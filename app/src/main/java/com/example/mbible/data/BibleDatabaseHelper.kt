package com.example.mbible.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object BibleDatabaseHelper {

    private const val DB_NAME = "bible.db"

    fun getDatabasePath(context: Context): String {
        val dbFile = context.getDatabasePath(DB_NAME)

        if (!dbFile.exists()) {
            copyDatabaseFromAssets(context, dbFile)
        }

        return dbFile.path
    }

    private fun copyDatabaseFromAssets(context: Context, dbFile: File) {
        dbFile.parentFile?.mkdirs()

        context.assets.open(DB_NAME).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }
}
