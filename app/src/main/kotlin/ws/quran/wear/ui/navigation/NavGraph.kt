package ws.quran.wear.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import ws.quran.wear.data.model.Bookmark
import ws.quran.wear.data.model.Surah
import ws.quran.wear.data.repository.QuranRepository
import ws.quran.wear.ui.bookmarks.BookmarksScreen
import ws.quran.wear.ui.home.HomeScreen
import ws.quran.wear.ui.juz.JuzListScreen
import ws.quran.wear.ui.reader.ReaderScreen
import ws.quran.wear.ui.surah.SurahListScreen

@Composable
fun WearNavGraph(
    quranRepository: QuranRepository,
    lastReadPage: Int,
    lastReadSurah: Surah,
    isDarkMode: Boolean,
    bookmarks: List<Bookmark>,
    onPageRead: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Int) -> Unit
) {
    val navController = rememberSwipeDismissableNavController()
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(null)
    val isReader = currentBackStackEntry?.destination?.route == Screen.Reader.route

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        userSwipeEnabled = !isReader
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                lastReadPage = lastReadPage,
                lastReadSurah = lastReadSurah,
                isDarkMode = isDarkMode,
                onContinueReading = { page ->
                    navController.navigate(Screen.Reader.createRoute(page))
                },
                onOpenSurahList = {
                    navController.navigate(Screen.SurahList.route)
                },
                onOpenJuzList = {
                    navController.navigate(Screen.JuzList.route)
                },
                onOpenBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onToggleTheme = onToggleTheme
            )
        }

        composable(Screen.SurahList.route) {
            SurahListScreen(
                surahs = quranRepository.getSurahs(),
                onSelectSurah = { page ->
                    navController.navigate(Screen.Reader.createRoute(page))
                }
            )
        }

        composable(Screen.JuzList.route) {
            JuzListScreen(
                juzList = quranRepository.getJuzList(),
                onSelectJuz = { page ->
                    navController.navigate(Screen.Reader.createRoute(page))
                }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                bookmarks = bookmarks,
                quranRepository = quranRepository,
                onSelectBookmark = { page ->
                    navController.navigate(Screen.Reader.createRoute(page))
                },
                onDeleteBookmark = onDeleteBookmark
            )
        }

        composable(Screen.Reader.route) { backStackEntry ->
            val pageParam = backStackEntry.arguments?.getString("page")?.toIntOrNull() ?: lastReadPage
            ReaderScreen(
                initialPage = pageParam,
                quranRepository = quranRepository,
                isDarkMode = isDarkMode,
                bookmarks = bookmarks,
                onPageChanged = onPageRead,
                onToggleBookmark = onToggleBookmark,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
