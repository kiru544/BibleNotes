package com.example.mbible.data

/**
 * Strategy interface used by BibleRepository to delegate verse lookups
 * to either the bundled local SQLite (KJV) or a remote provider
 * (YouVersion Platform SDK, with a chapter cache for opportunistic offline).
 *
 * All methods are suspend so remote implementations can fetch over the network
 * and local ones can move SQLite reads off the main thread.
 */
interface BibleSource {
    suspend fun getBooks(testament: String): List<String>
    suspend fun getChapterCount(bookName: String, testament: String): Int
    suspend fun getVerses(bookName: String, testament: String, chapter: Int): List<Verse>
    suspend fun getVerseRange(bookName: String, chapter: Int, startVerse: Int, endVerse: Int): List<Verse>
    suspend fun getVerseCount(bookName: String, chapter: Int): Int
    suspend fun verseExists(bookName: String, chapter: Int, verse: Int): Boolean
}