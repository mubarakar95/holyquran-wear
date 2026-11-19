package ws.quran.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import kotlinx.coroutines.launch
import ws.quran.wear.data.repository.PreferencesRepository
import ws.quran.wear.data.repository.QuranRepository
import ws.quran.wear.ui.navigation.WearNavGraph
import ws.quran.wear.ui.theme.QuranWearTheme

class MainActivity : ComponentActivity() {

    private lateinit var quranRepository: QuranRepository
    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        quranRepository = QuranRepository(applicationContext)
        preferencesRepository = PreferencesRepository(applicationContext)

        // Initialize atlas in background
        lifecycleScope.launch {
            quranRepository.ensureInitialized()
        }

        setContent {
            val lastReadPage by preferencesRepository.lastReadPageFlow.collectAsState(initial = 1)
            val isDarkMode by preferencesRepository.isDarkModeFlow.collectAsState(initial = true)
            val bookmarks by preferencesRepository.bookmarksFlow.collectAsState(initial = emptyList())

            val lastReadSurah = remember(lastReadPage) {
                quranRepository.getSurahForPage(lastReadPage)
            }

            QuranWearTheme(darkTheme = isDarkMode) {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    WearNavGraph(
                        quranRepository = quranRepository,
                        lastReadPage = lastReadPage,
                        lastReadSurah = lastReadSurah,
                        isDarkMode = isDarkMode,
                        bookmarks = bookmarks,
                        onPageRead = { page ->
                            lifecycleScope.launch {
                                preferencesRepository.saveLastReadPage(page)
                            }
                        },
                        onToggleTheme = {
                            lifecycleScope.launch {
                                preferencesRepository.toggleDarkMode()
                            }
                        },
                        onToggleBookmark = { bookmark ->
                            lifecycleScope.launch {
                                preferencesRepository.toggleBookmark(bookmark)
                            }
                        },
                        onDeleteBookmark = { page ->
                            lifecycleScope.launch {
                                preferencesRepository.removeBookmark(page)
                            }
                        }
                    )
                }
            }
        }
    }
}
