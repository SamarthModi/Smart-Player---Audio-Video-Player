package com.smartplayer.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Default constants for static/fallback usages
val DefaultPlayerBlack = Color(0xFF0D0E15)
val DefaultPlayerSurface = Color(0xFF161824)
val DefaultPlayerSurfaceVariant = Color(0xFF202334)
val DefaultPlayerSurfaceBorder = Color(0xFF2C3048)

// Dynamic theme-aware colors that adapt to Dark Obsidian, AMOLED Pitch Black, or Midnight Blue
val PlayerBlack: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val PlayerSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val PlayerSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val PlayerSurfaceBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val NeonCyan = Color(0xFF00E5FF)
val NeonCyanVariant = Color(0xFF00B0FF)
val ElectricViolet = Color(0xFF7C4DFF)
val VividAmber = Color(0xFFFFB300)
val VividPink = Color(0xFFFF4081)

val TextPrimary = Color(0xFFF0F2F8)
val TextSecondary = Color(0xFF9EABB8)
val TextTertiary = Color(0xFF6B7685)

val OverlayDark = Color(0xCC090A0F)
val OverlayGlass = Color(0x66131520)
