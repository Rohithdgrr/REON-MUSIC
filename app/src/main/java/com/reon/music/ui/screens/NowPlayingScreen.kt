/*
 * REON Music App - Enhanced Now Playing Screen
 * Copyright (c) 2024 REON
 * Pure White Background Design with Full Functionality
 */

package com.reon.music.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.services.DownloadStatus
import com.reon.music.services.DownloadProgress
import com.reon.music.ui.viewmodels.PlayerViewModel
import com.reon.music.ui.screens.nowplaying.DownloadButton

// Pure White Theme Colors - Clean Premium Design
private val PureWhiteBackground = Color(0xFFFFFFFF)
private val SurfaceColor = Color.White
private val CardColor = Color(0xFFF8F9FA)
private val AccentRed = Color(0xFFFF0000) // YouTube red for play button
private val AccentRedLight = Color(0xFFFF4444)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF666666)
private val DividerColor = Color(0xFFEEEEEE)
private val ProgressTrackColor = Color(0xFFE0E0E0)
private val ProgressActiveColor = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit
) {
val playerState by playerViewModel.playerState.collectAsState()
    val uiState by playerViewModel.uiState.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val downloadProgressMap by playerViewModel.downloadProgress.collectAsState()
    val downloadedSongs by playerViewModel.downloadedSongs.collectAsState()
    val context = LocalContext.current
    
    var showQueueSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    val isLiked = uiState.isLiked
    
    val currentSong = playerState.currentSong
    val durationMs = when {
        playerState.duration > 0 -> playerState.duration
        currentSong != null && currentSong.duration > 0 -> currentSong.duration * 1000L
        else -> 0L
    }
    val progress = if (durationMs > 0) {
        currentPosition.toFloat() / durationMs
    } else 0f
    
    // Gesture state for swipe to dismiss
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Pure white background - modern clean design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhiteBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 200f) {
                            onDismiss()
                        }
                        dragOffset = 0f
                        isDragging = false
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0f) { // Only allow downward drag
                            dragOffset = dragOffset + dragAmount
                            isDragging = true
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = dragOffset
                    alpha = 1f - (dragOffset / 1000f).coerceIn(0f, 0.5f)
                }
        ) {
            // Top Bar - Dark text on white background
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentSong?.album?.takeIf { it.isNotBlank() } ?: "Unknown Album",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { showOptionsSheet = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.15f))
            
            // Album Artwork - Larger square thumbnail centered with premium shadow
            Box(
                modifier = Modifier
                    .size(320.dp) // Increased size
                    .align(Alignment.CenterHorizontally)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(20.dp))
            ) {
                com.reon.music.ui.components.OptimizedAsyncImage(
                    imageUrl = currentSong?.getHighQualityArtwork() ?: currentSong?.artworkUrl,
                    contentDescription = "Album Art",
                    modifier = Modifier.fillMaxSize(),
                    quality = com.reon.music.ui.components.ImageQuality.HIGH,
                    shape = RoundedCornerShape(20.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.weight(0.15f))
            
            // Song Info with Like Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "No song playing",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                IconButton(
                    onClick = { playerViewModel.toggleLike() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) ProgressActiveColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Bar - Red themed
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Slider(
                    value = progress.coerceIn(0f, 1f),
                    onValueChange = { value ->
                        if (durationMs > 0) {
                            val newPosition = (value * durationMs).toLong()
                            playerViewModel.seekTo(newPosition)
                        }
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = ProgressActiveColor,
                        activeTrackColor = ProgressActiveColor,
                        inactiveTrackColor = ProgressTrackColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (durationMs > 0) formatTime(durationMs) else "0:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Controls - Shuffle, Previous, Play/Pause (YouTube Red), Next, Repeat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { playerViewModel.toggleShuffle() },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.shuffleEnabled) ProgressActiveColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Previous
                IconButton(
                    onClick = { playerViewModel.skipToPrevious() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                
                // Play/Pause - YouTube Red circle with white icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AccentRed)
                        .clickable { playerViewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
                
                // Next
                IconButton(
                    onClick = { playerViewModel.skipToNext() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = TextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                
                // Repeat - Cycles through OFF -> ALL -> ONE -> OFF
                IconButton(
                    onClick = { playerViewModel.cycleRepeatMode() },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            1 -> Icons.Default.RepeatOne
                            2 -> Icons.Default.Repeat
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (playerState.repeatMode > 0) ProgressActiveColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Bottom Controls (Info, Download, Add to Playlist, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), // Reduced spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Song Info Button
                IconButton(
                    onClick = { showSongInfoDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Song Info",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
// Download Button
                val currentSongId = currentSong?.id
                val downloadProgress = currentSongId?.let { downloadProgressMap[it] }
                val isDownloading = downloadProgress?.status == DownloadStatus.DOWNLOADING || downloadProgress?.status == DownloadStatus.QUEUED
                val isDownloaded = downloadedSongs.any { it.id == currentSongId }
                DownloadButton(
                    song = currentSong,
                    downloadProgress = downloadProgress?.progress,
                    isDownloaded = isDownloaded,
                    isDownloading = isDownloading,
                    onDownloadClick = {
                        currentSong?.let {
                            playerViewModel.downloadSong(it)
                        }
                    }
                )
                
                // Add to Playlist Button
                IconButton(
                    onClick = { showAddToPlaylistSheet = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to Playlist",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Radio Endless Button
                IconButton(
                    onClick = { playerViewModel.enableRadioMode(playerState.queue) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = "Radio",
                        tint = if (playerState.radioModeEnabled) AccentRed else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Queue Button
                IconButton(
                    onClick = { showQueueSheet = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Radio Mode Indicator (moved down, shown only if enabled)
            AnimatedVisibility(
                visible = playerState.radioModeEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentRed.copy(alpha = 0.1f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Radio Mode: Endless Songs Playing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.1f))
        }
        
        // Queue Bottom Sheet
        if (showQueueSheet) {
            QueueBottomSheet(
                currentSong = currentSong,
                queue = playerState.queue,
                currentIndex = playerState.currentIndex,
                onDismiss = { showQueueSheet = false },
                onSongClick = { index -> playerViewModel.playFromQueue(index) },
                onRemove = { index -> playerViewModel.removeFromQueue(index) }
            )
        }
        
        // Options Bottom Sheet
        if (showOptionsSheet) {
            OptionsBottomSheet(
                song = currentSong,
                playerViewModel = playerViewModel,
                onDismiss = { showOptionsSheet = false },
                onDownload = { 
                    currentSong?.let { playerViewModel.downloadSong(it) }
                    showOptionsSheet = false
                },
                onShare = { 
                    currentSong?.let { song ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out \"${song.title}\" by ${song.artist}")
                            putExtra(Intent.EXTRA_SUBJECT, "Sharing ${song.title}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                    showOptionsSheet = false
                },
                onViewArtist = { 
                    // Can navigate to artist detail
                    showOptionsSheet = false
                },
                onViewAlbum = {
                    // Can navigate to album detail
                    showOptionsSheet = false 
                },
                onAddToPlaylist = { 
                    showOptionsSheet = false
                    showAddToPlaylistSheet = true 
                },
                onSleepTimer = { 
                    showOptionsSheet = false
                    showSleepTimerDialog = true 
                },
                onEqualizer = { 
                    // Launch system equalizer
                    showOptionsSheet = false
                },
                onPlaybackSpeed = {
                    showOptionsSheet = false
                    showSpeedDialog = true
                }
            )
        }
        
        // Add to Playlist Bottom Sheet
        if (showAddToPlaylistSheet) {
            AddToPlaylistBottomSheet(
                playerViewModel = playerViewModel,
                currentSong = currentSong,
                onDismiss = { showAddToPlaylistSheet = false },
                onCreatePlaylist = { showCreatePlaylistDialog = true }
            )
        }
        
        // Create Playlist Dialog
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name, desc ->
                    playerViewModel.createPlaylist(name, desc)
                    showCreatePlaylistDialog = false
                    showAddToPlaylistSheet = true
                }
            )
        }
        
        // Song Info Dialog
        if (showSongInfoDialog && currentSong != null) {
            SongInfoDialog(
                song = currentSong,
                onDismiss = { showSongInfoDialog = false }
            )
        }
        
        // Sleep Timer Dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                onDismiss = { showSleepTimerDialog = false },
                onSetTimer = { minutes ->
                    playerViewModel.setSleepTimer(minutes)
                    showSleepTimerDialog = false
                }
            )
        }
        
        // Playback Speed Dialog
        if (showSpeedDialog) {
            PlaybackSpeedDialog(
                onDismiss = { showSpeedDialog = false },
                onSetSpeed = { speed ->
                    playerViewModel.setPlaybackSpeed(speed)
                    showSpeedDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueBottomSheet(
    currentSong: Song?,
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSongClick: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    var endlessQueue by remember { mutableStateOf(true) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            // Header
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            
            currentSong?.let { song ->
                QueueSongItem(
                    song = song,
                    isPlaying = true,
                    showEqualizer = true,
                    onClick = {},
                    onMoreClick = {}
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Queue Section with Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Queue (${queue.size} songs)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Similar Songs Button
                    TextButton(
                        onClick = { 
                            // Similar songs logic
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ProgressActiveColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Get Similar Songs",
                            style = MaterialTheme.typography.labelMedium,
                            color = ProgressActiveColor
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Endless",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = endlessQueue,
                        onCheckedChange = { endlessQueue = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ProgressActiveColor,
                            checkedTrackColor = ProgressActiveColor.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Queue List
            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Queue is empty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add songs to queue to see them here",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    itemsIndexed(queue) { index, song ->
                        QueueSongItem(
                            song = song,
                            isPlaying = index == currentIndex,
                            showEqualizer = index == currentIndex,
                            onClick = { onSongClick(index) },
                            onMoreClick = { onRemove(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueSongItem(
    song: Song,
    isPlaying: Boolean,
    showEqualizer: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isPlaying) ProgressActiveColor.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail or Equalizer
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardColor),
            contentAlignment = Alignment.Center
        ) {
            if (showEqualizer) {
                EqualizerAnimation()
            } else if (song.artworkUrl != null) {
                AsyncImage(
                    model = song.artworkUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isPlaying) ProgressActiveColor else TextPrimary,
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
        }
        
        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove from queue",
                tint = TextSecondary
            )
        }
    }
}

@Composable
private fun EqualizerAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    
    Row(
        modifier = Modifier.size(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(bar1)
                .clip(RoundedCornerShape(2.dp))
                .background(ProgressActiveColor)
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(bar2)
                .clip(RoundedCornerShape(2.dp))
                .background(ProgressActiveColor)
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight(bar3)
                .clip(RoundedCornerShape(2.dp))
                .background(ProgressActiveColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionsBottomSheet(
    song: Song?,
    playerViewModel: PlayerViewModel,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onViewArtist: () -> Unit,
    onViewAlbum: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onSleepTimer: () -> Unit,
    onEqualizer: () -> Unit,
    onPlaybackSpeed: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Song header
            song?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardColor)
                    ) {
                        if (it.artworkUrl != null) {
                            AsyncImage(
                                model = it.artworkUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = it.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                HorizontalDivider(color = CardColor)
            }
            
            // Options
            OptionItem(icon = Icons.Outlined.Download, title = "Download", onClick = onDownload)
            OptionItem(icon = Icons.AutoMirrored.Outlined.PlaylistAdd, title = "Add to Playlist", onClick = onAddToPlaylist)
            OptionItem(icon = Icons.Outlined.Person, title = "View Artist", onClick = onViewArtist)
            OptionItem(icon = Icons.Outlined.Album, title = "View Album", onClick = onViewAlbum)
            OptionItem(icon = Icons.Outlined.Share, title = "Share", onClick = onShare)
            OptionItem(icon = Icons.Outlined.Timer, title = "Sleep Timer", onClick = onSleepTimer)
            OptionItem(icon = Icons.Outlined.Speed, title = "Playback Speed", onClick = onPlaybackSpeed)
            OptionItem(icon = Icons.Outlined.Equalizer, title = "Equalizer", onClick = onEqualizer)
            
            // Like/Unlike option
            song?.let {
                val isLiked = playerViewModel.uiState.value.isLiked
                OptionItem(
                    icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    title = if (isLiked) "Remove from Liked" else "Add to Liked",
                    onClick = { 
                        playerViewModel.toggleLike()
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun OptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
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
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistBottomSheet(
    playerViewModel: PlayerViewModel,
    currentSong: Song?,
    onDismiss: () -> Unit,
    onCreatePlaylist: () -> Unit
) {
    val playlists = playerViewModel.uiState.value.userPlaylists
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onCreatePlaylist) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = ProgressActiveColor
                    )
                }
            }
            
            HorizontalDivider(color = CardColor)
            
            // Playlists List
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No playlists yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreatePlaylist,
                            colors = ButtonDefaults.buttonColors(containerColor = ProgressActiveColor)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Playlist")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSong?.let { song ->
                                        playerViewModel.addToPlaylist(playlist.id)
                                    }
                                    onDismiss()
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardColor),
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
                                        Icons.AutoMirrored.Default.PlaylistPlay,
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
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }
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
        title = { Text("Create Playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProgressActiveColor,
                        cursorColor = ProgressActiveColor
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProgressActiveColor,
                        cursorColor = ProgressActiveColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name, description.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ProgressActiveColor)
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
private fun SongInfoDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { 
            Text(
                "Song Information", 
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Song artwork
                if (song.artworkUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = song.getHighQualityArtwork(),
                            contentDescription = "Song artwork",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // === BASIC INFO SECTION ===
                SectionHeader("Basic Information")
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow("Title", song.title)
                InfoRow("Artist/Singer", if (song.artist.isNotBlank()) song.artist else "Unknown")
                InfoRow(
                    "Playlist",
                    song.extras["playlistName"]
                        ?: song.extras["playlist"]
                        ?: "Unknown"
                )
                
                if (song.album.isNotBlank()) {
                    InfoRow("Album", song.album)
                } else {
                    InfoRow("Album", "Unknown")
                }

                InfoRow(
                    "Movie",
                    song.movieName.takeIf { it.isNotBlank() }
                        ?: song.extras["movieName"]
                        ?: song.extras["movie"]
                        ?: "Unknown"
                )

                InfoRow(
                    "Genre (Category)",
                    song.genre.takeIf { it.isNotBlank() }
                        ?: song.extras["category"]
                        ?: song.extras["genre"]
                        ?: "Unknown"
                )
                
                val durationText = if (song.duration > 0) {
                    val minutes = song.duration / 60
                    val seconds = song.duration % 60
                    String.format("%d:%02d", minutes, seconds)
                } else "Unknown"
                InfoRow("Duration", durationText)
                
                if (song.language.isNotBlank()) {
                    InfoRow("Language", song.language)
                }
                
                if (song.genre.isNotBlank()) {
                    InfoRow("Genre", song.genre)
                }
                
                if (song.year.isNotBlank()) {
                    InfoRow("Year", song.year)
                } else if (song.releaseDate.isNotBlank()) {
                    InfoRow("Release Date", song.releaseDate)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // === MOVIE/FILM INFO SECTION ===
                if (song.movieName.isNotBlank() || song.heroName.isNotBlank() || 
                    song.heroineName.isNotBlank() || song.director.isNotBlank() || 
                    song.producer.isNotBlank() || song.movieGenre.isNotBlank()) {
                    
                    SectionHeader("Movie/Film Information")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (song.movieName.isNotBlank()) {
                        InfoRow("Movie Name", song.movieName)
                    }
                    
                    if (song.movieGenre.isNotBlank()) {
                        InfoRow("Movie Genre", song.movieGenre)
                    }
                    
                    if (song.heroName.isNotBlank()) {
                        InfoRow("Hero/Lead Actor", song.heroName)
                    }
                    
                    if (song.heroineName.isNotBlank()) {
                        InfoRow("Heroine/Lead Actress", song.heroineName)
                    }
                    
                    if (song.director.isNotBlank()) {
                        InfoRow("Director", song.director)
                    }
                    
                    if (song.producer.isNotBlank()) {
                        InfoRow("Producer", song.producer)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // === YOUTUBE/CHANNEL INFO SECTION ===
                if (song.channelName.isNotBlank() || song.viewCount > 0 || 
                    song.likeCount > 0 || song.channelSubscriberCount > 0) {
                    
                    SectionHeader("YouTube/Channel Information")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (song.channelName.isNotBlank()) {
                        InfoRow("Channel", song.channelName)
                    }
                    
                    if (song.channelId.isNotBlank()) {
                        InfoRow("Channel ID", song.channelId.take(20) + "...")
                    }
                    
                    if (song.channelSubscriberCount > 0) {
                        InfoRow("Channel Subscribers", formatCount(song.channelSubscriberCount))
                    }
                    
                    if (song.viewCount > 0) {
                        InfoRow("View Count", formatCount(song.viewCount) + " views")
                    }
                    
                    if (song.likeCount > 0) {
                        InfoRow("Likes", formatCount(song.likeCount))
                    }
                    
                    if (song.uploadDate.isNotBlank()) {
                        InfoRow("Published On", song.uploadDate)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // === QUALITY & TECHNICAL INFO SECTION ===
                SectionHeader("Quality & Technical Details")
                Spacer(modifier = Modifier.height(8.dp))
                
                if (song.quality.isNotBlank()) {
                    InfoRow("Video Quality", song.quality)
                }
                
                if (song.is320kbps) {
                    InfoRow("Audio Quality", "320 kbps (High Quality)")
                } else {
                    InfoRow("Audio Quality", "Standard")
                }
                
                if (song.source.isNotBlank()) {
                    InfoRow("Source/Platform", song.source.replaceFirstChar { it.uppercase() })
                }
                
                if (song.type.isNotBlank()) {
                    InfoRow("Type", song.type)
                }
                
                if (song.hasLyrics) {
                    InfoRow("Lyrics", "Available")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // === DESCRIPTION SECTION ===
                SectionHeader("Description")
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = song.description.takeIf { it.isNotBlank() } ?: "Not available",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // === IDs & LINKS SECTION ===
                SectionHeader("IDs & Links")
                Spacer(modifier = Modifier.height(8.dp))
                
                if (song.id.isNotBlank()) {
                    InfoRow("Song/Video ID", song.id)
                }
                
                song.albumId?.takeIf { it.isNotBlank() }?.let { albumId ->
                    InfoRow("Album ID", albumId)
                }
                
                song.permaUrl?.takeIf { it.isNotBlank() }?.let { permaUrl ->
                    InfoRow("Permalink", permaUrl.take(40) + "...")
                }
                
                song.streamUrl?.takeIf { it.isNotBlank() }?.let {
                    InfoRow("Stream URL", "Available")
                }
                
                // Extra metadata
                if (song.extras.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader("Additional Metadata")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    song.extras.forEach { (key, value) ->
                        if (value.isNotBlank()) {
                            InfoRow(key.replaceFirstChar { it.uppercase() }, value)
                        }
                    }
                }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ProgressActiveColor)
            ) {
                Text("Close")
            }
        }
    )
}

// Helper composable for section headers
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = AccentRed,
        modifier = Modifier.padding(top = 4.dp)
    )
}

// Helper function to format large numbers (YouTube style)
private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format("%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
private fun SleepTimerDialog(
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep Timer", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                listOf(
                    5 to "5 minutes",
                    10 to "10 minutes",
                    15 to "15 minutes",
                    30 to "30 minutes",
                    45 to "45 minutes",
                    60 to "1 hour"
                ).forEach { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetTimer(minutes) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun PlaybackSpeedDialog(
    onDismiss: () -> Unit,
    onSetSpeed: (Float) -> Unit
) {
    var selectedSpeed by remember { mutableStateOf(1.0f) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Speed", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                selectedSpeed = speed
                                onSetSpeed(speed)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSpeed == speed,
                            onClick = { 
                                selectedSpeed = speed
                                onSetSpeed(speed)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = ProgressActiveColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = if (speed == 1.0f) FontWeight.Bold else FontWeight.Normal
                        )
                        if (speed == 1.0f) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Normal)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}
