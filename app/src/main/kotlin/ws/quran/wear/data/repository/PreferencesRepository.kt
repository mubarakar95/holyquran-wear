package ws.quran.wear.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ws.quran.wear.data.model.Bookmark

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quran_wear_prefs")

class PreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_LAST_READ_PAGE = intPreferencesKey("key_last_read_page")
        private val KEY_DARK_MODE = booleanPreferencesKey("key_dark_mode")
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("key_haptic_enabled")
        private val KEY_BOOKMARKS_JSON = stringPreferencesKey("key_bookmarks_json")
    }

    val lastReadPageFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_READ_PAGE] ?: 1
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true // Default to true for OLED watch displays
    }

    val isHapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAPTIC_ENABLED] ?: true
    }

    val bookmarksFlow: Flow<List<Bookmark>> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[KEY_BOOKMARKS_JSON] ?: "[]"
        try {
            json.decodeFromString<List<Bookmark>>(rawJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveLastReadPage(page: Int) {
        if (page in 1..604) {
            context.dataStore.edit { preferences ->
                preferences[KEY_LAST_READ_PAGE] = page
            }
        }
    }

    suspend fun toggleDarkMode() {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_DARK_MODE] ?: true
            preferences[KEY_DARK_MODE] = !current
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun toggleBookmark(bookmark: Bookmark) {
        context.dataStore.edit { preferences ->
            val rawJson = preferences[KEY_BOOKMARKS_JSON] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<Bookmark>>(rawJson).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }

            val existingIndex = currentList.indexOfFirst { it.pageNumber == bookmark.pageNumber }
            if (existingIndex >= 0) {
                currentList.removeAt(existingIndex)
            } else {
                currentList.add(0, bookmark)
            }

            preferences[KEY_BOOKMARKS_JSON] = json.encodeToString(currentList)
        }
    }

    suspend fun removeBookmark(pageNumber: Int) {
        context.dataStore.edit { preferences ->
            val rawJson = preferences[KEY_BOOKMARKS_JSON] ?: "[]"
            val currentList = try {
                json.decodeFromString<List<Bookmark>>(rawJson).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }
            currentList.removeAll { it.pageNumber == pageNumber }
            preferences[KEY_BOOKMARKS_JSON] = json.encodeToString(currentList)
        }
    }
}
