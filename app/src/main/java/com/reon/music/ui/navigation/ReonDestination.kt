/*
 * REON Music App
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation destinations for REON app
 */
sealed class ReonDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : ReonDestination(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Search : ReonDestination(
        route = "search",
        title = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    data object Artists : ReonDestination(
        route = "artists",
        title = "Artists",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
    
    data object Library : ReonDestination(
        route = "library",
        title = "Library",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    )
    
    data object Downloads : ReonDestination(
        route = "downloads",
        title = "Downloads",
        selectedIcon = Icons.Filled.Download,
        unselectedIcon = Icons.Outlined.Download
    )
    
    // Settings
    data object Settings : ReonDestination(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    
    // Artist Detail Screen
    data object ArtistDetail : ReonDestination(
        route = "artist/{artistId}/{artistName}",
        title = "Artist",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ) {
        fun createRoute(artistId: String, artistName: String): String {
            return "artist/$artistId/${java.net.URLEncoder.encode(artistName, "UTF-8")}"
        }
    }
    
    // Chart Detail Screen
    data object ChartDetail : ReonDestination(
        route = "chart/{chartType}/{chartTitle}",
        title = "Chart",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ) {
        fun createRoute(chartType: String, chartTitle: String): String {
            return "chart/$chartType/${java.net.URLEncoder.encode(chartTitle, "UTF-8")}"
        }
    }
    
    // Playlist Detail Screen
    data object PlaylistDetail : ReonDestination(
        route = "playlist/{playlistId}/{playlistTitle}",
        title = "Playlist",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ) {
        fun createRoute(playlistId: String, playlistTitle: String): String {
            return "playlist/$playlistId/${java.net.URLEncoder.encode(playlistTitle, "UTF-8")}"
        }
    }
    
    // Album Detail Screen
    data object AlbumDetail : ReonDestination(
        route = "album/{albumName}/{artistName}",
        title = "Album",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ) {
        fun createRoute(albumName: String, artistName: String): String {
            return "album/${java.net.URLEncoder.encode(albumName, "UTF-8")}/${java.net.URLEncoder.encode(artistName, "UTF-8")}"
        }
    }
    
    data object FeaturedPlaylists : ReonDestination(
        route = "featured_playlists",
        title = "Featured Playlists",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    // Favorites Screen
    data object Favorites : ReonDestination(
        route = "favorites",
        title = "Favorites",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    // Most Played Screen
    data object MostPlayed : ReonDestination(
        route = "mostplayed",
        title = "Most Played",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    // Followed Screen
    data object Followed : ReonDestination(
        route = "followed",
        title = "Followed",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object History : ReonDestination(
        route = "history",
        title = "History",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Preferences : ReonDestination(
        route = "preferences",
        title = "Preferences",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    
    companion object {
        val bottomNavDestinations = listOf(Home, Search, Artists, Library, Downloads)
    }
}
