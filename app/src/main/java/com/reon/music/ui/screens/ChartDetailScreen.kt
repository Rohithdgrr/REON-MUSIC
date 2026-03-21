/*
 * REON Music App - Chart/Playlist Detail Screen
 * Copyright (c) 2024 REON
 * Modern Chart/Playlist Page with Numbered Song List
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.reon.music.core.model.*
import com.reon.music.ui.viewmodels.HomeViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel

// Premium Light Theme Colors
private val BackgroundLight = Color(0xFFFFFFFF)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF5F6368)
private val AccentBlue = Color(0xFF42A5F5)
private val AccentRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    chartTitle: String,
    chartType: String = "chart", // "chart", "playlist", "language", "mood"
    onBackClick: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    
    // Sorting state
    var sortOption by remember { mutableStateOf(com.reon.music.core.model.SongSortOption.DEFAULT) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    // Song options state
    var showSongOptions by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    
    // Endless scrolling state
    var currentPage by remember { mutableStateOf(1) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var hasMore by remember { mutableStateOf(true) }
    var isUnlimitedMode by remember { mutableStateOf(false) }
    
    // Initial songs based on chart type
    val initialSongs = remember(uiState, chartType) {
        when (chartType) {
            // Language categories
            "telugu" -> uiState.teluguSongs
            "hindi" -> uiState.hindiSongs
            "english" -> uiState.englishSongs
            "tamil" -> uiState.tamilSongs
            "punjabi" -> uiState.punjabiSongs
            "kannada" -> uiState.kannadaSongs
            "malayalam" -> uiState.malayalamSongs
            "marathi" -> uiState.marathiSongs
            "bengali" -> uiState.bengaliSongs
            "bhojpuri" -> uiState.bhojpuriSongs
            "gujarati" -> uiState.gujaratiSongs
            "rajasthani" -> uiState.rajasthaniSongs
            "banjara" -> uiState.banjaraSongs
            
            // International
            "international" -> uiState.internationalHits
            
            // Mood/Genre categories
            "love", "romantic" -> uiState.romanticSongs
            "party" -> uiState.partySongs
            "sad" -> uiState.sadSongs
            "lofi" -> uiState.lofiSongs
            "devotional" -> uiState.devotionalSongs
            "heartbreak" -> uiState.heartbreakSongs
            "dj" -> uiState.djRemixes
            "teluguDj" -> uiState.teluguDjSongs
            "wedding" -> uiState.weddingSongs
            "viral", "trending" -> uiState.trendingNowSongs
            
            // User curated
            "recent" -> uiState.recentlyPlayedSongs
            "quick-picks" -> uiState.quickPicksSongs
            "recommended" -> uiState.quickPicksSongs
            "new" -> uiState.newReleases
            "alltimefavorite" -> uiState.allTimeFavorites
            "mostlistening" -> uiState.mostListeningSongs
            
            // Artist spotlights
            "arijitsingh" -> uiState.arijitSinghSongs
            "arrahman" -> uiState.arRahmanSongs
            "spb" -> uiState.spbSongs
            "dsp" -> uiState.dspSongs
            "thaman" -> uiState.thamanSongs
            "pritam" -> uiState.pritam
            "harrisjayaraj" -> uiState.harishJeyaraj
            "manisharma" -> uiState.manisharma
            "latamangeshkar" -> uiState.lataMangeshkarSongs
            "kishorkumar" -> uiState.kishorKumarSongs
            "mohammedrafi" -> uiState.mohammedRafiSongs
            "shreyaghoshal" -> uiState.shreyaGhoshalSongs
            "sidsriram" -> uiState.sidSriram
            "anirudh" -> uiState.anirudhSongs
            "badshah" -> uiState.badshah
            "honeysingh" -> uiState.honeysingh
            "kanikakapoor" -> uiState.kanikKapoor
            
            else -> uiState.quickPicksSongs + uiState.newReleases
        }.distinctBy { it.id }
    }
    
    // Initialize with initial songs
    LaunchedEffect(chartType) {
        if (chartType.startsWith("pl")) { // Assuming JioSaavn playlist IDs start with "pl"
            homeViewModel.getPlaylistSongs(chartType)
        } else {
            allSongs = initialSongs
        }
        currentPage = 1
        hasMore = true
        isUnlimitedMode = false
    }

    // Update song list when chartSongs state changes
    LaunchedEffect(uiState.chartSongs) {
        if (chartType.startsWith("pl")) {
            allSongs = uiState.chartSongs
        }
    }
    
    // Apply sorting
    val sortedSongs = remember(allSongs, sortOption) {
        if (sortOption == com.reon.music.core.model.SongSortOption.DEFAULT) {
            allSongs
        } else {
            homeViewModel.sortSongs(allSongs, sortOption)
        }
    }
    
    // Unlimited mode - fetch all YouTube songs
    LaunchedEffect(isUnlimitedMode) {
        if (isUnlimitedMode && allSongs.isEmpty()) {
            val searchQuery = when (chartType) {
                "telugu" -> "telugu songs"
                "hindi" -> "hindi songs"
                "english" -> "english songs"
                else -> chartTitle.lowercase()
            }
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Use unlimited search
                    homeViewModel.searchSongsUnlimited(searchQuery, 1000).collect { songs ->
                        allSongs = songs.distinctBy { it.id }
                    }
                } catch (e: Exception) {
                    hasMore = false
                }
            }
        }
    }
    
    // Load more songs when scrolling - use HomeViewModel's repository
    LaunchedEffect(currentPage) {
        if (currentPage > 1 && hasMore && !isLoadingMore) {
            isLoadingMore = true
            val searchQuery = when (chartType) {
                "telugu" -> "telugu songs"
                "hindi" -> "hindi songs"
                "english" -> "english songs"
                "tamil" -> "tamil songs"
                "punjabi" -> "punjabi songs"
                "love" -> "love songs"
                "party" -> "party songs"
                "heartbreak" -> "heartbreak songs"
                "dj" -> "dj remix"
                "new" -> "new releases"
                "banjara" -> "banjara songs"
                else -> chartTitle.lowercase()
            }
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val newSongs = homeViewModel.searchSongsForChart(searchQuery, 50)
                    if (newSongs.isNotEmpty()) {
                        val uniqueNewSongs = newSongs.filter { newSong ->
                            !allSongs.any { it.id == newSong.id }
                        }
                        allSongs = allSongs + uniqueNewSongs
                        hasMore = uniqueNewSongs.size >= 20 // Keep loading if we got a good batch
                    } else {
                        hasMore = false
                    }
                } catch (e: Exception) {
                    hasMore = false
                }
            }
            isLoadingMore = false
        }
    }
    
    // Chart cover image (use first song artwork or placeholder)
    val coverImage = allSongs.firstOrNull()?.getHighQualityArtwork() ?: ""
    
    val scrollState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter songs based on search
    val filteredSongs = remember(sortedSongs, searchQuery) {
        if (searchQuery.isBlank()) sortedSongs
        else sortedSongs.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Load more when near bottom
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= sortedSongs.size - 5 && hasMore && !isLoadingMore) {
                    if (isUnlimitedMode) {
                        // Continue unlimited search
                        currentPage++
                    } else {
                        currentPage++
                    }
                }
            }
    }
    
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Chart Header
            item {
                ChartHeader(
                    title = chartTitle,
                    coverImage = coverImage,
                    songCount = allSongs.size,
                    onBackClick = onBackClick,
                    onPlayClick = {
                        if (allSongs.isNotEmpty()) {
                            playerViewModel.playQueue(allSongs)
                        }
                    },
                    onShuffleClick = {
                        if (allSongs.isNotEmpty()) {
                            playerViewModel.playQueue(allSongs.shuffled())
                        }
                    },
                    onDownloadAllClick = {
                        if (allSongs.isNotEmpty()) {
                            playerViewModel.downloadSongs(allSongs)
                            homeViewModel.markPlaylistDownloaded(chartType, chartTitle, allSongs)
                        }
                    }
                )
            }
            
            // Stats Row with Radio Mode Toggle
            item {
                Column {
                    ChartStatsRow(
                        shuffleCount = "${(10..99).random()}K",
                        subscriberCount = "${(1..9).random()}.${(1..9).random()}M"
                    )
                    
                    // Radio Mode Toggle
                    var radioModeEnabled by remember { mutableStateOf(false) }
                    LaunchedEffect(radioModeEnabled) {
                        if (radioModeEnabled && allSongs.isNotEmpty()) {
                            // Enable radio mode - play endless songs
                            playerViewModel.enableRadioMode(allSongs)
                        } else {
                            playerViewModel.disableRadioMode()
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Radio Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(Endless Playback)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = radioModeEnabled,
                            onCheckedChange = { radioModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentBlue,
                                checkedTrackColor = AccentBlue.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            
            // Search Bar Section
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Search in $chartTitle", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    } else null,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = CardWhite,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                        focusedBorderColor = AccentBlue.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }
            
            // Song List Header with Sorting
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All songs",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Unlimited mode toggle
                            Row(
                                modifier = Modifier
                                    .clickable { isUnlimitedMode = !isUnlimitedMode }
                                    .padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isUnlimitedMode) AccentBlue else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Unlimited",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isUnlimitedMode) AccentBlue else TextSecondary
                                )
                            }
                            
                            // Sort button
                            IconButton(
                                onClick = { showSortDialog = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Text(
                                text = "${filteredSongs.size}${if (hasMore) "+" else ""} songs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Songs List with ranking - Endless scrolling (using filtered songs)
            if (filteredSongs.isNotEmpty()) {
                itemsIndexed(filteredSongs) { index, song ->
                    ChartSongItem(
                        rank = index + 1,
                        song = song,
                        isTopThree = index < 3,
                        onClick = { onSongClick(song) },
                        onMoreClick = { 
                            selectedSong = song
                            showSongOptions = true
                        }
                    )
                    
                    if (index < filteredSongs.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                            color = Color.LightGray.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }

                
                // Loading indicator at bottom
                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = AccentBlue
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.MusicNote,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading songs...",
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Bottom padding for mini player
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        
        // Sort Dialog - moved outside LazyColumn
        if (showSortDialog) {
            SortOptionsDialog(
                currentSort = sortOption,
                onSortSelected = { 
                    sortOption = it
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
        
        // Song Options Bottom Sheet
        if (showSongOptions && selectedSong != null) {
            val downloadProgressMap by playerViewModel.downloadProgress.collectAsState()
            val songProgress = downloadProgressMap[selectedSong!!.id]
            
            // Check if downloaded from repository/DB
            var isDownloaded by remember(selectedSong) { mutableStateOf(false) }
            LaunchedEffect(selectedSong) {
                isDownloaded = homeViewModel.isSongDownloaded(selectedSong!!.id)
            }

            com.reon.music.ui.components.SongOptionsSheet(
                song = selectedSong!!,
                isDownloaded = isDownloaded,
                isDownloading = songProgress != null,
                downloadProgress = songProgress?.progress ?: 0,
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
                    // Navigate or show playlist dialog
                    showSongOptions = false
                },
                onDownload = { 
                    playerViewModel.downloadSong(selectedSong!!)
                    showSongOptions = false
                },
                onRemoveDownload = {
                    // Add remove download functionality if needed
                    showSongOptions = false
                },
                onShare = { 
                    showSongOptions = false 
                }
            )
        }
    }
}

@Composable
private fun ChartHeader(
    title: String,
    coverImage: String,
    songCount: Int,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onDownloadAllClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Auto height to fit content
    ) {
        // Subtle background blur or gradient (Light)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF5F5F5),
                            Color.White
                        )
                    )
                )
        )
        
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp) // Adjusted for status bar typical height
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.05f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary
            )
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Chart Cover - Enlarged (Big as player)
            Card(
                modifier = Modifier
                    .size(220.dp) // Increased size
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(0.15f)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp) // Shadow handled by modifier
            ) {
                AsyncImage(
                    model = coverImage,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Chart Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$songCount songs • Auto-updating",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons - Improved Responsive UI (Image 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button (Blue)
                Button(
                    onClick = onShuffleClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Shuffle",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Play Button (Dark)
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Play All",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Download Button
                OutlinedButton(
                    onClick = onDownloadAllClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = AccentRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(AccentRed.copy(alpha = 0.6f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download All",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Download",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartStatsRow(
    shuffleCount: String,
    subscriberCount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$shuffleCount shuffles",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$subscriberCount subscribers",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ChartSongItem(
    rank: Int,
    song: Song,
    isTopThree: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    // Rank color based on position
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> TextSecondary
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable {
                isPressed = true
                onClick()
            }
            .background(CardWhite)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number with special styling for top 3
        Box(
            modifier = Modifier.width(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTopThree) {
                // Medal/Crown icon for top 3
                Text(
                    text = when (rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "$rank"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                Text(
                    text = String.format("%02d", rank),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = rankColor
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Thumbnail
        Card(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            AsyncImage(
                model = song.getHighQualityArtwork(),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
            
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Show album/movie name if available
            if (song.album.isNotBlank()) {
                Text(
                    text = "🎬 ${song.album}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBlue.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Duration
        Text(
            text = if (song.duration > 0) song.formattedDuration() else "--:--",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // More Options Button
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(48.dp) // Proper touch target size
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp) // Larger icon
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Playlist Detail Screen - Similar to Chart but for user/editorial playlists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onBackClick: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    
    // Get playlist songs (for now, use a mix of songs)
    val songs = remember(uiState) {
        (uiState.quickPicksSongs + uiState.newReleases + uiState.hindiSongs)
            .distinctBy { it.id }
            .take(playlist.songCount.coerceAtMost(30))
    }
    
    ChartDetailScreen(
        chartTitle = playlist.name,
        chartType = "playlist",
        onBackClick = onBackClick,
        onSongClick = onSongClick,
        homeViewModel = homeViewModel,
        playerViewModel = playerViewModel
    )
}

/**
 * Song Options Sheet for Chart/Playlist
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartSongOptionsSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardWhite
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
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            
            // Options
            ChartOptionMenuItem(icon = Icons.Default.PlayArrow, title = "Play", onClick = onPlay)
            ChartOptionMenuItem(icon = Icons.Default.SkipNext, title = "Play Next", onClick = onPlayNext)
            ChartOptionMenuItem(icon = Icons.Default.QueueMusic, title = "Add to Queue", onClick = onAddToQueue)
            ChartOptionMenuItem(icon = Icons.Default.Download, title = "Download", onClick = onDownload)
            ChartOptionMenuItem(icon = Icons.Default.Share, title = "Share", onClick = onShare)
        }
    }
}

@Composable
private fun ChartOptionMenuItem(
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
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}

/**
 * Sort Options Dialog
 */
@Composable
private fun SortOptionsDialog(
    currentSort: SongSortOption,
    onSortSelected: (SongSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sort Songs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SongSortOption.values().forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == option,
                            onClick = { onSortSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (option) {
                                SongSortOption.DEFAULT -> "Default (Relevance)"
                                SongSortOption.VIEWS -> "Most Views"
                                SongSortOption.LIKES -> "Most Likes"
                                SongSortOption.CHANNEL_POPULARITY -> "Popular Channels"
                                SongSortOption.QUALITY -> "Highest Quality"
                                SongSortOption.RECENT -> "Most Recent"
                                SongSortOption.DURATION -> "Longest Duration"
                                SongSortOption.TITLE -> "Title (A-Z)"
                                SongSortOption.ARTIST -> "Artist (A-Z)"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
