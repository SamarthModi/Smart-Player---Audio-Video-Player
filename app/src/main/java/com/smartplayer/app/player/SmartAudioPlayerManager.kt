package com.smartplayer.app.player

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
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.AudioItem
import com.smartplayer.app.model.SmartRepeatMode
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
    val sleepTimerMinutesRemaining: Int? = null
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
            Media3Item.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
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
