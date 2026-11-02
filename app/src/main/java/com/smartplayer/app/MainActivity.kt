package com.smartplayer.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.smartplayer.app.data.MediaStoreRepository
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.AudioItem
import com.smartplayer.app.model.MediaFolder
import com.smartplayer.app.model.VideoItem
import com.smartplayer.app.player.SmartAudioPlayerManager
import com.smartplayer.app.service.SmartAudioService
import com.smartplayer.app.ui.components.MiniPlayer
import com.smartplayer.app.ui.screens.EqualizerScreen
import com.smartplayer.app.ui.screens.MusicScreen
import com.smartplayer.app.ui.screens.NowPlayingScreen
import com.smartplayer.app.ui.screens.PlaylistsScreen
import com.smartplayer.app.ui.screens.SettingsScreen
import com.smartplayer.app.ui.screens.VideoScreen
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.SmartPlayerTheme
import com.smartplayer.app.util.LocaleHelper
import kotlinx.coroutines.launch
import android.content.Context

class MainActivity : ComponentActivity() {

    companion object {
        var currentTab: NavigationTab = NavigationTab.VIDEOS
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferencesRepository.getInstance(newBase)
        val localizedContext = LocaleHelper.updateContextLocale(newBase, prefs.appLanguage)
        super.attachBaseContext(localizedContext)
    }

    private lateinit var mediaStoreRepository: MediaStoreRepository
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var playerManager: SmartAudioPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaStoreRepository = MediaStoreRepository.getInstance(this)
        preferencesRepository = PreferencesRepository.getInstance(this)
        playerManager = SmartAudioPlayerManager.getInstance(this)

        // Apply saved language
        LocaleHelper.applyLanguage(preferencesRepository.appLanguage)

        // Start Foreground Playback Service
        val serviceIntent = Intent(this, SmartAudioService::class.java)
        try {
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            val themeStyle by preferencesRepository.themeStyleFlow.collectAsState()
            SmartPlayerTheme(themeStyle = themeStyle) {
                MainAppScreen(
                    mediaStoreRepository = mediaStoreRepository,
                    preferencesRepository = preferencesRepository,
                    playerManager = playerManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentSong = playerManager.playbackState.value.currentSong
        if (currentSong != null && currentSong.artist == "Smart Player (Video Audio)" && playerManager.playbackState.value.isPlaying) {
            com.smartplayer.app.ui.player.VideoPlayerActivity.start(
                this,
                android.net.Uri.parse(currentSong.uriString),
                currentSong.title,
                currentSong.path,
                currentSong.id
            )
        }
    }
}

enum class NavigationTab(val titleRes: Int, val icon: ImageVector) {
    VIDEOS(R.string.tab_videos, Icons.Default.Videocam),
    MUSIC(R.string.tab_music, Icons.Default.MusicNote),
    PLAYLISTS(R.string.tab_playlists, Icons.Default.QueueMusic),
    SETTINGS(R.string.tab_settings, Icons.Default.Settings)
}

@Composable
fun MainAppScreen(
    mediaStoreRepository: MediaStoreRepository,
    preferencesRepository: PreferencesRepository,
    playerManager: SmartAudioPlayerManager
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by rememberSaveable(
        stateSaver = Saver(
            save = { it.name },
            restore = {
                try {
                    NavigationTab.valueOf(it)
                } catch (_: Exception) {
                    MainActivity.currentTab
                }
            }
        )
    ) { mutableStateOf(MainActivity.currentTab) }
    var isNowPlayingOpen by remember { mutableStateOf(false) }
    var isEqualizerOpen by remember { mutableStateOf(false) }

    // Media Data State
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var videoFolders by remember { mutableStateOf<List<MediaFolder>>(emptyList()) }
    var audios by remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    var audioFolders by remember { mutableStateOf<List<MediaFolder>>(emptyList()) }

    var hasPermission by remember { mutableStateOf(false) }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun reloadMedia() {
        coroutineScope.launch {
            val vids = mediaStoreRepository.getAllVideos()
            videos = vids
            videoFolders = mediaStoreRepository.groupVideosByFolders(vids)

            val auds = mediaStoreRepository.getAllAudios()
            audios = auds
            audioFolders = mediaStoreRepository.groupAudiosByFolders(auds)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results.values.any { it }
        hasPermission = granted
        if (granted) {
            reloadMedia()
        }
    }

    LaunchedEffect(Unit) {
        if (checkPermissions()) {
            hasPermission = true
            reloadMedia()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    val playbackState by playerManager.playbackState.collectAsState()

    Scaffold(
        containerColor = PlayerBlack,
        bottomBar = {
            Column {
                // Floating MiniPlayer (appears when audio or background video is playing)
                MiniPlayer(
                    currentSong = playbackState.currentSong,
                    isPlaying = playbackState.isPlaying,
                    currentPosition = playbackState.currentPosition,
                    duration = playbackState.duration,
                    onPlayPauseClick = { playerManager.playPause() },
                    onSkipNextClick = { playerManager.skipToNext() },
                    onPlayerClick = { isNowPlayingOpen = true }
                )

                // Bottom Navigation
                NavigationBar(
                    containerColor = PlayerSurface,
                    contentColor = NeonCyan
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                MainActivity.currentTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.titleRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(tab.titleRes),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonCyan,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan.copy(alpha = 0.15f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasPermission) {
                // Permission Explanation Screen
                PermissionRequestScreen(
                    onRequestPermission = { permissionLauncher.launch(requiredPermissions) }
                )
            } else {
                when (selectedTab) {
                    NavigationTab.VIDEOS -> {
                        VideoScreen(
                            videos = videos,
                            folders = videoFolders,
                            preferencesRepository = preferencesRepository
                        )
                    }
                    NavigationTab.MUSIC -> {
                        MusicScreen(
                            audios = audios,
                            audioFolders = audioFolders,
                            playerManager = playerManager,
                            preferencesRepository = preferencesRepository
                        )
                    }
                    NavigationTab.PLAYLISTS -> {
                        PlaylistsScreen(
                            allAudios = audios,
                            allVideos = videos,
                            playerManager = playerManager,
                            preferencesRepository = preferencesRepository
                        )
                    }
                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            preferencesRepository = preferencesRepository,
                            onOpenEqualizer = { isEqualizerOpen = true },
                            onRefreshMedia = { reloadMedia() }
                        )
                    }
                }
            }

            // Fullscreen Now Playing Modal Sheet
            AnimatedVisibility(
                visible = isNowPlayingOpen && playbackState.currentSong != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NowPlayingScreen(
                    playerManager = playerManager,
                    preferencesRepository = preferencesRepository,
                    onCollapse = { isNowPlayingOpen = false },
                    onOpenEqualizer = {
                        isNowPlayingOpen = false
                        isEqualizerOpen = true
                    }
                )
            }

            // Equalizer Screen Modal
            AnimatedVisibility(
                visible = isEqualizerOpen,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                EqualizerScreen(
                    equalizerController = playerManager.equalizerController,
                    onBackPress = { isEqualizerOpen = false }
                )
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(NeonCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.permission_required),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.permission_desc),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
        ) {
            Text(
                text = stringResource(R.string.grant_permission),
                color = PlayerBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
        ) {
            Text(
                text = "Open App Settings",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
