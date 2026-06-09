package com.example.mbible.data

import android.content.Context

/**
 * Public façade used by Fragments / ViewModels.
 *
 * Replaces the previous concrete-SQLite implementation. Picks a [BibleSource]
 * based on which translation the user has active in [TranslationPrefs],
 * then delegates every read.
 *
 * Behaviour notes:
 * - Methods are now `suspend`. Call them from `lifecycleScope.launch { ... }`
 *   or from a ViewModel scope. (Previous synchronous calls on the main
 *   thread were doing I/O on the UI thread anyway — this is an upgrade.)
 * - The active translation can change at runtime; the next call will use the
 *   new one.
 */
class BibleRepository(private val context: Context) {

    private val prefs = TranslationPrefs(context)
    private val local: BibleSource = LocalBibleSource(context)

    // Created lazily so apps with no remote translation never open the cache DB.
    private val cache: ChapterCache by lazy { ChapterCache(context) }

    /**
     * One remote source per translation id. Cached so we don't rebuild the
     * SDK plumbing on every call.
     */
    private val remoteSources = mutableMapOf<String, RemoteBibleSource>()

    private fun activeSource(): BibleSource {
        val t = prefs.activeTranslation
        val src: BibleSource = when (t.kind) {
            Translation.Kind.LOCAL -> local
            Translation.Kind.REMOTE -> remoteSources.getOrPut(t.id) {
                RemoteBibleSource(t, cache)
            }
        }
        return src
    }

    val activeTranslation: Translation get() = prefs.activeTranslation
    /** Most recent error from a remote source, or null. UI checks this after a fetch returns empty. */
    val lastRemoteError: Exception?
        get() = (activeSource() as? RemoteBibleSource)?.lastError
    fun setActiveTranslation(id: String) {
        prefs.activeTranslationId = id
    }

    // --- BibleSource pass-through ---------------------------------------

    suspend fun getBooks(testament: String): List<String> =
        activeSource().getBooks(testament)

    suspend fun getChapterCount(bookName: String, testament: String): Int =
        activeSource().getChapterCount(bookName, testament)

    suspend fun getVerses(bookName: String, testament: String, chapter: Int): List<Verse> =
        activeSource().getVerses(bookName, testament, chapter)

    suspend fun getVerseRange(
        bookName: String, chapter: Int, startVerse: Int, endVerse: Int
    ): List<Verse> =
        activeSource().getVerseRange(bookName, chapter, startVerse, endVerse)

    suspend fun getVerseCount(bookName: String, chapter: Int): Int =
        activeSource().getVerseCount(bookName, chapter)

    suspend fun verseExists(bookName: String, chapter: Int, verse: Int): Boolean =
        activeSource().verseExists(bookName, chapter, verse)
}