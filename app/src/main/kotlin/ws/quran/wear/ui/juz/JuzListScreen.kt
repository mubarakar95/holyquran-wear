package ws.quran.wear.ui.juz

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
import ws.quran.wear.R
import ws.quran.wear.data.model.Juz

@Composable
fun JuzListScreen(
    juzList: List<Juz>,
    onSelectJuz: (Int) -> Unit
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
                    text = stringResource(R.string.juz_index),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            items(juzList, key = { it.number }) { juz ->
                Card(
                    onClick = { onSelectJuz(juz.startPage) },
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
                                text = "${stringResource(R.string.juz)} ${juz.number}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Page ${juz.startPage}",
                                style = MaterialTheme.typography.bodyExtraSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${juz.startSurahLatin.ifEmpty { juz.startSurahArabic }} : ${juz.startAyahNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
