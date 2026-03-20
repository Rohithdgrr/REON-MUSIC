/*
 * REON Music App - Artists Screen
 * Copyright (c) 2024 REON
 * Light Purple Theme Design
 */

package com.reon.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Song
import com.reon.music.ui.viewmodels.HomeViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.*

// Light Purple Theme Colors (matching LibraryScreen & DownloadsScreen)
private val BackgroundPurple = Color(0xFFF5F0FF)
private val SurfacePurple = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFE9D5FF)
private val IconPink = Color(0xFFFF6B9D)
private val IconBlue = Color(0xFF4A90D9)
private val IconGreen = Color(0xFF50C878)

// Language filters
private val LANGUAGE_FILTERS = listOf("All", "Telugu", "Hindi", "Tamil", "English", "Malayalam")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreenEnhanced(
    navController: NavHostController? = null,
    onNavigateToHome: () -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    
    // Filter state
    var selectedLanguage by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    // Sample stats (would come from viewmodel in real implementation)
    val followingCount = 0
    val songsCount = 0
    val playedCount = 0
    
    // Filter artists by language
    val filteredArtists = remember(uiState, selectedLanguage) {
        getFilteredArtistsByLanguage(uiState, selectedLanguage)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Artists",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    // Search icon only
                    IconButton(
                        onClick = { isSearchActive = !isSearchActive },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = AccentPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPurple
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = BackgroundPurple
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPurple),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar (visible when search is active)
            if (isSearchActive) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search artists...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfacePurple,
                            unfocusedContainerColor = SurfacePurple,
                            focusedIndicatorColor = AccentPurple,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, null, tint = TextSecondary)
                        }
                    )
                }
            }
            
            // Language Filter Pills
            item {
                LanguageFilterPills(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )
            }
            
            // Stats Cards Row
            item {
                StatsCardsRow(
                    followingCount = followingCount,
                    songsCount = songsCount,
                    playedCount = playedCount
                )
            }
            
            // Top All Artists Section
            item {
                TopArtistsSection(
                    hasArtists = filteredArtists.isNotEmpty(),
                    onDiscoverClick = { /* Navigate to discover */ }
                )
            }
            
            // Suggested Artists Section
            if (filteredArtists.isNotEmpty()) {
                item {
                    Text(
                        text = "Suggested",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(filteredArtists.take(10)) { artist ->
                    SuggestedArtistItem(
                        artist = artist,
                        onClick = { onArtistClick(artist) },
                        onFollowClick = { /* Handle follow */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageFilterPills(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LANGUAGE_FILTERS.take(4).forEach { language ->
            val isSelected = language == selectedLanguage
            FilterPill(
                text = language,
                isSelected = isSelected,
                onClick = { onLanguageSelected(language) },
                modifier = if (language == "All") Modifier.wrapContentWidth() else Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilterPill(
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isSelected && text == "All") {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}

@Composable
private fun StatsCardsRow(
    followingCount: Int,
    songsCount: Int,
    playedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Following Card
        StatCard(
            icon = Icons.Default.Person,
            value = followingCount.toString(),
            label = "Following",
            iconBackgroundColor = LightPurple,
            iconColor = AccentPurple,
            modifier = Modifier.weight(1f)
        )
        
        // Songs Card
        StatCard(
            icon = Icons.Default.MusicNote,
            value = songsCount.toString(),
            label = "Songs",
            iconBackgroundColor = LightPurple,
            iconColor = AccentPurple,
            modifier = Modifier.weight(1f)
        )
        
        // Played Card
        StatCard(
            icon = Icons.Default.PlayCircle,
            value = playedCount.toString(),
            label = "Played",
            iconBackgroundColor = LightPurple,
            iconColor = AccentPurple,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconBackgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon in circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
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
private fun TopArtistsSection(
    hasArtists: Boolean,
    onDiscoverClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section title
            Text(
                text = "Top All Artists",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Empty state icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LightPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonOutline,
                    contentDescription = null,
                    tint = AccentPurple.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No artists found",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Follow your favourite artists to see them here",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Discover Artists Button
            Button(
                onClick = onDiscoverClick,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfacePurple
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text(
                    text = "Discover Artists",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SuggestedArtistItem(
    artist: Artist,
    onClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Artist avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentPurple, IconPink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = artist.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Artist info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${artist.topSongs.size} songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            
            // Follow button
            OutlinedButton(
                onClick = onFollowClick,
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LightPurple)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                )
            ) {
                Text(
                    text = "+ Follow",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper function to filter artists by language
private fun getFilteredArtistsByLanguage(uiState: com.reon.music.ui.viewmodels.HomeUiState, language: String): List<Artist> {
    return when (language) {
        "Telugu" -> uiState.teluguSongs.map { song -> Artist(song.id, song.artist, song.artworkUrl) }.distinctBy { it.id }
        "Hindi" -> uiState.hindiSongs.map { song -> Artist(song.id, song.artist, song.artworkUrl) }.distinctBy { it.id }
        "Tamil" -> uiState.tamilSongs.map { song -> Artist(song.id, song.artist, song.artworkUrl) }.distinctBy { it.id }
        "English" -> uiState.englishSongs.map { song -> Artist(song.id, song.artist, song.artworkUrl) }.distinctBy { it.id }
        "Malayalam" -> uiState.malayalamSongs.map { song -> Artist(song.id, song.artist, song.artworkUrl) }.distinctBy { it.id }
        else -> (uiState.topArtists + uiState.recommendedArtists + uiState.indianArtists).distinctBy { it.id }
    }
}
