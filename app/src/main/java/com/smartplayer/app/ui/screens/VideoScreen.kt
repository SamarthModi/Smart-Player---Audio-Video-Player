package com.smartplayer.app.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.MediaFolder
import com.smartplayer.app.model.SortDirection
import com.smartplayer.app.model.SortOption
import com.smartplayer.app.model.VideoItem
import com.smartplayer.app.ui.components.SortFilterDialog
import com.smartplayer.app.ui.player.VideoPlayerActivity
import com.smartplayer.app.ui.theme.ElectricViolet
import com.smartplayer.app.ui.theme.NeonCyan
import com.smartplayer.app.ui.theme.PlayerSurface
import com.smartplayer.app.ui.theme.PlayerSurfaceVariant
import com.smartplayer.app.util.FormatUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    videos: List<VideoItem>,
    folders: List<MediaFolder>,
    preferencesRepository: PreferencesRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Videos, 1: Folders
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var sortOption by remember { mutableStateOf(SortOption.DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESCENDING) }
    var showSortDialog by remember { mutableStateOf(false) }

    var selectedFolder by remember { mutableStateOf<MediaFolder?>(null) }
    var detailVideo by remember { mutableStateOf<VideoItem?>(null) }

    val favoriteVideoIds by preferencesRepository.favoriteVideoIds.collectAsState()

    // Filter and Sort videos
    val displayedVideos = remember(videos, searchQuery, sortOption, sortDirection, selectedFolder) {
        var list = if (selectedFolder != null) {
            videos.filter { it.folderName == selectedFolder?.name }
        } else {
            videos
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.folderName.contains(searchQuery, ignoreCase = true)
            }
        }

        when (sortOption) {
            SortOption.NAME -> list.sortedBy { it.displayName.lowercase() }
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
                    placeholder = { Text("Search videos...", color = Color.Gray) },
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
                    text = if (selectedFolder != null) selectedFolder!!.name else "Videos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { isSearchActive = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }

                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = Color.White
                    )
                }

                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color.White)
                }
            }
        }

        // Sub Tabs: All Videos vs Folders (when not inside a folder)
        if (selectedFolder == null) {
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
                            "All Videos (${videos.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) NeonCyan else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Folders (${folders.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NeonCyan else Color.Gray
                        )
                    }
                )
            }
        } else {
            // Folder header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { selectedFolder = null }) {
                    Text("← Back to Folders", color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${displayedVideos.size} Videos", color = Color.Gray, fontSize = 13.sp)
            }
        }

        // Content
        if (selectedTab == 1 && selectedFolder == null) {
            // Folders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(folders) { folder ->
                    FolderItemCard(folder = folder, onClick = { selectedFolder = folder })
                }
            }
        } else {
            // Videos Grid or List
            if (displayedVideos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No videos found", color = Color.Gray, fontSize = 16.sp)
                }
            } else if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedVideos, key = { it.id }) { video ->
                        val isFav = favoriteVideoIds.contains(video.id)
                        val lastPos = preferencesRepository.getVideoPosition(video.path)

                        VideoGridCard(
                            video = video,
                            isFavorite = isFav,
                            lastPosition = lastPos,
                            onClick = {
                                VideoPlayerActivity.start(context, video.uri, video.title, video.path)
                            },
                            onToggleFavorite = { preferencesRepository.toggleFavoriteVideo(video.id) },
                            onShowDetails = { detailVideo = video }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedVideos, key = { it.id }) { video ->
                        val isFav = favoriteVideoIds.contains(video.id)
                        val lastPos = preferencesRepository.getVideoPosition(video.path)

                        VideoListCard(
                            video = video,
                            isFavorite = isFav,
                            lastPosition = lastPos,
                            onClick = {
                                VideoPlayerActivity.start(context, video.uri, video.title, video.path)
                            },
                            onToggleFavorite = { preferencesRepository.toggleFavoriteVideo(video.id) },
                            onShowDetails = { detailVideo = video }
                        )
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

    // Video Detail Dialog
    detailVideo?.let { vid ->
        AlertDialog(
            onDismissRequest = { detailVideo = null },
            title = { Text(vid.displayName, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Duration: ${FormatUtils.formatDuration(vid.duration)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Resolution: ${vid.resolutionFormatted}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Size: ${FormatUtils.formatFileSize(vid.size)}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Folder: ${vid.folderName}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Path: ${vid.path}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { detailVideo = null }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }
}

@Composable
fun VideoListCard(
    video: VideoItem,
    isFavorite: Boolean,
    lastPosition: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PlayerSurface)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with duration pill
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = video.uri,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = FormatUtils.formatDuration(video.duration),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Resume progress indicator
            if (lastPosition > 0 && video.duration > 0) {
                LinearProgressIndicator(
                    progress = { (lastPosition.toFloat() / video.duration.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NeonCyan,
                    trackColor = Color.Transparent
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Resolution pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlayerSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.resolutionFormatted,
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = FormatUtils.formatFileSize(video.size),
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }

        // Action menu
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove Favorite" else "Add to Favorites") },
                    onClick = {
                        onToggleFavorite()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, video.uri)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Details") },
                    onClick = {
                        onShowDetails()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
            }
        }
    }
}

@Composable
fun VideoGridCard(
    video: VideoItem,
    isFavorite: Boolean,
    lastPosition: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PlayerSurface)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomEnd
        ) {
            AsyncImage(
                model = video.uri,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duration badge
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = FormatUtils.formatDuration(video.duration),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Resume progress indicator
            if (lastPosition > 0 && video.duration > 0) {
                LinearProgressIndicator(
                    progress = { (lastPosition.toFloat() / video.duration.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NeonCyan,
                    trackColor = Color.Transparent
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = video.resolutionFormatted,
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = FormatUtils.formatFileSize(video.size),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun FolderItemCard(
    folder: MediaFolder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PlayerSurface)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ElectricViolet.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = ElectricViolet,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${folder.mediaCount} videos",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Text("›", color = Color.Gray, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
