/*
 * REON Music App - Modern Artists Screen
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Song
import com.reon.music.data.network.youtube.IndianMusicChannels
import com.reon.music.ui.viewmodels.HomeViewModel
import com.reon.music.ui.viewmodels.HomeGroups
import com.reon.music.ui.viewmodels.PlayerViewModel
import kotlinx.coroutines.launch

// Claymorphism Color Palette
private val ClayBackground = Color(0xFFF5F5F0)
private val ClayCardLight = Color(0xFFFFFFFF)
private val ClayShadowLight = Color(0xFFE8E8E3)
private val ClayShadowDark = Color(0xFFC0C0B8)
private val TextPrimary = Color(0xFF2C2C2C)
private val TextSecondary = Color(0xFF6B6B6B)
private val AccentRed = Color(0xFFE74C3C)
private val AccentBlue = Color(0xFF3498DB)
private val AccentGold = Color(0xFFF39C12)

// Artist Categories with real image URLs from Top 500 channels
private val ARTIST_CATEGORIES = listOf(
    Triple("All", 0xFFE74C3C.toInt(), null),
    Triple("Telugu", 0xFFFF6B35.toInt(), "telugu"),
    Triple("Hindi", 0xFF9C27B0.toInt(), "hindi"),
    Triple("Indian", 0xFF4CAF50.toInt(), "indian"),
    Triple("Tamil", 0xFF2196F3.toInt(), "tamil"),
    Triple("English", 0xFF3F51B5.toInt(), "english"),
    Triple("Punjabi", 0xFFFF9800.toInt(), "punjabi"),
    Triple("Malayalam", 0xFF009688.toInt(), "malayalam")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
    onNavigateToHome: () -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // Extended language/artist data loads on demand
    LaunchedEffect(Unit) {
        homeViewModel.ensureGroupsLoaded(HomeGroups.LANGUAGES_EXT, HomeGroups.ARTISTS_EXT)
    }
    
    // Filter state
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Artists list with real images
    val artists = remember(uiState, selectedCategory) {
        getFilteredArtists(uiState, selectedCategory)
    }
    
    val filteredArtists = remember(artists, searchQuery) {
        if (searchQuery.isBlank()) artists
        else artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Scaffold(
        topBar = {
            ClayTopAppBar(
                title = "Artists",
                onSearchQueryChange = { searchQuery = it },
                searchQuery = searchQuery
            )
        },
        containerColor = ClayBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Section with Featured Artist
            item {
                FeaturedArtistSection(
                    featuredArtist = artists.firstOrNull(),
                    onArtistClick = onArtistClick,
                    onPlayClick = { artist ->
                        artist.topSongs.firstOrNull()?.let { onSongClick(it) }
                    }
                )
            }
            
            // Category Filter Chips
            item {
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }
            
            // Stats Section
            item {
                ClayStatsSection(
                    totalArtists = artists.size,
                    topGenre = selectedCategory.takeIf { it != "All" } ?: "All Genres",
                    monthlyListeners = artists.sumOf { it.followerCount.toLong() }
                )
            }
            
            // Section Title
            item {
                Text(
                    text = "Trending Artists",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
            
            // Artists Grid with Claymorphism Cards
            items(filteredArtists.chunked(2)) { rowArtists ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowArtists.forEach { artist ->
                        ClayArtistCard(
                            artist = artist,
                            onClick = { onArtistClick(artist) },
                            onPlayClick = {
                                artist.topSongs.firstOrNull()?.let { onSongClick(it) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Add empty spacer if odd number
                    if (rowArtists.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClayTopAppBar(
    title: String,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String
) {
    TopAppBar(
        title = {
            if (searchQuery.isEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = TextPrimary
                )
            } else {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search artists...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = AccentRed,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onSearchQueryChange(if (searchQuery.isEmpty()) " " else "") },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ClayCardLight)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (searchQuery.isEmpty()) Icons.Outlined.Search else Icons.Outlined.Close,
                    contentDescription = if (searchQuery.isEmpty()) "Search" else "Clear",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ClayBackground
        )
    )
}

@Composable
private fun FeaturedArtistSection(
    featuredArtist: Artist?,
    onArtistClick: (Artist) -> Unit,
    onPlayClick: (Artist) -> Unit
) {
    if (featuredArtist == null) return
    
    val isTop500 = IndianMusicChannels.isPriorityChannel(featuredArtist.name)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .height(280.dp)
    ) {
        // Claymorphism Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = ClayShadowDark.copy(alpha = 0.4f),
                    spotColor = ClayShadowDark.copy(alpha = 0.6f)
                ),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = ClayCardLight)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image with Gradient
                AsyncImage(
                    model = featuredArtist.artworkUrl 
                        ?: featuredArtist.topSongs.firstOrNull()?.getHighQualityArtwork(),
                    contentDescription = featuredArtist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.9f)
                                )
                            )
                        )
                )
                
                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    // Verified Badge
                    if (isTop500) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AccentRed.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Top Artist",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Featured Artist",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = featuredArtist.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${featuredArtist.topSongs.size} songs • ${formatFollowers(featuredArtist.followerCount)} followers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Play Button
                        ClayButton(
                            onClick = { onPlayClick(featuredArtist) },
                            text = "Play",
                            icon = Icons.Default.PlayArrow,
                            containerColor = AccentRed,
                            contentColor = Color.White
                        )
                        
                        // Follow Button
                        ClayButton(
                            onClick = { onArtistClick(featuredArtist) },
                            text = "View",
                            icon = Icons.Default.Person,
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(ARTIST_CATEGORIES) { (category, color, _) ->
            val isSelected = category == selectedCategory
            
            ClayFilterChip(
                text = category,
                isSelected = isSelected,
                color = Color(color),
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun ClayFilterChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    )
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) color else ClayCardLight,
        shadowElevation = if (isSelected) 8.dp else 4.dp,
        modifier = Modifier
            .height(44.dp)
            .scale(scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Color.White else TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ClayStatsSection(
    totalArtists: Int,
    topGenre: String,
    monthlyListeners: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ClayStatCard(
            value = totalArtists.toString(),
            label = "Artists",
            icon = Icons.Default.Person,
            color = AccentBlue,
            modifier = Modifier.weight(1f)
        )
        ClayStatCard(
            value = topGenre,
            label = "Category",
            icon = Icons.Default.Category,
            color = AccentGold,
            modifier = Modifier.weight(1f)
        )
        ClayStatCard(
            value = formatListeners(monthlyListeners),
            label = "Listeners",
            icon = Icons.Default.TrendingUp,
            color = AccentRed,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClayStatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = ClayShadowDark.copy(alpha = 0.3f),
                spotColor = ClayShadowDark.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ClayCardLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ClayArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTop500 = IndianMusicChannels.isPriorityChannel(artist.name)
    val rank = IndianMusicChannels.getChannelByName(artist.name)?.rank
    
    // Animation
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
    )
    
    Card(
        modifier = modifier
            .height(200.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isTop500) AccentRed.copy(alpha = 0.2f) else ClayShadowDark.copy(alpha = 0.3f),
                spotColor = if (isTop500) AccentRed.copy(alpha = 0.3f) else ClayShadowDark.copy(alpha = 0.4f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ClayCardLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                AsyncImage(
                    model = artist.artworkUrl 
                        ?: artist.topSongs.firstOrNull()?.getHighQualityArtwork(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                
                // Rank Badge
                if (rank != null) {
                    val badgeColor = when {
                        rank <= 20 -> Color(0xFFFFD700) // Gold
                        rank <= 50 -> Color(0xFFC0C0C0) // Silver
                        rank <= 100 -> Color(0xFFCD7F32) // Bronze
                        else -> AccentRed
                    }
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(badgeColor.copy(alpha = 0.9f), CircleShape)
                            .size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (rank <= 50) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Verified Badge
                if (isTop500) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(AccentRed, CircleShape)
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Play Button Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(AccentRed.copy(alpha = 0.9f), CircleShape)
                        .size(40.dp)
                        .clickable { onPlayClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${artist.topSongs.size} songs • ${formatFollowers(artist.followerCount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
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
private fun ClayButton(
    onClick: () -> Unit,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        shadowElevation = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Helper Functions
private fun getFilteredArtists(uiState: com.reon.music.ui.viewmodels.HomeUiState, category: String): List<Artist> {
    return when (category) {
        "Telugu" -> uiState.teluguSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "Hindi" -> uiState.hindiSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "Tamil" -> uiState.tamilSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "English" -> uiState.englishSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "Punjabi" -> uiState.punjabiSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "Malayalam" -> uiState.malayalamSongs.map { Artist(it.id, it.artist, it.artworkUrl) }
        "Indian" -> (uiState.indianArtists + uiState.topArtists).distinctBy { it.id }
        else -> (uiState.topArtists + uiState.recommendedArtists).distinctBy { it.id }
    }.take(50)
}

private fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 1_000_000.0).toInt()}M"
        count >= 1_000 -> "${(count / 1_000.0).toInt()}K"
        else -> count.toString()
    }
}

private fun formatListeners(count: Long): String {
    return when {
        count >= 1_000_000_000 -> "${(count / 1_000_000_000.0).toInt()}B"
        count >= 1_000_000 -> "${(count / 1_000_000.0).toInt()}M"
        count >= 1_000 -> "${(count / 1_000.0).toInt()}K"
        else -> count.toString()
    }
}
