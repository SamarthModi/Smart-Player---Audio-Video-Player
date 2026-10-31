package com.smartplayer.app.data

import android.content.Context
import android.content.SharedPreferences
import com.smartplayer.app.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PreferencesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_player_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private val _favoriteSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteSongIds: StateFlow<Set<Long>> = _favoriteSongIds.asStateFlow()

    private val _favoriteVideoIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteVideoIds: StateFlow<Set<Long>> = _favoriteVideoIds.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recentlyPlayedAudioIds = MutableStateFlow<List<Long>>(emptyList())
    val recentlyPlayedAudioIds: StateFlow<List<Long>> = _recentlyPlayedAudioIds.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val favSongs = prefs.getStringSet("favorite_song_ids", emptySet()) ?: emptySet()
        _favoriteSongIds.value = favSongs.mapNotNull { it.toLongOrNull() }.toSet()

        val favVideos = prefs.getStringSet("favorite_video_ids", emptySet()) ?: emptySet()
        _favoriteVideoIds.value = favVideos.mapNotNull { it.toLongOrNull() }.toSet()

        val recentAudio = prefs.getString("recent_audio_ids", "[]") ?: "[]"
        try {
            _recentlyPlayedAudioIds.value = json.decodeFromString(recentAudio)
        } catch (_: Exception) {
            _recentlyPlayedAudioIds.value = emptyList()
        }

        val playlistsJson = prefs.getString("custom_playlists", "[]") ?: "[]"
        try {
            _playlists.value = json.decodeFromString(playlistsJson)
        } catch (_: Exception) {
            _playlists.value = emptyList()
        }
    }

    // Video Resume Position
    fun saveVideoPosition(videoPath: String, positionMs: Long) {
        prefs.edit().putLong("vid_pos_${videoPath.hashCode()}", positionMs).apply()
    }

    fun getVideoPosition(videoPath: String): Long {
        return prefs.getLong("vid_pos_${videoPath.hashCode()}", 0L)
    }

    // Favorite Songs
    fun toggleFavoriteSong(songId: Long) {
        val current = _favoriteSongIds.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        _favoriteSongIds.value = current
        prefs.edit().putStringSet("favorite_song_ids", current.map { it.toString() }.toSet()).apply()
    }

    fun isFavoriteSong(songId: Long): Boolean = _favoriteSongIds.value.contains(songId)

    // Favorite Videos
    fun toggleFavoriteVideo(videoId: Long) {
        val current = _favoriteVideoIds.value.toMutableSet()
        if (current.contains(videoId)) {
            current.remove(videoId)
        } else {
            current.add(videoId)
        }
        _favoriteVideoIds.value = current
        prefs.edit().putStringSet("favorite_video_ids", current.map { it.toString() }.toSet()).apply()
    }

    fun isFavoriteVideo(videoId: Long): Boolean = _favoriteVideoIds.value.contains(videoId)

    // Recently played
    fun addRecentlyPlayedAudio(songId: Long) {
        val current = _recentlyPlayedAudioIds.value.toMutableList()
        current.remove(songId)
        current.add(0, songId)
        if (current.size > 100) {
            current.removeAt(current.size - 1)
        }
        _recentlyPlayedAudioIds.value = current
        prefs.edit().putString("recent_audio_ids", json.encodeToString(current)).apply()
    }

    // Custom Playlists
    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(
            id = "pl_${System.currentTimeMillis()}",
            name = name,
            createdAt = System.currentTimeMillis()
        )
        val current = _playlists.value.toMutableList()
        current.add(newPlaylist)
        savePlaylists(current)
    }

    fun addSongToPlaylist(playlistId: String, songId: Long) {
        val current = _playlists.value.toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val pl = current[index]
            if (!pl.songIds.contains(songId)) {
                current[index] = pl.copy(songIds = pl.songIds + songId)
                savePlaylists(current)
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: Long) {
        val current = _playlists.value.toMutableList()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val pl = current[index]
            current[index] = pl.copy(songIds = pl.songIds.filter { it != songId })
            savePlaylists(current)
        }
    }

    fun deletePlaylist(playlistId: String) {
        val current = _playlists.value.filter { it.id != playlistId }
        savePlaylists(current)
    }

    private fun savePlaylists(list: List<Playlist>) {
        _playlists.value = list
        prefs.edit().putString("custom_playlists", json.encodeToString(list)).apply()
    }

    // App Preferences
    var isAudioBoostEnabled: Boolean
        get() = prefs.getBoolean("audio_boost_enabled", true)
        set(value) = prefs.edit().putBoolean("audio_boost_enabled", value).apply()

    var isAutoResumeEnabled: Boolean
        get() = prefs.getBoolean("auto_resume_enabled", true)
        set(value) = prefs.edit().putBoolean("auto_resume_enabled", value).apply()

    var isPauseOnUnplugEnabled: Boolean
        get() = prefs.getBoolean("pause_on_unplug", true)
        set(value) = prefs.edit().putBoolean("pause_on_unplug", value).apply()

    var gestureSensitivity: Float
        get() = prefs.getFloat("gesture_sensitivity", 1.0f)
        set(value) = prefs.edit().putFloat("gesture_sensitivity", value).apply()

    var isEqEnabled: Boolean
        get() = prefs.getBoolean("eq_enabled", true)
        set(value) = prefs.edit().putBoolean("eq_enabled", value).apply()

    var eqPresetIndex: Int
        get() = prefs.getInt("eq_preset_index", 0)
        set(value) = prefs.edit().putInt("eq_preset_index", value).apply()

    var bassBoostLevel: Int
        get() = prefs.getInt("bass_boost_level", 300)
        set(value) = prefs.edit().putInt("bass_boost_level", value).apply()

    var virtualizerLevel: Int
        get() = prefs.getInt("virtualizer_level", 250)
        set(value) = prefs.edit().putInt("virtualizer_level", value).apply()

    // App Language ("system", "en", "gu", "hi")
    var appLanguage: String
        get() = prefs.getString("app_language", "system") ?: "system"
        set(value) = prefs.edit().putString("app_language", value).apply()

    // Decoder Mode ("HW", "SW")
    var decoderMode: String
        get() = prefs.getString("decoder_mode", "HW") ?: "HW"
        set(value) = prefs.edit().putString("decoder_mode", value).apply()

    // Double tap seek seconds (5, 10, 15, 30)
    var doubleTapSeekSeconds: Int
        get() = prefs.getInt("double_tap_seek_seconds", 10)
        set(value) = prefs.edit().putInt("double_tap_seek_seconds", value).apply()

    // Auto hide controls duration in seconds
    var autoHideControlsSec: Int
        get() = prefs.getInt("auto_hide_controls_sec", 4)
        set(value) = prefs.edit().putInt("auto_hide_controls_sec", value).apply()

    // Theme style ("AMOLED", "DARK", "BLUE")
    var themeStyle: String
        get() = prefs.getString("theme_style", "DARK") ?: "DARK"
        set(value) = prefs.edit().putString("theme_style", value).apply()

    fun clearPlaybackHistory() {
        _recentlyPlayedAudioIds.value = emptyList()
        prefs.edit().remove("recent_audio_ids").apply()
    }

    companion object {
        @Volatile
        private var instance: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: PreferencesRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
