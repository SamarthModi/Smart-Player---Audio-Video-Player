package com.youngestgen.smartplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.youngestgen.smartplayer.model.AudioItem
import com.youngestgen.smartplayer.model.MediaFolder
import com.youngestgen.smartplayer.model.SortDirection
import com.youngestgen.smartplayer.model.SortOption
import com.youngestgen.smartplayer.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreRepository(private val context: Context) {

    /**
     * Fetch all video files from MediaStore
     */
    suspend fun getAllVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Video"
                    val displayName = cursor.getString(displayNameColumn) ?: title
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val mimeType = cursor.getString(mimeTypeColumn) ?: "video/*"

                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val folderName = if (path.isNotBlank()) {
                        File(path).parentFile?.name ?: "Unknown"
                    } else "Unknown"

                    // Ignore tiny files < 10KB or empty files
                    if (size > 10240) {
                        videoList.add(
                            VideoItem(
                                id = id,
                                title = title,
                                displayName = displayName,
                                duration = duration,
                                size = size,
                                path = path,
                                uriString = uri.toString(),
                                dateAdded = dateAdded,
                                width = width,
                                height = height,
                                folderName = folderName,
                                mimeType = mimeType
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videoList
    }

    /**
     * Fetch all audio / music files from MediaStore
     */
    suspend fun getAllAudios(): List<AudioItem> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<AudioItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0) OR (${MediaStore.Audio.Media.DURATION} >= 2000)"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val displayName = cursor.getString(displayNameColumn) ?: title
                    val artist = cursor.getString(artistColumn) ?: "<Unknown Artist>"
                    val album = cursor.getString(albumColumn) ?: "<Unknown Album>"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val path = cursor.getString(dataColumn) ?: ""
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val track = cursor.getInt(trackColumn)
                    val year = cursor.getInt(yearColumn)

                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val folderName = if (path.isNotBlank()) {
                        File(path).parentFile?.name ?: "Unknown"
                    } else "Unknown"

                    // Ignore tiny corrupted files (< 3 seconds or < 5KB)
                    if (duration >= 3000 && size > 5120) {
                        audioList.add(
                            AudioItem(
                                id = id,
                                title = title,
                                displayName = displayName,
                                artist = artist,
                                album = album,
                                albumId = albumId,
                                duration = duration,
                                size = size,
                                path = path,
                                uriString = uri.toString(),
                                dateAdded = dateAdded,
                                folderName = folderName,
                                trackNumber = track,
                                year = year
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        audioList
    }

    /**
     * Group videos into folders
     */
    fun groupVideosByFolders(videos: List<VideoItem>): List<MediaFolder> {
        return videos.groupBy { it.folderName }
            .map { (folderName, items) ->
                val firstPath = items.firstOrNull()?.path ?: ""
                val folderPath = if (firstPath.isNotBlank()) File(firstPath).parent ?: "" else ""
                MediaFolder(
                    id = folderName,
                    name = folderName,
                    path = folderPath,
                    mediaCount = items.size,
                    isVideo = true
                )
            }.sortedByDescending { it.mediaCount }
    }

    /**
     * Group audios into folders
     */
    fun groupAudiosByFolders(audios: List<AudioItem>): List<MediaFolder> {
        return audios.groupBy { it.folderName }
            .map { (folderName, items) ->
                val firstPath = items.firstOrNull()?.path ?: ""
                val folderPath = if (firstPath.isNotBlank()) File(firstPath).parent ?: "" else ""
                MediaFolder(
                    id = folderName,
                    name = folderName,
                    path = folderPath,
                    mediaCount = items.size,
                    isVideo = false
                )
            }.sortedByDescending { it.mediaCount }
    }

    /**
     * Sort Videos according to option and direction
     */
    fun sortVideos(
        videos: List<VideoItem>,
        option: SortOption,
        direction: SortDirection
    ): List<VideoItem> {
        val sorted = when (option) {
            SortOption.NAME -> videos.sortedBy { it.displayName.lowercase() }
            SortOption.DATE -> videos.sortedBy { it.dateAdded }
            SortOption.SIZE -> videos.sortedBy { it.size }
            SortOption.DURATION -> videos.sortedBy { it.duration }
        }
        return if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    /**
     * Sort Audios according to option and direction
     */
    fun sortAudios(
        audios: List<AudioItem>,
        option: SortOption,
        direction: SortDirection
    ): List<AudioItem> {
        val sorted = when (option) {
            SortOption.NAME -> audios.sortedBy { it.title.lowercase() }
            SortOption.DATE -> audios.sortedBy { it.dateAdded }
            SortOption.SIZE -> audios.sortedBy { it.size }
            SortOption.DURATION -> audios.sortedBy { it.duration }
        }
        return if (direction == SortDirection.DESCENDING) sorted.reversed() else sorted
    }

    companion object {
        @Volatile
        private var instance: MediaStoreRepository? = null

        fun getInstance(context: Context): MediaStoreRepository {
            return instance ?: synchronized(this) {
                instance ?: MediaStoreRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
