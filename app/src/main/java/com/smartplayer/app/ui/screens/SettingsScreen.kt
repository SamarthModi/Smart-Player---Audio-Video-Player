package com.smartplayer.app.ui.screens

import android.app.Activity
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplayer.app.R
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
    val systemLangCode = remember { LocaleHelper.getSystemLanguageCode() }
    val systemLangDisplay = remember { LocaleHelper.getSystemLanguageDisplayName() }
    val systemDefaultPrefix = stringResource(R.string.system_default)
    var decoderMode by remember { mutableStateOf(preferencesRepository.decoderMode) }
    var isAudioBoost by remember { mutableStateOf(preferencesRepository.isAudioBoostEnabled) }
    var isAutoResume by remember { mutableStateOf(preferencesRepository.isAutoResumeEnabled) }
    var isPauseOnUnplug by remember { mutableStateOf(preferencesRepository.isPauseOnUnplugEnabled) }
    var isBgPlay by remember { mutableStateOf(preferencesRepository.isBackgroundVideoPlayEnabled) }
    var doubleTapSeekSeconds by remember { mutableIntStateOf(preferencesRepository.doubleTapSeekSeconds) }
    var autoHideControlsSec by remember { mutableIntStateOf(preferencesRepository.autoHideControlsSec) }
    var gestureSensitivity by remember { mutableFloatStateOf(preferencesRepository.gestureSensitivity) }
    val themeStyle by preferencesRepository.themeStyleFlow.collectAsState()

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
            text = stringResource(R.string.settings_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. GENERAL & LANGUAGE
        Text(
            text = stringResource(R.string.section_general),
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
                        Text(
                            stringResource(R.string.app_language),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            when {
                                appLanguage == "system" || appLanguage == systemLangCode -> "$systemDefaultPrefix ($systemLangDisplay)"
                                appLanguage == "gu" -> "ગુજરાતી (Gujarati)"
                                appLanguage == "hi" -> "हिन्दी (Hindi)"
                                appLanguage == "mr" -> "मराठी (Marathi)"
                                appLanguage == "bn" -> "বাংলা (Bengali)"
                                appLanguage == "ta" -> "தமிழ் (Tamil)"
                                appLanguage == "te" -> "తెలుగు (Telugu)"
                                appLanguage == "en" -> "English"
                                appLanguage == "es" -> "Español (Spanish)"
                                appLanguage == "fr" -> "Français (French)"
                                appLanguage == "de" -> "Deutsch (German)"
                                appLanguage == "ar" -> "العربية (Arabic)"
                                appLanguage == "ru" -> "Русский (Russian)"
                                appLanguage == "pt" -> "Português (Portuguese)"
                                appLanguage == "ja" -> "日本語 (Japanese)"
                                appLanguage == "ko" -> "한국어 (Korean)"
                                appLanguage == "zh" -> "中文 (Chinese)"
                                else -> "$systemDefaultPrefix ($systemLangDisplay)"
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
                        Text(
                            stringResource(R.string.theme_appearance),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            when {
                                themeStyle.contains("AMOLED", ignoreCase = true) -> "AMOLED Pitch Black"
                                themeStyle.contains("Blue", ignoreCase = true) || themeStyle.equals("BLUE", ignoreCase = true) -> "Midnight Blue"
                                else -> "Dark Obsidian (Default)"
                            },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. VIDEO PLAYER SETTINGS (MX PLAYER PRO STYLE)
        Text(
            text = stringResource(R.string.section_video_player),
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
                        Text(stringResource(R.string.default_decoder), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(
                            if (decoderMode == "HW") stringResource(R.string.decoder_hw_desc) else stringResource(R.string.decoder_sw_desc),
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
                        Text(stringResource(R.string.resume_playback), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.resume_playback_desc), color = Color.Gray, fontSize = 12.sp)
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
                        Text(stringResource(R.string.audio_boost), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.audio_boost_desc), color = Color.Gray, fontSize = 12.sp)
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

                // Background Video Playback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.bg_video_playback), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.bg_video_playback_desc), color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isBgPlay,
                        onCheckedChange = {
                            isBgPlay = it
                            preferencesRepository.isBackgroundVideoPlayEnabled = it
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
                        Text(stringResource(R.string.double_tap_seek), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$doubleTapSeekSeconds ${stringResource(R.string.seconds)}", color = Color.Gray, fontSize = 12.sp)
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
                        Text(stringResource(R.string.auto_hide_controls), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("$autoHideControlsSec ${stringResource(R.string.seconds)}", color = Color.Gray, fontSize = 12.sp)
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
            text = stringResource(R.string.section_audio_player),
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
                        Text(stringResource(R.string.open_equalizer), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
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
                        Text(stringResource(R.string.pause_on_unplug), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.pause_on_unplug_desc), color = Color.Gray, fontSize = 12.sp)
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
            text = stringResource(R.string.section_library_storage),
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
                        Text(stringResource(R.string.rescan_media), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.rescan_media_desc), color = Color.Gray, fontSize = 12.sp)
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
                        Text(stringResource(R.string.clear_history), color = Color.Red, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(stringResource(R.string.clear_history_desc), color = Color.Gray, fontSize = 12.sp)
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
                        Text(stringResource(R.string.app_name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(stringResource(R.string.app_version_info), color = Color.Gray, fontSize = 12.sp)
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
        val allOtherLanguages = listOf(
            Pair("gu", "ગુજરાતી (Gujarati)"),
            Pair("hi", "हिन्दी (Hindi)"),
            Pair("mr", "मराठी (Marathi)"),
            Pair("bn", "বাংলা (Bengali)"),
            Pair("ta", "தமிழ் (Tamil)"),
            Pair("te", "తెలుగు (Telugu)"),
            Pair("en", "English"),
            Pair("es", "Español (Spanish)"),
            Pair("fr", "Français (French)"),
            Pair("de", "Deutsch (German)"),
            Pair("ar", "العربية (Arabic)"),
            Pair("ru", "Русский (Russian)"),
            Pair("pt", "Português (Portuguese)"),
            Pair("ja", "日本語 (Japanese)"),
            Pair("ko", "한국어 (Korean)"),
            Pair("zh", "中文 (Chinese)")
        ).filter { it.first != systemLangCode }

        val langs = listOf(
            Pair("system", "$systemDefaultPrefix ($systemLangDisplay)")
        ) + allOtherLanguages

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    langs.forEach { (code, name) ->
                        val isSelected = when (code) {
                            "system" -> appLanguage == "system" || appLanguage == systemLangCode
                            else -> appLanguage == code
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    appLanguage = code
                                    preferencesRepository.appLanguage = code
                                    LocaleHelper.updateContextLocale(context, code)
                                    LocaleHelper.applyLanguage(context, code)
                                    showLanguageDialog = false
                                    (context as? Activity)?.recreate()
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                name,
                                color = if (isSelected) NeonCyan else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.close), color = NeonCyan) }
            }
        )
    }

    // Double Tap Interval Dialog
    if (showDoubleTapDialog) {
        val intervals = listOf(5, 10, 15, 30)
        AlertDialog(
            onDismissRequest = { showDoubleTapDialog = false },
            title = { Text(stringResource(R.string.double_tap_seek), fontWeight = FontWeight.Bold) },
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
                                "$sec ${context.getString(R.string.seconds)}",
                                color = if (doubleTapSeekSeconds == sec) NeonCyan else Color.White,
                                fontWeight = if (doubleTapSeekSeconds == sec) FontWeight.Bold else FontWeight.Normal
                            )
                            if (doubleTapSeekSeconds == sec) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDoubleTapDialog = false }) { Text(stringResource(R.string.close), color = NeonCyan) }
            }
        )
    }

    // Auto-Hide Delay Dialog
    if (showAutoHideDialog) {
        val delays = listOf(2, 4, 6, 8)
        AlertDialog(
            onDismissRequest = { showAutoHideDialog = false },
            title = { Text(stringResource(R.string.auto_hide_controls), fontWeight = FontWeight.Bold) },
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
                                "$sec ${context.getString(R.string.seconds)}",
                                color = if (autoHideControlsSec == sec) NeonCyan else Color.White,
                                fontWeight = if (autoHideControlsSec == sec) FontWeight.Bold else FontWeight.Normal
                            )
                            if (autoHideControlsSec == sec) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoHideDialog = false }) { Text(stringResource(R.string.close), color = NeonCyan) }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        val themes = listOf(
            Triple("DARK", "Dark Obsidian (Default)", "Classic deep dark with neon cyan highlights"),
            Triple("AMOLED", "AMOLED Pitch Black", "100% Pure OLED black for maximum battery savings"),
            Triple("BLUE", "Midnight Blue", "Deep oceanic sapphire navy with cool accents")
        )
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme_appearance), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { (key, title, desc) ->
                        val isSelected = when (key) {
                            "AMOLED" -> themeStyle.contains("AMOLED", ignoreCase = true)
                            "BLUE" -> themeStyle.contains("Blue", ignoreCase = true) || themeStyle.equals("BLUE", ignoreCase = true)
                            else -> !themeStyle.contains("AMOLED", ignoreCase = true) && !themeStyle.contains("Blue", ignoreCase = true) && !themeStyle.equals("BLUE", ignoreCase = true)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    preferencesRepository.themeStyle = key
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    title,
                                    color = if (isSelected) NeonCyan else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                Text(
                                    desc,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text(stringResource(R.string.close), color = NeonCyan) }
            }
        )
    }
}
