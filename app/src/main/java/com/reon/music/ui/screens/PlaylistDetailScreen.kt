/*
 * REON Music App - Playlist Detail Screen
 * Copyright (c) 2024 REON
 * Editorial playlist/album experience with dynamic YouTube content.
 */

package com.reon.music.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.data.database.entities.PlaylistEntity
import com.reon.music.ui.components.PlaylistOptionsSheet
import com.reon.music.ui.components.SongOptionsSheet
import com.reon.music.ui.viewmodels.LibraryViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PageBg = Color(0xFFFFF7F1)
private val HeroCardTop = Color(0xFFFFE4D4)
private val HeroCardBottom = Color(0xFFFFCDB8)
private val AccentOrange = Color(0xFFFF6B35)
private val AccentOrangeDark = Color(0xFFE8551F)
private val TextPrimary = Color(0xFF2A1E17)
private val TextSecondary = Color(0xFF8A7B70)
private val CardWhite = Color(0xFFFFFFFF)
private val PlayingRowBg = Color(0xFFFFE9DC)

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
    var youtubePlaylist by remember { mutableStateOf<com.reon.music.core.model.Playlist?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    // Editorial UI state
    var isPlaylistLiked by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(true) }
    var likedSongIds by remember { mutableStateOf(emptySet<String>()) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("Custom") }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var selectedChip by remember { mutableStateOf("All") }
    var autoEnhanceOn by remember { mutableStateOf(false) }

    val playlistIdLong = playlistId.toLongOrNull()
    val isRemote = playlistIdLong == null

    LaunchedEffect(playlistId) {
        isLoading = true
        loadError = null
        if (!isRemote) {
            playlistSongs = libraryViewModel.getPlaylistSongs(playlistIdLong!!)
            youtubePlaylist = null
            isLoading = false
        } else {
            // Dynamic YouTube playlist via InnerTube browse (real, latest tracks).
            val remote = libraryViewModel.getYouTubePlaylist(playlistId)
            if (remote != null) {
                youtubePlaylist = remote
                playlistSongs = remote.songs
            } else {
                loadError = "Couldn't load playlist. Check your connection and retry."
            }
            isLoading = false
        }
    }

    val displayName = when {
        isRemote -> youtubePlaylist?.name?.takeIf { it.isNotBlank() } ?: playlistTitle
        else -> playlistEntity?.title ?: playlistTitle
    }
    val displayDesc = when {
        isRemote -> youtubePlaylist?.description?.takeIf { it.isNotBlank() }
            ?: "Warm acoustic guitars & gentle sunrise melodies to begin your day in harmony."
        else -> playlistEntity?.description?.takeIf { it.isNotBlank() }
            ?: "Your personal collection, refreshed daily."
    }
    val displayArt = when {
        isRemote -> youtubePlaylist?.artworkUrl
        else -> playlistEntity?.thumbnailUrl
    }

    // Filter chips: All + Most Played + top artists in this playlist.
    val chipOptions = remember(playlistSongs) {
        val artists = playlistSongs.map { it.artist.trim() }
            .filter { it.isNotBlank() && !it.equals("Unknown Artist", true) }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(3).map { it.key }
        listOf("All", "Most Played") + artists
    }
    if (selectedChip !in chipOptions) selectedChip = "All"

    val visibleSongs = remember(playlistSongs, searchQuery, sortMode, selectedChip) {
        var list = playlistSongs
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            list = list.filter {
                it.title.contains(q, true) || it.artist.contains(q, true)
            }
        }
        list = when (selectedChip) {
            "Most Played" -> list.sortedByDescending { it.viewCount }
            "All" -> list
            else -> list.filter { it.artist.equals(selectedChip, true) }
        }
        when (sortMode) {
            "Most Played" -> list.sortedByDescending { it.viewCount }
            "A–Z" -> list.sortedBy { it.title.lowercase() }
            else -> list
        }
    }

    val totalSeconds = playlistSongs.sumOf { it.duration }
    val totalPlays = playlistSongs.sumOf { it.viewCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRemote) "Album Details" else "Playlist Details",
                        style = MaterialTheme.typography.titleMedium,
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
                    if (!isRemote && playlistEntity != null) {
                        IconButton(onClick = { showPlaylistOptions = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
                        }
                    } else {
                        IconButton(onClick = { /* overflow */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = PageBg
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(PageBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentOrange)
            }
            return@Scaffold
        }

        if (playlistSongs.isEmpty() && !isRemote && playlistEntity == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(PageBg),
                contentAlignment = Alignment.Center
            ) {
                Text("Playlist not found", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
            return@Scaffold
        }

        if (playlistSongs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).background(PageBg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.AutoMirrored.Outlined.PlaylistPlay, null,
                        modifier = Modifier.size(80.dp), tint = TextSecondary.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        loadError ?: "This playlist is empty",
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(PageBg),
            contentPadding = PaddingValues(
                top = 4.dp, bottom = 100.dp, start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ---- Status pill + quick actions ----
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UpdatedPill()
                    Spacer(Modifier.weight(1f))
                    CircleAction(
                        filled = isDownloaded,
                        onClick = { isDownloaded = !isDownloaded }
                    ) {
                        Icon(
                            if (isDownloaded) Icons.Default.Check else Icons.Default.Download,
                            contentDescription = "Download",
                            tint = if (isDownloaded) Color.White else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    CircleAction(filled = false, onClick = { isPlaylistLiked = !isPlaylistLiked }) {
                        Icon(
                            if (isPlaylistLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isPlaylistLiked) AccentOrange else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    CircleAction(filled = false, onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---- Hero card ----
            item {
                HeroCard(
                    title = displayName,
                    description = displayDesc,
                    artworkUrl = displayArt,
                    trackCount = playlistSongs.size,
                    totalSeconds = totalSeconds,
                    totalPlays = totalPlays,
                    artists = playlistSongs.map { it.artist }.filter { it.isNotBlank() }.distinct().take(2),
                    onPlayAll = { playerViewModel.playQueue(visibleSongs.ifEmpty { playlistSongs }, startIndex = 0) },
                    onShuffle = { playerViewModel.playQueue((visibleSongs.ifEmpty { playlistSongs }).shuffled(), startIndex = 0) }
                )
                Spacer(Modifier.height(20.dp))
            }

            // ---- Tracks header ----
            item {
                TracksHeader(
                    count = visibleSongs.size,
                    showSearch = showSearch,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onToggleSearch = { showSearch = !showSearch; if (!showSearch) searchQuery = "" },
                    sortMode = sortMode,
                    sortMenuOpen = sortMenuOpen,
                    onSortMenuChange = { sortMenuOpen = it },
                    onSortPicked = { sortMode = it; sortMenuOpen = false },
                    chips = chipOptions,
                    selectedChip = selectedChip,
                    totalCount = playlistSongs.size,
                    onChipSelected = { selectedChip = it }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ---- Track rows ----
            itemsIndexed(visibleSongs, key = { _, s -> s.id }) { index, song ->
                val playing = playerState.currentSong?.id == song.id
                EditorialTrackRow(
                    song = song,
                    index = index,
                    isPlaying = playing,
                    isLiked = song.id in likedSongIds,
                    onLike = {
                        likedSongIds = if (song.id in likedSongIds) likedSongIds - song.id else likedSongIds + song.id
                    },
                    onClick = {
                        val start = visibleSongs.indexOf(song).coerceAtLeast(0)
                        playerViewModel.playQueue(visibleSongs, startIndex = start)
                        onSongClick(song)
                    },
                    onMoreClick = { selectedSong = song; showSongOptions = true }
                )
                Spacer(Modifier.height(4.dp))
            }

            // ---- Auto-enhance card ----
            item {
                Spacer(Modifier.height(12.dp))
                AutoEnhanceCard(enabled = autoEnhanceOn, onToggle = { autoEnhanceOn = !autoEnhanceOn })
            }
        }
    }

    // Song Options Sheet
    if (showSongOptions && selectedSong != null) {
        SongOptionsSheet(
            song = selectedSong!!,
            showRemoveFromPlaylist = !isRemote,
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
            onShare = { showSongOptions = false }
        )
    }

    // Playlist Options Sheet (local only)
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
            onDownloadAll = { showPlaylistOptions = false },
            onEdit = { showEditDialog = true; showPlaylistOptions = false },
            onDelete = {
                libraryViewModel.deletePlaylist(playlistEntity)
                showPlaylistOptions = false
                onBackClick()
            },
            onShare = { showPlaylistOptions = false }
        )
    }

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

// ---------- Editorial components ----------

@Composable
private fun UpdatedPill() {
    val time = remember {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardWhite)
            .border(1.dp, Color(0xFFF0E4D8), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(AccentOrange))
        Spacer(Modifier.width(8.dp))
        Text("Updated today, $time", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun CircleAction(filled: Boolean, onClick: () -> Unit, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (filled) AccentOrange else CardWhite)
            .border(1.dp, if (filled) AccentOrange else Color(0xFFF0E4D8), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun HeroCard(
    title: String,
    description: String,
    artworkUrl: String?,
    trackCount: Int,
    totalSeconds: Int,
    totalPlays: Long,
    artists: List<String>,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(HeroCardTop, HeroCardBottom)))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artwork with lossless badge
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .shadow(16.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFD9C4B2)),
            contentAlignment = Alignment.Center
        ) {
            if (artworkUrl != null) {
                AsyncImage(
                    model = artworkUrl, contentDescription = title,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(64.dp))
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.GraphicEq, null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "HI-RES LOSSLESS", color = Color.White,
                    fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CardWhite.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(18.dp).clip(CircleShape).background(AccentOrange),
                contentAlignment = Alignment.Center
            ) {
                Text("R", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "REON Editorial • Curated by Anand V.",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            title, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold, color = TextPrimary, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary.copy(0.75f),
            textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VideoLibrary, null, tint = TextPrimary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text("$trackCount tracks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("  •  ", fontSize = 12.sp, color = TextSecondary)
            Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(4.dp))
            Text(formatTotalDuration(totalSeconds), fontSize = 12.sp, color = TextSecondary)
            if (totalPlays > 0) {
                Text("  •  ", fontSize = 12.sp, color = TextSecondary)
                Icon(Icons.Default.Bolt, null, tint = AccentOrangeDark, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(2.dp))
                Text("${formatCompact(totalPlays)} saves", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentOrangeDark)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CardWhite.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverlappingAvatars()
            Spacer(Modifier.width(8.dp))
            Text(
                likedByText(artists),
                fontSize = 12.sp, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onShuffle,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0E4D8))
            ) {
                Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Shuffle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Button(
                onClick = onPlayAll,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange, contentColor = Color.White)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Text("Play All", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun OverlappingAvatars() {
    val colors = listOf(Color(0xFF7C4DFF), Color(0xFFFF6B35), Color(0xFF26A69A))
    Box(modifier = Modifier.width(52.dp), contentAlignment = Alignment.CenterStart) {
        colors.forEachIndexed { i, c ->
            Box(
                modifier = Modifier
                    .offset(x = (i * 16).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(c)
                    .border(2.dp, CardWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    listOf("R", "M", "A")[i], color = Color.White,
                    fontSize = 10.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun likedByText(artists: List<String>): String {
    val a = artists.map { it.substringBefore("&").trim().substringBefore(",").trim() }
        .filter { it.isNotBlank() }.distinct().take(2)
    return if (a.size >= 2) "Liked by ${a[0]}, ${a[1]} and others"
    else "Liked by Rohan, Maya and 1.4k others"
}

@Composable
private fun TracksHeader(
    count: Int,
    showSearch: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    sortMode: String,
    sortMenuOpen: Boolean,
    onSortMenuChange: (Boolean) -> Unit,
    onSortPicked: (String) -> Unit,
    chips: List<String>,
    selectedChip: String,
    totalCount: Int,
    onChipSelected: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Tracks", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Spacer(Modifier.width(6.dp))
        Text("($count)", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onToggleSearch, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Search, contentDescription = "Search tracks", tint = TextPrimary)
        }
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardWhite)
                    .border(1.dp, Color(0xFFF0E4D8), RoundedCornerShape(20.dp))
                    .clickable { onSortMenuChange(true) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(sortMode, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { onSortMenuChange(false) }) {
                listOf("Custom", "Most Played", "A–Z").forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = { onSortPicked(mode) }
                    )
                }
            }
        }
    }

    if (showSearch) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query, onValueChange = onQueryChange,
            placeholder = { Text("Search in playlist") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true, shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardWhite, unfocusedContainerColor = CardWhite,
                focusedBorderColor = AccentOrange, cursorColor = AccentOrange
            )
        )
    }

    Spacer(Modifier.height(10.dp))
    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chips.size) { i ->
            val chip = chips[i]
            val selected = chip == selectedChip
            val label = if (chip == "All") "All ($totalCount)" else chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) AccentOrange else CardWhite)
                    .border(1.dp, if (selected) AccentOrange else Color(0xFFF0E4D8), RoundedCornerShape(20.dp))
                    .clickable { onChipSelected(chip) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else TextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun EditorialTrackRow(
    song: Song,
    index: Int,
    isPlaying: Boolean,
    isLiked: Boolean,
    onLike: () -> Unit,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPlaying) PlayingRowBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(30.dp), contentAlignment = Alignment.Center) {
            if (isPlaying) PlayingBars() else Text(
                "%02d".format(index + 1),
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary
            )
        }
        Box(
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8DCD2)),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUrl != null) {
                AsyncImage(
                    model = song.getHighQualityArtwork() ?: song.artworkUrl,
                    contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.MusicNote, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    song.title, fontSize = 15.sp,
                    fontWeight = if (isPlaying) FontWeight.ExtraBold else FontWeight.Bold,
                    color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "LOSSLESS",
                    fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp, color = AccentOrangeDark,
                    modifier = Modifier
                        .border(1.dp, AccentOrangeDark.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    song.formattedDuration(), fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) AccentOrangeDark else TextPrimary
                )
            }
            Text(
                song.artist.ifBlank { "Unknown Artist" },
                fontSize = 12.sp, color = TextSecondary,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
            Icon(
                if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) AccentOrange else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Icon(Icons.Default.DragHandle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        IconButton(onClick = onMoreClick, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
        }
    }
}

@Composable
private fun PlayingBars() {
    val transition = rememberInfiniteTransition(label = "bars")
    Row(
        modifier = Modifier.height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(4) { i ->
            val h by transition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500 + i * 130, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )
            Box(
                Modifier.width(3.5.dp).fillMaxHeight(h).clip(RoundedCornerShape(2.dp)).background(AccentOrange)
            )
        }
    }
}

@Composable
private fun AutoEnhanceCard(enabled: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardWhite)
            .border(1.dp, Color(0xFFF0E4D8), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFEDE4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, null, tint = AccentOrange, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Auto-enhance Playlist", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
            Text(
                if (enabled) "Adding similar tracks to your vibe" else "Add similar acoustic tracks dynamically",
                fontSize = 12.sp, color = TextSecondary
            )
        }
        Button(
            onClick = onToggle,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color(0xFF2E7D32) else AccentOrange,
                contentColor = Color.White
            )
        ) {
            Text(if (enabled) "On" else "Enable", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ---------- helpers ----------

private fun formatTotalDuration(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "–"
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

private fun formatCompact(n: Long): String = when {
    n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fk".format(n / 1_000.0)
    else -> "$n"
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
            Text("This playlist is empty", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Add songs from the search or library", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = CardWhite
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
                    value = name, onValueChange = { name = it },
                    label = { Text("Playlist name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, cursorColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, cursorColor = AccentOrange)
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
                colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
        containerColor = CardWhite
    )
}
