package ws.quran.wear.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object SurahList : Screen("surah_list")
    data object JuzList : Screen("juz_list")
    data object Bookmarks : Screen("bookmarks")
    data object Reader : Screen("reader/{page}") {
        fun createRoute(page: Int) = "reader/$page"
    }
}
