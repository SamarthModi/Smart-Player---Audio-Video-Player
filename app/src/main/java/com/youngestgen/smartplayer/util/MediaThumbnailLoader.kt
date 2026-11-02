package com.youngestgen.smartplayer.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.youngestgen.smartplayer.ui.theme.NeonCyan
import com.youngestgen.smartplayer.ui.theme.PlayerSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaThumbnailLoader {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 4).coerceAtLeast(1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return (bitmap.byteCount / 1024).coerceAtLeast(1)
        }
    }

    suspend fun loadVideoThumbnail(
        context: Context,
        videoUri: Uri,
        videoPath: String,
        width: Int = 320,
        height: Int = 240
    ): Bitmap? {
        val cacheKey = "vid_${videoUri}_${width}x${height}"
        memoryCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            var bitmap: Bitmap? = null

            // 1. Android 10+ (API 29+) hardware-accelerated MediaStore thumbnail
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    bitmap = context.contentResolver.loadThumbnail(videoUri, Size(width, height), null)
                } catch (_: Exception) {}
            }

            // 2. MediaMetadataRetriever
            if (bitmap == null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    var initialized = false
                    try {
                        retriever.setDataSource(context, videoUri)
                        initialized = true
                    } catch (_: Exception) {
                        if (videoPath.isNotBlank()) {
                            try {
                                retriever.setDataSource(videoPath)
                                initialized = true
                            } catch (_: Exception) {}
                        }
                    }
                    if (initialized) {
                        bitmap = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            ?: retriever.frameAtTime
                    }
                    retriever.release()
                } catch (_: Exception) {}
            }

            // 3. ThumbnailUtils fallback
            if (bitmap == null && videoPath.isNotBlank()) {
                try {
                    @Suppress("DEPRECATION")
                    bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND)
                } catch (_: Exception) {}
            }

            if (bitmap != null) {
                memoryCache.put(cacheKey, bitmap)
            }
            bitmap
        }
    }

    suspend fun loadAudioArtwork(
        context: Context,
        audioUri: Uri,
        albumId: Long,
        width: Int = 256,
        height: Int = 256
    ): Bitmap? {
        val cacheKey = "aud_${audioUri}_${albumId}_${width}x${height}"
        memoryCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            var bitmap: Bitmap? = null

            // 1. Android 10+ (API 29+) native MediaStore loadThumbnail
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    bitmap = context.contentResolver.loadThumbnail(audioUri, Size(width, height), null)
                } catch (_: Exception) {}
            }

            // 2. MediaMetadataRetriever embeddedPicture (extracts ID3 album art)
            if (bitmap == null) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, audioUri)
                    val artBytes = retriever.embeddedPicture
                    retriever.release()
                    if (artBytes != null && artBytes.isNotEmpty()) {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, options)
                        options.inSampleSize = calculateInSampleSize(options, width, height)
                        options.inJustDecodeBounds = false
                        bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, options)
                    }
                } catch (_: Exception) {}
            }

            // 3. Fallback to ContentUris albumart table
            if (bitmap == null && albumId > 0) {
                try {
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )
                    context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
                        bitmap = BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) {}
            }

            if (bitmap != null) {
                memoryCache.put(cacheKey, bitmap)
            }
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

@Composable
fun VideoThumbnail(
    videoUri: Uri,
    videoPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(videoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(videoUri, videoPath) {
        bitmap = MediaThumbnailLoader.loadVideoThumbnail(context, videoUri, videoPath)
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AudioAlbumArt(
    audioUri: Uri,
    albumId: Long,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    iconTint: Color = NeonCyan,
    iconSize: Dp = 22.dp
) {
    val context = LocalContext.current
    var bitmap by remember(audioUri, albumId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(audioUri, albumId) {
        bitmap = MediaThumbnailLoader.loadAudioArtwork(context, audioUri, albumId)
    }

    Box(
        modifier = modifier.background(PlayerSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
