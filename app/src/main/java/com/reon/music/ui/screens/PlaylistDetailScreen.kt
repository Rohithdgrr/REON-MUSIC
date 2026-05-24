/*
 * REON Music App - Playlist Detail Screen
 * Copyright (c) 2024 REON
 * Displays playlist songs from local Room database
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
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
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.data.database.entities.PlaylistEntity
import com.reon.music.ui.components.PlaylistOptionsSheet
import com.reon.music.ui.components.SongOptionsSheet
import com.reon.music.ui.viewmodels.LibraryViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import com.reon.music.playback.PlayerState

private val BackgroundLight = Color(0xFFF5F0FF)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playlistTitle: String,
    onBackClick: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by libraryViewModel.uiState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()

    val playlistEntity = remember(uiState.playlists, playlistId) {
        val id = playlistId.toLongOrNull()
        if (id != null) uiState.playlists.find { it.id == id }
        else null
    }

    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSongOptions by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var showPlaylistOptions by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val playlistIdLong = playlistId.toLongOrNull()

    LaunchedEffect(playlistIdLong) {
        if (playlistIdLong != null) {
            isLoading = true
            playlistSongs = libraryViewModel.getPlaylistSongs(playlistIdLong)
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlistTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    if (playlistEntity != null) {
                        IconButton(onClick = { showPlaylistOptions = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundLight)
        ) {
            if (playlistIdLong == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "YouTube playlists not available offline",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                return@Scaffold
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
                return@Scaffold
            }

            if (playlistEntity == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Playlist not found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                return@Scaffold
            }

            if (playlistSongs.isEmpty()) {
                EmptyPlaylistContent(onBackClick, playlistEntity, libraryViewModel, playerViewModel)
                return@Scaffold
            }

            // Playlist Header
            PlaylistHeader(
                playlist = playlistEntity,
                songs = playlistSongs,
                playerViewModel = playerViewModel,
                onPlayAll = {
                    playerViewModel.playQueue(playlistSongs, startIndex = 0)
                },
                onShuffle = {
                    playerViewModel.playQueue(playlistSongs.shuffled(), startIndex = 0)
                }
            )

            // Song list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(playlistSongs) { index, song ->
                    SongRow(
                        song = song,
                        index = index,
                        isPlaying = playerState.currentSong?.id == song.id,
                        onClick = { onSongClick(song) },
                        onMoreClick = {
                            selectedSong = song
                            showSongOptions = true
                        }
                    )
                }
            }
        }
    }

    // Song Options Sheet
    if (showSongOptions && selectedSong != null) {
        SongOptionsSheet(
            song = selectedSong!!,
            showRemoveFromPlaylist = true,
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
            onAddToPlaylist = {
                showAddToPlaylistDialog = true
                showSongOptions = false
            },
            onRemoveFromPlaylist = {
                if (playlistIdLong != null) {
                    libraryViewModel.removeFromPlaylist(playlistIdLong, selectedSong!!.id)
                    playlistSongs = playlistSongs.filter { it.id != selectedSong!!.id }
                }
                showSongOptions = false
            },
            onShare = {
                showSongOptions = false
            }
        )
    }

    // Playlist Options Sheet
    if (showPlaylistOptions && playlistEntity != null) {
        PlaylistOptionsSheet(
            playlist = playlistEntity,
            showEditOption = true,
            showDeleteOption = true,
            onDismiss = { showPlaylistOptions = false },
            onPlay = {
                libraryViewModel.playPlaylist(playlistEntity.id, playerViewModel, shuffle = false)
                showPlaylistOptions = false
            },
            onShuffle = {
                libraryViewModel.playPlaylist(playlistEntity.id, playerViewModel, shuffle = true)
                showPlaylistOptions = false
            },
            onAddToQueue = {
                libraryViewModel.addPlaylistToQueue(playlistEntity.id, playerViewModel)
                showPlaylistOptions = false
            },
            onDownloadAll = {
                showPlaylistOptions = false
            },
            onEdit = {
                showEditDialog = true
                showPlaylistOptions = false
            },
            onDelete = {
                libraryViewModel.deletePlaylist(playlistEntity)
                showPlaylistOptions = false
                onBackClick()
            },
            onShare = {
                showPlaylistOptions = false
            }
        )
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog && selectedSong != null) {
        AddToPlaylistDialog(
            playlists = uiState.playlists.filter { it.id != playlistIdLong },
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { targetPlaylist ->
                libraryViewModel.addToPlaylist(targetPlaylist.id, selectedSong!!)
                showAddToPlaylistDialog = false
            }
        )
    }

    // Edit Playlist Dialog
    if (showEditDialog && playlistEntity != null) {
        EditPlaylistDialog(
            currentName = playlistEntity.title,
            currentDescription = playlistEntity.description ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { newName, newDesc ->
                libraryViewModel.updatePlaylist(playlistEntity.id, newName, newDesc)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EmptyPlaylistContent(
    onBackClick: () -> Unit,
    playlist: PlaylistEntity,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = TextSecondary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This playlist is empty",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add songs from the search or library",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: PlaylistEntity,
    songs: List<Song>,
    playerViewModel: PlayerViewModel,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentPurple.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                if (playlist.thumbnailUrl != null) {
                    AsyncImage(
                        model = playlist.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                val desc = playlist.description
                if (desc != null) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${songs.size} song${if (songs.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPlayAll,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play All")
            }
            OutlinedButton(
                onClick = onShuffle,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Shuffle")
            }
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    index: Int,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.width(24.dp)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceWhite)
        ) {
            if (song.artworkUrl != null) {
                AsyncImage(
                    model = song.getHighQualityArtwork() ?: song.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxSize()
                )
            }
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AccentPurple.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
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
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary)
        }
    }
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
                Text("No other playlists found.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    itemsIndexed(playlists) { _, playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaylistSelected(playlist) }
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(playlist.title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color(0xFFFFFFFF)
    )
}

@Composable
private fun EditPlaylistDialog(
    currentName: String,
    currentDescription: String,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var description by remember { mutableStateOf(currentDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Playlist", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, cursorColor = AccentPurple)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, cursorColor = AccentPurple)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, description.takeIf { it.isNotBlank() })
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = Color(0xFFFFFFFF)
    )
}
