package com.youngestgen.smartplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.youngestgen.smartplayer.MainActivity
import com.youngestgen.smartplayer.R
import com.youngestgen.smartplayer.player.SmartAudioPlayerManager

@OptIn(UnstableApi::class)
class SmartAudioService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    private val noisyReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                val prefs = com.youngestgen.smartplayer.data.PreferencesRepository.getInstance(applicationContext)
                if (prefs.isPauseOnUnplugEnabled) {
                    val playerManager = SmartAudioPlayerManager.getInstance(applicationContext)
                    playerManager.pause()
                }
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                updateNotification()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val playerManager = SmartAudioPlayerManager.getInstance(this)

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, playerManager.exoPlayer)
            .setSessionActivity(pendingIntent)
            .setId("SmartAudioSession")
            .build()

        playerManager.exoPlayer.addListener(playerListener)

        val noisyFilter = android.content.IntentFilter(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(noisyReceiver, noisyFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(noisyReceiver, noisyFilter)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val playerManager = SmartAudioPlayerManager.getInstance(this)

        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                playerManager.stopPlayback()
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY_PAUSE -> {
                playerManager.playPause()
                updateNotification()
                return START_STICKY
            }
            ACTION_NEXT -> {
                playerManager.skipToNext()
                updateNotification()
                return START_STICKY
            }
            ACTION_PREV -> {
                playerManager.skipToPrevious()
                updateNotification()
                return START_STICKY
            }
        }

        // For ACTION_PLAY_IN_BACKGROUND, ACTION_START_FOREGROUND, or default start
        updateNotification()

        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        // If player is not playing or ended or idle, stop the service.
        // Sound continues playing uninterrupted in the foreground until user pauses or closes from notification!
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopSelf()
        }
    }

    private fun updateNotification() {
        val playerManager = SmartAudioPlayerManager.getInstance(this)
        val song = playerManager.playbackState.value.currentSong
        val isPlaying = playerManager.exoPlayer.isPlaying || playerManager.exoPlayer.playWhenReady

        if (song == null && !isPlaying) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (_: Exception) {}
            return
        }

        val title = song?.title ?: getString(R.string.app_name)
        val artist = song?.artist ?: getString(R.string.bg_video_playback)
        val notification = createForegroundNotification(title, artist, isPlaying)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createForegroundNotification(title: String, artist: String, isPlaying: Boolean): Notification {
        createNotificationChannel()

        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingActivityIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action: Previous
        val prevIntent = Intent(this, SmartAudioService::class.java).apply { action = ACTION_PREV }
        val pendingPrev = PendingIntent.getService(
            this, 101, prevIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action: Play / Pause
        val playPauseIntent = Intent(this, SmartAudioService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val pendingPlayPause = PendingIntent.getService(
            this, 102, playPauseIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action: Next
        val nextIntent = Intent(this, SmartAudioService::class.java).apply { action = ACTION_NEXT }
        val pendingNext = PendingIntent.getService(
            this, 103, nextIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action: Close / Stop
        val stopIntent = Intent(this, SmartAudioService::class.java).apply { action = ACTION_STOP_SERVICE }
        val pendingStop = PendingIntent.getService(
            this, 104, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist.ifBlank { getString(R.string.app_name) })
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingActivityIntent)
            .setDeleteIntent(pendingStop)
            .addAction(R.drawable.ic_notification_prev, getString(R.string.action_prev), pendingPrev)
            .addAction(
                if (isPlaying) R.drawable.ic_notification_pause else R.drawable.ic_notification_play,
                if (isPlaying) getString(R.string.action_pause) else getString(R.string.action_play),
                pendingPlayPause
            )
            .addAction(R.drawable.ic_notification_next, getString(R.string.action_next), pendingNext)
            .addAction(R.drawable.ic_notification_close, getString(R.string.close), pendingStop)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        mediaSession?.let { session ->
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(1, 2, 3)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(pendingStop)
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_desc)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        try {
            val playerManager = SmartAudioPlayerManager.getInstance(this)
            playerManager.exoPlayer.removeListener(playerListener)
        } catch (_: Exception) {}
        try {
            unregisterReceiver(noisyReceiver)
        } catch (_: Exception) {}
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "smart_player_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY_IN_BACKGROUND = "com.youngestgen.smartplayer.action.PLAY_IN_BACKGROUND"
        const val ACTION_START_FOREGROUND = "com.youngestgen.smartplayer.action.START_FOREGROUND"
        const val ACTION_PLAY_PAUSE = "com.youngestgen.smartplayer.action.PLAY_PAUSE"
        const val ACTION_NEXT = "com.youngestgen.smartplayer.action.NEXT"
        const val ACTION_PREV = "com.youngestgen.smartplayer.action.PREV"
        const val ACTION_STOP_SERVICE = "com.youngestgen.smartplayer.action.STOP_SERVICE"
    }
}
