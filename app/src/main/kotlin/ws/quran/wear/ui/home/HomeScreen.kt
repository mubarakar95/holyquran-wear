package ws.quran.wear.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.*
import ws.quran.wear.R
import ws.quran.wear.data.model.Surah

@Composable
fun HomeScreen(
    lastReadPage: Int,
    lastReadSurah: Surah,
    isDarkMode: Boolean,
    onContinueReading: (Int) -> Unit,
    onOpenSurahList: () -> Unit,
    onOpenJuzList: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onToggleTheme: () -> Unit
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
            // App Title Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Continue Reading Card
            item {
                Card(
                    onClick = { onContinueReading(lastReadPage) },
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.continue_reading),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${lastReadSurah.latin} • Page $lastReadPage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Surah Index Button
            item {
                Button(
                    onClick = onOpenSurahList,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.surah_index),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Juz & Hizb Index Button
            item {
                Button(
                    onClick = onOpenJuzList,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.juz_index),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Bookmarks Button
            item {
                Button(
                    onClick = onOpenBookmarks,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 3.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = stringResource(R.string.bookmarks),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Theme Toggle Button
            item {
                Button(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(top = 3.dp, bottom = 16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = if (isDarkMode) stringResource(R.string.light_mode) else stringResource(R.string.dark_mode),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
