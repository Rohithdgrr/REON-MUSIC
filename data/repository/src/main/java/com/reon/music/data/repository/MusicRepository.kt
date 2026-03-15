/*
 * REON Music App - Music Repository
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.repository

import com.reon.music.core.common.Result
import com.reon.music.core.model.Album
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Playlist
import com.reon.music.core.model.SearchResult
import com.reon.music.core.model.Song
import com.reon.music.core.model.SongSortOption
import com.reon.music.data.network.youtube.PipedClient
import com.reon.music.data.network.youtube.YouTubeMusicClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Music Repository
 * Combines JioSaavn and YouTube Music sources
 */
@Singleton
class MusicRepository @Inject constructor(
    private val youtubeMusicClient: YouTubeMusicClient,
    private val pipedClient: PipedClient,
    private val youtubeStreamUrlManager: YouTubeStreamUrlManager
) {
    /**
     * Search for songs - YouTube Music ONLY (JioSaavn temporarily disabled)
     */
    suspend fun searchSongs(query: String, page: Int = 1): Result<List<Song>> = coroutineScope {
        // JioSaavn DISABLED - Using YouTube Music only
        // val jiosaavnDeferred = async { jiosaavnClient.searchSongs(query, page) }
        val youtubeDeferred = async { youtubeMusicClient.searchSongs(query) }
        
        // Also try alternative search strategies for YouTube
        val youtubeAltDeferred = async { 
            // Try with "official" keyword
            youtubeMusicClient.searchSongs("$query official").getOrNull() ?: emptyList()
        }
        val youtubeMusicDeferred = async {
            // Try with "music" keyword
            youtubeMusicClient.searchSongs("$query music").getOrNull() ?: emptyList()
        }
        
        // val jiosaavnResult = jiosaavnDeferred.await()
        val youtubeResult = youtubeDeferred.await()
        val youtubeAlt = youtubeAltDeferred.await()
        val youtubeMusic = youtubeMusicDeferred.await()
        
        val songs = mutableListOf<Song>()
        
        // JioSaavn DISABLED
        // jiosaavnResult.getOrNull()?.let { songs.addAll(it) }
        
        // Add YouTube results - combine all YouTube searches
        youtubeResult.getOrNull()?.let { songs.addAll(it) }
        songs.addAll(youtubeAlt)
        songs.addAll(youtubeMusic)
        
        // Remove duplicates by ID
        val uniqueSongs = songs.distinctBy { it.id }
            .sortedByDescending { song ->
                // Boost songs whose movie/album/title closely match the query
                val q = query.lowercase()
                var score = 0
                if (song.movieName.isNotBlank() && song.movieName.lowercase().contains(q)) score += 4
                if (song.album.isNotBlank() && song.album.lowercase().contains(q)) score += 3
                if (song.title.lowercase().contains(q)) score += 2
                if (song.description.lowercase().contains(q)) score += 1
                score
            }
        
        if (uniqueSongs.isEmpty()) {
            // Return error if no YouTube results
            when {
                youtubeResult is Result.Error -> youtubeResult
                else -> Result.Success(emptyList())
            }
        } else {
            Result.Success(uniqueSongs)
        }
    }
    
    /**
     * Search songs flow - emits results as they come in
     */
    fun searchSongsFlow(query: String): Flow<List<Song>> = flow {
        youtubeMusicClient.searchSongs(query).getOrNull()?.let { emit(it) }
    }
    
    /**
     * Get stream URL for a song
     * Tries multiple sources with proper fallback chain
     */
    suspend fun getStreamUrl(song: Song): Result<String> {
        // Step 1: Try YouTubeStreamUrlManager (uses InnerTube API with caching)
        val youtubeUrl = youtubeStreamUrlManager.getStreamUrl(
            videoId = song.id,
            title = song.title,
            channelName = song.channelName
        )
        
        if (youtubeUrl != null && youtubeUrl.isNotBlank()) {
            return Result.Success(youtubeUrl)
        }
        
        // Step 2: Fallback to Piped API (more reliable for streaming)
        val pipedUrl = pipedClient.getStreamUrl(song.id).getOrNull()
        if (pipedUrl != null && pipedUrl.isNotBlank()) {
            return Result.Success(pipedUrl)
        }
        
        // Step 3: Last resort - try YouTubeMusicClient directly (bypass cache)
        val directUrl = youtubeMusicClient.getStreamUrl(song.id).getOrNull()
        if (directUrl != null && directUrl.isNotBlank()) {
            return Result.Success(directUrl)
        }
        
        // All methods failed
        return Result.Error(Exception("Could not get stream URL for: ${song.title}. Please check your internet connection and try again."))
    }
    
    /**
     * Get song details
     */
    suspend fun getSongDetails(songId: String, source: String): Result<Song?> {
        return youtubeMusicClient.getSongDetails(songId)
    }
    
    /**
     * Get album details
     */
    suspend fun getAlbumDetails(token: String): Result<Album?> {
        return Result.Success(null) // YouTube-only mode: no album endpoint
    }
    
    /**
     * Get playlist details
     */
    suspend fun getPlaylistDetails(token: String): Result<Playlist?> {
        val res = youtubeMusicClient.searchPlaylists(token, 1, 5)
        return when (res) {
            is Result.Success -> Result.Success(res.data.firstOrNull())
            is Result.Error -> res
            is Result.Loading -> Result.Success(null)
        }
    }
    
    /**
     * Get artist details
     */
    suspend fun getArtistDetails(token: String): Result<Artist?> {
        return Result.Success(null) // YouTube-only mode: artist details not implemented
    }
    
    /**
     * Get related songs - Enhanced to fetch ALL related songs
     */
    suspend fun getRelatedSongs(song: Song, limit: Int = 100): Result<List<Song>> = coroutineScope {
        try {
            val allRelatedSongs = mutableListOf<Song>()
            
            // Strategy 1: Get related songs from YouTube
            youtubeMusicClient.getRelatedSongs(song.id).getOrNull()?.let { allRelatedSongs.addAll(it) }
            
            // Strategy 2: Get songs from same artist
            if (song.artist.isNotBlank()) {
                val artistSongs = searchSongsWithLimit("${song.artist} songs", 30).getOrNull()
                artistSongs?.let { allRelatedSongs.addAll(it) }
            }
            
            // Strategy 3: Get songs from same album
            if (song.album.isNotBlank()) {
                val albumSongs = searchSongsWithLimit("${song.album} ${song.artist}", 20).getOrNull()
                albumSongs?.let { allRelatedSongs.addAll(it) }
            }
            
            // Strategy 4: Get songs from same genre
            if (song.genre.isNotBlank()) {
                val genreSongs = getSongsByGenre(song.genre).getOrNull()
                genreSongs?.let { allRelatedSongs.addAll(it) }
            }
            
            // Strategy 5: Get similar songs by searching title keywords
            val titleKeywords = song.title.split(" ").take(2)
            if (titleKeywords.isNotEmpty()) {
                val keywordQuery = titleKeywords.joinToString(" ")
                val similarSongs = searchSongsWithLimit(keywordQuery, 20).getOrNull()
                similarSongs?.let { allRelatedSongs.addAll(it) }
            }
            
            // Deduplicate and limit
            val uniqueSongs = allRelatedSongs
                .distinctBy { it.id }
                .filter { it.id != song.id }
                .take(limit)
            
            Result.Success(uniqueSongs)
        } catch (e: Exception) {
            Result.Error(Exception("Error fetching related songs: ${e.message}"))
        }
    }
    
    /**
     * Autocomplete search
     */
    suspend fun autocomplete(query: String): Result<SearchResult> {
        val songs = youtubeMusicClient.searchSongs(query).getOrNull().orEmpty()
        return Result.Success(SearchResult(songs = songs))
    }
    
    /**
     * Get trending songs (from JioSaavn)
     */
    suspend fun getTrendingSongs(): Result<List<Song>> = youtubeSongs("trending songs 2024", 20)
    
    /**
     * Get new releases
     */
    suspend fun getNewReleases(): Result<List<Song>> = youtubeSongs("latest songs 2024", 20)
    
    /**
     * Get Top 50 Hindi songs
     */
    suspend fun getTop50Hindi(): Result<List<Song>> = youtubeSongs("top 50 hindi songs", 50)
    
    /**
     * Get Top 100 songs
     */
    suspend fun getTop100(): Result<List<Song>> = youtubeSongs("top 100 bollywood songs", 50)
    
    /**
     * Get Telugu songs
     */
    suspend fun getTeluguSongs(): Result<List<Song>> = youtubeSongs("telugu songs 2024", 20)
    
    /**
     * Get Telugu Top songs
     */
    suspend fun getTeluguTop(): Result<List<Song>> = youtubeSongs("top telugu songs", 20)
    
    /**
     * Get Tamil songs
     */
    suspend fun getTamilSongs(): Result<List<Song>> = youtubeSongs("tamil songs 2024", 20)
    
    /**
     * Get Punjabi songs
     */
    suspend fun getPunjabiSongs(): Result<List<Song>> = youtubeSongs("punjabi songs 2024", 20)
    
    /**
     * Get English Top songs
     */
    suspend fun getEnglishSongs(): Result<List<Song>> = youtubeSongs("english pop songs 2024", 20)
    
    /**
     * Get Romantic songs
     */
    suspend fun getRomanticSongs(): Result<List<Song>> = youtubeSongs("romantic songs", 20)
    
    /**
     * Get Party/Dance songs
     */
    suspend fun getPartySongs(): Result<List<Song>> = youtubeSongs("party songs", 20)
    
    /**
     * Get Sad songs
     */
    suspend fun getSadSongs(): Result<List<Song>> = youtubeSongs("sad songs", 20)
    
    /**
     * Get Devotional songs
     */
    suspend fun getDevotionalSongs(): Result<List<Song>> = youtubeSongs("devotional songs", 20)
    
    /**
     * Get Lo-Fi/Chill songs
     */
    suspend fun getLofiSongs(): Result<List<Song>> = youtubeSongs("lofi songs", 20)
    
    /**
     * Get Workout songs
     */
    suspend fun getWorkoutSongs(): Result<List<Song>> = youtubeSongs("workout gym songs", 20)
    
    /**
     * Get 90s Retro songs
     */
    suspend fun getRetroSongs(): Result<List<Song>> = youtubeSongs("90s songs", 20)
    
    /**
     * Get Arijit Singh songs
     */
    suspend fun getArijitSinghSongs(): Result<List<Song>> = youtubeSongs("arijit singh songs", 20)
    
    /**
     * Get AR Rahman songs
     */
    suspend fun getARRahmanSongs(): Result<List<Song>> = youtubeSongs("ar rahman songs", 20)
    
    /**
     * Get Atif Aslam songs
     */
    suspend fun getAtifAslamSongs(): Result<List<Song>> = youtubeSongs("atif aslam songs", 20)
    
    /**
     * Get Shreya Ghoshal songs
     */
    suspend fun getShreyaGhoshalSongs(): Result<List<Song>> = youtubeSongs("shreya ghoshal songs", 20)
    
    /**
     * Get songs by language
     */
    suspend fun getSongsByLanguage(language: String): Result<List<Song>> = youtubeSongs("$language songs 2024", 30)
    
    /**
     * Get songs by genre
     */
    suspend fun getSongsByGenre(genre: String): Result<List<Song>> = youtubeSongs("$genre songs", 20)
    
    /**
     * Get songs by mood
     */
    suspend fun getSongsByMood(mood: String): Result<List<Song>> = youtubeSongs("$mood songs", 20)
    
    /**
     * Get featured playlists - Enhanced to fetch from both sources
     */
    suspend fun getFeaturedPlaylists(): Result<List<Playlist>> = coroutineScope {
        // Fetch playlists from multiple sources and queries
        val youtubeDeferred = async { youtubeMusicClient.searchPlaylists("trending playlists", 1, 20) }
        
        val allPlaylists = mutableListOf<Playlist>()
        
        // Add YouTube playlists
        youtubeDeferred.await().getOrNull()?.let { allPlaylists.addAll(it) }
        
        // Remove duplicates by ID
        val uniquePlaylists = allPlaylists.distinctBy { it.id }
        
        if (uniquePlaylists.isEmpty()) {
            Result.Success(emptyList())
        } else {
            Result.Success(uniquePlaylists.take(30)) // Return more playlists
        }
    }
    
    /**
     * Get top artists
     */
    suspend fun getTopArtists(): Result<List<Artist>> {
        return Result.Success(emptyList()) // YouTube-only mode: not yet implemented
    }
    
    /**
     * Search songs with custom limit - Enhanced to search both sources for endless results
     */
    suspend fun searchSongsWithLimit(query: String, limit: Int): Result<List<Song>> = coroutineScope {
        try {
            val youtubeDeferred = async { youtubeMusicClient.searchSongs(query) }
            
            val allSongs = mutableListOf<Song>()
            
            youtubeDeferred.await().getOrNull()?.let { songs ->
                allSongs.addAll(songs.take(limit))
            }
            
            if (allSongs.isEmpty()) {
                Result.Success(emptyList())
            } else {
                Result.Success(allSongs.distinctBy { it.id }.take(limit))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Search failed: ${e.message}"))
        }
    }
    
    /**
     * Search songs with live data streaming - emits results as they arrive
     */
    fun searchSongsLive(query: String, limit: Int = 30): Flow<List<Song>> = flow {
        try {
            // Emit initial results from YouTube Music
            val result = youtubeMusicClient.searchSongs(query)
            result.getOrNull()?.let { songs ->
                val limited = songs.distinctBy { it.id }.take(limit)
                if (limited.isNotEmpty()) {
                    emit(limited)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicRepository", "Live search error: ${e.message}", e)
        }
    }
    
    /**
     * Search songs with unlimited results from YouTube
     */
    fun searchSongsUnlimited(query: String, maxResults: Int = 1000): Flow<List<Song>> {
        return youtubeMusicClient.searchSongsUnlimited(query, maxResults)
    }
    
    /**
     * Sort songs by various criteria
     */
    fun sortSongs(songs: List<Song>, sortBy: SongSortOption): List<Song> {
        return when (sortBy) {
            SongSortOption.VIEWS -> songs.sortedByDescending { it.viewCount }
            SongSortOption.LIKES -> songs.sortedByDescending { it.likeCount }
            SongSortOption.CHANNEL_POPULARITY -> songs.sortedByDescending { it.channelSubscriberCount }
            SongSortOption.QUALITY -> songs.sortedByDescending { 
                when {
                    it.is320kbps -> 3
                    it.quality.contains("HD", ignoreCase = true) -> 2
                    it.quality.contains("4K", ignoreCase = true) -> 4
                    else -> 1
                }
            }
            SongSortOption.RECENT -> songs.sortedByDescending { it.uploadDate }
            SongSortOption.DURATION -> songs.sortedByDescending { it.duration }
            SongSortOption.TITLE -> songs.sortedBy { it.title.lowercase() }
            SongSortOption.ARTIST -> songs.sortedBy { it.artist.lowercase() }
            SongSortOption.DEFAULT -> songs
        }
    }
    
    /**
     * Get video metadata for a song
     */
    suspend fun getVideoMetadata(videoId: String): Result<com.reon.music.data.network.youtube.VideoMetadata> {
        return youtubeMusicClient.getVideoMetadata(videoId)
    }
    
    /**
     * Enhance songs with metadata
     */
    suspend fun enhanceSongsWithMetadata(songs: List<Song>): List<Song> = coroutineScope {
        songs.map { song ->
            if (song.source == "youtube" && song.viewCount == 0L) {
                // Fetch metadata if not already present
                val metadata = youtubeMusicClient.getVideoMetadata(song.id).getOrNull()
                metadata?.let {
                    song.copy(
                        viewCount = it.viewCount,
                        likeCount = it.likeCount,
                        channelName = it.channelName.ifEmpty { song.channelName },
                        channelId = it.channelId.ifEmpty { song.channelId },
                        uploadDate = it.uploadDate.ifEmpty { song.uploadDate },
                        description = it.description.ifEmpty { song.description },
                        // Add rich metadata from description parsing
                        year = it.releaseYear?.toString() ?: song.year,
                        // Store additional metadata in extras map
                        extras = song.extras + mapOfNotNull(
                            it.composer?.let { c -> "composer" to c },
                            it.lyricist?.let { l -> "lyricist" to l },
                            it.producer?.let { p -> "producer" to p },
                            it.musicLabel?.let { m -> "musicLabel" to m },
                            it.movieName?.let { m -> "movieName" to m }
                        ).toMap()
                    )
                } ?: song
            } else {
                song
            }
        }
    }
    
    /**
     * Helper function to create map from nullable pairs
     */
    private fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> {
        return pairs.filterNotNull().toMap()
    }

    
    /**
     * Search playlists - Enhanced to search both sources
     */
    suspend fun searchPlaylists(query: String): Result<List<Playlist>> = coroutineScope {
        val youtubeDeferred = async { youtubeMusicClient.searchPlaylists(query, 1, 20) }
        
        val allPlaylists = mutableListOf<Playlist>()
        
        youtubeDeferred.await().getOrNull()?.let { allPlaylists.addAll(it) }
        
        if (allPlaylists.isEmpty()) {
            Result.Success(emptyList())
        } else {
            Result.Success(allPlaylists.distinctBy { it.id }.take(30))
        }
    }
    
    /**
     * Prefetch stream URLs for upcoming songs in queue
     * Ensures smooth playback without loading delays
     */
    suspend fun prefetchQueueUrls(songs: List<Song>) {
        val youtubeVideos = songs.filter { it.source == "youtube" }.map { it.id }
        if (youtubeVideos.isNotEmpty()) {
            youtubeStreamUrlManager.prefetchUrls(youtubeVideos)
        }
    }
    
    /**
     * Refresh expiring stream URLs
     * Should be called periodically (e.g., every hour)
     */
    suspend fun refreshExpiringUrls() {
        youtubeStreamUrlManager.refreshExpiringSoon()
    }
    
    /**
     * Clean up expired cache entries
     * Frees up storage space
     */
    suspend fun cleanupExpiredCache() {
        youtubeStreamUrlManager.cleanupExpired()
    }
    
    /**
     * Get stream cache statistics
     */
    suspend fun getStreamCacheStats(): StreamCacheStats {
        return youtubeStreamUrlManager.getCacheStats()
    }
    
    /**
     * Search albums
     */
    suspend fun searchAlbums(query: String): Result<List<Album>> {
        return Result.Success(emptyList()) // YouTube-only mode: albums not implemented
    }
    
    /**
     * Search artists
     */
    suspend fun searchArtists(query: String): Result<List<Artist>> {
        return Result.Success(emptyList()) // YouTube-only mode: artists not implemented
    }
    
    /**
     * Get trending albums
     */
    suspend fun getTrendingAlbums(): Result<List<Album>> {
        return Result.Success(emptyList()) // YouTube-only mode: albums not implemented
    }

    private suspend fun youtubeSongs(query: String, limit: Int): Result<List<Song>> {
        return when (val res = youtubeMusicClient.searchSongs(query)) {
            is Result.Success -> Result.Success(res.data.take(limit))
            is Result.Error -> res
            is Result.Loading -> Result.Success(emptyList())
        }
    }
}
