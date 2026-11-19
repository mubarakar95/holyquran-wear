package ws.quran.wear.ui.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import androidx.compose.runtime.remember
import ws.quran.wear.R
import ws.quran.wear.data.model.Bookmark
import ws.quran.wear.data.repository.QuranRepository

@Composable
fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    quranRepository: QuranRepository,
    onSelectBookmark: (Int) -> Unit,
    onDeleteBookmark: (Int) -> Unit
) {
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = { ScrollIndicator(state = listState) }
    ) { contentPadding ->
        TransformingLazyColumn(
            state = listState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = stringResource(R.string.bookmarks),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            if (bookmarks.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_bookmarks),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(bookmarks, key = { it.id }) { bookmark ->
                    val surah = remember(bookmark.pageNumber) {
                        quranRepository.getSurahForPage(bookmark.pageNumber)
                    }
                    val displayName = bookmark.surahLatin.ifEmpty { surah.latin.ifEmpty { bookmark.surahArabic } }

                    Card(
                        onClick = { onSelectBookmark(bookmark.pageNumber) },
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Page ${bookmark.pageNumber}",
                                    style = MaterialTheme.typography.bodyExtraSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { onDeleteBookmark(bookmark.pageNumber) },
                                modifier = Modifier.size(32.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text("✕", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }
    }
}
