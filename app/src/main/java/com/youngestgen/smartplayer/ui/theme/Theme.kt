package com.youngestgen.smartplayer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Dark Obsidian (Default)
val DarkObsidianColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DefaultPlayerBlack,
    primaryContainer = Color(0xFF202334),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = DefaultPlayerBlack,
    secondaryContainer = Color(0xFF202334),
    onSecondaryContainer = ElectricViolet,
    tertiary = VividAmber,
    background = Color(0xFF0D0E15),
    onBackground = TextPrimary,
    surface = Color(0xFF161824),
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF202334),
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF2C3048)
)

// 2. AMOLED Pitch Black (True 100% Black for OLED displays)
val AmoledColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF181818),
    onPrimaryContainer = NeonCyan,
    secondary = ElectricViolet,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF181818),
    onSecondaryContainer = ElectricViolet,
    tertiary = VividAmber,
    background = Color(0xFF000000), // True 100% Pitch Black
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D0D0D),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF2A2A2A)
)

// 3. Midnight Blue (Deep Sapphire Navy)
val MidnightBlueColorScheme = darkColorScheme(
    primary = Color(0xFF48CAE4),
    onPrimary = Color(0xFF0B132B),
    primaryContainer = Color(0xFF283655),
    onPrimaryContainer = Color(0xFF48CAE4),
    secondary = Color(0xFF5BC0BE),
    onSecondary = Color(0xFF0B132B),
    secondaryContainer = Color(0xFF283655),
    onSecondaryContainer = Color(0xFF5BC0BE),
    tertiary = VividAmber,
    background = Color(0xFF0B132B), // Deep Sapphire Midnight Blue
    onBackground = Color(0xFFF1FAEE),
    surface = Color(0xFF1C2541),
    onSurface = Color(0xFFF1FAEE),
    surfaceVariant = Color(0xFF283655),
    onSurfaceVariant = Color(0xFFA8DADC),
    outline = Color(0xFF3A506B)
)

@Composable
fun SmartPlayerTheme(
    themeStyle: String = "DARK",
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeStyle.contains("AMOLED", ignoreCase = true) -> AmoledColorScheme
        themeStyle.contains("Blue", ignoreCase = true) || themeStyle == "BLUE" -> MidnightBlueColorScheme
        else -> DarkObsidianColorScheme
    }

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
