package com.smartplayer.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.AudioItem
import com.smartplayer.app.model.MediaFolder
import com.smartplayer.app.model.SortDirection
import com.smartplayer.app.model.SortOption
import com.smartplayer.app.player.SmartAudioPlayerManager
import com.smartplayer.app.ui.components.SortFilterDialog
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.PlayerSurfaceVariant
import com.smartplayer.app.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    audios: List<AudioItem>,
    audioFolders: List<MediaFolder>,
    playerManager: SmartAudioPlayerManager,
    preferencesRepository: PreferencesRepository,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tracks, 1: Albums, 2: Artists, 3: Folders
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf(SortOption.NAME) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASCENDING) }
    var showSortDialog by remember { mutableStateOf(false) }

    val playbackState by playerManager.playbackState.collectAsState()
    val favoriteSongIds by preferencesRepository.favoriteSongIds.collectAsState()
    val playlists by preferencesRepository.playlists.collectAsState()

    var showPlaylistChooserForSong by remember { mutableStateOf<AudioItem?>(null) }
    var detailSong by remember { mutableStateOf<AudioItem?>(null) }

    // Selected drilldown filters
    var selectedAlbum by remember { mutableStateOf<String?>(null) }
    var selectedArtist by remember { mutableStateOf<String?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    // Filter and Sort tracks
    val displayedTracks = remember(audios, searchQuery, sortOption, sortDirection, selectedAlbum, selectedArtist, selectedFolder) {
        var list = audios
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
                    placeholder = { Text("Search songs, artists...", color = Color.Gray) },
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
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                )
            } else {
                Text(
                    text = when {
                        selectedAlbum != null -> selectedAlbum!!
                        selectedArtist != null -> selectedArtist!!
                        selectedFolder != null -> selectedFolder!!
                        else -> "Music"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }

                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color.White)
                }
            }
        }

        // Sub Tabs (when not drilled down into album/artist/folder)
        if (selectedAlbum == null && selectedArtist == null && selectedFolder == null) {
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = NeonCyan
            ) {
                val tabs = listOf("Tracks (${audios.size})", "Albums", "Artists", "Folders")
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
        } else {
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
                    Text("← Back", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${displayedTracks.size} Songs", color = Color.Gray, fontSize = 13.sp)
            }
        }

        // Drilldown or Tab Content
        if (selectedAlbum != null || selectedArtist != null || selectedFolder != null || selectedTab == 0) {
            // Tracks List
            Column(modifier = Modifier.fillMaxSize()) {
                // Shuffle All Button Bar
                if (displayedTracks.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val shuffled = displayedTracks.shuffled()
                                playerManager.playQueue(shuffled, 0)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayerSurfaceVariant),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle All", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (displayedTracks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tracks found", color = Color.Gray, fontSize = 16.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(displayedTracks, key = { _, song -> song.id }) { index, song ->
                            val isCurrent = playbackState.currentSong?.id == song.id
                            val isFav = favoriteSongIds.contains(song.id)

                            AudioTrackItem(
                                song = song,
                                isPlayingNow = isCurrent && playbackState.isPlaying,
                                isFavorite = isFav,
                                onClick = {
                                    playerManager.playQueue(displayedTracks, index)
                                },
                                onToggleFavorite = { preferencesRepository.toggleFavoriteSong(song.id) },
                                onAddToQueue = { playerManager.addToQueue(song) },
                                onAddToPlaylist = { showPlaylistChooserForSong = song },
                                onShowDetails = { detailSong = song }
                            )
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Albums Grid
            val albums = remember(audios) {
                audios.groupBy { it.album }
                    .map { (name, songs) ->
                        AlbumInfo(name = name, artist = songs.firstOrNull()?.artist ?: "", count = songs.size, firstSong = songs.first())
                    }.sortedBy { it.name.lowercase() }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(albums) { album ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlayerSurface)
                            .clickable { selectedAlbum = album.name }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = album.firstSong.albumArtUri,
                                contentDescription = album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(Icons.Default.Album, contentDescription = null, tint = NeonCyan.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                        }
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(album.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(album.artist, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${album.count} songs", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (selectedTab == 2) {
            // Artists List
            val artists = remember(audios) {
                audios.groupBy { it.artist }
                    .map { (name, songs) -> Pair(name, songs.size) }
                    .sortedBy { it.first.lowercase() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(artists) { (artistName, count) ->
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ElectricViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricViolet)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(artistName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("$count songs", color = Color.Gray, fontSize = 12.sp)
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

    // Add to Playlist Dialog
    showPlaylistChooserForSong?.let { song ->
        AlertDialog(
            onDismissRequest = { showPlaylistChooserForSong = null },
            title = { Text("Add to Playlist", fontWeight = FontWeight.Bold) },
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
                    Text("Cancel", color = NeonCyan)
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

private data class AlbumInfo(
    val name: String,
    val artist: String,
    val count: Int,
    val firstSong: AudioItem
)

@Composable
fun AudioTrackItem(
    song: AudioItem,
    isPlayingNow: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowDetails: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPlayingNow) PlayerSurfaceVariant else PlayerSurface)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isPlayingNow) NeonCyan else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
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
                    text = { Text("Add to Queue") },
                    onClick = {
                        onAddToQueue()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist") },
                    onClick = {
                        onAddToPlaylist()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Details") },
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
