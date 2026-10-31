package com.smartplayer.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.SmartRepeatMode
import com.smartplayer.app.player.SmartAudioPlayerManager
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerBlack
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.PlayerSurfaceVariant
import com.smartplayer.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerManager: SmartAudioPlayerManager,
    preferencesRepository: PreferencesRepository,
    onCollapse: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackState by playerManager.playbackState.collectAsState()
    val favoriteSongIds by preferencesRepository.favoriteSongIds.collectAsState()
    val song = playbackState.currentSong ?: return

    val isFav = favoriteSongIds.contains(song.id)
    var showQueueSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showLyricsDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }

    val progress = if (playbackState.duration > 0) {
        (playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PlayerSurfaceVariant.copy(alpha = 0.9f),
                        PlayerBlack,
                        PlayerBlack
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Row {
                    IconButton(onClick = { showSongInfoDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large Vinyl / Album Artwork with ambient reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(24.dp), ambientColor = NeonCyan)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1B1E2E)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = NeonCyan.copy(alpha = 0.35f),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Song Title, Artist & Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${song.artist} • ${song.album}",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { preferencesRepository.toggleFavoriteSong(song.id) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) Color.Red else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Seekbar and Time labels
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progress,
                    onValueChange = { factor ->
                        val target = (factor * playbackState.duration).toLong()
                        playerManager.seekTo(target)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FormatUtils.formatDuration(playbackState.currentPosition),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = FormatUtils.formatDuration(playbackState.duration),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { playerManager.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffle) NeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = { playerManager.skipToPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause FAB
                IconButton(
                    onClick = { playerManager.playPause() },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(NeonCyan)
                ) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                        tint = PlayerBlack,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = { playerManager.skipToNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode
                IconButton(onClick = { playerManager.toggleRepeatMode() }) {
                    Icon(
                        imageVector = if (playbackState.repeatMode == SmartRepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.repeatMode != SmartRepeatMode.OFF) NeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Tools: Equalizer, Speed, Sleep Timer, Lyrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlayerSurfaceVariant)
                        .clickable { onOpenEqualizer() }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("EQ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Speed
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlayerSurfaceVariant)
                        .clickable { showSpeedDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${playbackState.playbackSpeed}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Lyrics
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlayerSurfaceVariant)
                        .clickable { showLyricsDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lyrics", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Sleep Timer
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlayerSurfaceVariant)
                        .clickable { showSleepTimerDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (playbackState.sleepTimerMinutesRemaining != null || playbackState.sleepAtEndOfSong) NeonCyan else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            playbackState.sleepAtEndOfSong -> "End Track"
                            playbackState.sleepTimerMinutesRemaining != null -> "${playbackState.sleepTimerMinutesRemaining}m"
                            else -> "Timer"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Interactive Queue Bottom Sheet (Reorder, Delete, Jump)
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PlayerSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playing Queue (${playbackState.queue.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    TextButton(onClick = {
                        // Empty queue except current
                        playbackState.currentSong?.let { cur ->
                            playerManager.playQueue(listOf(cur), 0)
                        }
                    }) {
                        Text("Clear Queue", color = Color.Red, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                    itemsIndexed(playbackState.queue) { idx, item ->
                        val isCurrent = idx == playbackState.queueIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) PlayerSurfaceVariant else Color.Transparent)
                                .clickable {
                                    playerManager.playQueue(playbackState.queue, idx)
                                }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}",
                                color = if (isCurrent) NeonCyan else Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.width(26.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.title,
                                    color = if (isCurrent) NeonCyan else Color.White,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(item.artist, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                            }

                            // Move Up
                            if (idx > 0) {
                                IconButton(
                                    onClick = { playerManager.moveQueueItem(idx, idx - 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Move Down
                            if (idx < playbackState.queue.size - 1) {
                                IconButton(
                                    onClick = { playerManager.moveQueueItem(idx, idx + 1) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Delete
                            IconButton(
                                onClick = { playerManager.removeFromQueue(idx) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Audio Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerManager.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${speed}x",
                                color = if (playbackState.playbackSpeed == speed) NeonCyan else Color.White,
                                fontWeight = if (playbackState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                            )
                            if (playbackState.playbackSpeed == speed) Text("✓", color = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Lyrics Dialog
    if (showLyricsDialog) {
        AlertDialog(
            onDismissRequest = { showLyricsDialog = false },
            title = { Text("Lyrics • ${song.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "♪ ${song.title} ♪\nby ${song.artist}\n\n" +
                                "Listening to local audio.\n" +
                                "Enjoy the high-fidelity sound output.\n" +
                                "Dynamic equalizer and bass boost active.\n\n" +
                                "Smart Player Pro • Lossless & Hi-Res Audio Engine",
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLyricsDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Sleep Timer Dialog with End of Song support
    if (showSleepTimerDialog) {
        val options = listOf(10, 15, 30, 45, 60, 90)
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Turn Off
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                playerManager.setSleepTimer(null)
                                playerManager.setSleepAtEndOfSong(false)
                                showSleepTimerDialog = false
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Turn Off", color = Color.White)
                        if (playbackState.sleepTimerMinutesRemaining == null && !playbackState.sleepAtEndOfSong) {
                            Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Stop at end of song
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                playerManager.setSleepAtEndOfSong(!playbackState.sleepAtEndOfSong)
                                playerManager.setSleepTimer(null)
                                showSleepTimerDialog = false
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stop at End of Current Song", color = Color.White)
                        if (playbackState.sleepAtEndOfSong) {
                            Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))

                    options.forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    playerManager.setSleepAtEndOfSong(false)
                                    playerManager.setSleepTimer(mins)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$mins Minutes", color = Color.White)
                            if (playbackState.sleepTimerMinutesRemaining == mins) {
                                Text("✓", color = NeonCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) { Text("Close", color = NeonCyan) }
            }
        )
    }

    // Detailed Song Metadata Dialog
    if (showSongInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSongInfoDialog = false },
            title = { Text("Audio Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Title: ${song.title}", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Artist: ${song.artist}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Album: ${song.album}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Duration: ${FormatUtils.formatDuration(song.duration)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("File Size: ${FormatUtils.formatFileSize(song.size)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Folder: ${song.folderName}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Path: ${song.path}", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSongInfoDialog = false }) { Text("OK", color = NeonCyan) }
            }
        )
    }
}
