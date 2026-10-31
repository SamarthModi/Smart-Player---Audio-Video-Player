package com.smartplayer.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.util.LocaleHelper

@Composable
fun SettingsScreen(
    preferencesRepository: PreferencesRepository,
    onOpenEqualizer: () -> Unit,
    onRefreshMedia: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var appLanguage by remember { mutableStateOf(preferencesRepository.appLanguage) }
    var decoderMode by remember { mutableStateOf(preferencesRepository.decoderMode) }
    var isAudioBoost by remember { mutableStateOf(preferencesRepository.isAudioBoostEnabled) }
    var isAutoResume by remember { mutableStateOf(preferencesRepository.isAutoResumeEnabled) }
    var isPauseOnUnplug by remember { mutableStateOf(preferencesRepository.isPauseOnUnplugEnabled) }
    var doubleTapSeekSeconds by remember { mutableIntStateOf(preferencesRepository.doubleTapSeekSeconds) }
    var autoHideControlsSec by remember { mutableIntStateOf(preferencesRepository.autoHideControlsSec) }
    var gestureSensitivity by remember { mutableFloatStateOf(preferencesRepository.gestureSensitivity) }
    var themeStyle by remember { mutableStateOf(preferencesRepository.themeStyle) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDoubleTapDialog by remember { mutableStateOf(false) }
    var showAutoHideDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. GENERAL & LANGUAGE
        Text(
            text = "GENERAL & LANGUAGE",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Language Selection Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Language / ભાષા / भाषा", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            when (appLanguage) {
                                "gu" -> "ગુજરાતી (Gujarati)"
                                "hi" -> "हिन्दी (Hindi)"
                                "en" -> "English"
                                else -> "System Default"
                            },
                            color = NeonCyan,
                            fontSize = 12.sp
                        )
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Theme Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDialog = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = ElectricViolet)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme Appearance", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(themeStyle, color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. VIDEO PLAYER SETTINGS (MX PLAYER PRO STYLE)
        Text(
            text = "VIDEO PLAYER (MX PLAYER STYLE)",
            color = Color(0xFFFFB300),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Hardware vs Software Decoder Preference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Decoder", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            if (decoderMode == "HW") "Hardware (HW) - Maximum GPU Performance" else "Software (SW) - High Compatibility Mode",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = decoderMode == "HW",
                        onCheckedChange = {
                            decoderMode = if (it) "HW" else "SW"
                            preferencesRepository.decoderMode = decoderMode
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PlayerBlack, checkedTrackColor = Color(0xFFFFB300))
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Auto-Resume Video
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Resume Playback", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Automatically resume videos where you left off", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isAutoResume,
                        onCheckedChange = {
                            isAutoResume = it
                            preferencesRepository.isAutoResumeEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PlayerBlack, checkedTrackColor = NeonCyan)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // 200% Audio Boost (VLC & MX Player)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Audio Boost (200%)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Boost video volume beyond normal 100% maximum", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isAudioBoost,
                        onCheckedChange = {
                            isAudioBoost = it
                            preferencesRepository.isAudioBoostEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PlayerBlack, checkedTrackColor = NeonCyan)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Double Tap Seek Interval
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDoubleTapDialog = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Double-Tap Seek Interval", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$doubleTapSeekSeconds seconds", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Auto Hide Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAutoHideDialog = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricViolet)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Hide Controls Timeout", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$autoHideControlsSec seconds", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Gesture Sensitivity Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gesture Swipe Sensitivity", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("${(gestureSensitivity * 100).toInt()}%", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = gestureSensitivity,
                        onValueChange = {
                            gestureSensitivity = it
                            preferencesRepository.gestureSensitivity = it
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. AUDIO & MUSIC PLAYER SETTINGS
        Text(
            text = "AUDIO & MUSIC PLAYER",
            color = ElectricViolet,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Open Equalizer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenEqualizer() }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = ElectricViolet)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("5-Band Audio Equalizer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Bass Boost, 3D Surround Virtualizer, Presets", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Headset Disconnect Pause
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pause on Headset Disconnect", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Automatically pause music when headphones are unplugged", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isPauseOnUnplug,
                        onCheckedChange = {
                            isPauseOnUnplug = it
                            preferencesRepository.isPauseOnUnplugEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PlayerBlack, checkedTrackColor = ElectricViolet)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. STORAGE & MAINTENANCE
        Text(
            text = "STORAGE & MAINTENANCE",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Rescan Local Media
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onRefreshMedia()
                            Toast.makeText(context, "Scanning storage for new media files...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rescan Media Files", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Refresh local video and music library", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.08f))

                // Clear History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            preferencesRepository.clearPlaybackHistory()
                            Toast.makeText(context, "Playback history cleared", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear Playback History", color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Remove all songs from recently played history", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. ABOUT
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Smart Player Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Version 1.1.0 • Hardware Accelerated MX & VLC Engine", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Video Decoders: Hardware (HW) & Software (SW) with 4K 60fps support\n" +
                            "• Audio Formats: FLAC, MP3, AAC, OGG, WAV, OPUS, ALAC (Lossless)\n" +
                            "• Gestures: Brightness, Volume, Seek, Double Tap, Aspect Ratio\n" +
                            "• Subtitles: Embedded + External SRT/VTT with Sync offset\n" +
                            "• Audio: Dual audio tracks, 5-Band EQ, Bass Boost, 200% Loudness",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        val langs = listOf(
            Pair("system", "System Default (Default)"),
            Pair("gu", "ગુજરાતી (Gujarati)"),
            Pair("hi", "हिन्दी (Hindi)"),
            Pair("en", "English")
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Language", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    langs.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appLanguage = code
                                    preferencesRepository.appLanguage = code
                                    LocaleHelper.applyLanguage(code)
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language changed to $name", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                name,
                                color = if (appLanguage == code) NeonCyan else Color.White,
                                fontWeight = if (appLanguage == code) FontWeight.Bold else FontWeight.Normal
                            )
                            if (appLanguage == code) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Double Tap Interval Dialog
    if (showDoubleTapDialog) {
        val intervals = listOf(5, 10, 15, 30)
        AlertDialog(
            onDismissRequest = { showDoubleTapDialog = false },
            title = { Text("Double-Tap Seek Interval", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    intervals.forEach { sec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    doubleTapSeekSeconds = sec
                                    preferencesRepository.doubleTapSeekSeconds = sec
                                    showDoubleTapDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "$sec Seconds",
                                color = if (doubleTapSeekSeconds == sec) NeonCyan else Color.White,
                                fontWeight = if (doubleTapSeekSeconds == sec) FontWeight.Bold else FontWeight.Normal
                            )
                            if (doubleTapSeekSeconds == sec) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoubleTapDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Auto-Hide Delay Dialog
    if (showAutoHideDialog) {
        val delays = listOf(2, 4, 6, 8)
        AlertDialog(
            onDismissRequest = { showAutoHideDialog = false },
            title = { Text("Auto-Hide Controls Delay", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    delays.forEach { sec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    autoHideControlsSec = sec
                                    preferencesRepository.autoHideControlsSec = sec
                                    showAutoHideDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "$sec Seconds",
                                color = if (autoHideControlsSec == sec) NeonCyan else Color.White,
                                fontWeight = if (autoHideControlsSec == sec) FontWeight.Bold else FontWeight.Normal
                            )
                            if (autoHideControlsSec == sec) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoHideDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf("Dark Obsidian (Default)", "AMOLED Pitch Black", "Midnight Blue")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme Appearance", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    themeStyle = t
                                    preferencesRepository.themeStyle = t
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                t,
                                color = if (themeStyle == t) NeonCyan else Color.White,
                                fontWeight = if (themeStyle == t) FontWeight.Bold else FontWeight.Normal
                            )
                            if (themeStyle == t) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }
}
