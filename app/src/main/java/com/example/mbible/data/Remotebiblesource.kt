package com.example.mbible.data

import com.youversion.platform.core.api.YouVersionApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * BibleSource backed by the YouVersion Platform SDK, with a local chapter
 * cache so the same passage isn't fetched twice.
 *
 * Translations served this way (e.g. NIV) require an internet connection
 * the *first* time the user opens any given chapter; after that, the
 * chapter is replayed from [ChapterCache] and works offline.
 *
 * IMPORTANT — field names below marked with TODOs are best-guesses from the
 * SDK's public API reference. After adding the SDK dependency, let Android
 * Studio's autocomplete confirm them and adjust as needed:
 *   - BibleVerse: verse number field & content field
 *   - BiblePassage: content field
 *
 * Reference:
 *   https://developers.youversion.com/sdks/kotlin
 *   https://mintlify.wiki/youversion/platform-sdk-kotlin/api/bible-api
 */
class RemoteBibleSource(
    private val translation: Translation,
    private val cache: ChapterCache
) : BibleSource {

    private val versionId: Int =
        requireNotNull(translation.youVersionId) {
            "REMOTE translation ${translation.id} must have a youVersionId"
        }
    @Volatile var lastError: Exception? = null
        private set

    /**
     * Books and the canon structure don't depend on the translation at our level
     * of abstraction — we always show the same 66 Protestant books. So instead of
     * an API round-trip on every screen, we serve book lists from BookMapping.
     *
     * If you later add translations from other canons (Catholic, Orthodox),
     * this is the place to revisit.
     */
    override suspend fun getBooks(testament: String): List<String> =
        BookMapping.namesInTestament(testament)

    /**
     * Chapter counts are likewise canon-stable across the translations we
     * currently support. Hardcoded fast-path: ask the cached version if we
     * have any verses for this book, otherwise fall back to a single API
     * call. (For NIV specifically, chapter counts match KJV exactly.)
     *
     * A future improvement: pre-seed chapter counts from
     * YouVersionApi.bible.versionIndex(versionId) on first launch.
     */
    override suspend fun getChapterCount(bookName: String, testament: String): Int {
        val meta = BookMapping.byName(bookName) ?: return 0
        return ChapterCounts.forBook(meta.usfm)
    }

    override suspend fun getVerses(
        bookName: String,
        testament: String,
        chapter: Int
    ): List<Verse> {
        val meta = BookMapping.byName(bookName) ?: return emptyList()
        return loadChapter(meta.usfm, chapter)
    }

    override suspend fun getVerseRange(
        bookName: String,
        chapter: Int,
        startVerse: Int,
        endVerse: Int
    ): List<Verse> {
        val meta = BookMapping.byName(bookName) ?: return emptyList()
        return loadChapter(meta.usfm, chapter)
            .filter { it.verse in startVerse..endVerse }
    }

    override suspend fun getVerseCount(bookName: String, chapter: Int): Int {
        val meta = BookMapping.byName(bookName) ?: return 0
        return loadChapter(meta.usfm, chapter).maxOfOrNull { it.verse } ?: 0
    }

    override suspend fun verseExists(bookName: String, chapter: Int, verse: Int): Boolean {
        val meta = BookMapping.byName(bookName) ?: return false
        return loadChapter(meta.usfm, chapter).any { it.verse == verse }
    }

    /**
     * Fetch a chapter, going through the cache.
     * Cache miss → call YouVersion SDK → parse → write cache → return.
     */
    private suspend fun loadChapter(bookUsfm: String, chapter: Int): List<Verse> =
        withContext(Dispatchers.IO) {
            cache.get(versionId, bookUsfm, chapter)?.let { return@withContext it }

            val verses = fetchChapterFromSdk(bookUsfm, chapter)
            if (verses.isNotEmpty()) {
                cache.put(versionId, bookUsfm, chapter, verses)
            }
            verses
        }

    /**
     * Fetches the chapter via the passage endpoint (returns HTML), then parses
     * individual verses out of it.
     *
     * BibleVerse (the SDK model) only carries id/passageId/title — no text.
     * Actual verse text lives in BiblePassage.content as HTML:
     *   <span class="yv-v" v="1"></span><span class="yv-vlbl">1</span>verse text…
     * We split on the v="N" markers and strip tags from each segment.
     */
    private suspend fun fetchChapterFromSdk(bookUsfm: String, chapter: Int): List<Verse> {
        val passageId = "${bookUsfm.uppercase()}.$chapter"
        return try {
            Log.d("RemoteBible", "Fetching versionId=$versionId passageId=$passageId")
            val passage = YouVersionApi.bible.passage(versionId, passageId, "html")
            Log.d("RemoteBible", "Raw HTML length=${passage.content.length}")
            Log.d("RemoteBible", "Raw HTML: ${passage.content.take(1000)}")
            val verses = parseHtmlPassage(passage.content)
            Log.d("RemoteBible", "Parsed ${verses.size} verses")
            verses
        } catch (e: Exception) {
            Log.e("RemoteBible", "Fetch failed for $passageId", e)
            lastError = e
            emptyList()
        }
    }

    private fun parseHtmlPassage(html: String): List<Verse> {
        val verseMarker = Regex("""<span[^>]+\bv="(\d+)"[^>]*/?>""")
        val matches = verseMarker.findAll(html).toList()
        val verses = mutableListOf<Verse>()
        for (i in matches.indices) {
            val num = matches[i].groupValues[1].toIntOrNull() ?: continue
            val start = matches[i].range.last + 1
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else html.length
            val text = html.substring(start, end).cleaned()
            if (text.isNotEmpty()) verses.add(Verse(num, text))
        }
        return verses
    }

    /** Strip verse-label spans, all other HTML tags, then collapse whitespace. */
    private fun String.cleaned(): String =
        replace(Regex("""<span[^>]*yv-vlbl[^>]*>\d+</span>"""), "")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}

/**
 * Chapter counts for the 66-book Protestant canon (same across KJV, NIV, ESV...).
 * Used to answer getChapterCount() for remote translations without an API call.
 */
private object ChapterCounts {
    private val byUsfm: Map<String, Int> = mapOf(
        "GEN" to 50, "EXO" to 40, "LEV" to 27, "NUM" to 36, "DEU" to 34,
        "JOS" to 24, "JDG" to 21, "RUT" to  4, "1SA" to 31, "2SA" to 24,
        "1KI" to 22, "2KI" to 25, "1CH" to 29, "2CH" to 36, "EZR" to 10,
        "NEH" to 13, "EST" to 10, "JOB" to 42, "PSA" to 150,"PRO" to 31,
        "ECC" to 12, "SNG" to  8, "ISA" to 66, "JER" to 52, "LAM" to  5,
        "EZK" to 48, "DAN" to 12, "HOS" to 14, "JOL" to  3, "AMO" to  9,
        "OBA" to  1, "JON" to  4, "MIC" to  7, "NAM" to  3, "HAB" to  3,
        "ZEP" to  3, "HAG" to  2, "ZEC" to 14, "MAL" to  4,
        "MAT" to 28, "MRK" to 16, "LUK" to 24, "JHN" to 21, "ACT" to 28,
        "ROM" to 16, "1CO" to 16, "2CO" to 13, "GAL" to  6, "EPH" to  6,
        "PHP" to  4, "COL" to  4, "1TH" to  5, "2TH" to  3, "1TI" to  6,
        "2TI" to  4, "TIT" to  3, "PHM" to  1, "HEB" to 13, "JAS" to  5,
        "1PE" to  5, "2PE" to  3, "1JN" to  5, "2JN" to  1, "3JN" to  1,
        "JUD" to  1, "REV" to 22
    )

    fun forBook(usfm: String): Int = byUsfm[usfm] ?: 0
}