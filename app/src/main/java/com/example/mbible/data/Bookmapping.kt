package com.example.mbible.data

/**
 * Canonical book metadata for the 66-book Protestant canon.
 *
 * Display names match the strings already used in MBible's bundled bible.db
 * (e.g. "Song of Solomon", "Psalms" with the 's'), so call sites that pass
 * book names like "Genesis" or "1 Samuel" don't need to change.
 *
 * USFM codes are the standard 3-letter identifiers used by the YouVersion
 * Platform SDK ("GEN", "JHN", "1KI"...).
 */
data class BookMeta(
    val order: Int,
    val displayName: String,
    val usfm: String,
    val testament: String  // "Old" or "New" — matches the values in bible.db
)

object BookMapping {

    val ALL: List<BookMeta> = listOf(
        // Old Testament
        BookMeta(1,  "Genesis",          "GEN", "Old"),
        BookMeta(2,  "Exodus",           "EXO", "Old"),
        BookMeta(3,  "Leviticus",        "LEV", "Old"),
        BookMeta(4,  "Numbers",          "NUM", "Old"),
        BookMeta(5,  "Deuteronomy",      "DEU", "Old"),
        BookMeta(6,  "Joshua",           "JOS", "Old"),
        BookMeta(7,  "Judges",           "JDG", "Old"),
        BookMeta(8,  "Ruth",             "RUT", "Old"),
        BookMeta(9,  "1 Samuel",         "1SA", "Old"),
        BookMeta(10, "2 Samuel",         "2SA", "Old"),
        BookMeta(11, "1 Kings",          "1KI", "Old"),
        BookMeta(12, "2 Kings",          "2KI", "Old"),
        BookMeta(13, "1 Chronicles",     "1CH", "Old"),
        BookMeta(14, "2 Chronicles",     "2CH", "Old"),
        BookMeta(15, "Ezra",             "EZR", "Old"),
        BookMeta(16, "Nehemiah",         "NEH", "Old"),
        BookMeta(17, "Esther",           "EST", "Old"),
        BookMeta(18, "Job",              "JOB", "Old"),
        BookMeta(19, "Psalms",           "PSA", "Old"),
        BookMeta(20, "Proverbs",         "PRO", "Old"),
        BookMeta(21, "Ecclesiastes",     "ECC", "Old"),
        BookMeta(22, "Song of Solomon",  "SNG", "Old"),
        BookMeta(23, "Isaiah",           "ISA", "Old"),
        BookMeta(24, "Jeremiah",         "JER", "Old"),
        BookMeta(25, "Lamentations",     "LAM", "Old"),
        BookMeta(26, "Ezekiel",          "EZK", "Old"),
        BookMeta(27, "Daniel",           "DAN", "Old"),
        BookMeta(28, "Hosea",            "HOS", "Old"),
        BookMeta(29, "Joel",             "JOL", "Old"),
        BookMeta(30, "Amos",             "AMO", "Old"),
        BookMeta(31, "Obadiah",          "OBA", "Old"),
        BookMeta(32, "Jonah",            "JON", "Old"),
        BookMeta(33, "Micah",            "MIC", "Old"),
        BookMeta(34, "Nahum",            "NAM", "Old"),
        BookMeta(35, "Habakkuk",         "HAB", "Old"),
        BookMeta(36, "Zephaniah",        "ZEP", "Old"),
        BookMeta(37, "Haggai",           "HAG", "Old"),
        BookMeta(38, "Zechariah",        "ZEC", "Old"),
        BookMeta(39, "Malachi",          "MAL", "Old"),

        // New Testament
        BookMeta(40, "Matthew",          "MAT", "New"),
        BookMeta(41, "Mark",             "MRK", "New"),
        BookMeta(42, "Luke",             "LUK", "New"),
        BookMeta(43, "John",             "JHN", "New"),
        BookMeta(44, "Acts",             "ACT", "New"),
        BookMeta(45, "Romans",           "ROM", "New"),
        BookMeta(46, "1 Corinthians",    "1CO", "New"),
        BookMeta(47, "2 Corinthians",    "2CO", "New"),
        BookMeta(48, "Galatians",        "GAL", "New"),
        BookMeta(49, "Ephesians",        "EPH", "New"),
        BookMeta(50, "Philippians",      "PHP", "New"),
        BookMeta(51, "Colossians",       "COL", "New"),
        BookMeta(52, "1 Thessalonians",  "1TH", "New"),
        BookMeta(53, "2 Thessalonians",  "2TH", "New"),
        BookMeta(54, "1 Timothy",        "1TI", "New"),
        BookMeta(55, "2 Timothy",        "2TI", "New"),
        BookMeta(56, "Titus",            "TIT", "New"),
        BookMeta(57, "Philemon",         "PHM", "New"),
        BookMeta(58, "Hebrews",          "HEB", "New"),
        BookMeta(59, "James",            "JAS", "New"),
        BookMeta(60, "1 Peter",          "1PE", "New"),
        BookMeta(61, "2 Peter",          "2PE", "New"),
        BookMeta(62, "1 John",           "1JN", "New"),
        BookMeta(63, "2 John",           "2JN", "New"),
        BookMeta(64, "3 John",           "3JN", "New"),
        BookMeta(65, "Jude",             "JUD", "New"),
        BookMeta(66, "Revelation",       "REV", "New"),
    )

    private val byName: Map<String, BookMeta> = ALL.associateBy { it.displayName }
    private val byUsfm: Map<String, BookMeta> = ALL.associateBy { it.usfm }

    fun byName(name: String): BookMeta? = byName[name]
    fun byUsfm(usfm: String): BookMeta? = byUsfm[usfm]

    fun namesInTestament(testament: String): List<String> =
        ALL.asSequence()
            .filter { it.testament == testament }
            .sortedBy { it.order }
            .map { it.displayName }
            .toList()
}