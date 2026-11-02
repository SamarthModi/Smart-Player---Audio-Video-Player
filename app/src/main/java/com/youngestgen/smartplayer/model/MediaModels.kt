package com.youngestgen.smartplayer.model

import android.net.Uri
import kotlinx.serialization.Serializable

/**
 * Common interface for playable media
 */
sealed interface MediaItem {
    val id: Long
    val title: String
    val duration: Long
    val size: Long
    val path: String
    val uri: Uri
    val dateAdded: Long
}

/**
 * Representation of a video stored on local device
 */
@Serializable
data class VideoItem(
    override val id: Long,
    override val title: String,
    val displayName: String,
    override val duration: Long,
    override val size: Long,
    override val path: String,
    val uriString: String,
    override val dateAdded: Long,
    val width: Int = 0,
    val height: Int = 0,
    val folderName: String = "",
    val mimeType: String = "video/*",
    var lastPosition: Long = 0L
) : MediaItem {
    override val uri: Uri get() = Uri.parse(uriString)

    val resolutionFormatted: String
        get() = when {
            width >= 3840 || height >= 2160 -> "4K UHD"
            width >= 1920 || height >= 1080 -> "1080p FHD"
            width >= 1280 || height >= 720 -> "720p HD"
            width > 0 && height > 0 -> "${width}x${height}"
            else -> "Video"
        }
}

/**
 * Representation of an audio track / song on local device
 */
@Serializable
data class AudioItem(
    override val id: Long,
    override val title: String,
    val displayName: String,
    val artist: String = "<Unknown Artist>",
    val album: String = "<Unknown Album>",
    val albumId: Long = -1L,
    override val duration: Long,
    override val size: Long,
    override val path: String,
    val uriString: String,
    override val dateAdded: Long,
    val folderName: String = "",
    val trackNumber: Int = 0,
    val year: Int = 0
) : MediaItem {
    override val uri: Uri get() = Uri.parse(uriString)

    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")
}

/**
 * Media Folder for grouping videos or audios
 */
data class MediaFolder(
    val id: String,
    val name: String,
    val path: String,
    val mediaCount: Int,
    val isVideo: Boolean = true
)

/**
 * Custom or System Playlist
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSystemFavorites: Boolean = false,
    val isHistory: Boolean = false,
    val songIds: List<Long> = emptyList(),
    val videoIds: List<Long> = emptyList()
)

/**
 * Sorting options for media lists
 */
enum class SortOption(val displayName: String) {
    NAME("Name (A-Z)"),
    DATE("Date Added"),
    SIZE("File Size"),
    DURATION("Duration")
}

enum class SortDirection {
    ASCENDING,
    DESCENDING
}

/**
 * Aspect Ratio modes for Video Player (MX Player style)
 */
enum class AspectRatioMode(val displayName: String) {
    FIT("Fit to Screen"),
    FILL("Stretch / Fill"),
    ZOOM("Zoom / Crop"),
    RATIO_16_9("16:9 Widescreen"),
    RATIO_4_3("4:3 Standard")
}

/**
 * Playback Repeat Mode
 */
enum class SmartRepeatMode {
    OFF,
    ALL,
    ONE
}
