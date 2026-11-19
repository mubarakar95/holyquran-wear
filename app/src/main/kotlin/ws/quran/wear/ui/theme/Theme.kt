package ws.quran.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val DarkGoldPrimary = Color(0xFFE2C98F)
val DarkGoldSecondary = Color(0xFFC7AC6B)
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF141414)
val DarkOnSurface = Color(0xFFEAEAEA)
val DarkOnPrimary = Color(0xFF1A1A1A)

val LightGoldPrimary = Color(0xFF8C6D37)
val LightGoldSecondary = Color(0xFFA6854C)
val LightBackground = Color(0xFFFBF8F1)
val LightSurface = Color(0xFFEFE8DB)
val LightOnSurface = Color(0xFF231F20)
val LightOnPrimary = Color(0xFFFFFFFF)

private val DarkColorScheme = ColorScheme(
    primary = DarkGoldPrimary,
    primaryDim = DarkGoldSecondary,
    onPrimary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surfaceContainer = DarkSurface,
    onSurface = DarkOnSurface,
    secondary = DarkGoldSecondary,
    onSecondary = DarkOnPrimary
)

private val LightColorScheme = ColorScheme(
    primary = LightGoldPrimary,
    primaryDim = LightGoldSecondary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnSurface,
    surfaceContainer = LightSurface,
    onSurface = LightOnSurface,
    secondary = LightGoldSecondary,
    onSecondary = LightOnPrimary
)

@Composable
fun QuranWearTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
