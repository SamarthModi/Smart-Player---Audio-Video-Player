package com.smartplayer.app.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.AspectRatioMode
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.SmartPlayerTheme
import com.smartplayer.app.util.FormatUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@OptIn(UnstableApi::class)
class VideoPlayerActivity : ComponentActivity() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var preferencesRepository: PreferencesRepository
    private var videoUriString: String = ""
    private var videoTitle: String = ""
    private var videoPath: String = ""

    private var audioManager: AudioManager? = null
    private var maxVolume: Int = 15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUi()

        preferencesRepository = PreferencesRepository.getInstance(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15

        // Extract intent extras or data URI
        intent.data?.let { uri ->
            videoUriString = uri.toString()
            videoTitle = uri.lastPathSegment ?: "Playing Video"
            videoPath = uri.path ?: ""
        }
        intent.getStringExtra(EXTRA_VIDEO_URI)?.let { videoUriString = it }
        intent.getStringExtra(EXTRA_VIDEO_TITLE)?.let { videoTitle = it }
        intent.getStringExtra(EXTRA_VIDEO_PATH)?.let { videoPath = it }

        initPlayer()

        setContent {
            SmartPlayerTheme {
                VideoPlayerScreen(
                    exoPlayer = exoPlayer,
                    videoTitle = videoTitle,
                    videoPath = videoPath,
                    onBackPress = { finish() },
                    onEnterPip = { enterPipMode() },
                    onBrightnessChange = { setScreenBrightness(it) },
                    onVolumeChange = { setMediaVolume(it) },
                    getCurrentVolume = { getCurrentMediaVolume() },
                    getMaxVolume = { maxVolume }
                )
            }
        }
    }

    private fun initPlayer() {
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()

        exoPlayer = player

        if (videoUriString.isNotBlank()) {
            val mediaItem = Media3Item.fromUri(Uri.parse(videoUriString))
            player.setMediaItem(mediaItem)

            // Auto-resume from last saved position
            val savedPos = preferencesRepository.getVideoPosition(videoPath)
            if (savedPos > 3000 && preferencesRepository.isAutoResumeEnabled) {
                player.seekTo(savedPos)
            }

            player.prepare()
            player.play()
        }
    }

    private fun hideSystemUi() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun setScreenBrightness(brightnessDelta: Float) {
        val layout = window.attributes
        var current = layout.screenBrightness
        if (current < 0) current = 0.5f
        current = (current + brightnessDelta).coerceIn(0.01f, 1.0f)
        layout.screenBrightness = current
        window.attributes = layout
    }

    private fun getCurrentMediaVolume(): Int {
        return audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
    }

    private fun setMediaVolume(volumeDelta: Int) {
        val current = getCurrentMediaVolume()
        val target = (current + volumeDelta).coerceIn(0, maxVolume)
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val format = exoPlayer?.videoFormat
            val width = format?.width ?: 16
            val height = format?.height ?: 9
            val rational = if (width > 0 && height > 0) Rational(width, height) else Rational(16, 9)
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(rational)
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            hideSystemUi()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto PiP when user presses home button
        if (exoPlayer?.isPlaying == true) {
            enterPipMode()
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.let { player ->
            if (videoPath.isNotBlank()) {
                preferencesRepository.saveVideoPosition(videoPath, player.currentPosition)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInPictureInPictureMode) {
                player.pause()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }

    companion object {
        const val EXTRA_VIDEO_URI = "extra_video_uri"
        const val EXTRA_VIDEO_TITLE = "extra_video_title"
        const val EXTRA_VIDEO_PATH = "extra_video_path"

        fun start(context: Context, uri: Uri, title: String, path: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URI, uri.toString())
                putExtra(EXTRA_VIDEO_TITLE, title)
                putExtra(EXTRA_VIDEO_PATH, path)
            }
            context.startActivity(intent)
        }
    }
}

/**
 * Fullscreen Interactive Video Player View
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    exoPlayer: ExoPlayer?,
    videoTitle: String,
    videoPath: String,
    onBackPress: () -> Unit,
    onEnterPip: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Int) -> Unit,
    getCurrentVolume: () -> Int,
    getMaxVolume: () -> Int
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isControlsVisible by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var bufferPosition by remember { mutableLongStateOf(0L) }

    var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.FIT) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Gesture HUD States
    var gestureHudType by remember { mutableStateOf<String?>(null) } // "volume", "brightness", "seek"
    var gestureHudValue by remember { mutableFloatStateOf(0f) }
    var gestureSeekTimeText by remember { mutableStateOf("") }
    var gestureSeekDeltaText by remember { mutableStateOf("") }

    // Double tap indicators
    var showDoubleTapRewind by remember { mutableStateOf(false) }
    var showDoubleTapForward by remember { mutableStateOf(false) }

    // Dialogs
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAudioTrackDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleControlsAutoHide() {
        autoHideJob?.cancel()
        if (isControlsVisible && !isScreenLocked) {
            autoHideJob = coroutineScope.launch {
                delay(4000)
                isControlsVisible = false
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        })

        while (isActive) {
            exoPlayer?.let { player ->
                currentPosition = player.currentPosition.coerceAtLeast(0L)
                duration = player.duration.coerceAtLeast(0L)
                bufferPosition = player.bufferedPosition.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    LaunchedEffect(isControlsVisible) {
        scheduleControlsAutoHide()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Player View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // We provide custom pro UI!
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (aspectRatioMode) {
                    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatioMode.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom MX Player / VLC Gesture Touch Interceptor
        AndroidView(
            factory = { ctx ->
                View(ctx).apply {
                    var startX = 0f
                    var startY = 0f
                    var isHorizontalSeek = false
                    var isVerticalSwipe = false
                    var initialSeekPos = 0L

                    val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            if (isScreenLocked) {
                                isControlsVisible = !isControlsVisible
                            } else {
                                isControlsVisible = !isControlsVisible
                                if (isControlsVisible) scheduleControlsAutoHide()
                            }
                            return true
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            if (isScreenLocked) return false
                            val screenWidth = width
                            val tapX = e.x

                            if (tapX < screenWidth * 0.35f) {
                                // Double tap left: -10s
                                exoPlayer?.let {
                                    val target = (it.currentPosition - 10000).coerceAtLeast(0L)
                                    it.seekTo(target)
                                    showDoubleTapRewind = true
                                    coroutineScope.launch {
                                        delay(800)
                                        showDoubleTapRewind = false
                                    }
                                }
                            } else if (tapX > screenWidth * 0.65f) {
                                // Double tap right: +10s
                                exoPlayer?.let {
                                    val target = (it.currentPosition + 10000).coerceAtMost(duration)
                                    it.seekTo(target)
                                    showDoubleTapForward = true
                                    coroutineScope.launch {
                                        delay(800)
                                        showDoubleTapForward = false
                                    }
                                }
                            } else {
                                // Double tap center: Play/Pause
                                exoPlayer?.let {
                                    if (it.isPlaying) it.pause() else it.play()
                                }
                            }
                            return true
                        }
                    })

                    setOnTouchListener { _, event ->
                        gestureDetector.onTouchEvent(event)

                        if (isScreenLocked) return@setOnTouchListener true

                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                startX = event.x
                                startY = event.y
                                isHorizontalSeek = false
                                isVerticalSwipe = false
                                initialSeekPos = exoPlayer?.currentPosition ?: 0L
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val dx = event.x - startX
                                val dy = event.y - startY
                                val absDx = abs(dx)
                                val absDy = abs(dy)

                                if (!isHorizontalSeek && !isVerticalSwipe) {
                                    if (absDx > 50 && absDx > absDy) {
                                        isHorizontalSeek = true
                                    } else if (absDy > 50 && absDy > absDx) {
                                        isVerticalSwipe = true
                                    }
                                }

                                if (isHorizontalSeek) {
                                    // Seek Gesture
                                    val seekDeltaMs = ((dx / width) * 90000L).toLong() // +/- 90 seconds max per swipe
                                    val targetPos = (initialSeekPos + seekDeltaMs).coerceIn(0L, duration)
                                    val deltaSign = if (seekDeltaMs >= 0) "+ " else "- "

                                    gestureHudType = "seek"
                                    gestureSeekDeltaText = "$deltaSign${FormatUtils.formatDuration(abs(seekDeltaMs))}"
                                    gestureSeekTimeText = "${FormatUtils.formatDuration(targetPos)} / ${FormatUtils.formatDuration(duration)}"
                                } else if (isVerticalSwipe) {
                                    val deltaYFactor = -dy / height
                                    if (startX < width * 0.5f) {
                                        // Left half: Brightness
                                        gestureHudType = "brightness"
                                        onBrightnessChange(deltaYFactor * 0.05f)
                                        val curBrightness = (ctx as? Activity)?.window?.attributes?.screenBrightness ?: 0.5f
                                        gestureHudValue = if (curBrightness < 0) 0.5f else curBrightness
                                    } else {
                                        // Right half: Volume
                                        gestureHudType = "volume"
                                        val volChange = if (dy < 0) 1 else -1
                                        if (absDy > 70) {
                                            onVolumeChange(volChange)
                                            startY = event.y // reset step
                                        }
                                        val curVol = getCurrentVolume().toFloat() / getMaxVolume().toFloat()
                                        gestureHudValue = curVol
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                if (isHorizontalSeek) {
                                    val dx = event.x - startX
                                    val seekDeltaMs = ((dx / width) * 90000L).toLong()
                                    val targetPos = (initialSeekPos + seekDeltaMs).coerceIn(0L, duration)
                                    exoPlayer?.seekTo(targetPos)
                                }
                                gestureHudType = null
                                isHorizontalSeek = false
                                isVerticalSwipe = false
                            }
                        }
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Gesture HUD Overlays (Brightness, Volume, Seek)
        when (gestureHudType) {
            "volume" -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xCC131520))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Volume",
                            tint = NeonCyan,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { gestureHudValue.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(140.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonCyan,
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(gestureHudValue * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            "brightness" -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xCC131520))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = "Brightness",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { gestureHudValue.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(140.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFFFD54F),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(gestureHudValue * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            "seek" -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xCC131520))
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = gestureSeekDeltaText,
                            color = NeonCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = gestureSeekTimeText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Double Tap Feedback Indicators
        if (showDoubleTapRewind) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 48.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Rewind 10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("-10s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showDoubleTapForward) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Forward 10s",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Text("+10s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Screen Lock Indicator (When locked)
        if (isScreenLocked) {
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = {
                        isScreenLocked = false
                        isControlsVisible = true
                        scheduleControlsAutoHide()
                        Toast.makeText(context, "Screen Unlocked", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC131520))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Normal Player Controls Overlay (Top bar, Center, Bottom bar)
        if (!isScreenLocked) {
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xB3090A0F))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        text = videoTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Aspect Ratio Button
                    IconButton(onClick = {
                        aspectRatioMode = when (aspectRatioMode) {
                            AspectRatioMode.FIT -> AspectRatioMode.FILL
                            AspectRatioMode.FILL -> AspectRatioMode.ZOOM
                            AspectRatioMode.ZOOM -> AspectRatioMode.RATIO_16_9
                            AspectRatioMode.RATIO_16_9 -> AspectRatioMode.RATIO_4_3
                            AspectRatioMode.RATIO_4_3 -> AspectRatioMode.FIT
                        }
                        Toast.makeText(context, aspectRatioMode.displayName, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = "Aspect Ratio",
                            tint = Color.White
                        )
                    }

                    // Audio Track Selector
                    IconButton(onClick = { showAudioTrackDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Audio Tracks",
                            tint = Color.White
                        )
                    }

                    // Subtitles Selector
                    IconButton(onClick = { showSubtitleDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles",
                            tint = Color.White
                        )
                    }

                    // Speed Selector
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Speed",
                            tint = Color.White
                        )
                    }

                    // PiP Button
                    IconButton(onClick = onEnterPip) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Picture in Picture",
                            tint = Color.White
                        )
                    }

                    // Video Info Dialog
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Video Info",
                            tint = Color.White
                        )
                    }
                }
            }

            // Center Play / Pause Big Button
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            exoPlayer?.let { it.seekTo((it.currentPosition - 10000).coerceAtLeast(0L)) }
                            scheduleControlsAutoHide()
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    IconButton(
                        onClick = {
                            exoPlayer?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                            scheduleControlsAutoHide()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = PlayerBlack,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    IconButton(
                        onClick = {
                            exoPlayer?.let { it.seekTo((it.currentPosition + 10000).coerceAtMost(duration)) }
                            scheduleControlsAutoHide()
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Bottom Bar (Seekbar, Times, Lock, Rotate)
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xB3090A0F))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Time and Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = FormatUtils.formatDuration(currentPosition),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Slider(
                            value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                            onValueChange = { factor ->
                                val target = (factor * duration).toLong()
                                exoPlayer?.seekTo(target)
                                scheduleControlsAutoHide()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            )
                        )

                        Text(
                            text = FormatUtils.formatDuration(duration),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Bottom action buttons: Screen Lock, Orientation toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            isScreenLocked = true
                            Toast.makeText(context, "Screen Locked", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock Controls",
                                tint = Color.White
                            )
                        }

                        // Orientation toggle
                        IconButton(onClick = {
                            val activity = context as? Activity ?: return@IconButton
                            val currentOrientation = activity.resources.configuration.orientation
                            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate Screen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Speed Dialog
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
            text = {
                val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playbackSpeed = speed
                                    exoPlayer?.playbackParameters = PlaybackParameters(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (playbackSpeed == speed) NeonCyan else Color.White,
                                fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                            if (playbackSpeed == speed) {
                                Text("✓", color = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }

    // Video Info Dialog
    if (showInfoDialog) {
        val format = exoPlayer?.videoFormat
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Video Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Title: $videoTitle", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Resolution: ${format?.width ?: 0} x ${format?.height ?: 0}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Frame Rate: ${format?.frameRate ?: 0f} fps")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Duration: ${FormatUtils.formatDuration(duration)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    if (videoPath.isNotBlank()) {
                        val file = File(videoPath)
                        if (file.exists()) {
                            Text("File Size: ${FormatUtils.formatFileSize(file.length())}")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Path: $videoPath", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("OK", color = NeonCyan)
                }
            }
        )
    }

    // Audio Track Selection Dialog
    if (showAudioTrackDialog) {
        val currentTracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
        val audioTrackGroups = currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }

        AlertDialog(
            onDismissRequest = { showAudioTrackDialog = false },
            title = { Text("Audio Track", fontWeight = FontWeight.Bold) },
            text = {
                if (audioTrackGroups.isEmpty()) {
                    Text("No alternate audio tracks found.")
                } else {
                    Column {
                        audioTrackGroups.forEachIndexed { groupIdx, group ->
                            for (trackIdx in 0 until group.length) {
                                val trackFormat = group.getTrackFormat(trackIdx)
                                val isSelected = group.isTrackSelected(trackIdx)
                                val label = trackFormat.label
                                    ?: trackFormat.language
                                    ?: "Audio Track ${groupIdx + 1}"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                ?.buildUpon()
                                                ?.setPreferredAudioLanguage(trackFormat.language)
                                                ?.build() ?: TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT
                                            showAudioTrackDialog = false
                                        }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) NeonCyan else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) Text("✓", color = NeonCyan)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioTrackDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }

    // Subtitle Selection Dialog
    if (showSubtitleDialog) {
        val currentTracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
        val textTrackGroups = currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Subtitles", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Disable Subtitles option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                    ?.buildUpon()
                                    ?.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                                    ?.build() ?: TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT
                                showSubtitleDialog = false
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Text("Off / Disabled", color = Color.White)
                    }

                    if (textTrackGroups.isEmpty()) {
                        Text("No embedded subtitles found.", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        textTrackGroups.forEachIndexed { groupIdx, group ->
                            for (trackIdx in 0 until group.length) {
                                val format = group.getTrackFormat(trackIdx)
                                val isSelected = group.isTrackSelected(trackIdx)
                                val label = format.label ?: format.language ?: "Subtitle ${groupIdx + 1}"

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                ?.buildUpon()
                                                ?.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                                ?.setPreferredTextLanguage(format.language)
                                                ?.build() ?: TrackSelectionParameters.DEFAULT_WITHOUT_CONTEXT
                                            showSubtitleDialog = false
                                        }
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) NeonCyan else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) Text("✓", color = NeonCyan)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }
}
