package com.youngestgen.smartplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.youngestgen.smartplayer.player.EqualizerController
import com.youngestgen.smartplayer.ui.theme.ElectricViolet
import com.youngestgen.smartplayer.ui.theme.NeonCyan
import com.youngestgen.smartplayer.ui.theme.PlayerBlack
import com.youngestgen.smartplayer.ui.theme.PlayerSurface
import com.youngestgen.smartplayer.ui.theme.PlayerSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    equalizerController: EqualizerController,
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by equalizerController.uiState.collectAsState()
    var isPresetDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PlayerBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Audio Equalizer",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Master On/Off Switch
            Switch(
                checked = uiState.isEnabled,
                onCheckedChange = { equalizerController.setEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PlayerBlack,
                    checkedTrackColor = NeonCyan
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Selector Dropdown
        if (uiState.presets.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = isPresetDropdownExpanded,
                onExpandedChange = { if (uiState.isEnabled) isPresetDropdownExpanded = it }
            ) {
                val currentPresetName = uiState.presets.getOrNull(uiState.currentPresetIndex) ?: "Custom"
                OutlinedTextField(
                    value = currentPresetName,
                    onValueChange = {},
                    readOnly = true,
                    enabled = uiState.isEnabled,
                    label = { Text("Sound Preset", color = NeonCyan) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPresetDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = isPresetDropdownExpanded,
                    onDismissRequest = { isPresetDropdownExpanded = false },
                    modifier = Modifier.background(PlayerSurface)
                ) {
                    uiState.presets.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.White) },
                            onClick = {
                                equalizerController.setPreset(index)
                                isPresetDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5-Band Equalizer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Frequency Bands",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                uiState.bands.forEach { band ->
                    val minLevel = uiState.minBandLevel.toFloat()
                    val maxLevel = uiState.maxBandLevel.toFloat()
                    val currentVal = band.levelMilliBels.toFloat()

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = band.centerFreqFormatted,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${band.levelMilliBels / 100} dB",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = currentVal,
                            onValueChange = { newVal ->
                                equalizerController.setBandLevel(band.index, newVal.toInt().toShort())
                            },
                            valueRange = minLevel..maxLevel,
                            enabled = uiState.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bass Boost & Virtualizer 3D Sound
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PlayerSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Bass Boost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bass Boost", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.bassBoostStrength / 10}%", color = ElectricViolet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = uiState.bassBoostStrength.toFloat(),
                    onValueChange = { equalizerController.setBassBoost(it.toInt()) },
                    valueRange = 0f..1000f,
                    enabled = uiState.isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricViolet,
                        activeTrackColor = ElectricViolet,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Virtualizer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3D Surround Virtualizer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${uiState.virtualizerStrength / 10}%", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = uiState.virtualizerStrength.toFloat(),
                    onValueChange = { equalizerController.setVirtualizer(it.toInt()) },
                    valueRange = 0f..1000f,
                    enabled = uiState.isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Audio Boost (Loudness Gain)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Loudness / Audio Boost (VLC Style)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("+${uiState.loudnessEnhancerGainMb / 100} dB", color = Color(0xFFFFB300), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = uiState.loudnessEnhancerGainMb.toFloat(),
                    onValueChange = { equalizerController.setAudioBoostGain(it.toInt()) },
                    valueRange = 0f..2000f,
                    enabled = uiState.isEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFFB300),
                        activeTrackColor = Color(0xFFFFB300),
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
