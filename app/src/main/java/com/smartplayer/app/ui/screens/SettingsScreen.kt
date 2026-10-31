package com.smartplayer.app.ui.screens

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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.PlayerSurfaceVariant

@Composable
fun SettingsScreen(
    preferencesRepository: PreferencesRepository,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAudioBoost by remember { mutableStateOf(preferencesRepository.isAudioBoostEnabled) }
    var isAutoResume by remember { mutableStateOf(preferencesRepository.isAutoResumeEnabled) }
    var isPauseOnUnplug by remember { mutableStateOf(preferencesRepository.isPauseOnUnplugEnabled) }
    var gestureSensitivity by remember { mutableFloatStateOf(preferencesRepository.gestureSensitivity) }

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

        // Video Player Settings Card
        Text(
            text = "VIDEO PLAYER",
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                // Audio Boost (VLC Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Audio Boost (200%)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Boost volume beyond system maximum (VLC style)", color = Color.Gray, fontSize = 12.sp)
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                // Gesture Sensitivity
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gesture Sensitivity", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("${(gestureSensitivity * 100).toInt()}%", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Slider(
                        value = gestureSensitivity,
                        onValueChange = {
                            gestureSensitivity = it
                            preferencesRepository.gestureSensitivity = it
                        },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Music & Audio Settings Card
        Text(
            text = "AUDIO & EQUALIZER",
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
                // Open Equalizer Row
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
                        Text("Audio Equalizer", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Bass Boost, 3D Virtualizer, Presets", color = Color.Gray, fontSize = 12.sp)
                    }
                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

                // Pause on Headset Disconnect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pause on Headset Unplug", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Automatically pause audio when headphones are disconnected", color = Color.Gray, fontSize = 12.sp)
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

        // About Card
        Text(
            text = "ABOUT SMART PLAYER",
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB300))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Smart Player Pro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Version 1.0.0 • Hardware Accelerated", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Supported Codecs & Formats:\n" +
                            "• Video: MP4, MKV, WEBM, AVI, 3GP, MOV, FLV, TS, M4V (up to 4K 60fps)\n" +
                            "• Audio: MP3, FLAC, AAC, WAV, OGG, M4A, OPUS, ALAC, AMR\n" +
                            "• Subtitles: SRT, VTT, SSA, ASS\n" +
                            "• Engine: AndroidX Media3 ExoPlayer & MediaSessionService",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
