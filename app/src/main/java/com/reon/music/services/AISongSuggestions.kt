/*
 * REON Music App - AI Song Suggestions
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.services

import android.util.Log
import com.reon.music.core.model.Song
import com.reon.music.data.database.dao.SongDao
import com.reon.music.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of an AI suggestion request.
 * Carries failures to the caller instead of silently returning an empty list.
 */
sealed interface SuggestionResult {
    data class Success(val songs: List<Song>) : SuggestionResult
    data class Error(val message: String, val cause: Throwable? = null) : SuggestionResult
}

/**
 * Pure merge step: combine candidate lists, deduplicate, drop the
 * currently playing song, shuffle and cap. Kept free of I/O so it
 * is unit-testable on the JVM.
 */
internal fun mergeSuggestions(
    vararg candidates: List<Song>,
    excludeSongId: String? = null,
    limit: Int = 20
): List<Song> {
    return candidates.flatMap { it }
        .distinctBy { it.id }
        .filter { excludeSongId == null || it.id != excludeSongId }
        .shuffled()
        .take(limit)
}

/**
 * AI Song Suggestions Manager
 * Provides intelligent song recommendations based on listening history
 * 
 * Uses collaborative filtering and content-based filtering
 */
@Singleton
class AISongSuggestions @Inject constructor(
    private val songDao: SongDao,
    private val repository: MusicRepository
) {
    companion object {
        private const val TAG = "AISongSuggestions"
        private const val SUGGESTION_LIMIT = 20
    }

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    /**
     * Get song suggestions based on current song
     */
    suspend fun getSuggestions(currentSong: Song): SuggestionResult {
        if (!_isEnabled.value) return SuggestionResult.Success(emptyList())

        return try {
            // Strategy 1: Get related songs from same artist
            val artistSongs = repository.getRelatedSongs(currentSong).getOrNull() ?: emptyList()
            
            // Strategy 2: Get songs from same genre
            val genreSongs = if (currentSong.genre.isNotBlank()) {
                repository.getSongsByGenre(currentSong.genre).getOrNull() ?: emptyList()
            } else {
                emptyList()
            }
            
            // Strategy 3: Get songs from listening history (collaborative filtering)
            val historySongs = songDao.getRecentlyPlayed(50)
                .first()
                .mapNotNull { it.toSong() }
                .filter { it.genre == currentSong.genre || it.artist == currentSong.artist }
            
            // Combine and deduplicate
            val allSuggestions = mergeSuggestions(
                artistSongs, genreSongs, historySongs,
                excludeSongId = currentSong.id
            )

            Log.d(TAG, "Generated ${allSuggestions.size} suggestions for ${currentSong.title}")
            SuggestionResult.Success(allSuggestions)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating suggestions", e)
            SuggestionResult.Error("Could not load suggestions", e)
        }
    }

    /**
     * Get personalized recommendations based on listening history
     */
    suspend fun getPersonalizedRecommendations(): SuggestionResult {
        if (!_isEnabled.value) return SuggestionResult.Success(emptyList())
        
        return try {
            // Get user's listening history
            val history = songDao.getRecentlyPlayed(100)
                .first()
                .mapNotNull { it.toSong() }
            
            if (history.isEmpty()) {
                // Fallback to trending songs
                return when (val trending = repository.getTrendingSongs().getOrNull()) {
                    null -> SuggestionResult.Error("Could not load trending songs")
                    else -> SuggestionResult.Success(trending)
                }
            }
            
            // Analyze listening patterns
            val topGenres = history.groupBy { it.genre }
                .filterKeys { it.isNotBlank() }
                .toList()
                .sortedByDescending { it.second.size }
                .take(3)
                .map { it.first }
            
            val topArtists = history.groupBy { it.artist }
                .toList()
                .sortedByDescending { it.second.size }
                .take(5)
                .map { it.first }
            
            // Get recommendations based on patterns
            val recommendations = mutableListOf<Song>()
            
            topGenres.forEach { genre ->
                repository.getSongsByGenre(genre).getOrNull()?.let {
                    recommendations.addAll(it)
                }
            }
            
            // Get songs from top artists (simplified - would need artist details endpoint)
            topArtists.forEach { artistName ->
                repository.searchSongsWithLimit("$artistName songs", 10).getOrNull()?.let {
                    recommendations.addAll(it)
                }
            }
            
            // Remove already played songs and deduplicate
            val playedIds = history.map { it.id }.toSet()
            val result = mergeSuggestions(recommendations)
                .filter { it.id !in playedIds }
                .take(SUGGESTION_LIMIT)
            SuggestionResult.Success(result)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating personalized recommendations", e)
            SuggestionResult.Error("Could not load recommendations", e)
        }
    }

    /**
     * Enable/disable AI suggestions
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.update { enabled }
    }
}

