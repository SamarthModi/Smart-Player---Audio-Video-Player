package com.youngestgen.smartplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.youngestgen.smartplayer.R
import com.youngestgen.smartplayer.data.PreferencesRepository
import com.youngestgen.smartplayer.model.AudioItem
import com.youngestgen.smartplayer.model.Playlist
import com.youngestgen.smartplayer.model.VideoItem
import com.youngestgen.smartplayer.player.SmartAudioPlayerManager
import com.youngestgen.smartplayer.ui.player.VideoPlayerActivity
import com.youngestgen.smartplayer.ui.theme.ElectricViolet
import com.youngestgen.smartplayer.ui.theme.NeonCyan
import com.youngestgen.smartplayer.ui.theme.PlayerBlack
import com.youngestgen.smartplayer.ui.theme.PlayerSurface
import com.youngestgen.smartplayer.ui.theme.PlayerSurfaceVariant
import com.youngestgen.smartplayer.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    allAudios: List<AudioItem>,
    allVideos: List<VideoItem>,
    playerManager: SmartAudioPlayerManager,
    preferencesRepository: PreferencesRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: History, 2: Playlists
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

    val favoriteSongIds by preferencesRepository.favoriteSongIds.collectAsState()
    val favoriteVideoIds by preferencesRepository.favoriteVideoIds.collectAsState()
    val recentlyPlayedIds by preferencesRepository.recentlyPlayedAudioIds.collectAsState()
    val playlists by preferencesRepository.playlists.collectAsState()

    val favoriteSongs = remember(allAudios, favoriteSongIds) {
        allAudios.filter { favoriteSongIds.contains(it.id) }
    }
    val favoriteVideos = remember(allVideos, favoriteVideoIds) {
        allVideos.filter { favoriteVideoIds.contains(it.id) }
    }
    val recentSongs = remember(allAudios, recentlyPlayedIds) {
        val audioMap = allAudios.associateBy { it.id }
        recentlyPlayedIds.mapNotNull { audioMap[it] }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedPlaylist != null) selectedPlaylist!!.name else stringResource(R.string.tab_playlists),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (selectedPlaylist == null && selectedTab == 2) {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = NeonCyan)
                    }
                }
            }

            if (selectedPlaylist == null) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = NeonCyan
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "${stringResource(R.string.favorites)} (${favoriteSongs.size + favoriteVideos.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) NeonCyan else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "${stringResource(R.string.history)} (${recentSongs.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) NeonCyan else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "${stringResource(R.string.custom_playlists)} (${playlists.size})",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) NeonCyan else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            } else {
                // Header with Back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedPlaylist = null }) {
                        Text("← " + stringResource(R.string.tab_playlists), color = NeonCyan, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        preferencesRepository.deletePlaylist(selectedPlaylist!!.id)
                        selectedPlaylist = null
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = Color.Red)
                    }
                }
            }

            // Tab Content
            if (selectedPlaylist != null) {
                // Playlist Detail Songs
                val plSongs = remember(allAudios, selectedPlaylist) {
                    val map = allAudios.associateBy { it.id }
                    selectedPlaylist!!.songIds.mapNotNull { map[it] }
                }

                if (plSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No songs in this playlist yet. Add some from the Music tab!", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(plSongs) { idx, song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PlayerSurface)
                                    .clickable { playerManager.playQueue(plSongs, idx) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(model = song.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text(song.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                }
                                IconButton(onClick = { preferencesRepository.removeSongFromPlaylist(selectedPlaylist!!.id, song.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else when (selectedTab) {
                0 -> {
                    // Favorites Tab (Songs + Videos)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (favoriteSongs.isEmpty() && favoriteVideos.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("No favorites yet. Tap the heart icon on any video or song!", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }

                        if (favoriteSongs.isNotEmpty()) {
                            item {
                                Text("Favorite Songs (${favoriteSongs.size})", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            itemsIndexed(favoriteSongs) { idx, song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PlayerSurface)
                                        .clickable { playerManager.playQueue(favoriteSongs, idx) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(model = song.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = NeonCyan)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(song.artist, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { preferencesRepository.toggleFavoriteSong(song.id) }) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Unfavorite", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        if (favoriteVideos.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Favorite Videos (${favoriteVideos.size})", color = ElectricViolet, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            items(favoriteVideos) { video ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PlayerSurface)
                                        .clickable {
                                            VideoPlayerActivity.start(context, video.uri, video.title, video.path)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 80.dp, height = 50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    ) {
                                        AsyncImage(model = video.uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(video.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(FormatUtils.formatDuration(video.duration), color = Color.Gray, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { preferencesRepository.toggleFavoriteVideo(video.id) }) {
                                        Icon(Icons.Default.Favorite, contentDescription = "Unfavorite", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // History Tab
                    if (recentSongs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No playback history yet.", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(recentSongs) { idx, song ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PlayerSurface)
                                        .clickable { playerManager.playQueue(recentSongs, idx) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(model = song.albumArtUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFFFFB300))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(song.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(song.artist, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    Text(FormatUtils.formatDuration(song.duration), color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Custom Playlists Tab
                    if (playlists.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PlaylistPlay, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No custom playlists yet", color = Color.Gray, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showCreateDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                ) {
                                    Text("Create Playlist", color = PlayerBlack, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(playlists) { pl ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PlayerSurface)
                                        .clickable { selectedPlaylist = pl }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ElectricViolet.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = ElectricViolet)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pl.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${pl.songIds.size} songs", color = Color.Gray, fontSize = 12.sp)
                                    }
                                    Text("›", color = Color.Gray, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newPlaylistName = ""
            },
            title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PlayerSurfaceVariant,
                        unfocusedContainerColor = PlayerSurfaceVariant,
                        focusedIndicatorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            preferencesRepository.createPlaylist(newPlaylistName.trim())
                            showCreateDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok), color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newPlaylistName = ""
                }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }
}
