package com.youngestgen.smartplayer.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.youngestgen.smartplayer.data.PreferencesRepository
import com.youngestgen.smartplayer.model.AudioItem
import com.youngestgen.smartplayer.model.SmartRepeatMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AudioPlaybackState(
    val currentSong: AudioItem? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: SmartRepeatMode = SmartRepeatMode.OFF,
    val isShuffle: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val queue: List<AudioItem> = emptyList(),
    val queueIndex: Int = 0,
    val sleepTimerMinutesRemaining: Int? = null,
    val sleepAtEndOfSong: Boolean = false
)

@OptIn(UnstableApi::class)
class SmartAudioPlayerManager private constructor(private val context: Context) {

    private val preferencesRepository = PreferencesRepository.getInstance(context)
    val equalizerController = EqualizerController(preferencesRepository)

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true // handle audio focus automatically!
        )
        .setHandleAudioBecomingNoisy(true) // pause on headphone disconnect!
        .build()

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    private var progressTrackingJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        setupPlayerListener()
        startProgressTracker()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _playbackState.value = _playbackState.value.copy(
                        duration = exoPlayer.duration.coerceAtLeast(0L),
                        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                    )
                    // Bind audio session to Equalizer
                    equalizerController.bindAudioSession(exoPlayer.audioSessionId)
                } else if (state == Player.STATE_ENDED) {
                    handleTrackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: Media3Item?, reason: Int) {
                val currentIdx = exoPlayer.currentMediaItemIndex
                val queue = _playbackState.value.queue
                if (currentIdx in queue.indices) {
                    val song = queue[currentIdx]
                    _playbackState.value = _playbackState.value.copy(
                        currentSong = song,
                        queueIndex = currentIdx,
                        duration = song.duration
                    )
                    preferencesRepository.addRecentlyPlayedAudio(song.id)
                }
            }
        })
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = coroutineScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L),
                        duration = exoPlayer.duration.coerceAtLeast(0L)
                    )
                }
                delay(500)
            }
        }
    }

    private fun handleTrackEnded() {
        if (_playbackState.value.sleepAtEndOfSong) {
            _playbackState.value = _playbackState.value.copy(sleepAtEndOfSong = false)
            exoPlayer.pause()
            exoPlayer.seekTo(0)
            return
        }

        when (_playbackState.value.repeatMode) {
            SmartRepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            SmartRepeatMode.ALL -> {
                skipToNext()
            }
            SmartRepeatMode.OFF -> {
                if (exoPlayer.hasNextMediaItem()) {
                    skipToNext()
                } else {
                    exoPlayer.pause()
                    exoPlayer.seekTo(0)
                }
            }
        }
    }

    fun playSingleMedia(uri: android.net.Uri, title: String, path: String, startPositionMs: Long = 0L) {
        val audioItem = AudioItem(
            id = uri.hashCode().toLong(),
            title = title,
            displayName = title,
            artist = "SmartAV Player (Video Audio)",
            album = "Videos",
            albumId = -1L,
            duration = 0L,
            size = 0L,
            path = path,
            uriString = uri.toString(),
            dateAdded = System.currentTimeMillis()
        )
        _playbackState.value = _playbackState.value.copy(
            queue = listOf(audioItem),
            queueIndex = 0,
            currentSong = audioItem
        )
        exoPlayer.clearMediaItems()
        val mediaMetadata = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(title)
            .setArtist("SmartAV Player (Video Audio)")
            .build()
        val media3Item = Media3Item.Builder()
            .setUri(uri)
            .setMediaId(audioItem.id.toString())
            .setMediaMetadata(mediaMetadata)
            .build()
        exoPlayer.setMediaItem(media3Item, startPositionMs)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playQueue(songs: List<AudioItem>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        val validIndex = startIndex.coerceIn(0, songs.size - 1)

        _playbackState.value = _playbackState.value.copy(
            queue = songs,
            queueIndex = validIndex,
            currentSong = songs[validIndex]
        )

        exoPlayer.clearMediaItems()
        val media3Items = songs.map { song ->
            val mediaMetadata = androidx.media3.common.MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.albumArtUri)
                .build()
            Media3Item.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(mediaMetadata)
                .build()
        }
        exoPlayer.setMediaItems(media3Items, validIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        preferencesRepository.addRecentlyPlayedAudio(songs[validIndex].id)
    }

    fun playPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stopPlayback() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentSong = null,
            currentPosition = 0L,
            duration = 0L,
            queue = emptyList(),
            queueIndex = 0
        )
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_playbackState.value.repeatMode == SmartRepeatMode.ALL && _playbackState.value.queue.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000) {
            // Seek to beginning if more than 3s into the song
            exoPlayer.seekTo(0)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(0)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_playbackState.value.isShuffle
        exoPlayer.shuffleModeEnabled = newShuffle
        _playbackState.value = _playbackState.value.copy(isShuffle = newShuffle)
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            SmartRepeatMode.OFF -> SmartRepeatMode.ALL
            SmartRepeatMode.ALL -> SmartRepeatMode.ONE
            SmartRepeatMode.ONE -> SmartRepeatMode.OFF
        }
        val exoRepeatMode = when (nextMode) {
            SmartRepeatMode.OFF -> Player.REPEAT_MODE_OFF
            SmartRepeatMode.ALL -> Player.REPEAT_MODE_ALL
            SmartRepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        exoPlayer.repeatMode = exoRepeatMode
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun addToQueue(song: AudioItem) {
        val currentQueue = _playbackState.value.queue.toMutableList()
        currentQueue.add(song)
        _playbackState.value = _playbackState.value.copy(queue = currentQueue)

        exoPlayer.addMediaItem(
            Media3Item.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .build()
        )
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val current = _playbackState.value.queue.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            val newIdx = when (_playbackState.value.queueIndex) {
                fromIndex -> toIndex
                in (fromIndex + 1)..toIndex -> _playbackState.value.queueIndex - 1
                in toIndex until fromIndex -> _playbackState.value.queueIndex + 1
                else -> _playbackState.value.queueIndex
            }
            _playbackState.value = _playbackState.value.copy(queue = current, queueIndex = newIdx)
            exoPlayer.moveMediaItem(fromIndex, toIndex)
        }
    }

    fun removeFromQueue(index: Int) {
        val current = _playbackState.value.queue.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            exoPlayer.removeMediaItem(index)
            val newIdx = if (index < _playbackState.value.queueIndex) {
                _playbackState.value.queueIndex - 1
            } else _playbackState.value.queueIndex
            _playbackState.value = _playbackState.value.copy(queue = current, queueIndex = newIdx)
        }
    }

    fun setSleepAtEndOfSong(enabled: Boolean) {
        _playbackState.value = _playbackState.value.copy(sleepAtEndOfSong = enabled)
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = null)
            return
        }

        _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = minutes)

        sleepTimerJob = coroutineScope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60000L) // 1 minute
                remaining--
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = remaining)
            }
            if (isActive) {
                pause()
                _playbackState.value = _playbackState.value.copy(sleepTimerMinutesRemaining = null)
            }
        }
    }

    fun release() {
        progressTrackingJob?.cancel()
        sleepTimerJob?.cancel()
        equalizerController.release()
        exoPlayer.release()
    }

    companion object {
        @Volatile
        private var instance: SmartAudioPlayerManager? = null

        fun getInstance(context: Context): SmartAudioPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: SmartAudioPlayerManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
