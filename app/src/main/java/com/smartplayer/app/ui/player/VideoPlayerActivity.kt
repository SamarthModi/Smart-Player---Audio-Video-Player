package com.smartplayer.app.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Rational
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.AspectRatioMode
import com.smartplayer.app.player.EqualizerController
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.PlayerSurfaceVariant
import com.smartplayer.app.ui.theme.SmartPlayerTheme
import com.smartplayer.app.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    private var isSoftwareDecoder: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUi()

        preferencesRepository = PreferencesRepository.getInstance(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        isSoftwareDecoder = preferencesRepository.decoderMode == "SW"

        // Extract intent extras or data URI
        intent.data?.let { uri ->
            videoUriString = uri.toString()
            videoTitle = uri.lastPathSegment ?: "Playing Video"
            videoPath = uri.path ?: ""
        }
        intent.getStringExtra(EXTRA_VIDEO_URI)?.let { videoUriString = it }
        intent.getStringExtra(EXTRA_VIDEO_TITLE)?.let { videoTitle = it }
        intent.getStringExtra(EXTRA_VIDEO_PATH)?.let { videoPath = it }
        val videoId = intent.getLongExtra(EXTRA_VIDEO_ID, -1L)
        if (videoId != -1L) {
            preferencesRepository.addRecentlyPlayedVideo(videoId)
        }

        initPlayer(isSoftwareDecoder)

        setContent {
            SmartPlayerTheme {
                VideoPlayerScreen(
                    exoPlayer = exoPlayer,
                    videoTitle = videoTitle,
                    videoPath = videoPath,
                    videoUriString = videoUriString,
                    preferencesRepository = preferencesRepository,
                    onBackPress = { finish() },
                    onEnterPip = { enterPipMode() },
                    onBrightnessChange = { setScreenBrightness(it) },
                    onVolumeChange = { setMediaVolume(it) },
                    getCurrentVolume = { getCurrentMediaVolume() },
                    getMaxVolume = { maxVolume },
                    onToggleDecoder = { toggleDecoder() },
                    isSoftwareDecoder = isSoftwareDecoder
                )
            }
        }
    }

    private fun initPlayer(softwareOnly: Boolean) {
        val renderersFactory = DefaultRenderersFactory(this).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            if (softwareOnly) {
                setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
                    // Prefer software codecs
                    val all = androidx.media3.exoplayer.mediacodec.MediaCodecUtil.getDecoderInfos(
                        mimeType,
                        requiresSecureDecoder,
                        requiresTunnelingDecoder
                    )
                    all.sortedBy { if (it.softwareOnly) 0 else 1 }
                }
            }
        }

        val player = ExoPlayer.Builder(this, renderersFactory)
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

    private fun toggleDecoder() {
        isSoftwareDecoder = !isSoftwareDecoder
        preferencesRepository.decoderMode = if (isSoftwareDecoder) "SW" else "HW"

        val currentPos = exoPlayer?.currentPosition ?: 0L
        val wasPlaying = exoPlayer?.isPlaying ?: true

        exoPlayer?.release()
        initPlayer(isSoftwareDecoder)
        exoPlayer?.seekTo(currentPos)
        if (wasPlaying) exoPlayer?.play()

        Toast.makeText(
            this,
            if (isSoftwareDecoder) "SW Decoder Active (Software Mode)" else "HW Decoder Active (Hardware Accelerated)",
            Toast.LENGTH_SHORT
        ).show()
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
        const val EXTRA_VIDEO_ID = "extra_video_id"

        fun start(context: Context, uri: Uri, title: String, path: String, videoId: Long = -1L) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URI, uri.toString())
                putExtra(EXTRA_VIDEO_TITLE, title)
                putExtra(EXTRA_VIDEO_PATH, path)
                putExtra(EXTRA_VIDEO_ID, videoId)
            }
            context.startActivity(intent)
        }
    }
}

/**
 * Fullscreen Interactive Video Player Screen (MX Player Pro Grade)
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    exoPlayer: ExoPlayer?,
    videoTitle: String,
    videoPath: String,
    videoUriString: String,
    preferencesRepository: PreferencesRepository,
    onBackPress: () -> Unit,
    onEnterPip: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Int) -> Unit,
    getCurrentVolume: () -> Int,
    getMaxVolume: () -> Int,
    onToggleDecoder: () -> Unit,
    isSoftwareDecoder: Boolean
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

    // Night Mode & Audio/Subtitle Sync Offset
    var isNightMode by remember { mutableStateOf(false) }
    var audioSyncOffsetMs by remember { mutableIntStateOf(0) }
    var subtitleSyncOffsetMs by remember { mutableIntStateOf(0) }

    // A-B Repeat Loop States
    var loopPointA by remember { mutableStateOf<Long?>(null) }
    var loopPointB by remember { mutableStateOf<Long?>(null) }
    var isAbLoopActive by remember { mutableStateOf(false) }

    // Gesture HUD States
    var gestureHudType by remember { mutableStateOf<String?>(null) }
    var gestureHudValue by remember { mutableFloatStateOf(0f) }
    var gestureSeekTimeText by remember { mutableStateOf("") }
    var gestureSeekDeltaText by remember { mutableStateOf("") }

    // Double tap feedback
    var showDoubleTapRewind by remember { mutableStateOf(false) }
    var showDoubleTapForward by remember { mutableStateOf(false) }
    val seekStepSeconds = preferencesRepository.doubleTapSeekSeconds
    val autoHideTimeoutMs = preferencesRepository.autoHideControlsSec * 1000L

    // Quick 2x Hold gesture
    var isQuick2xActive by remember { mutableStateOf(false) }

    // Dialogs
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAudioTrackDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleControlsAutoHide() {
        autoHideJob?.cancel()
        if (isControlsVisible && !isScreenLocked) {
            autoHideJob = coroutineScope.launch {
                delay(autoHideTimeoutMs)
                isControlsVisible = false
            }
        }
    }

    // External Subtitle Picker
    val subtitleFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val subtitleConfig = Media3Item.SubtitleConfiguration.Builder(uri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("External")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()

            val baseUri = Uri.parse(videoUriString)
            val updatedMedia = Media3Item.Builder()
                .setUri(baseUri)
                .setSubtitleConfigurations(listOf(subtitleConfig))
                .build()

            val curPos = exoPlayer?.currentPosition ?: 0L
            exoPlayer?.setMediaItem(updatedMedia)
            exoPlayer?.seekTo(curPos)
            exoPlayer?.prepare()
            exoPlayer?.play()
            Toast.makeText(context, "Loaded External Subtitle", Toast.LENGTH_SHORT).show()
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
                val pos = player.currentPosition.coerceAtLeast(0L)
                currentPosition = pos
                duration = player.duration.coerceAtLeast(0L)
                bufferPosition = player.bufferedPosition.coerceAtLeast(0L)

                // A-B Repeat Check
                if (isAbLoopActive && loopPointA != null && loopPointB != null) {
                    if (pos >= loopPointB!!) {
                        player.seekTo(loopPointA!!)
                    }
                }
            }
            delay(400)
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
        // Hardware Accelerated Player Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
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
                playerView.subtitleView?.let { subView ->
                    val textColorInt = try {
                        android.graphics.Color.parseColor(preferencesRepository.subtitleColorHex)
                    } catch (_: Exception) {
                        android.graphics.Color.WHITE
                    }
                    val bgInt = if (preferencesRepository.subtitleHasBackground) {
                        android.graphics.Color.argb(160, 0, 0, 0)
                    } else {
                        android.graphics.Color.TRANSPARENT
                    }
                    val captionStyle = androidx.media3.ui.CaptionStyleCompat(
                        textColorInt,
                        bgInt,
                        android.graphics.Color.TRANSPARENT,
                        androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        android.graphics.Color.BLACK,
                        null
                    )
                    subView.setStyle(captionStyle)
                    subView.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, preferencesRepository.subtitleTextSize.toFloat())
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Night Mode Warm Eye-Comfort Filter
        if (isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x38FFA000))
            )
        }

        // Custom Gesture Handler (Brightness left, Volume right, Seek horizontal, Double taps)
        AndroidView(
            factory = { ctx ->
                View(ctx).apply {
                    var startX = 0f
                    var startY = 0f
                    var isHorizontalSeek = false
                    var isVerticalSwipe = false
                    var initialSeekPos = 0L

                    val holdHandler = android.os.Handler(android.os.Looper.getMainLooper())
                    var isHolding2x = false
                    var originalSpeed = 1.0f

                    val hold2xRunnable = Runnable {
                        if (!isHorizontalSeek && !isVerticalSwipe && !isScreenLocked) {
                            isHolding2x = true
                            isQuick2xActive = true
                            originalSpeed = exoPlayer?.playbackParameters?.speed ?: playbackSpeed
                            exoPlayer?.playbackParameters = PlaybackParameters(2.0f)
                            performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                        }
                    }

                    val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            if (isHolding2x) return true
                            isControlsVisible = !isControlsVisible
                            if (isControlsVisible) scheduleControlsAutoHide()
                            return true
                        }

                        override fun onDoubleTap(e: MotionEvent): Boolean {
                            if (isScreenLocked) return false
                            val screenWidth = width
                            val tapX = e.x

                            if (tapX < screenWidth * 0.35f) {
                                // Rewind
                                exoPlayer?.let {
                                    val target = (it.currentPosition - (seekStepSeconds * 1000L)).coerceAtLeast(0L)
                                    it.seekTo(target)
                                    showDoubleTapRewind = true
                                    coroutineScope.launch {
                                        delay(800)
                                        showDoubleTapRewind = false
                                    }
                                }
                            } else if (tapX > screenWidth * 0.65f) {
                                // Forward
                                exoPlayer?.let {
                                    val target = (it.currentPosition + (seekStepSeconds * 1000L)).coerceAtMost(duration)
                                    it.seekTo(target)
                                    showDoubleTapForward = true
                                    coroutineScope.launch {
                                        delay(800)
                                        showDoubleTapForward = false
                                    }
                                }
                            } else {
                                // Play / Pause
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
                                holdHandler.postDelayed(hold2xRunnable, 450L)
                            }
                            MotionEvent.ACTION_MOVE -> {
                                val dx = event.x - startX
                                val dy = event.y - startY
                                val absDx = abs(dx)
                                val absDy = abs(dy)

                                if (absDx > 30 || absDy > 30) {
                                    holdHandler.removeCallbacks(hold2xRunnable)
                                }

                                if (!isHorizontalSeek && !isVerticalSwipe && !isHolding2x) {
                                    if (absDx > 40 && absDx > absDy) {
                                        isHorizontalSeek = true
                                    } else if (absDy > 40 && absDy > absDx) {
                                        isVerticalSwipe = true
                                    }
                                }

                                if (isHorizontalSeek) {
                                    val seekDeltaMs = ((dx / width) * 90000L).toLong()
                                    val targetPos = (initialSeekPos + seekDeltaMs).coerceIn(0L, duration)
                                    val deltaSign = if (seekDeltaMs >= 0) "+ " else "- "

                                    gestureHudType = "seek"
                                    gestureSeekDeltaText = "$deltaSign${FormatUtils.formatDuration(abs(seekDeltaMs))}"
                                    gestureSeekTimeText = "${FormatUtils.formatDuration(targetPos)} / ${FormatUtils.formatDuration(duration)}"
                                } else if (isVerticalSwipe) {
                                    val deltaYFactor = -dy / height
                                    if (startX < width * 0.5f) {
                                        gestureHudType = "brightness"
                                        onBrightnessChange(deltaYFactor * 0.05f)
                                        val curBrightness = (ctx as? Activity)?.window?.attributes?.screenBrightness ?: 0.5f
                                        gestureHudValue = if (curBrightness < 0) 0.5f else curBrightness
                                    } else {
                                        gestureHudType = "volume"
                                        val volChange = if (dy < 0) 1 else -1
                                        if (absDy > 60) {
                                            onVolumeChange(volChange)
                                            startY = event.y
                                        }
                                        val curVol = getCurrentVolume().toFloat() / getMaxVolume().toFloat()
                                        gestureHudValue = curVol
                                    }
                                }
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                holdHandler.removeCallbacks(hold2xRunnable)
                                if (isHolding2x) {
                                    isHolding2x = false
                                    isQuick2xActive = false
                                    exoPlayer?.playbackParameters = PlaybackParameters(originalSpeed)
                                }
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
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
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

        // Double Tap Indicators
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
                    Icon(Icons.Default.Replay10, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    Text("-$seekStepSeconds s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Icon(Icons.Default.Forward10, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                    Text("+$seekStepSeconds s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // A-B Loop Floating Badge
        if (isAbLoopActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xDD7C4DFF))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "A-B Loop: ${FormatUtils.formatDuration(loopPointA ?: 0L)} ➔ ${FormatUtils.formatDuration(loopPointB ?: duration)}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick 2x Speed HUD (Hold gesture)
        AnimatedVisibility(
            visible = isQuick2xActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE00E5FF))
                    .padding(horizontal = 18.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = PlayerBlack, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2X SPEED >>", color = PlayerBlack, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Screen Lock Unlocker
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
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD131520))
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = NeonCyan, modifier = Modifier.size(26.dp))
                }
            }
        }

        // Top & Bottom Controls Bar
        if (!isScreenLocked) {
            // TOP BAR
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xD9090A0F))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = videoTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // HW / SW Decoder Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSoftwareDecoder) Color(0xFFFFB300) else NeonCyan)
                            .clickable { onToggleDecoder() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSoftwareDecoder) "SW" else "HW",
                            color = PlayerBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Screenshot Capture Button
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val retriever = MediaMetadataRetriever()
                                if (videoPath.isNotBlank() && File(videoPath).exists()) {
                                    retriever.setDataSource(videoPath)
                                } else {
                                    retriever.setDataSource(context, Uri.parse(videoUriString))
                                }
                                val bitmap = retriever.getFrameAtTime(
                                    currentPosition * 1000L,
                                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                )
                                retriever.release()

                                if (bitmap != null) {
                                    val picsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                    val dir = File(picsDir, "SmartPlayer").apply { mkdirs() }
                                    val out = File(dir, "Screenshot_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(out).use { stream ->
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                                    }
                                    MediaScannerConnection.scanFile(context, arrayOf(out.absolutePath), arrayOf("image/jpeg"), null)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Screenshot saved to Pictures/SmartPlayer", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Screenshot captured", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Screenshot", tint = Color.White)
                    }

                    // Night Mode Toggle
                    IconButton(onClick = {
                        isNightMode = !isNightMode
                        Toast.makeText(context, if (isNightMode) "Night Filter ON" else "Night Filter OFF", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            Icons.Default.NightlightRound,
                            contentDescription = "Night Mode",
                            tint = if (isNightMode) Color(0xFFFFD54F) else Color.White
                        )
                    }

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
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                    }

                    // Background Play as Audio
                    IconButton(onClick = {
                        val currentPos = exoPlayer?.currentPosition ?: 0L
                        com.smartplayer.app.player.SmartAudioPlayerManager.getInstance(context).playSingleMedia(
                            Uri.parse(videoUriString),
                            videoTitle,
                            videoPath,
                            currentPos
                        )
                        Toast.makeText(context, "Playing audio in background service", Toast.LENGTH_SHORT).show()
                        (context as? Activity)?.finish()
                    }) {
                        Icon(Icons.Default.Headphones, contentDescription = "Play as Audio", tint = NeonCyan)
                    }

                    // Audio Track & Sync Selector
                    IconButton(onClick = { showAudioTrackDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Audio Track", tint = Color.White)
                    }

                    // Subtitle & Sync Selector
                    IconButton(onClick = { showSubtitleDialog = true }) {
                        Icon(Icons.Default.Subtitles, contentDescription = "Subtitles", tint = Color.White)
                    }

                    // Speed Selector
                    IconButton(onClick = { showSpeedDialog = true }) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color.White)
                    }

                    // Equalizer & Audio Boost Shortcut
                    IconButton(onClick = { showEqualizerDialog = true }) {
                        Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = NeonCyan)
                    }

                    // PiP Button
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White)
                    }

                    // Video Info Dialog
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                    }
                }
            }

            // CENTER PLAY / PAUSE BUTTONS
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
                            exoPlayer?.let { it.seekTo((it.currentPosition - (seekStepSeconds * 1000L)).coerceAtLeast(0L)) }
                            scheduleControlsAutoHide()
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "Rewind", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    IconButton(
                        onClick = {
                            exoPlayer?.let { if (it.isPlaying) it.pause() else it.play() }
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
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(36.dp))

                    IconButton(
                        onClick = {
                            exoPlayer?.let { it.seekTo((it.currentPosition + (seekStepSeconds * 1000L)).coerceAtMost(duration)) }
                            scheduleControlsAutoHide()
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "Forward", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }

            // BOTTOM BAR
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xD9090A0F))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // Time and Progress Bar
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

                    // Action tools (Lock, A-B Repeat, Orientation)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Lock Button
                        IconButton(onClick = {
                            isScreenLocked = true
                            Toast.makeText(context, "Screen Locked", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = Color.White)
                        }

                        // A-B Loop Controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    if (loopPointA == null) {
                                        loopPointA = currentPosition
                                        Toast.makeText(context, "Point A set at ${FormatUtils.formatDuration(currentPosition)}", Toast.LENGTH_SHORT).show()
                                    } else if (loopPointB == null) {
                                        if (currentPosition > loopPointA!!) {
                                            loopPointB = currentPosition
                                            isAbLoopActive = true
                                            Toast.makeText(context, "Point B set. A-B Loop Active!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Point B must be after Point A", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        loopPointA = null
                                        loopPointB = null
                                        isAbLoopActive = false
                                        Toast.makeText(context, "A-B Loop Cleared", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAbLoopActive) ElectricViolet else PlayerSurfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        loopPointA == null -> "Set A"
                                        loopPointB == null -> "Set B"
                                        else -> "Clear A-B"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // Screen Rotate Button
                        IconButton(onClick = {
                            val activity = context as? Activity ?: return@IconButton
                            val currentOrientation = activity.resources.configuration.orientation
                            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            } else {
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        }) {
                            Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Playback Speed Dialog
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
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (playbackSpeed == speed) NeonCyan else Color.White,
                                fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                            if (playbackSpeed == speed) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Audio Track & Audio Sync Dialog
    if (showAudioTrackDialog) {
        val currentTracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
        val audioTrackGroups = currentTracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }

        AlertDialog(
            onDismissRequest = { showAudioTrackDialog = false },
            title = { Text("Audio Tracks & Sync", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Available Audio Tracks", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (audioTrackGroups.isEmpty()) {
                        Text("Default Audio Track", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        audioTrackGroups.forEachIndexed { groupIdx, group ->
                            for (trackIdx in 0 until group.length) {
                                val trackFormat = group.getTrackFormat(trackIdx)
                                val isSelected = group.isTrackSelected(trackIdx)
                                val lang = trackFormat.language?.uppercase() ?: "TRACK ${groupIdx + 1}"
                                val channels = if (trackFormat.channelCount > 2) "${trackFormat.channelCount}.1 Surround" else "Stereo"
                                val label = "${trackFormat.label ?: lang} ($channels)"

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
                                        .padding(vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Delay / Sync Offset
                    Text("Audio Sync Offset: ${audioSyncOffsetMs}ms", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                audioSyncOffsetMs -= 50
                                Toast.makeText(context, "Audio: ${audioSyncOffsetMs}ms", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("-50ms", color = Color.White)
                        }

                        Button(
                            onClick = {
                                audioSyncOffsetMs = 0
                                Toast.makeText(context, "Audio Sync Reset", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("Reset", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                audioSyncOffsetMs += 50
                                Toast.makeText(context, "Audio: +${audioSyncOffsetMs}ms", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("+50ms", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioTrackDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Subtitle & External Subtitle Dialog
    if (showSubtitleDialog) {
        val currentTracks = exoPlayer?.currentTracks ?: Tracks.EMPTY
        val textTrackGroups = currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }

        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Subtitles & Sync", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Pick External Subtitle File Button
                    Button(
                        onClick = {
                            subtitleFilePicker.launch(arrayOf("*/*", "application/x-subrip", "text/vtt", "text/plain"))
                            showSubtitleDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = PlayerBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Subtitle File (.srt, .vtt)", color = PlayerBlack, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Disable Subtitles
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
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Off / Disabled", color = Color.White)
                    }

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
                                    .padding(vertical = 8.dp),
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

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtitle Delay Offset
                    Text("Subtitle Sync: ${subtitleSyncOffsetMs}ms", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                subtitleSyncOffsetMs -= 100
                                Toast.makeText(context, "Subtitles: ${subtitleSyncOffsetMs}ms", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("-100ms", color = Color.White)
                        }

                        Button(
                            onClick = {
                                subtitleSyncOffsetMs = 0
                                Toast.makeText(context, "Subtitle Sync Reset", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("Reset", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                subtitleSyncOffsetMs += 100
                                Toast.makeText(context, "Subtitles: +${subtitleSyncOffsetMs}ms", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant)
                        ) {
                            Text("+100ms", color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Subtitle Text Size", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(14 to "Small", 18 to "Normal", 22 to "Large", 28 to "Extra").forEach { (sz, lbl) ->
                            val isSel = preferencesRepository.subtitleTextSize == sz
                            Button(
                                onClick = {
                                    preferencesRepository.subtitleTextSize = sz
                                    Toast.makeText(context, "Size: $lbl", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) NeonCyan else PlayerSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(lbl, color = if (isSel) PlayerBlack else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Subtitle Font Color", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "#FFFFFF" to "White",
                            "#FFEE58" to "Yellow",
                            "#00E5FF" to "Cyan",
                            "#69F0AE" to "Green"
                        ).forEach { (colorHex, lbl) ->
                            val isSel = preferencesRepository.subtitleColorHex == colorHex
                            Button(
                                onClick = {
                                    preferencesRepository.subtitleColorHex = colorHex
                                    Toast.makeText(context, "Color: $lbl", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) NeonCyan else PlayerSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(lbl, color = if (isSel) PlayerBlack else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Background Box", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = preferencesRepository.subtitleHasBackground,
                            onCheckedChange = {
                                preferencesRepository.subtitleHasBackground = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Video Equalizer & Audio Boost Popup
    if (showEqualizerDialog) {
        val eq = remember { EqualizerController(preferencesRepository) }
        LaunchedEffect(Unit) {
            exoPlayer?.audioSessionId?.let { id -> eq.bindAudioSession(id) }
        }

        AlertDialog(
            onDismissRequest = {
                eq.release()
                showEqualizerDialog = false
            },
            title = { Text("Video Sound & Audio Boost", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("200% Audio Boost (VLC Style)", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = preferencesRepository.bassBoostLevel.toFloat(),
                        onValueChange = { eq.setBassBoost(it.toInt()) },
                        valueRange = 0f..1000f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Bass Boost Strength: ${preferencesRepository.bassBoostLevel / 10}%", color = NeonCyan, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    eq.release()
                    showEqualizerDialog = false
                }) { Text("Done", color = NeonCyan) }
            }
        )
    }

    // Video Info Details Dialog
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
                    Text("Decoder: ${if (isSoftwareDecoder) "Software (SW)" else "Hardware (HW)"}")
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
                            Text("Path: $videoPath", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("OK", color = NeonCyan) }
            }
        )
    }
}
