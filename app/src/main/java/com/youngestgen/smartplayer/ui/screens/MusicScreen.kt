package com.youngestgen.smartplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.youngestgen.smartplayer.util.AudioAlbumArt
import com.youngestgen.smartplayer.R
import com.youngestgen.smartplayer.data.PreferencesRepository
import com.youngestgen.smartplayer.model.AudioItem
import com.youngestgen.smartplayer.model.MediaFolder
import com.youngestgen.smartplayer.model.SortDirection
import com.youngestgen.smartplayer.model.SortOption
import com.youngestgen.smartplayer.player.SmartAudioPlayerManager
import com.youngestgen.smartplayer.ui.components.PrivateVaultPinDialog
import com.youngestgen.smartplayer.ui.components.SortFilterDialog
import com.youngestgen.smartplayer.ui.theme.ElectricViolet
import com.youngestgen.smartplayer.ui.theme.NeonCyan
import com.youngestgen.smartplayer.ui.theme.PlayerSurface
import com.youngestgen.smartplayer.ui.theme.PlayerSurfaceVariant
import com.youngestgen.smartplayer.util.FormatUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    audios: List<AudioItem>,
    audioFolders: List<MediaFolder>,
    playerManager: SmartAudioPlayerManager,
    preferencesRepository: PreferencesRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tracks, 1: Albums, 2: Artists, 3: Folders
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf(SortOption.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }
    var showSortDialog by remember { mutableStateOf(false) }

    val playbackState by playerManager.playbackState.collectAsState()
    val favoriteSongIds by preferencesRepository.favoriteSongIds.collectAsState()
    val vaultAudioIds by preferencesRepository.vaultAudioIds.collectAsState()
    val recentlyPlayedAudioIds by preferencesRepository.recentlyPlayedAudioIds.collectAsState()
    val playlists by preferencesRepository.playlists.collectAsState()

    var showPlaylistChooserForSong by remember { mutableStateOf<AudioItem?>(null) }
    var detailSong by remember { mutableStateOf<AudioItem?>(null) }
    var songToDelete by remember { mutableStateOf<AudioItem?>(null) }

    // Private Vault States
    var isVaultOpen by remember { mutableStateOf(false) }
    var showVaultPinDialog by remember { mutableStateOf(false) }

    // Selected drilldown filters
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var selectedArtist by remember { mutableStateOf<String?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    // Filter public vs private vault songs
    val activeAudiosPool = remember(audios, vaultAudioIds, isVaultOpen) {
        if (isVaultOpen) {
            audios.filter { vaultAudioIds.contains(it.id) }
        } else {
            audios.filter { !vaultAudioIds.contains(it.id) }
        }
    }

    // Recently played audio list for shelf
    val recentSongs = remember(audios, recentlyPlayedAudioIds, vaultAudioIds, isVaultOpen) {
        if (isVaultOpen) emptyList()
        else {
            val audioMap = audios.associateBy { it.id }
            recentlyPlayedAudioIds.filter { !vaultAudioIds.contains(it) }.mapNotNull { audioMap[it] }.take(15)
        }
    }

    // Filter and Sort tracks
    val displayedTracks = remember(activeAudiosPool, searchQuery, sortOption, sortDirection, selectedAlbum, selectedArtist, selectedFolder) {
        var list = activeAudiosPool
        if (selectedAlbum != null) {
            list = list.filter { it.album == selectedAlbum }
        } else if (selectedArtist != null) {
            list = list.filter { it.artist == selectedArtist }
        } else if (selectedFolder != null) {
            list = list.filter { it.folderName == selectedFolder }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        when (sortOption) {
            SortOption.NAME -> list.sortedBy { it.title.lowercase() }
            SortOption.DATE -> list.sortedBy { it.dateAdded }
            SortOption.SIZE -> list.sortedBy { it.size }
            SortOption.DURATION -> list.sortedBy { it.duration }
        }.let {
            if (sortDirection == SortDirection.DESCENDING) it.reversed() else it
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.search_hint_music), color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PlayerSurfaceVariant,
                        unfocusedContainerColor = PlayerSurfaceVariant,
                        focusedIndicatorColor = NeonCyan,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            isSearchActive = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
                        }
                    }
                )
            } else {
                Text(
                    text = when {
                        isVaultOpen -> "🔒 " + stringResource(R.string.private_vault)
                        selectedAlbum != null -> selectedAlbum!!
                        selectedArtist != null -> selectedArtist!!
                        selectedFolder != null -> selectedFolder!!
                        else -> stringResource(R.string.tab_music)
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVaultOpen) ElectricViolet else Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Vault Toggle
                IconButton(onClick = {
                    if (isVaultOpen) {
                        isVaultOpen = false
                        Toast.makeText(context, "Exited Private Vault", Toast.LENGTH_SHORT).show()
                    } else {
                        showVaultPinDialog = true
                    }
                }) {
                    Icon(
                        imageVector = if (isVaultOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = stringResource(R.string.private_vault),
                        tint = if (isVaultOpen) ElectricViolet else Color.White
                    )
                }

                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }

                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color.White)
                }
            }
        }

        // Vault Active Banner
        if (isVaultOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricViolet.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stringResource(R.string.secret_vault_active)}: ${displayedTracks.size}",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                TextButton(onClick = { isVaultOpen = false }) {
                    Text(stringResource(R.string.exit_vault), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Sub Tabs (when not drilled down and not in vault)
        if (selectedAlbum == null && selectedArtist == null && selectedFolder == null && !isVaultOpen) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = NeonCyan
            ) {
                val tabs = listOf(
                    "${stringResource(R.string.tracks)} (${activeAudiosPool.size})",
                    stringResource(R.string.albums),
                    stringResource(R.string.artists),
                    stringResource(R.string.folders)
                )
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) NeonCyan else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }
        } else if (selectedAlbum != null || selectedArtist != null || selectedFolder != null) {
            // Header when filtered
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    selectedAlbum = null
                    selectedArtist = null
                    selectedFolder = null
                }) {
                    Text("← " + stringResource(R.string.close), color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${displayedTracks.size} ${stringResource(R.string.tracks)}", color = Color.Gray, fontSize = 13.sp)
            }
        }

        // Drilldown or Tab Content
        if (selectedAlbum != null || selectedArtist != null || selectedFolder != null || selectedTab == 0 || isVaultOpen) {
            // Tracks Tab Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Recently Played shelf (when on main tracks tab and not in vault)
                if (selectedTab == 0 && selectedAlbum == null && selectedArtist == null && selectedFolder == null && !isVaultOpen && recentSongs.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.recently_played), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(recentSongs, key = { "rec_${it.id}" }) { s ->
                                    val isCur = playbackState.currentSong?.id == s.id
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCur) ElectricViolet.copy(alpha = 0.3f) else PlayerSurface)
                                            .clickable {
                                                val idx = recentSongs.indexOf(s)
                                                playerManager.playQueue(recentSongs, idx)
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(PlayerSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AudioAlbumArt(
                                                audioUri = s.uri,
                                                albumId = s.albumId,
                                                contentDescription = s.title,
                                                modifier = Modifier.fillMaxSize(),
                                                iconTint = NeonCyan,
                                                iconSize = 18.dp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = s.title,
                                                color = if (isCur) NeonCyan else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = s.artist,
                                                color = Color.Gray,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Shuffle All & Play buttons header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { playerManager.playQueue(displayedTracks, 0) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text("${stringResource(R.string.play_all)} (${displayedTracks.size})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val shuffled = displayedTracks.shuffled()
                                playerManager.playQueue(shuffled, 0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.shuffle), color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                if (displayedTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isVaultOpen) stringResource(R.string.no_vault_songs) else stringResource(R.string.no_songs_found),
                                color = Color.Gray,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(displayedTracks, key = { _, song -> song.id }) { index, song ->
                        val isPlayingNow = playbackState.currentSong?.id == song.id
                        val isFav = favoriteSongIds.contains(song.id)

                        SongListItem(
                            song = song,
                            isPlayingNow = isPlayingNow,
                            isFavorite = isFav,
                            isVaultItem = isVaultOpen,
                            onClick = { playerManager.playQueue(displayedTracks, index) },
                            onToggleFavorite = { preferencesRepository.toggleFavoriteSong(song.id) },
                            onToggleVault = {
                                preferencesRepository.toggleVaultAudio(song.id)
                                Toast.makeText(
                                    context,
                                    if (isVaultOpen) "Unhid song from vault" else "Moved to Private Vault",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onAddToQueue = { playerManager.addToQueue(song) },
                            onAddToPlaylist = { showPlaylistChooserForSong = song },
                            onSetAsRingtone = {
                                setAsRingtone(context, song)
                            },
                            onDelete = { songToDelete = song },
                            onShowDetails = { detailSong = song }
                        )
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Albums Grid
            val albums = remember(activeAudiosPool) {
                activeAudiosPool.groupBy { it.album }
                    .map { (name, songs) -> name to songs }
                    .sortedByDescending { it.second.size }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(albums) { (albumName, songs) ->
                    val firstSong = songs.firstOrNull()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlayerSurface)
                            .clickable { selectedAlbum = albumName }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(PlayerSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (firstSong != null) {
                                AudioAlbumArt(
                                    audioUri = firstSong.uri,
                                    albumId = firstSong.albumId,
                                    contentDescription = albumName,
                                    modifier = Modifier.fillMaxSize(),
                                    iconTint = NeonCyan,
                                    iconSize = 36.dp
                                )
                            } else {
                                Icon(Icons.Default.Album, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(36.dp))
                            }
                        }
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(albumName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text("${songs.size} songs", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // Artists List
            val artists = remember(activeAudiosPool) {
                activeAudiosPool.groupBy { it.artist }
                    .map { (name, songs) -> name to songs }
                    .sortedByDescending { it.second.size }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(artists) { (artistName, songs) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlayerSurface)
                            .clickable { selectedArtist = artistName }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(ElectricViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricViolet)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(artistName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${songs.size} songs", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("›", color = Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (selectedTab == 3) {
            // Folders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(audioFolders) { folder ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlayerSurface)
                            .clickable { selectedFolder = folder.name }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = NeonCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(folder.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("${folder.mediaCount} songs", color = Color.Gray, fontSize = 12.sp)
                        }
                        Text("›", color = Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Sort Dialog
    if (showSortDialog) {
        SortFilterDialog(
            currentOption = sortOption,
            currentDirection = sortDirection,
            onOptionSelected = { opt, dir ->
                sortOption = opt
                sortDirection = dir
            },
            onDismiss = { showSortDialog = false }
        )
    }

    // Private Vault PIN Dialog
    if (showVaultPinDialog) {
        PrivateVaultPinDialog(
            preferencesRepository = preferencesRepository,
            onSuccess = {
                showVaultPinDialog = false
                isVaultOpen = true
                Toast.makeText(context, "Private Music Vault Unlocked", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showVaultPinDialog = false }
        )
    }

    // Delete Song Confirmation Dialog
    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text(stringResource(R.string.delete_track), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val f = File(song.path)
                            if (f.exists()) f.delete()
                            context.contentResolver.delete(song.uri, null, null)
                            Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not delete song", Toast.LENGTH_SHORT).show()
                        }
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
        )
    }

    // Add to Playlist Dialog
    showPlaylistChooserForSong?.let { song ->
        AlertDialog(
            onDismissRequest = { showPlaylistChooserForSong = null },
            title = { Text(stringResource(R.string.add_to_playlist), fontWeight = FontWeight.Bold) },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists created yet. Create one in the Playlists tab!")
                } else {
                    Column {
                        playlists.forEach { pl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        preferencesRepository.addSongToPlaylist(pl.id, song.id)
                                        showPlaylistChooserForSong = null
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(pl.name, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistChooserForSong = null }) {
                    Text(stringResource(R.string.cancel), color = NeonCyan)
                }
            }
        )
    }

    // Song Details Dialog
    detailSong?.let { s ->
        AlertDialog(
            onDismissRequest = { detailSong = null },
            title = { Text(s.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Artist: ${s.artist}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Album: ${s.album}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Duration: ${FormatUtils.formatDuration(s.duration)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("File Size: ${FormatUtils.formatFileSize(s.size)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Path: ${s.path}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { detailSong = null }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }
}

private fun setAsRingtone(context: Context, song: AudioItem) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(context, "Please allow Modify System Settings to set ringtone", Toast.LENGTH_LONG).show()
                return
            }
        }
        RingtoneManager.setActualDefaultRingtoneUri(
            context,
            RingtoneManager.TYPE_RINGTONE,
            song.uri
        )
        Toast.makeText(context, "Set as Ringtone: ${song.title}", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Could not set ringtone: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SongListItem(
    song: AudioItem,
    isPlayingNow: Boolean,
    isFavorite: Boolean,
    isVaultItem: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVault: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onSetAsRingtone: () -> Unit,
    onDelete: () -> Unit,
    onShowDetails: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isPlayingNow) PlayerSurfaceVariant.copy(alpha = 0.5f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PlayerSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AudioAlbumArt(
                audioUri = song.uri,
                albumId = song.albumId,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize(),
                iconTint = if (isPlayingNow) NeonCyan else Color.White.copy(alpha = 0.6f),
                iconSize = 22.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isPlayingNow) NeonCyan else Color.White,
                fontSize = 14.sp,
                fontWeight = if (isPlayingNow) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artist} • ${FormatUtils.formatDuration(song.duration)}",
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Favorite Toggle
        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        // More Menu
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray, modifier = Modifier.size(20.dp))
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isVaultItem) stringResource(R.string.remove_from_vault) else stringResource(R.string.move_to_vault)) },
                    onClick = {
                        onToggleVault()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            if (isVaultItem) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = ElectricViolet
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.set_as_ringtone)) },
                    onClick = {
                        onSetAsRingtone()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = NeonCyan) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_to_queue)) },
                    onClick = {
                        onAddToQueue()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_to_playlist)) },
                    onClick = {
                        onAddToPlaylist()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.share)) },
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "audio/*"
                            putExtra(Intent.EXTRA_STREAM, song.uri)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Track"))
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_track)) },
                    onClick = {
                        onDelete()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.details)) },
                    onClick = {
                        onShowDetails()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Album, contentDescription = null) }
                )
            }
        }
    }
}
