package ws.quran.wear.ui.reader

import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ws.quran.qvp.QvpColor
import ws.quran.qvp.QvpDressSpec
import ws.quran.qvp.QvpOrnamentRef
import ws.quran.qvp.QvpOrnaments
import ws.quran.qvp.QvpPage
import ws.quran.qvp.QvpPageView
import ws.quran.qvp.QvpTheme

@Composable
fun QuranPageViewComposable(
    pageBytes: ByteArray,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    ornaments: QvpOrnaments? = null,
    onViewReady: ((QvpPageView) -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // For circular Wear OS displays:
    // Vertical padding allows text to start from and scroll all the way to the center (widest chord).
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val padVertical = screenHeightPx * 0.44f
    val padHorizontal = 8f

    var qvpPage by remember(pageBytes, ornaments) {
        mutableStateOf<QvpPage?>(null)
    }

    DisposableEffect(pageBytes, ornaments) {
        val page = try {
            val p = QvpPage(pageBytes)
            if (ornaments != null && !ornaments.isClosed) {
                try {
                    val dressSpec = QvpDressSpec(
                        style = "mushaf-hafs-adi",
                        ayahMark = QvpOrnamentRef.Style("mushaf-hafs-adi"),
                        surahHeader = QvpOrnamentRef.Style("mushaf-hafs-adi"),
                        pageFrame = QvpOrnamentRef.None
                    )
                    val info = p.dress(ornaments, dressSpec)
                    Log.i("QuranWear", "Dressed page ${p.pageNo}: ayahMarks=${info?.ayahMarks}, surahHeaders=${info?.surahHeaders}")
                } catch (de: Exception) {
                    Log.e("QuranWear", "Error dressing page ${p.pageNo}", de)
                }
            }
            Log.i("QuranWear", "QvpPage loaded: pageNo=${p.pageNo}, lines=${p.nLines}, paths=${p.nPaths}")
            p
        } catch (e: Exception) {
            Log.e("QuranWear", "Error creating QvpPage", e)
            null
        }
        qvpPage = page

        onDispose {
            try {
                page?.close()
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val pageView = QvpPageView(context).apply {
                zoomEnabled = false // Gestures handled natively in Compose
                fillHeight = false // Fit width so text fills the circular diameter
                padTop = padVertical
                padBottom = padVertical
                padSide = padHorizontal
                onSwipe = null
                isClickable = false
                isFocusable = false

                // Ensure layout pass doesn't reset viewScale to fitScale (which shrinks for height)
                addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                    val pv = v as? QvpPageView ?: return@addOnLayoutChangeListener
                    if (pv.viewScale != 1.0f) {
                        pv.viewScale = 1.0f
                        pv.viewOx = 0.0f
                        pv.invalidate()
                    }
                }
            }

            val container = object : FrameLayout(context) {
                // By returning false from dispatchTouchEvent, the embedded Android View hierarchy
                // NEVER intercepts or consumes any touch events (ACTION_DOWN, MOVE, UP, CANCEL).
                // This ensures Compose's HorizontalPager and gesture modifiers have 100% unimpeded
                // control over pointer inputs, fling velocity, and snap settling.
                override fun dispatchTouchEvent(ev: MotionEvent?): Boolean = false

                // Pre-draw guarantee: QvpPageView.resetView() inside setPage/onSizeChanged resets
                // viewScale to fitScale. On Wear OS we always enforce fit-width (viewScale = 1.0f)
                // so the text fills the screen width legibly.
                override fun dispatchDraw(canvas: Canvas) {
                    val pv = getChildAt(0) as? QvpPageView
                    if (pv != null && pv.viewScale != 1.0f) {
                        pv.viewScale = 1.0f
                        pv.viewOx = 0.0f
                    }
                    super.dispatchDraw(canvas)
                }
            }
            container.addView(
                pageView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            onViewReady?.invoke(pageView)
            container
        },
        update = { container ->
            val view = container.getChildAt(0) as? QvpPageView ?: return@AndroidView
            onViewReady?.invoke(view)
            view.padTop = padVertical
            view.padBottom = padVertical
            view.padSide = padHorizontal
            view.onSwipe = null

            val page = qvpPage
            if (page != null && page.isOpen) {
                // QVP engine colors are uint32 RGBA (0xRRGGBBAA), not Android ARGB (0xAARRGGBB).
                // Use QvpColor.parse to produce RGBA colors with full alpha (0xFF).
                val inkRgba = if (isDarkMode) {
                    QvpColor.parse("#E8D5B5")
                } else {
                    QvpColor.parse("#231F20")
                }

                // View paperColor is an Android View property drawn directly with Paint.setColor(I),
                // so it expects standard Android ARGB.
                val paperColor = if (isDarkMode) {
                    Color.BLACK
                } else {
                    Color.rgb(0xFB, 0xF8, 0xF1)
                }

                view.paperColor = paperColor

                try {
                    page.setDefaultColor(inkRgba)
                    page.theme(
                        QvpTheme(
                            ink = inkRgba,
                            diacritics = inkRgba,
                            dots = inkRgba,
                            waqf = inkRgba,
                            sifr = inkRgba,
                            ayahMark = inkRgba,
                            numeral = inkRgba,
                            headers = inkRgba,
                            marks = emptyMap(),
                            transitionMs = 0
                        )
                    )
                } catch (e: Exception) {
                    Log.e("QuranWear", "Error setting page theme", e)
                }

                val pageChanged = (view.page !== page)
                if (pageChanged) {
                    view.page = page
                    view.viewScale = 1.0f
                    view.viewOx = 0.0f
                    view.viewOy = 0.0f
                } else if (view.viewScale != 1.0f) {
                    view.viewScale = 1.0f
                    view.viewOx = 0.0f
                }
                view.invalidate()
            }
        }
    )
}
