package com.example.mbible.data

/**
 * Describes one Bible translation the app can show.
 *
 * - LOCAL translations are served from the bundled SQLite file.
 * - REMOTE translations are fetched via the YouVersion Platform SDK
 *   and cached opportunistically.
 */
data class Translation(
    val id: String,            // stable short id used in prefs, e.g. "KJV", "NIV"
    val displayName: String,
    val abbreviation: String,
    val language: String,      // ISO-639-3 like "eng"
    val kind: Kind,
    val youVersionId: Int? = null  // required when kind == REMOTE
) {
    enum class Kind { LOCAL, REMOTE }
}

object Translations {

    val KJV = Translation(
        id = "KJV",
        displayName = "King James Version",
        abbreviation = "KJV",
        language = "eng",
        kind = Translation.Kind.LOCAL
    )

    /**
     * NIV via YouVersion Platform.
     *
     * TODO: verify [youVersionId] at runtime by listing English versions:
     *   YouVersionApi.bible.versions(languageCode = "eng")
     * Historically NIV is 111 on YouVersion's catalog; confirm before shipping.
     */
    val NIV = Translation(
        id = "NIV",
        displayName = "New International Version",
        abbreviation = "NIV",
        language = "eng",
        kind = Translation.Kind.REMOTE,
        youVersionId = 111
    )

    val ALL: List<Translation> = listOf(KJV, NIV)

    fun byId(id: String): Translation =
        ALL.firstOrNull { it.id == id } ?: KJV
}