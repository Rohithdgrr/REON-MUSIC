/*
 * REON Music App - Library Screen
 * Copyright (c) 2024 REON
 * Light Purple Theme with Pastel Category Cards
 */

package com.reon.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.data.database.entities.PlaylistEntity
import com.reon.music.ui.viewmodels.LibraryViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import com.reon.music.playback.PlayerState
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.reon.music.services.DownloadStatus
import java.text.SimpleDateFormat
import java.util.*

// Light Purple Theme Colors
private val BackgroundPurple = Color(0xFFF5F0FF)
private val SurfacePurple = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)

// Category colors - Pastel theme
private val CategoryFavorite = Color(0xFFFFD6E0)     // Light Pink
private val CategoryFollowed = Color(0xFFFFF4B8)     // Light Yellow
private val CategoryMostPlayed = Color(0xFFC8E6FF)    // Light Blue
private val CategoryHistory = Color(0xFFB8FFD6)      // Light Green

// Icon colors
private val IconPink = Color(0xFFFF6B9D)
private val IconYellow = Color(0xFFFFB946)
private val IconBlue = Color(0xFF4A90D9)
private val IconGreen = Color(0xFF50C878)

private enum class LibraryQuickCategory { NONE, FAVORITES, FOLLOWED, MOST_PLAYED, HISTORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by libraryViewModel.uiState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    val downloadProgressMap by playerViewModel.downloadProgress.collectAsState()
    
    val context = LocalContext.current
    val downloadedSongIds = remember(uiState.downloadedSongs) {
        uiState.downloadedSongs.map { it.id }.toSet()
    }
    
    // Sheet states
    var showSongOptions by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showPlaylistOptions by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistEntity?>(null) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedQuickCategory by remember { mutableStateOf(LibraryQuickCategory.NONE) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecentlyPlayed = remember(uiState.recentlyPlayed, searchQuery) {
        if (searchQuery.isBlank()) uiState.recentlyPlayed
        else uiState.recentlyPlayed.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true) 
        }
    }

    fun toggleQuickCategory(category: LibraryQuickCategory) {
        selectedQuickCategory = if (selectedQuickCategory == category) {
            LibraryQuickCategory.NONE
        } else {
            category
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    // No actions needed - time/battery removed
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPurple
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        floatingActionButton = {
            // Purple gradient FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFA78BFA),
                                Color(0xFF8B5CF6)
                            )
                        )
                    )
                    .clickable { libraryViewModel.showCreatePlaylistDialog() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Playlist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = BackgroundPurple
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPurple)
        ) {
            // Search Bar - Rounded with light background
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { 
                    Text(
                        "Search in library",
                        color = TextSecondary.copy(alpha = 0.6f)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = null,
                        tint = TextSecondary
                    ) 
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                } else null,
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = SurfacePurple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AccentPurple.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            LibraryOverviewContent(
                uiState = uiState,
                playerState = playerState,
                downloadedSongIds = downloadedSongIds,
                downloadProgressMap = downloadProgressMap,
                onSongClick = { playerViewModel.playSong(it) },
                onSongMoreClick = { song ->
                    selectedSong = song
                    showSongOptions = true
                },
                selectedQuickCategory = selectedQuickCategory,
                onQuickCategorySelected = { selected ->
                    toggleQuickCategory(selected)
                },
                onQuickCategoryClear = { selectedQuickCategory = LibraryQuickCategory.NONE },
                onPlaylistClick = { playlist ->
                    selectedPlaylist = playlist
                    showPlaylistOptions = true
                }
            )
        }
    }
        
    // Create playlist dialog
    if (uiState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { libraryViewModel.hideCreatePlaylistDialog() },
            onCreate = { name, desc -> libraryViewModel.createPlaylist(name, desc) }
        )
    }
        
    // Song Options Bottom Sheet
    if (showSongOptions && selectedSong != null) {
        LibrarySongOptionsSheet(
            song = selectedSong!!,
            onDismiss = { showSongOptions = false },
            onPlay = { 
                playerViewModel.playSong(selectedSong!!)
                showSongOptions = false
            },
            onPlayNext = { 
                playerViewModel.addToQueue(selectedSong!!, playNext = true)
                showSongOptions = false
            },
            onAddToQueue = { 
                playerViewModel.addToQueue(selectedSong!!)
                showSongOptions = false
            },
            onDownload = { 
                playerViewModel.downloadSong(selectedSong!!)
                showSongOptions = false
            },
            onAddToPlaylist = { 
                showAddToPlaylistDialog = true
                showSongOptions = false
            },
            onRemoveFromLibrary = { 
                libraryViewModel.removeSongFromLibrary(selectedSong!!)
                showSongOptions = false
            },
            onShare = { 
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Listen to ${selectedSong!!.title} by ${selectedSong!!.artist}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Song"))
                showSongOptions = false
            }
        )
    }
        
    // Playlist Options Bottom Sheet
    if (showPlaylistOptions && selectedPlaylist != null) {
        PlaylistOptionsSheet(
            playlist = selectedPlaylist!!,
            onDismiss = { showPlaylistOptions = false },
            onPlay = { 
                libraryViewModel.playPlaylist(selectedPlaylist!!.id, playerViewModel, shuffle = false)
                showPlaylistOptions = false
            },
            onShuffle = { 
                libraryViewModel.playPlaylist(selectedPlaylist!!.id, playerViewModel, shuffle = true)
                showPlaylistOptions = false
            },
            onAddToQueue = { 
                libraryViewModel.addPlaylistToQueue(selectedPlaylist!!.id, playerViewModel)
                showPlaylistOptions = false
            },
            onDownload = { 
                showPlaylistOptions = false
            },
            onDelete = { 
                libraryViewModel.deletePlaylist(selectedPlaylist!!)
                showPlaylistOptions = false
            },
            onShare = { 
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this playlist: ${selectedPlaylist!!.title}")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Playlist"))
                showPlaylistOptions = false
            }
        )
    }
        
    // Add to Playlist Dialog
    if (showAddToPlaylistDialog && selectedSong != null) {
        AddToPlaylistDialog(
            playlists = uiState.playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                libraryViewModel.addToPlaylist(playlist.id, selectedSong!!)
                showAddToPlaylistDialog = false
            }
        )
    }
}

@Composable
private fun QuickCategoryHeader(
    category: LibraryQuickCategory,
    uiState: com.reon.music.ui.viewmodels.LibraryUiState,
    onClear: () -> Unit
) {
    val (title, count) = when (category) {
        LibraryQuickCategory.FAVORITES -> "Favorite Songs" to uiState.likedSongs.size
        LibraryQuickCategory.FOLLOWED -> "Followed Playlists" to uiState.playlists.size
        LibraryQuickCategory.MOST_PLAYED -> "Most Played" to uiState.mostPlayed.size
        LibraryQuickCategory.HISTORY -> "Listening History" to uiState.recentlyPlayed.size
        LibraryQuickCategory.NONE -> "" to 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$count item${if (count == 1) "" else "s"}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
        TextButton(onClick = onClear) {
            Text("Close", color = AccentPurple)
        }
    }
}

@Composable
private fun QuickSongList(
    songs: List<Song>,
    playerState: PlayerState,
    downloadedSongIds: Set<String>,
    downloadProgressMap: Map<String, com.reon.music.services.DownloadProgress>,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    emptyMessage: String = "No songs to display yet."
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfacePurple)
    ) {
        songs.forEachIndexed { index, song ->
            LibrarySongRow(
                song = song,
                isPlaying = playerState.currentSong?.id == song.id,
                isDownloaded = downloadedSongIds.contains(song.id),
                isDownloading = downloadProgressMap[song.id]?.let { dp ->
                    dp.status == DownloadStatus.DOWNLOADING || dp.status == DownloadStatus.QUEUED
                } ?: false,
                downloadProgress = downloadProgressMap[song.id]?.progress ?: 0,
                onClick = { onSongClick(song) },
                onMoreClick = { onSongMoreClick(song) }
            )
            if (index < songs.lastIndex) {
                HorizontalDivider(color = BackgroundPurple.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun QuickPlaylistList(
    playlists: List<PlaylistEntity>,
    onPlaylistClick: (PlaylistEntity) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No followed playlists yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfacePurple)
    ) {
        playlists.forEachIndexed { index, playlist ->
            QuickPlaylistRow(playlist = playlist, onClick = { onPlaylistClick(playlist) })
            if (index < playlists.lastIndex) {
                HorizontalDivider(color = BackgroundPurple.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun LibrarySongRow(
    song: Song,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Int,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = song.getHighQualityArtwork() ?: song.artworkUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isPlaying) AccentPurple else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isDownloading) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        progress = (downloadProgress.coerceIn(0, 100) / 100f),
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = AccentPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${downloadProgress.coerceIn(0, 100)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentPurple
                    )
                }
            } else if (isDownloaded) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.DownloadDone,
                        contentDescription = "Downloaded",
                        tint = AccentPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentPurple
                    )
                }
            }
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun QuickPlaylistRow(
    playlist: PlaylistEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPurple.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = Color.White
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.description ?: "${playlist.songCount} songs",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary
        )
    }
}

@Composable
private fun LibraryOverviewContent(
    uiState: com.reon.music.ui.viewmodels.LibraryUiState,
    playerState: PlayerState,
    downloadedSongIds: Set<String>,
    downloadProgressMap: Map<String, com.reon.music.services.DownloadProgress>,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
    selectedQuickCategory: LibraryQuickCategory,
    onQuickCategorySelected: (LibraryQuickCategory) -> Unit,
    onQuickCategoryClear: () -> Unit,
    onPlaylistClick: (PlaylistEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Cards (2x2 Grid) with improved spacing
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite,
                        title = "Favorites",
                        subtitle = "${uiState.likedSongs.size} songs",
                        backgroundColor = CategoryFavorite,
                        iconColor = IconPink,
                        onClick = { onQuickCategorySelected(LibraryQuickCategory.FAVORITES) }
                    )
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bookmark,
                        title = "Followed",
                        subtitle = "${uiState.playlists.size} playlists",
                        backgroundColor = CategoryFollowed,
                        iconColor = IconYellow,
                        onClick = { onQuickCategorySelected(LibraryQuickCategory.FOLLOWED) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        title = "Most Played",
                        subtitle = "${uiState.mostPlayed.size} songs",
                        backgroundColor = CategoryMostPlayed,
                        iconColor = IconBlue,
                        onClick = { onQuickCategorySelected(LibraryQuickCategory.MOST_PLAYED) }
                    )
                    CategoryCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.History,
                        title = "History",
                        subtitle = "${uiState.recentlyPlayed.size} songs",
                        backgroundColor = CategoryHistory,
                        iconColor = IconGreen,
                        onClick = { onQuickCategorySelected(LibraryQuickCategory.HISTORY) }
                    )
                }
            }
        }
        
        if (selectedQuickCategory != LibraryQuickCategory.NONE) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                QuickCategoryHeader(
                    category = selectedQuickCategory,
                    uiState = uiState,
                    onClear = onQuickCategoryClear
                )
            }

            when (selectedQuickCategory) {
                LibraryQuickCategory.FAVORITES -> {
                    item {
                        QuickSongList(
                            songs = uiState.likedSongs,
                            playerState = playerState,
                            downloadedSongIds = downloadedSongIds,
                            downloadProgressMap = downloadProgressMap,
                            onSongClick = onSongClick,
                            onSongMoreClick = onSongMoreClick
                        )
                    }
                }
                LibraryQuickCategory.FOLLOWED -> {
                    item {
                        QuickPlaylistList(
                            playlists = uiState.playlists,
                            onPlaylistClick = onPlaylistClick
                        )
                    }
                }
                LibraryQuickCategory.MOST_PLAYED -> {
                    item {
                        QuickSongList(
                            songs = uiState.mostPlayed,
                            playerState = playerState,
                            downloadedSongIds = downloadedSongIds,
                            downloadProgressMap = downloadProgressMap,
                            onSongClick = onSongClick,
                            onSongMoreClick = onSongMoreClick,
                            emptyMessage = "Keep listening to build your most played mix."
                        )
                    }
                }
                LibraryQuickCategory.HISTORY -> {
                    item {
                        QuickSongList(
                            songs = uiState.recentlyPlayed,
                            playerState = playerState,
                            downloadedSongIds = downloadedSongIds,
                            downloadProgressMap = downloadProgressMap,
                            onSongClick = onSongClick,
                            onSongMoreClick = onSongMoreClick,
                            emptyMessage = "Play a few songs and your listening history will appear here."
                        )
                    }
                }
                LibraryQuickCategory.NONE -> Unit
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Recently Played Section - Horizontal row
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                Text(
                    text = "Recently Played",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.recentlyPlayed.take(10)) { song ->
                        RecentlyPlayedItem(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Icon at top-left
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Title and subtitle at bottom-left
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CompactCategoryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(58.dp)
            .clickable(onClick = {
                try {
                    onClick()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentlyPlayedItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Square album art with rounded corners
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfacePurple)
        ) {
            AsyncImage(
                model = song.getHighQualityArtwork(),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccentPurple.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Song title
        Text(
            text = song.title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Artist name
        Text(
            text = song.artist,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RecentlyAddedItem(
    item: Any,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    when (item) {
        is Song -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isPlaying) AccentPurple.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfacePurple)
                ) {
                    AsyncImage(
                        model = (item as Song).getHighQualityArtwork(),
                        contentDescription = (item as Song).title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AccentPurple.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (item as Song).title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlaying) AccentPurple else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = (item as Song).artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Show album/movie name if available
                    if ((item as Song).album.isNotBlank()) {
                        Text(
                            text = (item as Song).album,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentPurple.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // NOW FUNCTIONAL
                IconButton(onClick = onMoreClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        is PlaylistEntity -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfacePurple),
                    contentAlignment = Alignment.Center
                ) {
                    if ((item as PlaylistEntity).thumbnailUrl != null) {
                        AsyncImage(
                            model = (item as PlaylistEntity).thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (item as PlaylistEntity).title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Playlist • YouTube Music",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubePlaylistsContent(
    playlists: List<PlaylistEntity>,
    onPlaylistClick: (PlaylistEntity) -> Unit,
    onPlaylistMoreClick: (PlaylistEntity) -> Unit
) {
    if (playlists.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.PlaylistPlay,
            title = "No YouTube playlists",
            subtitle = "Your YouTube Music playlists will appear here"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    onMoreClick = { onPlaylistMoreClick(playlist) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun MadeForYouContent(
    uiState: com.reon.music.ui.viewmodels.LibraryUiState,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.recentlyPlayed.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.MusicNote,
                    title = "No recommendations yet",
                    subtitle = "Start listening to get personalized recommendations"
                )
            }
        } else {
            itemsIndexed(uiState.recentlyPlayed.take(20)) { _, song ->
                SongListItem(
                    song = song,
                    isPlaying = false,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun PlaylistItem(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfacePurple),
            contentAlignment = Alignment.Center
        ) {
            if (playlist.thumbnailUrl != null) {
                AsyncImage(
                    model = playlist.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.songCount} songs",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        
        // NOW FUNCTIONAL
        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun SongListItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isPlaying) AccentPurple.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.getHighQualityArtwork(),
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isPlaying) AccentPurple else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Show album/movie name if available
            if (song.album.isNotBlank()) {
                Text(
                    text = "Album: ${song.album}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentPurple.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // NOW FUNCTIONAL
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextSecondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrarySongOptionsSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemoveFromLibrary: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfacePurple
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Song header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.getHighQualityArtwork(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimary
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            HorizontalDivider(color = BackgroundPurple)
            
            // Options
            OptionMenuItem(icon = Icons.Default.PlayArrow, title = "Play", onClick = onPlay)
            OptionMenuItem(icon = Icons.Default.PlaylistAdd, title = "Play Next", onClick = onPlayNext)
            OptionMenuItem(icon = Icons.Default.QueueMusic, title = "Add to Queue", onClick = onAddToQueue)
            OptionMenuItem(icon = Icons.Outlined.Download, title = "Download", onClick = onDownload)
            OptionMenuItem(icon = Icons.Outlined.PlaylistAdd, title = "Add to Playlist", onClick = onAddToPlaylist)
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BackgroundPurple)
            
            OptionMenuItem(icon = Icons.Default.Share, title = "Share", onClick = onShare)
            OptionMenuItem(
                icon = Icons.Default.RemoveCircleOutline,
                title = "Remove from Library",
                onClick = onRemoveFromLibrary,
                tint = AccentPurple
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistOptionsSheet(
    playlist: PlaylistEntity,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfacePurple
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Playlist header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundPurple),
                    contentAlignment = Alignment.Center
                ) {
                    if (playlist.thumbnailUrl != null) {
                        AsyncImage(
                            model = playlist.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimary
                    )
                    Text(
                        text = "${playlist.songCount} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            HorizontalDivider(color = BackgroundPurple)
            
            // Options
            OptionMenuItem(icon = Icons.Default.PlayArrow, title = "Play All", onClick = onPlay)
            OptionMenuItem(icon = Icons.Default.Shuffle, title = "Shuffle Play", onClick = onShuffle)
            OptionMenuItem(icon = Icons.Default.QueueMusic, title = "Add to Queue", onClick = onAddToQueue)
            OptionMenuItem(icon = Icons.Outlined.Download, title = "Download", onClick = onDownload)
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BackgroundPurple)
            
            OptionMenuItem(icon = Icons.Default.Share, title = "Share", onClick = onShare)
            OptionMenuItem(
                icon = Icons.Default.Delete,
                title = "Delete Playlist",
                onClick = onDelete,
                tint = AccentPurple
            )
        }
    }
}

@Composable
private fun OptionMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = tint
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Playlist",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        cursorColor = AccentPurple
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        cursorColor = AccentPurple
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, description.takeIf { it.isNotBlank() })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    text = "No playlists found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistSelected(playlist) }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = playlist.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFFFFFFFF),
        titleContentColor = Color(0xFF1C1C1C),
        textContentColor = Color(0xFF1C1C1C)
    )
}
