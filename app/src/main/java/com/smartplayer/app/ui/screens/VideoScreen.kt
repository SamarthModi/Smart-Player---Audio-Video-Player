package com.smartplayer.app.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.smartplayer.app.R
import com.smartplayer.app.data.PreferencesRepository
import com.smartplayer.app.model.MediaFolder
import com.smartplayer.app.model.SortDirection
import com.smartplayer.app.model.SortOption
import com.smartplayer.app.model.VideoItem
import com.smartplayer.app.player.SmartAudioPlayerManager
import com.smartplayer.app.ui.components.PrivateVaultPinDialog
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
    var videoToDelete by remember { mutableStateOf<VideoItem?>(null) }

    // Private Secret Vault States
    var isVaultOpen by remember { mutableStateOf(false) }
    var showVaultPinDialog by remember { mutableStateOf(false) }

    val favoriteVideoIds by preferencesRepository.favoriteVideoIds.collectAsState()
    val vaultVideoIds by preferencesRepository.vaultVideoIds.collectAsState()
    val historyVersion by preferencesRepository.historyVersion.collectAsState()

    // Filter public vs private vault videos
    val activeVideosPool = remember(videos, vaultVideoIds, isVaultOpen) {
        if (isVaultOpen) {
            videos.filter { vaultVideoIds.contains(it.id) }
        } else {
            videos.filter { !vaultVideoIds.contains(it.id) }
        }
    }

    // Continue Watching list (for uncompleted videos with progress > 4s)
    val continueWatchingVideos = remember(videos, vaultVideoIds, isVaultOpen, historyVersion) {
        if (isVaultOpen) emptyList()
        else videos.filter {
            !vaultVideoIds.contains(it.id) &&
            preferencesRepository.getVideoPosition(it.path) > 4000L &&
            preferencesRepository.getVideoPosition(it.path) < (it.duration - 10000L)
        }
    }

    // Filter and Sort displayed videos
    val displayedVideos = remember(activeVideosPool, searchQuery, sortOption, sortDirection, selectedFolder) {
        var list = if (selectedFolder != null) {
            activeVideosPool.filter { it.folderName == selectedFolder?.name }
        } else {
            activeVideosPool
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
                    placeholder = { Text(stringResource(R.string.search_hint_video), color = Color.Gray) },
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
                        selectedFolder != null -> selectedFolder!!.name
                        else -> stringResource(R.string.tab_videos)
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVaultOpen) ElectricViolet else Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Private Vault Toggle Button
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
                    text = "${stringResource(R.string.secret_vault_active)}: ${displayedVideos.size}",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                TextButton(onClick = { isVaultOpen = false }) {
                    Text(stringResource(R.string.exit_vault), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Sub Tabs: All Videos vs Folders (when not inside a folder and not in vault)
        if (selectedFolder == null && !isVaultOpen) {
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
                            "${stringResource(R.string.all_videos)} (${activeVideosPool.size})",
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
                            "${stringResource(R.string.folders)} (${folders.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NeonCyan else Color.Gray
                        )
                    }
                )
            }
        } else if (selectedFolder != null) {
            // Folder header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { selectedFolder = null }) {
                    Text("← " + stringResource(R.string.folders), color = NeonCyan, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("${displayedVideos.size} ${stringResource(R.string.tab_videos)}", color = Color.Gray, fontSize = 13.sp)
            }
        }

        // Continue Watching Shelf (Only on Main Videos Tab)
        if (selectedTab == 0 && selectedFolder == null && !isVaultOpen && continueWatchingVideos.isNotEmpty()) {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.continue_watching), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatchingVideos, key = { "cw_${it.id}" }) { video ->
                        val savedPos = preferencesRepository.getVideoPosition(video.path)
                        val progressFraction = if (video.duration > 0) savedPos.toFloat() / video.duration.toFloat() else 0f

                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PlayerSurface)
                                .clickable {
                                    VideoPlayerActivity.start(context, video.uri, video.title, video.path, video.id)
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .background(Color.Black),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                AsyncImage(
                                    model = video.uri,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                LinearProgressIndicator(
                                    progress = { progressFraction.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = NeonCyan,
                                    trackColor = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = video.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Resume at ${FormatUtils.formatDuration(savedPos)}",
                                    color = NeonCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Main Content List / Grid
        if (selectedTab == 1 && selectedFolder == null && !isVaultOpen) {
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
                    Text(
                        text = if (isVaultOpen) stringResource(R.string.no_vault_videos) else stringResource(R.string.no_videos_found),
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
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
                        val lastPos = if (historyVersion >= 0) preferencesRepository.getVideoPosition(video.path) else 0L

                        VideoGridCard(
                            video = video,
                            isFavorite = isFav,
                            isVaultItem = isVaultOpen,
                            lastPosition = lastPos,
                            onClick = {
                                VideoPlayerActivity.start(context, video.uri, video.title, video.path, video.id)
                            },
                            onToggleFavorite = { preferencesRepository.toggleFavoriteVideo(video.id) },
                            onToggleVault = {
                                preferencesRepository.toggleVaultVideo(video.id)
                                Toast.makeText(
                                    context,
                                    if (isVaultOpen) "Unhid video from vault" else "Moved to Private Vault",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onPlayAsAudio = {
                                val mgr = SmartAudioPlayerManager.getInstance(context)
                                mgr.playSingleMedia(video.uri, video.title, video.path, lastPos)
                                Toast.makeText(context, "Playing video audio in background", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { videoToDelete = video },
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
                        val lastPos = if (historyVersion >= 0) preferencesRepository.getVideoPosition(video.path) else 0L

                        VideoListCard(
                            video = video,
                            isFavorite = isFav,
                            isVaultItem = isVaultOpen,
                            lastPosition = lastPos,
                            onClick = {
                                VideoPlayerActivity.start(context, video.uri, video.title, video.path, video.id)
                            },
                            onToggleFavorite = { preferencesRepository.toggleFavoriteVideo(video.id) },
                            onToggleVault = {
                                preferencesRepository.toggleVaultVideo(video.id)
                                Toast.makeText(
                                    context,
                                    if (isVaultOpen) "Unhid video from vault" else "Moved to Private Vault",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onPlayAsAudio = {
                                val mgr = SmartAudioPlayerManager.getInstance(context)
                                mgr.playSingleMedia(video.uri, video.title, video.path, lastPos)
                                Toast.makeText(context, "Playing video audio in background", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { videoToDelete = video },
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

    // Private Vault PIN Dialog
    if (showVaultPinDialog) {
        PrivateVaultPinDialog(
            preferencesRepository = preferencesRepository,
            onSuccess = {
                showVaultPinDialog = false
                isVaultOpen = true
                Toast.makeText(context, "Private Vault Unlocked", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showVaultPinDialog = false }
        )
    }

    // Delete Confirmation Dialog
    videoToDelete?.let { video ->
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            title = { Text(stringResource(R.string.delete_video), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val f = File(video.path)
                            if (f.exists()) f.delete()
                            context.contentResolver.delete(video.uri, null, null)
                            Toast.makeText(context, context.getString(R.string.file_deleted), Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not delete file", Toast.LENGTH_SHORT).show()
                        }
                        videoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            }
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
                    Text(stringResource(R.string.close), color = NeonCyan)
                }
            }
        )
    }
}

@Composable
fun VideoListCard(
    video: VideoItem,
    isFavorite: Boolean,
    isVaultItem: Boolean,
    lastPosition: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVault: () -> Unit,
    onPlayAsAudio: () -> Unit,
    onDelete: () -> Unit,
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
                    text = { Text(stringResource(R.string.play_as_audio)) },
                    onClick = {
                        onPlayAsAudio()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null, tint = NeonCyan) }
                )
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
                    text = { Text(if (isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_to_favorites)) },
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
                    text = { Text(stringResource(R.string.share)) },
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
                    text = { Text(stringResource(R.string.delete_video)) },
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
    isVaultItem: Boolean,
    lastPosition: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleVault: () -> Unit,
    onPlayAsAudio: () -> Unit,
    onDelete: () -> Unit,
    onShowDetails: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
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

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.play_as_audio)) },
                        onClick = {
                            onPlayAsAudio()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null, tint = NeonCyan) }
                    )
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
                        text = { Text(if (isFavorite) stringResource(R.string.remove_favorite) else stringResource(R.string.add_to_favorites)) },
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
                        text = { Text(stringResource(R.string.share)) },
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
                        text = { Text(stringResource(R.string.delete_video)) },
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
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
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
