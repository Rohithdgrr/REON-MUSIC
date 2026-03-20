/*
 * REON Music App - Downloads Screen
 * Copyright (c) 2024 REON
 * Light Purple Theme Design
 */

package com.reon.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.ui.viewmodels.LibraryViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Light Purple Theme Colors (matching LibraryScreen)
private val BackgroundPurple = Color(0xFFF5F0FF)
private val SurfacePurple = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFE9D5FF)

private enum class DownloadsTab { ALL_SONGS, BY_ARTIST, BY_ALBUM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    navController: NavHostController,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by libraryViewModel.uiState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(DownloadsTab.ALL_SONGS) }
    var showSongOptions by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    val downloadedSongs = uiState.downloadedSongs
    
    val filteredSongs = remember(downloadedSongs, selectedTab) {
        when (selectedTab) {
            DownloadsTab.ALL_SONGS -> downloadedSongs
            DownloadsTab.BY_ARTIST -> downloadedSongs.sortedBy { it.artist }
            DownloadsTab.BY_ALBUM -> downloadedSongs.sortedBy { it.album }
        }
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Downloads",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
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
        containerColor = BackgroundPurple
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPurple)
        ) {
            if (downloadedSongs.isEmpty()) {
                EmptyDownloadsState()
            } else {
                // Tab Row - Pill style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabPill(
                        text = "All Songs",
                        isSelected = selectedTab == DownloadsTab.ALL_SONGS,
                        onClick = { selectedTab = DownloadsTab.ALL_SONGS },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        text = "By Artist",
                        isSelected = selectedTab == DownloadsTab.BY_ARTIST,
                        onClick = { selectedTab = DownloadsTab.BY_ARTIST },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        text = "By Album",
                        isSelected = selectedTab == DownloadsTab.BY_ALBUM,
                        onClick = { selectedTab = DownloadsTab.BY_ALBUM },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Play All / Shuffle Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play All Button - Purple gradient
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFFA78BFA)
                                    )
                                )
                            )
                            .clickable { 
                                if (filteredSongs.isNotEmpty()) {
                                    playerViewModel.playQueue(filteredSongs)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play All",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Shuffle Button - Light purple background
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfacePurple)
                            .clickable { 
                                if (filteredSongs.isNotEmpty()) {
                                    playerViewModel.playQueue(filteredSongs.shuffled())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shuffle",
                                color = AccentPurple,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                // Song List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredSongs) { index, song ->
                        DownloadSongItem(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id,
                            onClick = { playerViewModel.playSong(song) },
                            onMoreClick = {
                                selectedSong = song
                                showSongOptions = true
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Storage Indicator
                        StorageIndicator(
                            downloadedCount = downloadedSongs.size,
                            totalStorage = "128 MB"
                        )
                        
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
        
        // Song Options Bottom Sheet
        if (showSongOptions && selectedSong != null) {
            DownloadSongOptionsSheet(
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
                onRemoveDownload = { 
                    libraryViewModel.removeDownload(selectedSong!!)
                    showSongOptions = false
                },
                onShare = { 
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${selectedSong!!.title} by ${selectedSong!!.artist}")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Song"))
                    showSongOptions = false
                }
            )
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) AccentPurple else SurfacePurple)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun DownloadSongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfacePurple
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Music note icon in purple rounded square
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFFA78BFA)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Song info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (song.album.isNotBlank()) {
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentPurple.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // More options button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BackgroundPurple)
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = AccentPurple,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageIndicator(
    downloadedCount: Int,
    totalStorage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Storage,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Storage",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
                
                Text(
                    text = "$downloadedCount / 8 songs",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPurple
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { downloadedCount / 8f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AccentPurple,
                trackColor = LightPurple
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadSongOptionsSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveDownload: () -> Unit,
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
                        .clip(RoundedCornerShape(12.dp))
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
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = BackgroundPurple)
            
            OptionMenuItem(icon = Icons.Default.Share, title = "Share", onClick = onShare)
            OptionMenuItem(
                icon = Icons.Default.Delete,
                title = "Remove Download",
                onClick = onRemoveDownload,
                tint = AccentPurple
            )
        }
    }
}

@Composable
private fun OptionMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
private fun EmptyDownloadsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large download icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(AccentPurple.copy(alpha = 0.1f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = AccentPurple
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No downloads yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Download songs to listen offline",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(
            onClick = { /* Navigate to home to download */ },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentPurple)
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Browse & Download")
        }
    }
}

