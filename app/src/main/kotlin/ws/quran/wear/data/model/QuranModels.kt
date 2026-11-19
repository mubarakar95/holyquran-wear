package ws.quran.wear.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Surah metadata loaded from atlas.json.
 */
@Serializable
data class Surah(
    val n: Int,
    val page: Int,
    val ayahs: Int,
    val place: String,
    val arabic: String,
    val latin: String,
    val english: String
) {
    val isMakki: Boolean get() = place.equals("makkah", ignoreCase = true)
}

/**
 * Page boundary entry in atlas.json: [pageNumber, "firstSurah:firstAyah", "lastSurah:lastAyah", wordCount]
 */
data class QuranPage(
    val pageNumber: Int,
    val firstAyah: String,
    val lastAyah: String,
    val wordCount: Int
)

/**
 * Juz (1 to 30) representation derived from rubu_al_hizb boundaries.
 */
data class Juz(
    val number: Int,
    val startPage: Int,
    val startSurahNumber: Int,
    val startAyahNumber: Int,
    val startSurahArabic: String,
    val startSurahLatin: String = ""
)

/**
 * Rubu al-Hizb (1 to 240) representation from atlas.json: [rubuNumber, "surah:ayah", pageNumber]
 */
data class RubuAlHizb(
    val number: Int,
    val surahAyah: String,
    val pageNumber: Int
) {
    val juz: Int get() = ((number - 1) / 8) + 1
    val hizb: Int get() = ((number - 1) / 4) + 1
    val nisf: Int get() = ((number - 1) / 2) + 1
}

/**
 * User saved bookmark.
 */
@Serializable
data class Bookmark(
    val id: String,
    val pageNumber: Int,
    val surahNumber: Int,
    val surahArabic: String,
    val timestamp: Long,
    val surahLatin: String = ""
)

/**
 * Reader display settings.
 */
data class ReaderDisplaySettings(
    val isDarkMode: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
) {
    val backgroundColor: Long get() = if (isDarkMode) 0xFF000000 else 0xFFFBF8F1
    val inkColor: Long get() = if (isDarkMode) 0xFFE8D5B5 else 0xFF231F20
}

/**
 * DTO for deserializing atlas.json directly with kotlinx.serialization.
 */
@Serializable
data class AtlasDto(
    val surahs: List<Surah>,
    val pages: List<List<kotlinx.serialization.json.JsonElement>>,
    @SerialName("rubu_al_hizbs")
    val rubuAlHizbs: List<List<kotlinx.serialization.json.JsonElement>>
)
