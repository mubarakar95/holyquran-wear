package ws.quran.wear.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.foundation.rotary.rotaryScrollable
import androidx.wear.compose.material3.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ws.quran.qvp.QvpPageView
import ws.quran.wear.data.model.Bookmark
import ws.quran.wear.data.repository.QuranRepository

@Composable
fun ReaderScreen(
    initialPage: Int,
    quranRepository: QuranRepository,
    isDarkMode: Boolean,
    bookmarks: List<Bookmark>,
    onPageChanged: (Int) -> Unit,
    onToggleBookmark: (Bookmark) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Map 1-based pageNumber (1..604) to standard LTR pager index (0..603) inverted
    // so dragging Left-to-Right (→) naturally advances to the NEXT page (42 -> 43),
    // and dragging Right-to-Left (←) goes to PREVIOUS page (42 -> 41), without
    // triggering Compose's reverseLayout / RTL snap fling oscillation bug.
    val clampedInitial = (604 - initialPage).coerceIn(0, 603)
    val pagerState = rememberPagerState(
        initialPage = clampedInitial,
        pageCount = { 604 }
    )

    // Notify page change only when settled on a page, preventing mid-swipe DataStore writes & recompositions
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { settledIndex ->
                onPageChanged(604 - settledIndex)
            }
    }

    // Overlay visibility (surah info and bookmark button)
    var showOverlay by remember { mutableStateOf(false) }

    // Auto-hide overlay after 3 seconds
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000)
            showOverlay = false
        }
    }

    // Use settledPage so overlay and surah info stay stable during swiping and don't flicker at midpoint
    val currentPage = (604 - pagerState.settledPage).coerceIn(1, 604)
    val isBookmarked = bookmarks.any { it.pageNumber == currentPage }
    val currentSurah = remember(currentPage) {
        quranRepository.getSurahForPage(currentPage)
    }
    val ornaments = remember {
        quranRepository.getOrnaments()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color.Black else Color(0xFFFBF8F1))
    ) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
            key = { pageIndex -> pageIndex }
        ) { pageIndex ->
            val pageNumber = 604 - pageIndex
            val focusRequester = remember { FocusRequester() }
            var pageViewRef by remember { mutableStateOf<QvpPageView?>(null) }

            val scrollableState = remember(pageIndex) {
                ScrollableState { delta ->
                    val pv = pageViewRef ?: return@ScrollableState 0f
                    val oldOy = pv.viewOy
                    pv.scrollPage(delta)
                    pv.viewOy - oldOy
                }
            }
            val rotaryBehavior = RotaryScrollableDefaults.behavior(scrollableState)

            // Request focus for crown / bezel rotary input only when settled on this page
            LaunchedEffect(pagerState.settledPage, pageIndex) {
                if (pagerState.settledPage == pageIndex) {
                    delay(50)
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
            }

            // Load page bytes for this page
            val pageBytes = remember(pageNumber) {
                quranRepository.loadPageBytes(pageNumber)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .rotaryScrollable(
                        behavior = rotaryBehavior,
                        focusRequester = focusRequester
                    )
                    .scrollable(
                        state = scrollableState,
                        orientation = Orientation.Vertical
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        try {
                            focusRequester.requestFocus()
                        } catch (_: Exception) {}
                        showOverlay = !showOverlay
                    }
            ) {
                if (pageBytes.isNotEmpty()) {
                    QuranPageViewComposable(
                        pageBytes = pageBytes,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.fillMaxSize(),
                        ornaments = ornaments,
                        onViewReady = { pageViewRef = it }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Page $pageNumber",
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // Top Overlay Bar: Surah name & Page number
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                        .clickable(enabled = onBack != null) { onBack?.invoke() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${if (onBack != null) "‹ " else ""}${currentSurah.latin} • p. $currentPage",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bottom Bookmark Toggle Button
            AnimatedVisibility(
                visible = showOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                IconButton(
                    onClick = {
                        onToggleBookmark(
                            Bookmark(
                                id = "page_$currentPage",
                                pageNumber = currentPage,
                                surahNumber = currentSurah.n,
                                surahArabic = currentSurah.arabic,
                                surahLatin = currentSurah.latin,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isBookmarked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Text(
                        text = if (isBookmarked) "★" else "☆",
                        color = if (isBookmarked) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
