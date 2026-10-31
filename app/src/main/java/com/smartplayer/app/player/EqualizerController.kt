package com.smartplayer.app.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import com.smartplayer.app.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BandInfo(
    val index: Short,
    val centerFreqHz: Int,
    var levelMilliBels: Short
) {
    val centerFreqFormatted: String
        get() = if (centerFreqHz >= 1000) "${centerFreqHz / 1000}kHz" else "${centerFreqHz}Hz"
}

data class EqualizerUiState(
    val isEnabled: Boolean = true,
    val minBandLevel: Short = -1500,
    val maxBandLevel: Short = 1500,
    val bands: List<BandInfo> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPresetIndex: Int = 0,
    val bassBoostStrength: Int = 300, // 0 - 1000
    val virtualizerStrength: Int = 250, // 0 - 1000
    val loudnessEnhancerGainMb: Int = 0 // 0 - 2000 mB for audio boost
)

class EqualizerController(
    private val preferencesRepository: PreferencesRepository
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentSessionId: Int = 0

    private val _uiState = MutableStateFlow(EqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()

    fun bindAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            // Equalizer
            val eq = Equalizer(0, audioSessionId).apply {
                enabled = preferencesRepository.isEqEnabled
            }
            equalizer = eq

            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]
            val numBands = eq.numberOfBands

            val bandList = mutableListOf<BandInfo>()
            for (i in 0 until numBands) {
                val band = i.toShort()
                val freq = eq.getCenterFreq(band) / 1000
                val level = eq.getBandLevel(band)
                bandList.add(BandInfo(index = band, centerFreqHz = freq, levelMilliBels = level))
            }

            val presetList = mutableListOf<String>()
            val numPresets = eq.numberOfPresets
            for (i in 0 until numPresets) {
                presetList.add(eq.getPresetName(i.toShort()))
            }
            presetList.add("Custom")

            // Bass Boost
            val bb = BassBoost(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(preferencesRepository.bassBoostLevel.toShort())
                    enabled = preferencesRepository.isEqEnabled
                }
            }
            bassBoost = bb

            // Virtualizer
            val virt = Virtualizer(0, audioSessionId).apply {
                if (strengthSupported) {
                    setStrength(preferencesRepository.virtualizerLevel.toShort())
                    enabled = preferencesRepository.isEqEnabled
                }
            }
            virtualizer = virt

            // Loudness Enhancer (Android 4.4+) for Audio Boost
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val le = LoudnessEnhancer(audioSessionId).apply {
                    enabled = preferencesRepository.isAudioBoostEnabled
                }
                loudnessEnhancer = le
            }

            _uiState.value = _uiState.value.copy(
                isEnabled = preferencesRepository.isEqEnabled,
                minBandLevel = minLevel,
                maxBandLevel = maxLevel,
                bands = bandList,
                presets = presetList,
                currentPresetIndex = preferencesRepository.eqPresetIndex,
                bassBoostStrength = preferencesRepository.bassBoostLevel,
                virtualizerStrength = preferencesRepository.virtualizerLevel
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEnabled(enabled: Boolean) {
        preferencesRepository.isEqEnabled = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(isEnabled = enabled)
    }

    fun setBandLevel(bandIndex: Short, level: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, level)
            val updatedBands = _uiState.value.bands.map {
                if (it.index == bandIndex) it.copy(levelMilliBels = level) else it
            }
            val customIndex = _uiState.value.presets.size - 1
            _uiState.value = _uiState.value.copy(
                bands = updatedBands,
                currentPresetIndex = customIndex
            )
            preferencesRepository.eqPresetIndex = customIndex
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPreset(presetIndex: Int) {
        try {
            val numPresets = equalizer?.numberOfPresets ?: 0
            if (presetIndex < numPresets) {
                equalizer?.usePreset(presetIndex.toShort())
                val updatedBands = _uiState.value.bands.map {
                    it.copy(levelMilliBels = equalizer?.getBandLevel(it.index) ?: it.levelMilliBels)
                }
                _uiState.value = _uiState.value.copy(
                    bands = updatedBands,
                    currentPresetIndex = presetIndex
                )
                preferencesRepository.eqPresetIndex = presetIndex
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoost(strength: Int) {
        preferencesRepository.bassBoostLevel = strength
        try {
            bassBoost?.setStrength(strength.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(bassBoostStrength = strength)
    }

    fun setVirtualizer(strength: Int) {
        preferencesRepository.virtualizerLevel = strength
        try {
            virtualizer?.setStrength(strength.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(virtualizerStrength = strength)
    }

    fun setAudioBoostGain(gainMilliBels: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMilliBels)
            loudnessEnhancer?.enabled = gainMilliBels > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = _uiState.value.copy(loudnessEnhancerGainMb = gainMilliBels)
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            equalizer = null
            bassBoost = null
            virtualizer = null
            loudnessEnhancer = null
            currentSessionId = 0
        }
    }
}
