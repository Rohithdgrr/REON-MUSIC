/*
 * REON Music App - Home Screen
 * Copyright (c) 2024 REON
 * Light Grey + Sunrise Orange Theme
 */

package com.reon.music.ui.screens

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Playlist
import com.reon.music.core.model.Song
import com.reon.music.ui.components.HomeScreenSkeleton
import com.reon.music.ui.components.ImageQuality
import com.reon.music.ui.viewmodels.DailyMix
import com.reon.music.ui.components.OptimizedAsyncImage
import com.reon.music.ui.viewmodels.Genre
import com.reon.music.ui.viewmodels.HomeViewModel
import com.reon.music.ui.viewmodels.PlayerViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Single-accent palette: surfaces and text follow the app theme
// (light / dark / AMOLED via ReonTheme) so Home respects the user's
// display mode; Sunrise Orange remains the one accent color.
private val AccentOrange = Color(0xFFFF6B35)
private val AccentOrangeDeep = Color(0xFFE5531F)
private val AccentOrangeSoft = Color(0xFFFFE3D6)

/**
 * Theme-aware home colors. Light mode keeps the light-grey look;
 * dark mode uses the Material scheme (pure-black friendly) with the
 * same single orange accent, reducing color noise.
 */
private data class HomeColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val neutral: Color,
    val neutralDark: Color
)

@Composable
private fun rememberHomeColors(): HomeColors {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val scheme = MaterialTheme.colorScheme
    return remember(dark, scheme) {
        if (dark) {
            HomeColors(
                background = scheme.background,
                surface = scheme.surface,
                textPrimary = scheme.onBackground,
                textSecondary = scheme.onSurfaceVariant,
                neutral = scheme.surfaceVariant,
                neutralDark = scheme.onSurfaceVariant
            )
        } else {
            HomeColors(
                background = Color(0xFFF5F5F5),
                surface = Color(0xFFFFFFFF),
                textPrimary = Color(0xFF212121),
                textSecondary = Color(0xFF757575),
                neutral = Color(0xFFEEEEEE),
                neutralDark = Color(0xFF616161)
            )
        }
    }
}

// Greeting based on time
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }

}

/**
 * Extracts a vibrant tint from artwork on a background thread.
 * Returns null while loading or on failure so callers fall back
 * to the default accent.
 */
@Composable
private fun rememberArtworkTint(imageUrl: String?): Color? {
    var tint by remember { mutableStateOf<Color?>(null) }
    val context = LocalContext.current
    LaunchedEffect(imageUrl) {
        tint = null
        if (imageUrl == null) return@LaunchedEffect
        tint = withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .size(64)
                    .build()
                val result = context.imageLoader.execute(request)
                val bitmap = (result as? SuccessResult)?.drawable as? BitmapDrawable
                    ?: return@withContext null
                val vibrant = Palette.from(bitmap.bitmap).generate().getVibrantColor(0)
                if (vibrant != 0) Color(vibrant) else null
            } catch (_: Exception) {
                null
            }
        }
    }
    return tint
}

/**
 * Listening-progress ring shown on artwork corners for "Jump Back In".
 * Progress comes from stored play duration vs. track duration.
 */
@Composable
private fun ProgressRingBadge(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            strokeWidth = 2.dp,
            color = AccentOrange,
            trackColor = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Small orange play button overlaid on artwork corners.
 */
@Composable
private fun PlayBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(AccentOrange),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ShowMoreButton(
    onClick: () -> Unit,
    label: String = "Show more"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AccentOrange
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun JumpBackInRow(
    items: List<com.reon.music.ui.viewmodels.JumpBackInItem>,
    onSongClick: (Song) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedVisibilityScope: AnimatedVisibilityScope? = null
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.song.id }, contentType = { "song" }) { item ->
            JumpBackInCard(
                item = item,
                onClick = { onSongClick(item.song) },
                sharedTransitionScope = sharedTransitionScope,
                sharedVisibilityScope = sharedVisibilityScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun JumpBackInCard(
    item: com.reon.music.ui.viewmodels.JumpBackInItem,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val artworkSharedModifier = if (sharedTransitionScope != null && sharedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                rememberSharedContentState(key = "player-artwork-${item.song.id}"),
                animatedVisibilityScope = sharedVisibilityScope
            )
        }
    } else {
        Modifier
    }
    Card(
        modifier = Modifier
            .width(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentOrange, AccentOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAsyncImage(
                    imageUrl = item.song.artworkUrl,
                    contentDescription = "Artwork for ${item.song.title}",
                    quality = ImageQuality.MEDIUM,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize().then(artworkSharedModifier)
                )
                if (item.progress > 0f) {
                    ProgressRingBadge(
                        progress = item.progress,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.song.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = rememberHomeColors().textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GenresRow(
    genres: List<Genre>,
    onGenreClick: (Genre) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(genres, key = { it.id }, contentType = { "genre" }) { genre ->
            GenreChip(
                genre = genre,
                onClick = { onGenreClick(genre) }
            )
        }
    }
}

@Composable
private fun GenreChip(
    genre: Genre,
    onClick: () -> Unit
) {
    val accent = Color(genre.accentColor)
    Card(
        modifier = Modifier
            .height(48.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = genre.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = rememberHomeColors().textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navController: androidx.navigation.NavHostController? = null,
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit = { playerViewModel.playSong(it) },
    onMixClick: (DailyMix) -> Unit = { mix -> playerViewModel.playQueue(mix.songs) },
    onAlbumClick: (com.reon.music.core.model.Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onSeeAllClick: (String) -> Unit = {},
    onChartClick: (String, String) -> Unit = { _, _ -> },
    onSettingsClick: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToPlayer: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val headerTint = rememberArtworkTint(uiState.recentlyPlayedSongs.firstOrNull()?.artworkUrl)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            AccentOrange,
                                            headerTint ?: AccentOrangeDeep
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "U",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Column {
                            Text(
                                text = getGreeting(),
                                style = MaterialTheme.typography.bodySmall,
                                color = rememberHomeColors().textSecondary
                            )
                            Text(
                                text = "User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = rememberHomeColors().textPrimary
                            )
                        }
                    }
                },
                actions = {
                    // Manual refresh + settings
                    IconButton(
                        onClick = {
                            homeViewModel.clearError()
                            homeViewModel.refresh()
                        },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = AccentOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = AccentOrange
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = AccentOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerTint?.copy(alpha = 0.18f)
                        ?.compositeOver(rememberHomeColors().background) ?: rememberHomeColors().background
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = rememberHomeColors().background
    ) { paddingValues ->
        val hasContent = uiState.recentlyPlayedSongs.isNotEmpty() ||
            uiState.quickPicksSongs.isNotEmpty() ||
            uiState.newReleases.isNotEmpty() ||
            uiState.featuredPlaylists.isNotEmpty() ||
            uiState.jumpBackIn.isNotEmpty() ||
            uiState.charts.isNotEmpty() ||
            uiState.mostPlayedSongs.isNotEmpty() ||
            uiState.trendingAlbums.isNotEmpty() ||
            uiState.allTimeFavorites.isNotEmpty() ||
            uiState.trendingNowSongs.isNotEmpty() ||
            uiState.punjabiSongs.isNotEmpty() ||
            uiState.englishSongs.isNotEmpty() ||
            uiState.internationalHits.isNotEmpty() ||
            uiState.partySongs.isNotEmpty() ||
            uiState.romanticSongs.isNotEmpty() ||
            uiState.arijitSinghSongs.isNotEmpty() ||
            uiState.arRahmanSongs.isNotEmpty()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(rememberHomeColors().background)
        ) {
            when {
                uiState.isLoading && !hasContent -> {
                    HomeScreenSkeleton(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    )
                }
                uiState.error != null && !hasContent -> {
                    HomeErrorState(
                        message = uiState.error ?: "Failed to load content",
                        onRetry = {
                            homeViewModel.clearError()
                            homeViewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    // State firewall: only the back-to-top button recomposes
                    // when scrolling crosses the visibility threshold.
                    val showBackToTop by remember {
                        derivedStateOf { listState.firstVisibleItemIndex > 8 }
                    }
                    // Auto-refresh stale content when returning to Home.
                    // (platform LocalLifecycleOwner avoids a new lifecycle
                    // runtime-compose dependency; rows auto-mirror for RTL.)
                    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                homeViewModel.refreshIfStale()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                    }
                    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                        isRefreshing = uiState.isLoading && hasContent,
                        onRefresh = {
                            homeViewModel.clearError()
                            homeViewModel.refresh()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val sectionErrorMessage = if (uiState.sectionErrors.isNotEmpty()) {
                            "Couldn't load: ${uiState.sectionErrors.keys.joinToString(", ")}"
                        } else null
                        val bannerMessage = uiState.error ?: sectionErrorMessage
                        if (bannerMessage != null) {
                            item {
                                HomeErrorBanner(
                                    message = bannerMessage,
                                    onRetry = {
                                        homeViewModel.clearError()
                                        homeViewModel.retryFailedSections()
                                    },
                                    onDismiss = {
                                        homeViewModel.clearError()
                                        homeViewModel.clearSectionErrors()
                                    }
                                )
                            }
                        }
            // Recently Played (horizontal cards with play badges)
            if (uiState.recentlyPlayedSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently Played",
                        showSeeAll = false,
                        actionIcon = Icons.Outlined.History,
                        onActionClick = { onSeeAllClick("recently-played") }
                    )
                }
                item {
                    RecentlyPlayedRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.RECENT,
                            uiState.recentlyPlayedSongs
                        ),
                        progressById = uiState.songProgress,
                        onSongClick = onSongClick
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.RECENT,
                        uiState.recentlyPlayedSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.RECENT,
                                    uiState.recentlyPlayedSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Jump Back In (unfinished listens with progress rings)
            if (uiState.jumpBackIn.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Jump Back In",
                        onSeeAllClick = { onSeeAllClick("recently-played") }
                    )
                }
                item {
                    JumpBackInRow(
                        items = homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.JUMP_BACK_IN,
                            uiState.jumpBackIn
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
            }

            // Quick Picks Section
            if (uiState.quickPicksSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Quick Picks",
                        onSeeAllClick = { onSeeAllClick("quick-picks") }
                    )
                }
                item {
                    QuickPicksGrid(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.QUICK_PICKS,
                            uiState.quickPicksSongs
                        ),
                        onSongClick = onSongClick
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.QUICK_PICKS,
                        uiState.quickPicksSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.QUICK_PICKS,
                                    uiState.quickPicksSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Made For You Section
            if (uiState.dailyMixes.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Made For You",
                        showSeeAll = false,
                        trailingLabel = "Personalized",
                        onTrailingLabelClick = { onSeeAllClick("daily-mix") }
                    )
                }
                item {
                    DailyMixHero(
                        mix = uiState.dailyMixes.first(),
                        mixNumber = 1,
                        onPlayClick = { onMixClick(uiState.dailyMixes.first()) }
                    )
                }
                if (uiState.dailyMixes.size > 1) {
                    item {
                        MixesRow(
                            mixes = uiState.dailyMixes.drop(1),
                            onMixClick = onMixClick
                        )
                    }
                }
            }

            // Recommended Section - use quickPicksSongs as recommended
            if (uiState.quickPicksSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recommended For You",
                        onSeeAllClick = { onSeeAllClick("recommended") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.RECOMMENDED,
                            uiState.quickPicksSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.RECOMMENDED,
                        uiState.quickPicksSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.RECOMMENDED,
                                    uiState.quickPicksSongs.size
                                )
                            }
                        )
                    }
                }
            }
            
            // Most Played Section
            if (uiState.mostPlayedSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Most Played",
                        onSeeAllClick = { onSeeAllClick("mostplayed") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.MOST_PLAYED,
                            uiState.mostPlayedSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.MOST_PLAYED,
                        uiState.mostPlayedSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.MOST_PLAYED,
                                    uiState.mostPlayedSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Charts Section
            if (uiState.charts.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Top Charts",
                        onSeeAllClick = { onSeeAllClick("charts") }
                    )
                }
                item {
                    ChartsRow(
                        charts = homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.CHARTS,
                            uiState.charts
                        ),
                        onChartClick = onChartClick
                    )
                }
            }
            
            // New Releases Section
            if (uiState.newReleases.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "New Releases",
                        onSeeAllClick = { onSeeAllClick("new") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.NEW_RELEASES,
                            uiState.newReleases
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        showNewBadge = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.NEW_RELEASES,
                        uiState.newReleases.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.NEW_RELEASES,
                                    uiState.newReleases.size
                                )
                            }
                        )
                    }
                }
            }

            // Trending Albums Section
            if (uiState.trendingAlbums.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Trending Albums",
                        showSeeAll = false
                    )
                }
                item {
                    AlbumsRow(
                        albums = homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.TRENDING_ALBUMS,
                            uiState.trendingAlbums
                        ),
                        onAlbumClick = onAlbumClick
                    )
                }
            }

            // All Time Favorites Section (liked songs)
            if (uiState.allTimeFavorites.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "All Time Favorites",
                        onSeeAllClick = { onSeeAllClick("alltimefavorite") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.ALL_TIME_FAVORITES,
                            uiState.allTimeFavorites
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ALL_TIME_FAVORITES,
                        uiState.allTimeFavorites.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ALL_TIME_FAVORITES,
                                    uiState.allTimeFavorites.size
                                )
                            }
                        )
                    }
                }
            }

            // Trending Now Section (viral hits)
            if (uiState.trendingNowSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Trending Now",
                        onSeeAllClick = { onSeeAllClick("trending") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.TRENDING_NOW,
                            uiState.trendingNowSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.TRENDING_NOW,
                        uiState.trendingNowSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.TRENDING_NOW,
                                    uiState.trendingNowSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Genres Section
            if (uiState.genres.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Browse Genres",
                        onSeeAllClick = { onSeeAllClick("genres") }
                    )
                }
                item {
                    GenresRow(
                        genres = listOf(
                            Genre(
                                id = "all-trending",
                                name = "All / Trending",
                                iconName = "trending_up",
                                accentColor = 0xFFFF6B35.toInt()
                            )
                        ) + homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.GENRES,
                            uiState.genres
                        ),
                        onGenreClick = { genre ->
                            if (genre.id == "all-trending") {
                                onSeeAllClick("trending")
                            } else {
                                onChartClick("genre-${genre.id}", genre.name)
                            }
                        }
                    )
                }
            }

            // Telugu Songs Section
            if (uiState.teluguSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Telugu Hits ⚡",
                        onSeeAllClick = { onSeeAllClick("telugu") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.TELUGU,
                            uiState.teluguSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.TELUGU,
                        uiState.teluguSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.TELUGU,
                                    uiState.teluguSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Hindi Songs Section
            if (uiState.hindiSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Hindi Hits",
                        onSeeAllClick = { onSeeAllClick("hindi") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.HINDI,
                            uiState.hindiSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.HINDI,
                        uiState.hindiSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.HINDI,
                                    uiState.hindiSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Tamil Songs Section
            if (uiState.tamilSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Tamil Hits ⚡",
                        onSeeAllClick = { onSeeAllClick("tamil") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.TAMIL,
                            uiState.tamilSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.TAMIL,
                        uiState.tamilSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.TAMIL,
                                    uiState.tamilSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Punjabi Hits Section
            if (uiState.punjabiSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Punjabi Hits",
                        onSeeAllClick = { onSeeAllClick("punjabi") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.PUNJABI,
                            uiState.punjabiSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.PUNJABI,
                        uiState.punjabiSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.PUNJABI,
                                    uiState.punjabiSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // English Hits Section
            if (uiState.englishSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "English Hits",
                        onSeeAllClick = { onSeeAllClick("english") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.ENGLISH,
                            uiState.englishSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ENGLISH,
                        uiState.englishSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ENGLISH,
                                    uiState.englishSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // International Hits Section
            if (uiState.internationalHits.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "International Hits",
                        onSeeAllClick = { onSeeAllClick("international") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.INTERNATIONAL,
                            uiState.internationalHits
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope,
                        showPlayBadge = false,
                        posterAspect = true
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.INTERNATIONAL,
                        uiState.internationalHits.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.INTERNATIONAL,
                                    uiState.internationalHits.size
                                )
                            }
                        )
                    }
                }
            }

            // Party Hits Section
            if (uiState.partySongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Party Hits",
                        onSeeAllClick = { onSeeAllClick("party") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.PARTY,
                            uiState.partySongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.PARTY,
                        uiState.partySongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.PARTY,
                                    uiState.partySongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Romantic Hits Section
            if (uiState.romanticSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Romantic Hits",
                        onSeeAllClick = { onSeeAllClick("romantic") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.ROMANTIC,
                            uiState.romanticSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ROMANTIC,
                        uiState.romanticSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ROMANTIC,
                                    uiState.romanticSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Arijit Singh Essentials Section
            if (uiState.arijitSinghSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Arijit Singh Essentials",
                        onSeeAllClick = { onSeeAllClick("arijitsingh") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.ARIJIT,
                            uiState.arijitSinghSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ARIJIT,
                        uiState.arijitSinghSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ARIJIT,
                                    uiState.arijitSinghSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // A.R. Rahman Essentials Section
            if (uiState.arRahmanSongs.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "A.R. Rahman Essentials",
                        onSeeAllClick = { onSeeAllClick("arrahman") }
                    )
                }
                item {
                    SongsRow(
                        songs = homeViewModel.homeRowSongs(
                            com.reon.music.ui.viewmodels.HomeSections.ARRAHMAN,
                            uiState.arRahmanSongs
                        ),
                        onSongClick = onSongClick,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedVisibilityScope = sharedVisibilityScope
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ARRAHMAN,
                        uiState.arRahmanSongs.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ARRAHMAN,
                                    uiState.arRahmanSongs.size
                                )
                            }
                        )
                    }
                }
            }

            // Top Artists Section
            if (uiState.topArtists.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Top Artists",
                        onSeeAllClick = { onSeeAllClick("artists") }
                    )
                }
                item {
                    ArtistsRow(
                        artists = homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.ARTISTS,
                            uiState.topArtists
                        ),
                        onArtistClick = onArtistClick
                    )
                }
                if (homeViewModel.canLoadMore(
                        com.reon.music.ui.viewmodels.HomeSections.ARTISTS,
                        uiState.topArtists.size
                    )
                ) {
                    item {
                        ShowMoreButton(
                            onClick = {
                                homeViewModel.loadMore(
                                    com.reon.music.ui.viewmodels.HomeSections.ARTISTS,
                                    uiState.topArtists.size
                                )
                            }
                        )
                    }
                }
            }

            // Featured Playlists Section
            if (uiState.featuredPlaylists.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Featured Playlists",
                        onSeeAllClick = { onSeeAllClick("playlists") }
                    )
                }
                item {
                    PlaylistsRow(
                        playlists = homeViewModel.homeRowItems(
                            com.reon.music.ui.viewmodels.HomeSections.PLAYLISTS,
                            uiState.featuredPlaylists
                        ),
                        onPlaylistClick = onPlaylistClick
                    )
                }
            }
                    } // LazyColumn
                    // Back-to-top: appears after scrolling past 8 items.
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showBackToTop,
                        enter = fadeIn(animationSpec = tween(200)),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                scope.launch { listState.animateScrollToItem(0) }
                            },
                            containerColor = AccentOrange,
                            contentColor = Color.White,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Back to top"
                            )
                        }
                    }
                    } // PullToRefreshBox
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onLibraryClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionCard(
            icon = Icons.Outlined.LibraryMusic,
            label = "Library",
            backgroundColor = AccentOrangeSoft,
            iconColor = AccentOrange,
            onClick = onLibraryClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Outlined.Favorite,
            label = "Favorites",
            backgroundColor = rememberHomeColors().neutral,
            iconColor = AccentOrange,
            onClick = onFavoritesClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Outlined.Download,
            label = "Downloads",
            backgroundColor = AccentOrangeSoft,
            iconColor = AccentOrange,
            onClick = onDownloadsClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Outlined.History,
            label = "History",
            backgroundColor = rememberHomeColors().neutral,
            iconColor = rememberHomeColors().neutralDark,
            onClick = onHistoryClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = rememberHomeColors().textPrimary
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit = {},
    showSeeAll: Boolean = true,
    trailingLabel: String? = null,
    onTrailingLabelClick: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 4 }
        )
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = rememberHomeColors().textPrimary
        )

        if (showSeeAll) {
        TextButton(
            onClick = onSeeAllClick,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Text(
                text = "See all",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = AccentOrange
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AccentOrange,
                modifier = Modifier.size(16.dp)
            )
        }
        } else if (trailingLabel != null) {
            TextButton(
                onClick = { onTrailingLabelClick?.invoke() },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentOrange
                )
            }
        } else if (actionIcon != null) {
            IconButton(
                onClick = { onActionClick?.invoke() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = title,
                    tint = rememberHomeColors().textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    }
}

/**
 * Clickable with a springy press-scale effect. Shares its
 * interaction source with the ripple so both stay in sync.
 */
@Composable
private fun Modifier.pressScaleClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "pressScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

@Composable
private fun HomeErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = rememberHomeColors().textSecondary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = rememberHomeColors().textSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun HomeErrorBanner(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AccentOrangeSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = rememberHomeColors().textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onRetry) {
                Text(text = "Retry", color = AccentOrangeDeep)
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = rememberHomeColors().textSecondary
                )
            }
        }
    }
}

@Composable
private fun RecentlyPlayedRow(
    songs: List<Song>,
    progressById: Map<String, Float> = emptyMap(),
    onSongClick: (Song) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(songs, key = { it.id }, contentType = { "song" }) { song ->
            RecentlyPlayedCard(
                song = song,
                progress = progressById[song.id],
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
private fun RecentlyPlayedCard(
    song: Song,
    progress: Float? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Album art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentOrange, AccentOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAsyncImage(
                    imageUrl = song.artworkUrl,
                    contentDescription = "Artwork for ${song.title}",
                    quality = ImageQuality.MEDIUM,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize()
                )
                if (progress != null && progress > 0f && progress < 1f) {
                    ProgressRingBadge(
                        progress = progress,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    )
                }
                PlayBadge(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }

            // Song info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = rememberHomeColors().textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickPicksGrid(
    songs: List<Song>,
    onSongClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        songs.chunked(2).forEach { rowSongs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSongs.forEach { song ->
                    QuickPickCard(
                        song = song,
                        onClick = { onSongClick(song) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowSongs.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickPickCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentOrange, AccentOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAsyncImage(
                    imageUrl = song.artworkUrl,
                    contentDescription = "Artwork for ${song.title}",
                    quality = ImageQuality.THUMBNAIL,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = rememberHomeColors().textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DailyMixHero(
    mix: DailyMix,
    mixNumber: Int,
    onPlayClick: () -> Unit
) {
    val artworks = remember(mix) {
        mix.songs.mapNotNull { it.artworkUrl }.distinct().take(3)
    }
    val artistsLine = remember(mix) {
        mix.songs.map { it.artist }.distinct().take(3).joinToString(", ")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(172.dp)
            .pressScaleClickable(onClick = onPlayClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AccentOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AccentOrange, AccentOrangeDeep)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CUSTOM MIX",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daily Mix $mixNumber",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (artistsLine.isNotBlank()) {
                        Text(
                            text = "$artistsLine, and more",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Play Mix pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .clickable(onClick = onPlayClick)
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = AccentOrangeDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Play Mix",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrangeDeep
                            )
                        }
                    }
                }
                // Stacked artwork covers
                if (artworks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        artworks.forEachIndexed { index, url ->
                            val offsetX = (index * -14).dp
                            val rotation = when (index) {
                                0 -> 8f
                                1 -> -6f
                                else -> 0f
                            }
                            Card(
                                modifier = Modifier
                                    .size(76.dp)
                                    .offset(x = offsetX)
                                    .graphicsLayer { rotationZ = rotation },
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                OptimizedAsyncImage(
                                    imageUrl = url,
                                    contentDescription = null,
                                    quality = ImageQuality.THUMBNAIL,
                                    shape = RectangleShape,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MixesRow(
    mixes: List<DailyMix>,
    onMixClick: (DailyMix) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(mixes, key = { it.id }, contentType = { "mix" }) { mix ->
            MixCard(
                mix = mix,
                onClick = { onMixClick(mix) }
            )
        }
    }
}

@Composable
private fun MixCard(
    mix: DailyMix,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AccentOrange, AccentOrangeDeep)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            OptimizedAsyncImage(
                imageUrl = mix.artworkUrl,
                contentDescription = "Artwork for ${mix.title}",
                quality = ImageQuality.MEDIUM,
                shape = RectangleShape,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = mix.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = mix.subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = rememberHomeColors().textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SongsRow(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedVisibilityScope: AnimatedVisibilityScope? = null,
    showPlayBadge: Boolean = true,
    showNewBadge: Boolean = false,
    posterAspect: Boolean = false
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(songs, key = { it.id }, contentType = { "song" }) { song ->
            SongCard(
                song = song,
                onClick = { onSongClick(song) },
                sharedTransitionScope = sharedTransitionScope,
                sharedVisibilityScope = sharedVisibilityScope,
                showPlayBadge = showPlayBadge,
                showNewBadge = showNewBadge,
                posterAspect = posterAspect
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SongCard(
    song: Song,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    sharedVisibilityScope: AnimatedVisibilityScope? = null,
    showPlayBadge: Boolean = false,
    showNewBadge: Boolean = false,
    posterAspect: Boolean = false
) {
    val artworkSharedModifier = if (sharedTransitionScope != null && sharedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                rememberSharedContentState(key = "player-artwork-${song.id}"),
                animatedVisibilityScope = sharedVisibilityScope
            )
        }
    } else {
        Modifier
    }
    Card(
        modifier = Modifier
            .width(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Album art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (posterAspect) 200.dp else 160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentOrange, AccentOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAsyncImage(
                    imageUrl = song.artworkUrl,
                    contentDescription = "Artwork for ${song.title}",
                    quality = ImageQuality.MEDIUM,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize().then(artworkSharedModifier)
                )
                if (showNewBadge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentOrange)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
                if (showPlayBadge) {
                    PlayBadge(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            }
            
            // Song info
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = rememberHomeColors().textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ArtistsRow(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(artists, key = { it.id }, contentType = { "artist" }) { artist ->
            ArtistCard(
                artist = artist,
                onClick = { onArtistClick(artist) }
            )
        }
    }
}

@Composable
private fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .pressScaleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist avatar with accent ring
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(AccentOrange)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AccentOrange, AccentOrangeDeep)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (artist.artworkUrl != null) {
                OptimizedAsyncImage(
                    imageUrl = artist.artworkUrl,
                    contentDescription = "Photo of ${artist.name}",
                    quality = ImageQuality.THUMBNAIL,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = artist.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AccentOrangeDeep,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlbumsRow(
    albums: List<com.reon.music.core.model.Album>,
    onAlbumClick: (com.reon.music.core.model.Album) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(albums, key = { it.id }, contentType = { "album" }) { album ->
            AlbumCard(
                album = album,
                onClick = { onAlbumClick(album) }
            )
        }
    }
}

@Composable
private fun AlbumCard(
    album: com.reon.music.core.model.Album,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentOrange, AccentOrangeDeep)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                OptimizedAsyncImage(
                    imageUrl = album.artworkUrl,
                    contentDescription = "Artwork for ${album.name}",
                    quality = ImageQuality.MEDIUM,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.labelSmall,
                    color = rememberHomeColors().textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlaylistsRow(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(playlists, key = { it.id }, contentType = { "playlist" }) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) }
            )
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(100.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (playlist.id.hashCode() % 5) {
                0 -> AccentOrangeDeep.copy(alpha = 0.2f)
                1 -> rememberHomeColors().neutralDark.copy(alpha = 0.2f)
                2 -> rememberHomeColors().neutralDark.copy(alpha = 0.2f)
                3 -> AccentOrange.copy(alpha = 0.2f)
                else -> AccentOrangeDeep.copy(alpha = 0.15f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = rememberHomeColors().textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${playlist.songCount} songs",
                    style = MaterialTheme.typography.labelSmall,
                    color = rememberHomeColors().textSecondary
                )
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(rememberHomeColors().surface),
                contentAlignment = Alignment.Center
            ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = AccentOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ChartsRow(
    charts: List<com.reon.music.ui.viewmodels.ChartSection>,
    onChartClick: (String, String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(charts, key = { it.id }, contentType = { "chart" }) { chart ->
            ChartCard(
                chart = chart,
                onClick = { onChartClick(chart.id, chart.title) }
            )
        }
    }
}

@Composable
private fun ChartCard(
    chart: com.reon.music.ui.viewmodels.ChartSection,
    onClick: () -> Unit
) {
    val gradientColors = when (chart.id.lowercase()) {
        "telugu" -> listOf(AccentOrange, AccentOrangeDeep)
        "tamil" -> listOf(rememberHomeColors().neutralDark, rememberHomeColors().textPrimary)
        "hindi" -> listOf(AccentOrangeDeep, rememberHomeColors().neutralDark)
        "international" -> listOf(AccentOrange, AccentOrangeDeep)
        else -> listOf(AccentOrange, rememberHomeColors().neutralDark)
    }
    
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(170.dp)
            .pressScaleClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rememberHomeColors().surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                    )
                )
        ) {
            // Cover art backdrop with bottom gradient overlay
            val coverUrl = remember(chart) {
                chart.songs.firstOrNull()?.artworkUrl
            }
            if (coverUrl != null) {
                OptimizedAsyncImage(
                    imageUrl = coverUrl,
                    contentDescription = null,
                    quality = ImageQuality.THUMBNAIL,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = chart.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${chart.songs.size} tracks",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

