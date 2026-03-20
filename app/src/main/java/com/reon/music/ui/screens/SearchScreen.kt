/*
 * REON Music App - Search Screen
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.reon.music.core.model.Song
import com.reon.music.ui.viewmodels.SearchViewModel
import java.text.SimpleDateFormat
import java.util.*

// Light Purple Theme Colors (matching other screens)
private val BackgroundPurple = Color(0xFFF5F0FF)
private val SurfacePurple = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFE9D5FF)

// Search history suggestions
private val SEARCH_HISTORY = listOf(
    "AR Rahman", "Arijit Singh", "Shreya Ghoshal", "Sid Sriram", "Anirudh"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController? = null,
    modifier: Modifier = Modifier,
    onNavigateToPlayer: (Song) -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onDismiss: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search",
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
        containerColor = BackgroundPurple
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPurple)
        ) {
            // Search Bar
            SearchBarSection(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    viewModel.updateQuery(it)
                },
                onClear = { 
                    searchQuery = ""
                    viewModel.updateQuery("")
                },
                onMicClick = { isListening = !isListening },
                isListening = isListening,
                onSearch = {
                    keyboardController?.hide()
                    if (searchQuery.isNotBlank()) {
                        viewModel.search(searchQuery)
                    }
                }
            )
            
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
                uiState.songs.isNotEmpty() -> {
                    SearchResultsList(
                        songs = uiState.songs,
                        onSongClick = { song ->
                            onNavigateToPlayer(song)
                        }
                    )
                }
                searchQuery.isBlank() -> {
                    SearchHistorySection(
                        history = SEARCH_HISTORY,
                        onHistoryClick = { historyItem ->
                            searchQuery = historyItem
                            viewModel.updateQuery(historyItem)
                            viewModel.search(historyItem)
                        }
                    )
                }
                else -> {
                    EmptySearchResults(query = searchQuery)
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onMicClick: () -> Unit,
    isListening: Boolean,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Icon
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            
            // Search TextField
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        "Search songs, artists, albums...",
                        color = TextSecondary.copy(alpha = 0.6f)
                    ) 
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
            
            // Mic or Clear Button
            if (query.isNotBlank()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Outlined.Mic,
                        contentDescription = "Voice Search",
                        tint = if (isListening) AccentPurple else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    history: List<String>,
    onHistoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Recent Searches",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Search history chips
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEach { item ->
                SearchHistoryChip(
                    text = item,
                    onClick = { onHistoryClick(item) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Trending searches section
        Text(
            text = "Trending",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Trending items
        val trending = listOf("Leo", "Pushpa 2", "Devara", "Kalki 2898 AD", "Indian 2")
        trending.forEachIndexed { index, item ->
            TrendingItem(
                rank = index + 1,
                text = item,
                onClick = { onHistoryClick(item) }
            )
        }
    }
}

@Composable
private fun SearchHistoryChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.NorthWest,
                contentDescription = "Search",
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TrendingItem(
    rank: Int,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank number
            Text(
                text = "$rank",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AccentPurple,
                modifier = Modifier.width(24.dp)
            )
            
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Trending",
                tint = AccentPurple,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Songs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(songs) { song ->
            SearchResultItem(
                song = song,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
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
            // Album art or music note placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentPurple, LightPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (song.artworkUrl != null) {
                    AsyncImage(
                        model = song.artworkUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
            }
            
            // More options
            IconButton(
                onClick = { /* Show options */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LightPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = AccentPurple.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No results found",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Try different keywords or check your spelling",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
