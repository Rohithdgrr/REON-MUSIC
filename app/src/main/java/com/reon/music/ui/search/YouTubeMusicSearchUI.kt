/*
 * REON Music App - YouTube Music Style Search UI
 * Copyright (c) 2024 REON
 * Modern search interface with crash-proof handling
 */

package com.reon.music.ui.search

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.reon.music.ui.viewmodels.SearchViewModel
import com.reon.music.ui.viewmodels.SearchUiState
import com.reon.music.ui.viewmodels.SearchFilter as ViewModelFilter
import com.reon.music.core.model.Song
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Album
import com.reon.music.ui.theme.*
import com.reon.music.ui.components.OptimizedAsyncImage
import com.reon.music.ui.components.ImageQuality
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// White theme colors matching HomeScreen
private val TextPrimaryLight = Color(0xFF1A1A1A)
private val TextSecondaryLight = Color(0xFF666666)
private val AccentRed = Color(0xFFE53935)
private val CardBackgroundLight = Color(0xFFF5F5F5)
private val GradientRed = listOf(Color(0xFFE53935), Color(0xFFFF7043))

@Composable
fun YouTubeMusicStyleSearchScreen(
    onNavigateToPlayer: (Song) -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // Pure white
                        Color(0xFFFFF8F6), // Very light warm white
                        Color(0xFFFFEFEC).copy(alpha = 0.3f) // Subtle sunrise tint
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with Search Bar
            SearchHeaderSection(
                query = uiState.query,
                onQueryChange = { viewModel.updateQuery(it) },
                onClear = { 
                    viewModel.updateQuery("")
                    scope.launch {
                        delay(100)
                        keyboardController?.hide()
                    }
                },
                isLoading = uiState.isLoading
            )
            
            // Search Filters
            SearchFiltersSection(
                availableFilters = availableFilters,
                selectedFilters = setOf(
                    when (uiState.activeFilter) {
                        ViewModelFilter.ALL -> "all"
                        ViewModelFilter.SONGS -> "songs"
                        ViewModelFilter.ARTISTS -> "artists"
                        ViewModelFilter.ALBUMS -> "albums"
                        ViewModelFilter.MOVIES -> "videos"
                    }
                ),
                onFilterToggle = { filter ->
                    viewModel.setFilter(
                        when (filter.id) {
                            "all" -> ViewModelFilter.ALL
                            "songs" -> ViewModelFilter.SONGS
                            "artists" -> ViewModelFilter.ARTISTS
                            "albums" -> ViewModelFilter.ALBUMS
                            "videos" -> ViewModelFilter.MOVIES
                            else -> ViewModelFilter.ALL
                        }
                    )
                }
            )
            
            // Content Area
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.error != null -> {
                        ErrorSection(
                            error = uiState.error!!,
                            onRetry = { viewModel.search(uiState.query) }
                        )
                    }
                    uiState.songs.isNotEmpty() -> {
                        SearchResultsSection(
                            songs = uiState.songs,
                            artists = uiState.artists,
                            albums = uiState.albums,
                            movies = uiState.movies,
                            onSongClick = { song ->
                                onNavigateToPlayer(song)
                            },
                            onArtistClick = { artist ->
                                onNavigateToLibrary()
                            },
                            onAlbumClick = { album ->
                                onNavigateToLibrary()
                            }
                        )
                    }
                    uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty() -> {
                        SuggestionsSection(
                            suggestions = uiState.suggestions,
                            onSuggestionClick = { suggestion ->
                                viewModel.search(suggestion.text)
                            }
                        )
                    }
                    uiState.isLoading -> {
                        LoadingSection()
                    }
                    uiState.query.isNotBlank() -> {
                        EmptyResultsSection(query = uiState.query)
                    }
                    uiState.trendingSearches.isNotEmpty() -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            WelcomeSection()
                            SuggestionsSection(
                                suggestions = uiState.trendingSearches,
                                onSuggestionClick = { suggestion ->
                                    viewModel.search(suggestion.text)
                                }
                            )
                        }
                    }
                    else -> {
                        WelcomeSection()
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeaderSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // App Title - Premium Look
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(GradientRed)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Discover",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Bar - Modern Pill Design
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CardBackgroundLight)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Songs, artists, or albums...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondaryLight.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AccentRed,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (query.isNotBlank()) {
                            IconButton(
                                onClick = onClear,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = AccentRed,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimaryLight,
                    unfocusedTextColor = TextPrimaryLight
                )
            )
        }
    }
}

@Composable
private fun SearchFiltersSection(
    availableFilters: List<SearchFilter>,
    selectedFilters: Set<String>,
    onFilterToggle: (SearchFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(availableFilters) { filter ->
            val isSelected = selectedFilters.contains(filter.id)
            Surface(
                onClick = { onFilterToggle(filter) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AccentRed else CardBackgroundLight,
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = filter.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondaryLight
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    songs: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    movies: List<Album>,
    onSongClick: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // High-level Result Highlights or Top Result could be added here
        
        if (artists.isNotEmpty()) {
            item {
                Text(
                    text = "Artists",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(artists) { artist ->
                        ArtistGridItem(artist = artist, onClick = { onArtistClick(artist) })
                    }
                }
            }
        }

        if (songs.isNotEmpty()) {
            item {
                Text(
                    text = "Top Songs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }
            items(songs) { song ->
                SongResultItem(song = song, onClick = { onSongClick(song) })
            }
        }
        
        if (albums.isNotEmpty()) {
            item {
                Text(
                    text = "Albums",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryLight,
                    modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp)
                ) {
                    items(albums) { album ->
                        AlbumGridItem(album = album, onClick = { onAlbumClick(album) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SongResultItem(
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackgroundLight)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art with Shadow/Elevation effect - Optimized loading
        Box(
            modifier = Modifier.size(52.dp)
        ) {
            OptimizedAsyncImage(
                imageUrl = song.artworkUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                quality = ImageQuality.THUMBNAIL
            )
            
            // Official Channel Badge
            if (isOfficialChannel(song.channelName)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(16.dp)
                        .background(AccentRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Official",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Song Info with Ranking
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            // Artist and Channel Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Verified Badge
                if (song.channelSubscriberCount > 1000000) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            // Ranking Stats Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Channel Rank Badge (if in Top 500)
                val channelRank = com.reon.music.data.network.youtube.IndianMusicChannels.getChannelByName(song.channelName)?.rank
                if (channelRank != null) {
                    val rankColor = when {
                        channelRank <= 20 -> Color(0xFFFFD700) // Gold
                        channelRank <= 50 -> Color(0xFFC0C0C0) // Silver
                        channelRank <= 100 -> Color(0xFFCD7F32) // Bronze
                        else -> AccentRed
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = rankColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = rankColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "#$channelRank",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = rankColor
                            )
                        }
                    }
                }
                
                // Views
                if (song.viewCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = TextSecondaryLight.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatViewCount(song.viewCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // Likes
                if (song.likeCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = null,
                            tint = TextSecondaryLight.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formatViewCount(song.likeCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryLight.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // HD Quality Badge
                if (song.quality.contains("HD", ignoreCase = true) || song.quality.contains("4K", ignoreCase = true)) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = AccentRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = song.quality.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AccentRed
                        )
                    }
                }
                
                // Priority Score Badge (based on official channel + views + likes)
                val priorityScore = calculatePriorityScore(song)
                if (priorityScore > 0) {
                    val priorityColor = when {
                        priorityScore >= 80 -> Color(0xFF4CAF50) // High - Green
                        priorityScore >= 50 -> Color(0xFFFF9800) // Medium - Orange
                        else -> Color(0xFF9E9E9E) // Low - Gray
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = priorityColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${priorityScore}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = priorityColor
                        )
                    }
                }
            }
        }
        
        // Duration
        val durationText = try {
            if (song.duration > 0) {
                val minutes = song.duration / 60
                val seconds = song.duration % 60
                String.format("%d:%02d", minutes, seconds)
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
        
        if (durationText.isNotBlank()) {
            Text(
                text = durationText,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight
            )
        }
    }
}

/**
 * Calculate priority score for search ranking
 * Based on: Top 500 Indian Music Channels first, then official channels, views, likes
 * Score range: 0-100+
 */
private fun calculatePriorityScore(song: Song): Int {
    var score = 0
    
    // Top 500 Indian Music Channels priority boost (up to 50 points based on rank)
    val channelBoost = com.reon.music.data.network.youtube.IndianMusicChannels.getChannelPriorityBoost(song.channelName)
    score += (channelBoost / 2).toInt() // Scale down: 100 -> 50, 80 -> 40, etc.
    
    // Verified channel bonus (additional 25 points)
    if (com.reon.music.data.network.youtube.IndianMusicChannels.isPriorityChannel(song.channelName)) {
        score += 25
    }
    
    // Official channel bonus (15 points for official keywords not in Top 500)
    val officialKeywords = listOf("official", "music", "records", "studio", "label", "vevo")
    if (officialKeywords.any { song.channelName.lowercase().contains(it) }) {
        score += 15
    }
    
    // Views score (20 points max) - logarithmic scale
    score += (kotlin.math.log10(song.viewCount.coerceAtLeast(1).toDouble()) * 2).toInt().coerceAtMost(20)
    
    // Likes score (15 points max) - logarithmic scale
    score += (kotlin.math.log10(song.likeCount.coerceAtLeast(1).toDouble()) * 1.5).toInt().coerceAtMost(15)
    
    // Quality bonus (10 points)
    if (song.quality.contains("HD", ignoreCase = true) || song.quality.contains("4K", ignoreCase = true)) {
        score += 5
    }
    if (song.is320kbps) {
        score += 5
    }
    
    return score.coerceIn(0, 100)
}

/**
 * Check if channel is an official music channel
 * Uses Top 500 Indian Music Channels for priority verification
 */
private fun isOfficialChannel(channelName: String): Boolean {
    // First check if it's in Top 500 Indian Music Channels
    if (com.reon.music.data.network.youtube.IndianMusicChannels.isPriorityChannel(channelName)) {
        return true
    }
    
    // Fallback to keyword matching
    val officialKeywords = listOf(
        "official", "music", "records", "studio", "audio", "label",
        "t-series", "zee", "sony", "aditya", "lahari", "mango"
    )
    return officialKeywords.any { channelName.lowercase().contains(it) }
}

/**
 * Format view count to compact format (e.g., 1.2M, 456K)
 */
private fun formatViewCount(count: Long): String {
    return when {
        count >= 1_000_000_000 -> String.format("%.1fB", count / 1_000_000_000.0)
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
private fun ArtistGridItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OptimizedAsyncImage(
            imageUrl = artist.artworkUrl,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(CardBackgroundLight),
            quality = ImageQuality.THUMBNAIL
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        OptimizedAsyncImage(
            imageUrl = album.artworkUrl,
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackgroundLight),
            quality = ImageQuality.MEDIUM
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = album.artist,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SuggestionsSection(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        val trending = suggestions.filter { it.type == SuggestionType.TRENDING }
        if (trending.isNotEmpty()) {
            Text(
                text = "Trending Searches",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryLight,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(trending) { suggestion ->
                    Surface(
                        onClick = { onSuggestionClick(suggestion) },
                        shape = RoundedCornerShape(16.dp),
                        color = CardBackground,
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = suggestion.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryLight,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        val other = suggestions.filter { it.type != SuggestionType.TRENDING }
        if (other.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Recent Searches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryLight,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(other) { suggestion ->
                    SuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (suggestion.type) {
                SuggestionType.RECENT -> Icons.Rounded.History
                else -> Icons.Rounded.Search
            },
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = suggestion.text,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimaryLight,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun WelcomeSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Brush.linearGradient(GradientRed)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Explore Music",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Discover millions of tracks, artists and albums tailored just for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondaryLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentRed)
    }
}

@Composable
private fun ErrorSection(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = ReonError, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(error, color = TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
private fun EmptyResultsSection(query: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.SearchOff, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No results for \"$query\"", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Try checking your spelling or use different keywords.", color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

