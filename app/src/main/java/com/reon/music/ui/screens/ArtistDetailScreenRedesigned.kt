/*
 * REON Music App - Modern Artist Detail Screen (Redesigned)
 * Copyright (c) 2024 REON
 * Claymorphism Design with Real Artist Images
 */

package com.reon.music.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.reon.music.core.model.Album
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Playlist
import com.reon.music.core.model.Song
import com.reon.music.data.network.youtube.IndianMusicChannels
import com.reon.music.ui.viewmodels.HomeViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import com.reon.music.ui.components.EnhancedSongInformationDialog

// Claymorphism Color Palette
private val ClayBackground = Color(0xFFF5F5F0)
private val ClayCardLight = Color(0xFFFFFFFF)
private val ClayCardDark = Color(0xFFE8E8E3)
private val ClayShadowLight = Color(0xFFFFFFFF)
private val ClayShadowDark = Color(0xFFC0C0B8)
private val TextPrimary = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFF6B6B6B)
private val AccentRed = Color(0xFFE74C3C)
private val AccentBlue = Color(0xFF3498DB)
private val AccentGold = Color(0xFFF39C12)
private val AccentPurple = Color(0xFF9B59B6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreenRedesigned(
    artist: Artist,
    onBackClick: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onNavigateToArtistPage: (String) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // States
    var isFollowing by remember { mutableStateOf(false) }
    var followerCount by remember { mutableStateOf(artist.followerCount) }
    var artistImageUrl by remember { mutableStateOf(artist.artworkUrl) }
    var artistBannerUrl by remember { mutableStateOf<String?>(null) }
    
    // Song Info Dialog State
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var selectedSongForInfo by remember { mutableStateOf<Song?>(null) }
    
    // Claymorphism animation
    var isHeaderExpanded by remember { mutableStateOf(true) }
    val headerHeight by animateDpAsState(
        targetValue = if (isHeaderExpanded) 380.dp else 200.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )
    
    // Check if artist is in Top 500
    val isTop500 = IndianMusicChannels.isPriorityChannel(artist.name)
    val rank = IndianMusicChannels.getChannelByName(artist.name)?.rank
    
    // Fetch real images
    LaunchedEffect(artist.name) {
        try {
            val searchResults = homeViewModel.searchSongsForChart("${artist.name} official channel", 5)
            val channelSong = searchResults.firstOrNull { 
                it.artist.contains(artist.name, ignoreCase = true) || 
                it.channelName.contains(artist.name, ignoreCase = true)
            }
            channelSong?.let { song ->
                if (artistImageUrl == null) artistImageUrl = song.artworkUrl
                artistBannerUrl = song.getHighQualityArtwork()
            }
        } catch (e: Exception) { }
    }
    
    // Load follow state
    LaunchedEffect(artist.id) {
        val prefs = context.getSharedPreferences("artist_follows", android.content.Context.MODE_PRIVATE)
        isFollowing = prefs.getBoolean("follow_${artist.id}", false)
        followerCount = prefs.getInt("followers_${artist.id}", artist.followerCount)
    }
    
    // Songs
    var allArtistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var artistAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var movieSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var concertSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var artistPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }

    val tabTitles = remember {
        listOf("Songs", "Albums", "Movie Songs", "Concert")
    }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(artist.name) {
        val initialSongs = (uiState.quickPicksSongs + uiState.newReleases + uiState.hindiSongs +
            uiState.teluguSongs + uiState.englishSongs + uiState.tamilSongs + uiState.punjabiSongs)
            .filter { it.artist.contains(artist.name, ignoreCase = true) }
            .distinctBy { it.id }

        val fetchedSongs = try {
            homeViewModel.searchSongsForChart("${artist.name} songs", 120)
                .filter { it.artist.contains(artist.name, ignoreCase = true) }
        } catch (e: Exception) {
            emptyList()
        }

        val merged = (initialSongs + fetchedSongs).distinctBy { it.id }
        allArtistSongs = merged

        movieSongs = merged.filter { song ->
            song.movieName.isNotBlank() ||
                song.extras["movieName"]?.isNotBlank() == true
        }

        concertSongs = merged.filter { song ->
            val t = song.title.lowercase()
            val d = song.description.lowercase()
            t.contains("live") || t.contains("concert") || t.contains("performance") ||
                d.contains("live") || d.contains("concert")
        }

        artistAlbums = merged
            .filter { it.album.isNotBlank() }
            .groupBy { it.album.trim().lowercase() }
            .mapNotNull { (key, songs) ->
                if (key.isBlank() || songs.isEmpty()) return@mapNotNull null
                val rep = songs.first()
                Album(
                    id = key.hashCode().toString(),
                    name = rep.album,
                    artist = artist.name,
                    artworkUrl = rep.artworkUrl,
                    songCount = songs.size,
                    songs = songs
                )
            }
            .sortedByDescending { it.songCount }

        try {
            artistPlaylists = homeViewModel.searchPlaylistsForArtist(artist.name)
        } catch (e: Exception) { }
    }
    
    val scrollState = rememberLazyListState()
    
    // Collapse header on scroll
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemScrollOffset }
            .collect { offset ->
                isHeaderExpanded = offset < 100
            }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Claymorphism Header with Parallax
            item {
                ClayArtistHeader(
                    artist = artist,
                    artistImageUrl = artistImageUrl,
                    artistBannerUrl = artistBannerUrl,
                    isFollowing = isFollowing,
                    followerCount = followerCount,
                    isTop500 = isTop500,
                    rank = rank,
                    headerHeight = headerHeight,
                    onBackClick = onBackClick,
                    onFollowClick = {
                        isFollowing = !isFollowing
                        val prefs = context.getSharedPreferences("artist_follows", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("follow_${artist.id}", isFollowing).apply()
                        followerCount = if (isFollowing) followerCount + 1 else (followerCount - 1).coerceAtLeast(0)
                        prefs.edit().putInt("followers_${artist.id}", followerCount).apply()
                    },
                    onShuffleClick = {
                        if (allArtistSongs.isNotEmpty()) {
                            playerViewModel.playQueue(allArtistSongs.shuffled())
                        }
                    },
                    onDownloadAllClick = {
                        if (allArtistSongs.isNotEmpty()) {
                            playerViewModel.downloadSongs(allArtistSongs)
                            homeViewModel.markArtistDownloaded(artist, allArtistSongs)
                        }
                    }
                )
            }
            
            // Stats Row
            item {
                ClayArtistStats(
                    songsCount = allArtistSongs.size,
                    followers = followerCount,
                    monthlyListeners = (followerCount * 0.8).toInt()
                )
            }
            
            // Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = ClayBackground,
                    contentColor = AccentBlue,
                    edgePadding = 20.dp
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    if (allArtistSongs.isNotEmpty()) {
                        item {
                            ClaySectionTitle(title = "All Songs")
                        }

                        itemsIndexed(allArtistSongs) { index, song ->
                            ClayArtistSongItem(
                                index = index + 1,
                                song = song,
                                onClick = { onSongClick(song) },
                                onInfoClick = {
                                    selectedSongForInfo = song
                                    showSongInfoDialog = true
                                }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Loading songs...", color = TextSecondary)
                            }
                        }
                    }
                }

                1 -> {
                    if (artistAlbums.isNotEmpty()) {
                        item {
                            ClaySectionTitle(title = "Albums")
                        }

                        items(artistAlbums) { album ->
                            ClayAlbumRow(
                                album = album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No albums found", color = TextSecondary)
                            }
                        }
                    }
                }

                2 -> {
                    if (movieSongs.isNotEmpty()) {
                        item {
                            ClaySectionTitle(title = "Movie Songs")
                        }

                        itemsIndexed(movieSongs) { index, song ->
                            ClayArtistSongItem(
                                index = index + 1,
                                song = song,
                                onClick = { onSongClick(song) },
                                onInfoClick = {
                                    selectedSongForInfo = song
                                    showSongInfoDialog = true
                                }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No movie songs found", color = TextSecondary)
                            }
                        }
                    }
                }

                3 -> {
                    if (concertSongs.isNotEmpty()) {
                        item {
                            ClaySectionTitle(title = "Concert / Live")
                        }

                        itemsIndexed(concertSongs) { index, song ->
                            ClayArtistSongItem(
                                index = index + 1,
                                song = song,
                                onClick = { onSongClick(song) },
                                onInfoClick = {
                                    selectedSongForInfo = song
                                    showSongInfoDialog = true
                                }
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No concert songs found", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Playlists Section
            if (artistPlaylists.isNotEmpty()) {
                item {
                    ClaySectionTitle(title = "Playlists")
                }
                
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(artistPlaylists.take(5)) { playlist ->
                            ClayPlaylistCard(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) }
                            )
                        }
                    }
                }
            }
            
            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Enhanced Song Information Dialog
        if (showSongInfoDialog && selectedSongForInfo != null) {
            EnhancedSongInformationDialog(
                song = selectedSongForInfo!!,
                onDismiss = {
                    showSongInfoDialog = false
                    selectedSongForInfo = null
                }
            )
        }
    }
}

@Composable
private fun ClayAlbumRow(
    album: Album,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(ClayCardLight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp))
                .background(ClayCardDark, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = album.artworkUrl,
                contentDescription = album.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${album.songCount} songs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ClayArtistHeader(
    artist: Artist,
    artistImageUrl: String?,
    artistBannerUrl: String?,
    isFollowing: Boolean,
    followerCount: Int,
    isTop500: Boolean,
    rank: Int?,
    headerHeight: androidx.compose.ui.unit.Dp,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadAllClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        // Banner Image with Claymorphism
        AsyncImage(
            model = artistBannerUrl ?: artistImageUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
        
        // Back Button - Claymorphism
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .background(ClayCardLight.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        
        // Top 500 Badge
        if (isTop500) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(AccentRed, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (rank != null) "Top 500 • #$rank" else "Top 500",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Artist Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Claymorphism Artist Image
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = CircleShape,
                        ambientColor = ClayShadowDark.copy(alpha = 0.4f),
                        spotColor = ClayShadowDark.copy(alpha = 0.6f)
                    )
            ) {
                AsyncImage(
                    model = artistImageUrl ?: artist.artworkUrl,
                    contentDescription = artist.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(4.dp, ClayCardLight, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                if (isTop500) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset((-4).dp, (-4).dp)
                            .background(AccentRed, CircleShape)
                            .size(36.dp)
                            .border(3.dp, ClayCardLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Artist Name
            Text(
                text = artist.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            // Followers
            val formattedFollowers = when {
                followerCount >= 1_000_000 -> "${String.format("%.1f", followerCount / 1_000_000.0)}M"
                followerCount >= 1_000 -> "${String.format("%.1f", followerCount / 1_000.0)}K"
                else -> followerCount.toString()
            }
            
            Text(
                text = "$formattedFollowers followers",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                // Follow Button
                ClayActionButton(
                    onClick = onFollowClick,
                    text = if (isFollowing) "Following" else "Follow",
                    icon = if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                    containerColor = if (isFollowing) Color(0xFF4CAF50) else AccentRed,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                // Shuffle Button
                ClayActionButton(
                    onClick = onShuffleClick,
                    text = "Shuffle",
                    icon = Icons.Default.Shuffle,
                    containerColor = AccentBlue,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                // Download Button
                ClayActionButton(
                    onClick = onDownloadAllClick,
                    text = "Download",
                    icon = Icons.Default.Download,
                    containerColor = ClayCardLight,
                    contentColor = TextPrimary,
                    modifier = Modifier.width(110.dp)
                )
            }
        }
    }
}

@Composable
private fun ClayActionButton(
    onClick: () -> Unit,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
        shadowElevation = 8.dp,
        modifier = modifier.height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClayArtistStats(
    songsCount: Int,
    followers: Int,
    monthlyListeners: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .background(ClayCardLight, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ClayStatItem(
            value = songsCount.toString(),
            label = "Songs",
            color = AccentBlue
        )
        ClayStatItem(
            value = formatStatNumber(followers),
            label = "Followers",
            color = AccentPurple
        )
        ClayStatItem(
            value = formatStatNumber(monthlyListeners),
            label = "Monthly",
            color = AccentGold
        )
    }
}

@Composable
private fun ClayStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ClaySectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        ),
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
private fun ClayArtistSongItem(
    index: Int,
    song: Song,
    onClick: () -> Unit,
    onInfoClick: () -> Unit = {}
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = ClayShadowDark.copy(alpha = 0.2f),
                spotColor = ClayShadowDark.copy(alpha = 0.3f)
            )
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClayCardLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index or Rank
            if (index <= 3) {
                val medalColor = when (index) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> TextSecondary
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(medalColor.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, medalColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = medalColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = String.format("%02d", index),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Thumbnail
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = song.artworkUrl ?: song.getHighQualityArtwork(),
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Song Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Verified badge if from Top 500 channel
                    if (IndianMusicChannels.isPriorityChannel(song.channelName)) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = AccentRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = song.album.takeIf { it.isNotBlank() } ?: song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Duration
            Text(
                text = song.formattedDuration(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Info Button
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .size(36.dp)
                    .shadow(4.dp, CircleShape)
                    .background(ClayCardLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Song Info",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ClayPlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ClayCardLight)
        ) {
            AsyncImage(
                model = playlist.artworkUrl,
                contentDescription = playlist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Text(
            text = "${playlist.songCount} songs",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

private fun formatStatNumber(num: Int): String {
    return when {
        num >= 1_000_000 -> "${(num / 1_000_000.0).toInt()}M"
        num >= 1_000 -> "${(num / 1_000.0).toInt()}K"
        else -> num.toString()
    }
}
