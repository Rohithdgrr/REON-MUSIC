/*
 * REON Music App - Search Data Models
 * Copyright (c) 2024 REON
 * Data models for search functionality
 */

package com.reon.music.ui.search

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*


data class SearchSuggestion(
    val text: String,
    val type: SuggestionType,
    val metadata: String? = null
)

enum class SuggestionType {
    TRENDING, RECENT, ARTIST, PERSONALIZED
}

data class SearchFilter(
    val id: String,
    val name: String,
    val icon: ImageVector
)

// Mock filter data for YouTubeMusicSearchUI
val availableFilters = listOf(
    SearchFilter("all", "All", Icons.Filled.Search),
    SearchFilter("songs", "Songs", Icons.Filled.MusicNote),
    SearchFilter("videos", "Videos", Icons.Filled.VideoFile),
    SearchFilter("artists", "Artists", Icons.Filled.Person),
    SearchFilter("albums", "Albums", Icons.Filled.Album),
    SearchFilter("playlists", "Playlists", Icons.Filled.QueueMusic)
)