package com.smartplayer.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = PlayerBlack,
    primaryContainer = PlayerSurfaceVariant,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = PlayerBlack,
    secondaryContainer = PlayerSurfaceVariant,
    onSecondaryContainer = ElectricViolet,
    tertiary = VividAmber,
    background = PlayerBlack,
    onBackground = TextPrimary,
    surface = PlayerSurface,
    onSurface = TextPrimary,
    surfaceVariant = PlayerSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = PlayerSurfaceBorder
)

@Composable
fun SmartPlayerTheme(
    darkTheme: Boolean = true, // default to premium dark theme for media playback
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
