/*
 * REON Music App - Main App Composable
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.reon.music.core.preferences.AppTheme
import com.reon.music.ui.theme.ReonTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Song
import com.reon.music.ui.components.MiniPlayer
import com.reon.music.ui.components.ReonBottomNavigation
import com.reon.music.ui.navigation.ReonDestination
import com.reon.music.ui.screens.ArtistDetailScreen
import com.reon.music.ui.screens.ArtistsScreen
import com.reon.music.ui.screens.ArtistDetailScreenRedesigned
import com.reon.music.ui.screens.ArtistsScreenEnhanced
import com.reon.music.ui.screens.ChartDetailScreen
import com.reon.music.ui.screens.DownloadsScreen
import com.reon.music.ui.screens.HomeScreen
import com.reon.music.ui.screens.LibraryScreen
import com.reon.music.ui.screens.NowPlayingScreen
import com.reon.music.ui.screens.PlaylistDetailScreen
import com.reon.music.ui.screens.PreferencesScreen
import com.reon.music.ui.screens.SearchScreen
import com.reon.music.ui.screens.SettingsScreen
import com.reon.music.ui.viewmodels.PlayerViewModel
import java.net.URLDecoder

/**
 * Main App Composable
 * Contains navigation, mini player (separated from bottom nav), and now playing screen
 */
@Composable
fun ReonApp(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    settingsViewModel: com.reon.music.ui.viewmodels.SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val playerState by playerViewModel.playerState.collectAsState()
    val playerUiState by playerViewModel.uiState.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Determine theme based on user preference
    val darkTheme = when (settingsUiState.theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.AMOLED -> true
        AppTheme.SYSTEM -> systemDarkTheme
    }
    
    val pureBlack = settingsUiState.theme == AppTheme.AMOLED
    
    var showNowPlaying by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle back button when NowPlaying screen is visible
    BackHandler(enabled = showNowPlaying) {
        showNowPlaying = false
    }
    
    // Show errors in snackbar
    LaunchedEffect(playerUiState.error) {
        playerUiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            playerViewModel.clearError()
        }
    }
    
    // Check if we're on a detail screen (hide bottom nav)
    val isDetailScreen = currentRoute?.startsWith("artist/") == true ||
                         currentRoute?.startsWith("chart/") == true ||
                         currentRoute?.startsWith("playlist/") == true
    
    // Check if mini player should be visible
    val showMiniPlayer = playerState.currentSong != null && !showNowPlaying
    
    ReonTheme(
        darkTheme = darkTheme,
        pureBlack = pureBlack,
        dynamicColor = settingsUiState.dynamicColors,
        themePresetId = settingsUiState.themePresetId,
        fontPresetId = settingsUiState.fontPresetId,
        fontSizePreset = settingsUiState.fontSizePreset
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    // Combined Mini Player + Bottom Navigation
                    // Mini Player is placed ABOVE the Bottom Navigation Bar (separated)
                    if (!showNowPlaying && !isDetailScreen) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Mini Player - Above Bottom Nav
                            if (showMiniPlayer) {
                                MiniPlayer(
                                    playerState = playerState,
                                    currentPosition = currentPosition,
                                    isLoading = false,
                                    onPlayPause = { playerViewModel.togglePlayPause() },
                                    onNext = { playerViewModel.skipToNext() },
                                    onPrevious = { playerViewModel.skipToPrevious() },
                                    onClick = { showNowPlaying = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                // Subtle divider between mini player and bottom nav
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = Color(0xFFE0E0E0)
                                )
                            }
                            
                            // Bottom Navigation Bar
                            ReonBottomNavigation(
                                destinations = ReonDestination.bottomNavDestinations,
                                currentDestination = navBackStackEntry?.destination,
                                onNavigate = { destination ->
                                    navController.navigate(destination.route) {
                                        popUpTo(ReonDestination.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    } else if (!showNowPlaying && isDetailScreen && showMiniPlayer) {
                        // On detail screens, only show mini player without bottom nav
                        MiniPlayer(
                            playerState = playerState,
                            currentPosition = currentPosition,
                            isLoading = false,
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onNext = { playerViewModel.skipToNext() },
                            onPrevious = { playerViewModel.skipToPrevious() },
                            onClick = { showNowPlaying = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(snackbarHostState)
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ReonDestination.Home.route
                    ) {
                        composable(ReonDestination.Home.route) {
                            HomeScreen(
                                navController = navController,
                                playerViewModel = playerViewModel,
                                onSettingsClick = {
                                    navController.navigate(ReonDestination.Settings.route)
                                },
                                onArtistClick = { artist ->
                                    navController.navigate(
                                        ReonDestination.ArtistDetail.createRoute(artist.id, artist.name)
                                    )
                                },
                                onChartClick = { chartType, chartTitle ->
                                    navController.navigate(
                                        ReonDestination.ChartDetail.createRoute(chartType, chartTitle)
                                    )
                                }
                            )
                        }
                        
                        composable(ReonDestination.Search.route) {
                            SearchScreen(
                                navController = navController,
                                modifier = Modifier,
                                onNavigateToPlayer = { song ->
                                    playerViewModel.playSong(song)
                                    showNowPlaying = true
                                },
                                onNavigateToLibrary = {
                                    navController.navigate(ReonDestination.Library.route)
                                }
                            )
                        }
                        
                        composable(ReonDestination.Artists.route) {
                            ArtistsScreenEnhanced(
                                navController = navController,
                                onNavigateToHome = {
                                    navController.navigate(ReonDestination.Home.route) {
                                        popUpTo(ReonDestination.Home.route) { inclusive = true }
                                    }
                                },
                                onArtistClick = { artist: Artist ->
                                    navController.navigate(
                                        ReonDestination.ArtistDetail.createRoute(artist.id, artist.name)
                                    )
                                },
                                onSongClick = { song: Song ->
                                    playerViewModel.playSong(song)
                                }
                            )
                        }
                        
                        composable(ReonDestination.Library.route) {
                            LibraryScreen(
                                playerViewModel = playerViewModel,
                                navController = navController
                            )
                        }
                        
                        composable(ReonDestination.Downloads.route) {
                            DownloadsScreen(
                                navController = navController,
                                playerViewModel = playerViewModel
                            )
                        }
                        
                        composable(ReonDestination.Settings.route) {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        
                        composable(ReonDestination.Preferences.route) {
                            PreferencesScreen()
                        }
                        
                        composable(
                            route = ReonDestination.ArtistDetail.route,
                            arguments = listOf(
                                navArgument("artistId") { type = NavType.StringType },
                                navArgument("artistName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                            val artistName = URLDecoder.decode(
                                backStackEntry.arguments?.getString("artistName") ?: "",
                                "UTF-8"
                            )
                            
                            // Use redesigned artist detail screen
                            ArtistDetailScreenRedesigned(
                                artist = Artist(
                                    id = artistId,
                                    name = artistName,
                                    artworkUrl = ""
                                ),
                                onBackClick = { navController.popBackStack() },
                                onSongClick = { song: Song ->
                                    playerViewModel.playSong(song)
                                },
                                onNavigateToArtistPage = { id: String ->
                                    // Already on artist page
                                }
                            )
                        }
                        
                        composable(
                            route = ReonDestination.ChartDetail.route,
                            arguments = listOf(
                                navArgument("chartType") { type = NavType.StringType },
                                navArgument("chartTitle") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val chartType = backStackEntry.arguments?.getString("chartType") ?: "chart"
                            val chartTitle = URLDecoder.decode(
                                backStackEntry.arguments?.getString("chartTitle") ?: "Chart",
                                "UTF-8"
                            )
                            
                            ChartDetailScreen(
                                chartTitle = chartTitle,
                                chartType = chartType,
                                onBackClick = { navController.popBackStack() },
                                onSongClick = { song ->
                                    playerViewModel.playSong(song)
                                }
                            )
                        }
                        
                        composable(
                            route = ReonDestination.PlaylistDetail.route,
                            arguments = listOf(
                                navArgument("playlistId") { type = NavType.StringType },
                                navArgument("playlistName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                            val playlistName = URLDecoder.decode(
                                backStackEntry.arguments?.getString("playlistName") ?: "Playlist",
                                "UTF-8"
                            )
                            
                            PlaylistDetailScreen(
                                playlist = com.reon.music.core.model.Playlist(
                                    id = playlistId,
                                    name = playlistName
                                ),
                                onBackClick = { navController.popBackStack() },
                                onSongClick = { song ->
                                    playerViewModel.playSong(song)
                                }
                            )
                        }
                    }
                }
            }
            
            // Now Playing Screen Overlay - Full Screen
            AnimatedVisibility(
                visible = showNowPlaying,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    playerViewModel = playerViewModel,
                    onDismiss = { showNowPlaying = false }
                )
            }
        }
    }
}
