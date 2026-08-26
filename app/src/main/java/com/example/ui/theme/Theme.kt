package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(false) }

private val DarkColorScheme = darkColorScheme(
    primary = GeoPrimaryDark,
    onPrimary = Color(0xFF381E72),
    primaryContainer = GeoPrimaryContainerDark,
    onPrimaryContainer = GeoOnPrimaryContainerDark,
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = GeoSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = GeoTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = GeoPrimary,
    onPrimary = Color.White,
    primaryContainer = GeoPrimaryContainerLight,
    onPrimaryContainer = GeoOnPrimaryContainerLight,
    secondary = GeoSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeoSecondaryContainerLight,
    onSecondaryContainer = GeoOnSecondaryContainerLight,
    tertiary = GeoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = GeoTertiaryContainerLight,
    onTertiaryContainer = Color(0xFF31111D),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder
)

@Composable
fun FitnessTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val darkThemeState = remember(darkTheme) { mutableStateOf(darkTheme) }

    CompositionLocalProvider(LocalThemeIsDark provides darkThemeState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

