/*
 * REON Music App - Home ViewModel
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reon.music.core.model.Album
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Playlist
import com.reon.music.core.model.Song
import com.reon.music.core.preferences.MusicSource
import com.reon.music.core.preferences.UserPreferences
import com.reon.music.data.database.dao.HistoryDao
import com.reon.music.data.database.dao.SongDao
import com.reon.music.data.repository.MusicRepository
import com.reon.music.data.network.youtube.IndianMusicChannels
import com.reon.music.data.database.entities.DownloadState
import com.reon.music.data.database.entities.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents a chart section with title and songs
 */
data class ChartSection(
    val id: String,
    val title: String,
    val songs: List<Song> = emptyList()
)

/**
 * Represents a genre for selection
 */
data class Genre(
    val id: String,
    val name: String,
    val iconName: String,  // Icon identifier instead of emoji
    val accentColor: Int,
    val searchQuery: String = name
)

/**
 * UI State for Home Screen
 */
data class HomeUiState(
    // Quick Picks - User's recently played
    val quickPicksSongs: List<Song> = emptyList(),
    val quickPicksArtists: List<Artist> = emptyList(),
    val quickPicksPlaylists: List<Playlist> = emptyList(),
    
    // Most Played
    val mostPlayedSongs: List<Song> = emptyList(),
    
    // Recently Played
    val recentlyPlayedSongs: List<Song> = emptyList(),
    
    // All Time Favorites (most liked songs)
    val allTimeFavoriteSongs: List<Song> = emptyList(),
    
    // Indian Songs Playlists
    val indianPlaylists: List<Playlist> = emptyList(),
    
    // Telugu Section
    val teluguSongs: List<Song> = emptyList(),
    val teluguPlaylists: List<Playlist> = emptyList(),
    val teluguDjSongs: List<Song> = emptyList(),
    
    // ST Banjara Songs
    val banjaraSongs: List<Song> = emptyList(),
    
    // Indian Playlists - Love & Romance
    val loveSongs: List<Song> = emptyList(),
    val romanticHits: List<Song> = emptyList(),
    val weddingSongs: List<Song> = emptyList(),
    
    // Heartbreak & Sad Songs
    val heartbreakSongs: List<Song> = emptyList(),
    val sadSongPlaylist: List<Song> = emptyList(),
    
    // Party & Dance
    val partyHits: List<Song> = emptyList(),
    val djRemixes: List<Song> = emptyList(),
    val danceFloor: List<Song> = emptyList(),
    
    // Mood Categories
    val romanticSongs: List<Song> = emptyList(),
    val partySongs: List<Song> = emptyList(),
    val sadSongs: List<Song> = emptyList(),
    val lofiSongs: List<Song> = emptyList(),
    val devotionalSongs: List<Song> = emptyList(),
    val workoutSongs: List<Song> = emptyList(),
    val retroSongs: List<Song> = emptyList(),
    
    // Artist Spotlight
    val arijitSinghSongs: List<Song> = emptyList(),
    val arRahmanSongs: List<Song> = emptyList(),
    val shreyaGhoshalSongs: List<Song> = emptyList(),
    val sidSriram: List<Song> = emptyList(),
    val anirudhSongs: List<Song> = emptyList(),
    val kanikKapoor: List<Song> = emptyList(),
    val badshah: List<Song> = emptyList(),
    val honeysingh: List<Song> = emptyList(),
    
    // Telugu Playlists (YouTube-only)
    val teluguPlaylistsYoutube: List<Playlist> = emptyList(),
    val teluguSongsYoutube: List<Song> = emptyList(),
    
    // Indian Playlists (YouTube-only)
    val indianPlaylistsYoutube: List<Playlist> = emptyList(),
    val indianSongsYoutube: List<Song> = emptyList(),
    
    // Language-specific Playlists
    val bhojpuriSongs: List<Song> = emptyList(),
    val malayalamSongs: List<Song> = emptyList(),
    val kannadaSongs: List<Song> = emptyList(),
    val marathiSongs: List<Song> = emptyList(),
    val bengaliSongs: List<Song> = emptyList(),
    val gujaratiSongs: List<Song> = emptyList(),
    val rajasthaniSongs: List<Song> = emptyList(),
    
    // Mix & Trending
    val mixSongs: List<Song> = emptyList(),
    val viralHits: List<Song> = emptyList(),
    val reelsTrending: List<Song> = emptyList(),
    
    // Playlists
//    val teluguPlaylistCollection: List<Playlist> = emptyList(),
//    val hindiPlaylistCollection: List<Playlist> = emptyList(),
//    val tamilPlaylistCollection: List<Playlist> = emptyList(),
    val featuredPlaylists: List<Playlist> = emptyList(),
    val moodPlaylists: List<Playlist> = emptyList(),
    val trendingPlaylists: List<Playlist> = emptyList(),
    
    // New Releases
    val newReleases: List<Song> = emptyList(),
    val newAlbums: List<Album> = emptyList(),
    
    // Albums
    val trendingAlbums: List<Album> = emptyList(),
    
    // Artists
    val topArtists: List<Artist> = emptyList(),
    val recommendedArtists: List<Artist> = emptyList(),
    val indianArtists: List<Artist> = emptyList(),
    
    // Charts
    val charts: List<ChartSection> = emptyList(),
    val chartSongs: List<Song> = emptyList(),
    val top50Hindi: List<Song> = emptyList(),
    val top50English: List<Song> = emptyList(),
    val top50Telugu: List<Song> = emptyList(),
    val top50Tamil: List<Song> = emptyList(),
    val top50Punjabi: List<Song> = emptyList(),
    
    // Language/Region Sections
    val hindiSongs: List<Song> = emptyList(),
    val tamilSongs: List<Song> = emptyList(),
    val punjabiSongs: List<Song> = emptyList(),
    val englishSongs: List<Song> = emptyList(),
    
    // Mood/Genre Sections
    val moods: List<Genre> = defaultMoods,
    val genres: List<Genre> = defaultGenres,
    val selectedGenre: Genre? = null,
    val genreSongs: List<Song> = emptyList(),
    
    
    // NEW: Curated Playlist Categories
    val mostListeningTeluguSongs: List<Song> = emptyList(),
    val mostListeningHindiSongs: List<Song> = emptyList(),
    val allTimeFavoriteTeluguSongs: List<Song> = emptyList(),
    val allTimeFavoriteHindiSongs: List<Song> = emptyList(),
    val popularIndianSongs: List<Song> = emptyList(),
    val mostListeningIndianSongs: List<Song> = emptyList(),
    val everGreenHindiSongs: List<Song> = emptyList(),
    val everGreenTeluguSongs: List<Song> = emptyList(),
    val top100IndianSongs: List<Song> = emptyList(),
    
    // NEW: Followed Artists
    val followedArtists: List<Artist> = emptyList(),
    
    // International Songs
    val internationalSongs: List<Song> = emptyList(),
    val internationalHits: List<Song> = emptyList(),
    val globalTop50: List<Song> = emptyList(),
    val englishPopSongs: List<Song> = emptyList(),
    val edm: List<Song> = emptyList(),
    val kpopSongs: List<Song> = emptyList(),
    
    // All Time Favorites - User curated
    val allTimeFavorites: List<Song> = emptyList(),
    
    // Most Listening Categories
    val mostListeningSongs: List<Song> = emptyList(),
    val trendingNowSongs: List<Song> = emptyList(),
    
    // Additional Artists Collections
    val spbSongs: List<Song> = emptyList(),
    val lataMangeshkarSongs: List<Song> = emptyList(),
    val kishorKumarSongs: List<Song> = emptyList(),
    val mohammedRafiSongs: List<Song> = emptyList(),
    val aborubaPattuSongs: List<Song> = emptyList(),
    
    // Indian Artists - Popular
    val anupamKher: List<Song> = emptyList(),
    val pritam: List<Song> = emptyList(),
    val harishJeyaraj: List<Song> = emptyList(),
    val shaileshoSongs: List<Song> = emptyList(),
    val vijaySongs: List<Song> = emptyList(),
    val dspSongs: List<Song> = emptyList(),
    val manisharma: List<Song> = emptyList(),
    val thamanSongs: List<Song> = emptyList(),
    
    // Top 500 Indian Music Channels - Priority Songs
    val priorityChannelSongs: List<Song> = emptyList(),
    val topTierSongs: List<Song> = emptyList(), // Top 20 channels (Tier 1)
    val verifiedChannelSongs: List<Song> = emptyList(), // Songs from verified Top 500 channels
    
    // State
    val isLoading: Boolean = true,
    val error: String? = null,
    val preferredSource: MusicSource = MusicSource.BOTH

) {
    companion object {
        val defaultMoods = listOf(
            Genre("chill", "Chill", "beach_access", 0xFFFF9800.toInt()),
            Genre("feelgood", "Feel good", "sentiment_very_satisfied", 0xFF2196F3.toInt()),
            Genre("commute", "Commute", "directions_car", 0xFF3F51B5.toInt()),
            Genre("focus", "Focus", "center_focus_strong", 0xFF607D8B.toInt()),
            Genre("energize", "Energize", "flash_on", 0xFFE91E63.toInt()),
            Genre("gaming", "Gaming", "videogame_asset", 0xFF795548.toInt())
        )

        val defaultGenres = listOf(
            Genre("african", "African", "public", 0xFF4CAF50.toInt()),
            Genre("bhojpuri", "Bhojpuri", "music_note", 0xFFBA68C8.toInt()),
            Genre("arabic", "Arabic", "star", 0xFF81C784.toInt()),
            Genre("carnatic", "Carnatic classical", "music_note", 0xFF9C27B0.toInt()),
            Genre("bengali", "Bengali", "music_note", 0xFF00BCD4.toInt()),
            Genre("classical", "Classical", "piano", 0xFF673AB7.toInt()),
            Genre("pop", "Pop", "mic", 0xFFE91E63.toInt()),
            Genre("rock", "Rock", "guitar", 0xFF9C27B0.toInt())
        )
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val userPreferences: UserPreferences,
    private val historyDao: HistoryDao,
    private val songDao: SongDao,
    private val recommendationDataSource: com.reon.music.data.repository.RecommendationDataSource
) : ViewModel() {
    
    companion object {
        private const val TAG = "HomeViewModel"
        private const val AUTO_UPDATE_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private var lastUpdateTime: Long = 0L
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        // Observe preferred source
        viewModelScope.launch {
            userPreferences.preferredSource.collect { source ->
                _uiState.value = _uiState.value.copy(preferredSource = source)
            }
        }
        // Check if auto-update is needed on init
        checkAndLoadHomeContent()
    }
    
    /**
     * Check if content needs to be refreshed (24 hours elapsed) and load if needed
     */
    private fun checkAndLoadHomeContent() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastUpdate = currentTime - lastUpdateTime
        
        // Load if first time (lastUpdateTime == 0) or if 24 hours have passed
        if (lastUpdateTime == 0L || timeSinceLastUpdate >= AUTO_UPDATE_INTERVAL_MS) {
            Log.d(TAG, "Auto-updating home content. Time since last update: ${timeSinceLastUpdate / 1000 / 60 / 60} hours")
            loadHomeContent()
        } else {
            Log.d(TAG, "Skipping auto-update. Content is fresh. Time since last update: ${timeSinceLastUpdate / 1000 / 60 / 60} hours")
        }
    }
    
    /**
     * Public refresh function - forces reload of home content
     */
    fun refresh() {
        Log.d(TAG, "Manual refresh triggered")
        loadHomeContent()
    }
    
    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "Loading home content...")
            
            try {
                // Load Quick Picks (recently played) from database
                loadQuickPicks()
                
                // Load Most Played
                loadMostPlayed()
                
                // Load primary sections in parallel
                val trendingDeferred = async { repository.getTrendingSongs() }
                val newReleasesDeferred = async { repository.getNewReleases() }
                val playlistsDeferred = async { repository.getFeaturedPlaylists() }
                val albumsDeferred = async { repository.getTrendingAlbums() }
                
                val trendingResult = trendingDeferred.await()
                val newReleasesResult = newReleasesDeferred.await()
                val playlistsResult = playlistsDeferred.await()
                val albumsResult = albumsDeferred.await()
                
                _uiState.value = _uiState.value.copy(
                    newReleases = newReleasesResult.getOrNull() ?: emptyList(),
                    featuredPlaylists = (playlistsResult.getOrNull() ?: emptyList()).filter { playlist ->
                        // Filter out problematic playlists that cause crashes
                        val hasValidSongs = playlist.songCount > 0
                        val isNotProblematic = !playlist.name.contains("1990s", ignoreCase = true) &&
                                              !playlist.name.contains("2000s", ignoreCase = true) &&
                                              !playlist.name.contains("Hindi 1990", ignoreCase = true) &&
                                              !playlist.name.contains("Hindi 2000", ignoreCase = true)
                        hasValidSongs && isNotProblematic
                    },
                    trendingAlbums = albumsResult.getOrNull() ?: emptyList(),
                    isLoading = false
                )
                
                Log.d(TAG, "Primary content loaded")
                
                // Update last update time after successful load
                lastUpdateTime = System.currentTimeMillis()
                
                // Load secondary sections in background
                loadSecondarySections()
                loadCharts()
                loadArtists()
                loadLanguageSections()
                loadMoodSections()
                loadArtistSpotlights()
                loadIndianPlaylists()
                loadRecentlyPlayed()
                
                // Load Top 500 Indian Music Channels priority content
                loadPriorityChannels()
                loadVerifiedChannelSongs()

                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading home content", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load content"
                )
            }
        }
    }

    /**
     * Check if a song is already downloaded
     */
    suspend fun isSongDownloaded(songId: String): Boolean {
        return try {
            val song = songDao.getSongById(songId)
            song?.downloadState == DownloadState.DOWNLOADED && 
            !song.localPath.isNullOrBlank() && 
            java.io.File(song.localPath!!).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Mark a playlist download in DB for offline availability
     */
    fun markPlaylistDownloaded(id: String, title: String, songs: List<Song>) {
        viewModelScope.launch {
            try {
                songs.forEach { song ->
                    val existing = songDao.getSongById(song.id)
                    if (existing == null) songDao.insert(SongEntity.fromSong(song))
                    songDao.updateDownloadState(song.id, DownloadState.DOWNLOADING, null)
                }
                Log.d(TAG, "Playlist '$title' queued for download (${songs.size} songs)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark playlist downloaded", e)
            }
        }
    }

    /**
     * Mark an artist collection download in DB
     */
    fun markArtistDownloaded(artist: Artist, songs: List<Song>) {
        viewModelScope.launch {
            try {
                songs.forEach { song ->
                    val existing = songDao.getSongById(song.id)
                    if (existing == null) songDao.insert(SongEntity.fromSong(song))
                    songDao.updateDownloadState(song.id, DownloadState.DOWNLOADING, null)
                }
                Log.d(TAG, "Artist '${artist.name}' queued for download (${songs.size} songs)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mark artist downloaded", e)
            }
        }
    }
    
    private suspend fun loadQuickPicks() {
        try {
            val recentSongs = recommendationDataSource.getQuickPicks(limit = 30).first()
            _uiState.value = _uiState.value.copy(quickPicksSongs = recentSongs)
            Log.d(TAG, "Loaded ${recentSongs.size} quick picks for 5x6 grid")
            
            // Get unique artists from recent songs
            val recentArtistNames = recentSongs.map { it.artist }.distinct().take(10)
            val artists = recentArtistNames.map { name ->
                Artist(id = name.hashCode().toString(), name = name, artworkUrl = null)
            }
            _uiState.value = _uiState.value.copy(quickPicksArtists = artists)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quick picks", e)
        }
    }
    
    private suspend fun loadMostPlayed() {
        try {
            val songs = recommendationDataSource.getMostPlayed(limit = 15).first()
            _uiState.value = _uiState.value.copy(mostPlayedSongs = songs)
            Log.d(TAG, "Loaded ${songs.size} most played songs")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading most played", e)
        }
    }
    
    private fun loadSecondarySections() {
        // Telugu Songs
        viewModelScope.launch {
            try {
                repository.getTeluguSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(teluguSongs = songs)
                    Log.d(TAG, "Loaded ${songs.size} Telugu songs")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Telugu songs", e)
            }
        }
        
        
        // ST Banjara/Lambadi - YouTube-only (Channel-filtered)
        viewModelScope.launch {
            try {
                val allowedChannels = listOf(
                    "banjara",
                    "lambadi",
                    "lambani",
                    "st banjara",
                    "banjara beats",
                    "banjara folk",
                    "banjara dj",
                    "tribal telugu"
                )
                val allowedLower = allowedChannels.map { it.lowercase() }

                // Build targeted queries using channel keywords + generic fallbacks
                val channelQueries = allowedChannels.map { "$it songs" }
                val fallbackQueries = listOf(
                    "lambadi songs indian tribal",
                    "banjara folk songs telugu",
                    "lambani traditional music",
                    "banjara dj songs",
                    "lambadi songs"
                )

                val collected = mutableListOf<Song>()

                fun channelMatch(song: Song): Boolean {
                    val ch = song.channelName.lowercase()
                    return allowedLower.any { key -> ch.contains(key) }
                }

                // First pass: channel-focused queries
                for (q in channelQueries) {
                    repository.searchSongsWithLimit(q, 20).getOrNull()?.let { songs ->
                        val filtered = songs.filter {
                            it.source.equals("youtube", true) && channelMatch(it)
                        }
                        collected += filtered
                    }
                    if (collected.size >= 20) break
                }

                // Fallback: generic tribal queries if not enough results
                if (collected.size < 12) {
                    for (q in fallbackQueries) {
                        repository.searchSongsWithLimit(q, 20).getOrNull()?.let { songs ->
                            val filtered = songs.filter { it.source.equals("youtube", true) }
                            collected += filtered
                        }
                        if (collected.size >= 20) break
                    }
                }

                val finalList = collected
                    .distinctBy { it.id }
                    .take(20)

                _uiState.value = _uiState.value.copy(banjaraSongs = finalList)
                Log.d(TAG, "Loaded ${finalList.size} Banjara/Lambadi songs (YouTube-only, channel-filtered)")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Banjara songs", e)
            }
        }
        
        // Telugu Playlists (curated)
        viewModelScope.launch {
            try {
                val teluguQueries = listOf(
                    "latest telugu hits playlist",
                    "telugu 2000s classics",
                    "telugu dj party mix",
                    "telugu romantic songs playlist",
                    "telugu workout hype mix"
                )
                val teluguPlaylists = mutableListOf<Playlist>()
                teluguQueries.forEach { query ->
                    repository.searchPlaylists(query).getOrNull()?.let { results ->
                        teluguPlaylists += results
                    }
                }
                val curatedTelugu = teluguPlaylists
                    .distinctBy { it.id }
                    .filter { it.name.isNotBlank() }
                    .take(12)
                if (curatedTelugu.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(teluguPlaylistsYoutube = curatedTelugu)
                }
                Log.d(TAG, "Loaded ${curatedTelugu.size} curated Telugu playlists")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Telugu playlists", e)
            }
        }

        // Telugu Songs (YouTube-only)
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("telugu music 2024", 20).getOrNull()?.let { songs ->
                    val youtubeOnly = songs.filter { it.source.equals("youtube", ignoreCase = true) }
                    _uiState.value = _uiState.value.copy(teluguSongsYoutube = youtubeOnly)
                    Log.d(TAG, "Loaded ${youtubeOnly.size} Telugu songs (YouTube-only)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Telugu songs YouTube", e)
            }
        }
        
        // Indian Playlists (curated)
        viewModelScope.launch {
            try {
                val panIndiaQueries = listOf(
                    "all time bollywood romance playlist",
                    "indian indie chill playlist",
                    "latest hindi tamil telugu mashup",
                    "party anthems india",
                    "indian devotional morning playlist",
                    "indian workout pump playlist"
                )
                val indianPlaylists = mutableListOf<Playlist>()
                panIndiaQueries.forEach { query ->
                    repository.searchPlaylists(query).getOrNull()?.let { results ->
                        indianPlaylists += results
                    }
                }
                val curatedIndian = indianPlaylists
                    .distinctBy { it.id }
                    .filter { it.name.isNotBlank() }
                    .take(12)
                if (curatedIndian.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(indianPlaylistsYoutube = curatedIndian)
                }
                Log.d(TAG, "Loaded ${curatedIndian.size} curated Indian playlists")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Indian playlists", e)
            }
        }
        
        // Indian Songs (YouTube-only)
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("indian songs 2024", 20).getOrNull()?.let { songs ->
                    val youtubeOnly = songs.filter { it.source.equals("youtube", ignoreCase = true) }
                    _uiState.value = _uiState.value.copy(indianSongsYoutube = youtubeOnly)
                    Log.d(TAG, "Loaded ${youtubeOnly.size} Indian songs (YouTube-only)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Indian songs YouTube", e)
            }
        }
        
        // Mood Playlists
        viewModelScope.launch {
            try {
                repository.searchPlaylists("mood chill relax").getOrNull()?.let { playlists ->
                    _uiState.value = _uiState.value.copy(
                        moodPlaylists = playlists.filter { playlist ->
                            // Filter out problematic playlists
                            val hasValidSongs = playlist.songCount > 0
                            val isNotProblematic = !playlist.name.contains("2000s", ignoreCase = true) &&
                                                  !playlist.name.contains("2020s", ignoreCase = true) &&
                                                  !playlist.name.contains("1990s", ignoreCase = true)
                            hasValidSongs && isNotProblematic
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading mood playlists", e)
            }
        }
        
        // New Albums
        viewModelScope.launch {
            try {
                repository.searchAlbums("new 2024").getOrNull()?.let { albums ->
                    _uiState.value = _uiState.value.copy(newAlbums = albums)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading new albums", e)
            }
        }
    }
    
    private fun loadCharts() {
        // Top 50 Hindi
        viewModelScope.launch {
            try {
                repository.getTop50Hindi().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top50Hindi = songs)
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top 50 Hindi", e)
            }
        }
        
        // Top 50 English
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("top english songs 2024", 50).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top50English = songs)
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top 50 English", e)
            }
        }
        
        // Top 50 Telugu
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("top telugu songs 2024", 50).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top50Telugu = songs)
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top 50 Telugu", e)
            }
        }
        
        // Top 50 Tamil
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("top tamil songs 2024", 50).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top50Tamil = songs)
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top 50 Tamil", e)
            }
        }
        
        // Top 50 Punjabi
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("top punjabi songs 2024", 50).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top50Punjabi = songs)
                    updateCharts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top 50 Punjabi", e)
            }
        }
    }
    
    private fun updateCharts() {
        val state = _uiState.value
        val charts = mutableListOf<ChartSection>()
        
        if (state.top50Hindi.isNotEmpty()) {
            charts.add(ChartSection("hindi", "Top 50 Hindi", state.top50Hindi))
        }
        if (state.top50English.isNotEmpty()) {
            charts.add(ChartSection("english", "Top 50 English", state.top50English))
        }
        if (state.top50Telugu.isNotEmpty()) {
            charts.add(ChartSection("telugu", "Top 50 Telugu", state.top50Telugu))
        }
        if (state.top50Tamil.isNotEmpty()) {
            charts.add(ChartSection("tamil", "Top 50 Tamil", state.top50Tamil))
        }
        if (state.top50Punjabi.isNotEmpty()) {
            charts.add(ChartSection("punjabi", "Top 50 Punjabi", state.top50Punjabi))
        }
        
        _uiState.value = state.copy(charts = charts)
    }
    
    private fun loadArtists() {
        viewModelScope.launch {
            try {
                val extendedArtists = listOf(
                    // Telugu
                    "Ghantasala", "M.M. Keeravani", "Mani Sharma", "Devi Sri Prasad", "Thaman S",
                    "Koti", "Chakri", "Anup Rubens", "R.P. Patnaik", "S.P. Balasubrahmanyam",
                    "K.S. Chithra", "Sid Sriram", "Sunitha Upadrashta", "Geetha Madhuri",
                    "Hemachandra", "Mangli", "Kaala Bhairava",
                    // Hindi
                    "R.D. Burman", "Laxmikant–Pyarelal", "Naushad", "Anand–Milind", "A.R. Rahman",
                    "Pritam", "Vishal–Shekhar", "Shankar–Ehsaan–Loy", "Anu Malik", "Amit Trivedi",
                    "Ajay–Atul", "Lata Mangeshkar", "Asha Bhosle", "Kishore Kumar", "Mohammed Rafi",
                    "Arijit Singh", "Shreya Ghoshal", "Sonu Nigam", "Alka Yagnik", "Neha Kakkar",
                    "Jubin Nautiyal", "Badshah",
                    // Tamil
                    "Ilaiyaraaja", "Yuvan Shankar Raja", "Harris Jayaraj", "Santhosh Narayanan",
                    "D. Imman", "Anirudh Ravichander", "K.J. Yesudas", "Chinmayi", "Shweta Mohan",
                    "Dhee", "Andrea Jeremiah", "Haricharan",
                    // Pan-Indian
                    "G.V. Prakash Kumar", "Himesh Reshammiya", "Ankit Tiwari", "Jeet Gannguli",
                    "Bappi Lahiri", "Adnan Sami", "Amaal Mallik", "Salim–Sulaiman", "Udit Narayan",
                    "Kumar Sanu", "Palak Muchhal", "Sukhwinder Singh", "Ravi Basrur", "Vijay Yesudas",
                    "Ranjith", "Shankar Mahadevan",
                    // International
                    "Quincy Jones", "Max Martin", "Pharrell Williams", "Rick Rubin", "David Guetta",
                    "Hans Zimmer", "Michael Jackson", "Madonna", "Elvis Presley", "Taylor Swift",
                    "Adele", "Ed Sheeran", "Beyoncé", "Rihanna", "Bruno Mars", "Freddie Mercury",
                    "Whitney Houston", "Lady Gaga", "The Beatles", "Eminem", "Justin Bieber"
                ).distinct().map { name ->
                    Artist(id = name.hashCode().toString(), name = name, artworkUrl = null)
                }

                repository.getTopArtists().getOrNull()?.let { artists ->
                    val combined = (extendedArtists + artists).distinctBy { it.name.lowercase() }
                    _uiState.value = _uiState.value.copy(topArtists = combined)
                    Log.d(TAG, "Loaded ${combined.size} Top Artists (extended)")
                } ?: run {
                    _uiState.value = _uiState.value.copy(topArtists = extendedArtists)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Top Artists", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.searchArtists("recommended indian artists").getOrNull()?.let { artists ->
                    _uiState.value = _uiState.value.copy(recommendedArtists = artists)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recommended artists", e)
            }
        }
    }
    
    private fun loadLanguageSections() {
        // Hindi
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("latest hindi songs", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(hindiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Hindi songs", e)
            }
        }
        
        // Tamil
        viewModelScope.launch {
            try {
                repository.getTamilSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(tamilSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Tamil songs", e)
            }
        }
        
        // Punjabi
        viewModelScope.launch {
            try {
                repository.getPunjabiSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(punjabiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Punjabi songs", e)
            }
        }
        
        // English
        viewModelScope.launch {
            try {
                repository.getEnglishSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(englishSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading English songs", e)
            }
        }
    }
    
    private fun loadMoodSections() {
        viewModelScope.launch {
            try {
                repository.getRomanticSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(romanticSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Romantic songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getPartySongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(partySongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Party songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getSadSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(sadSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Sad songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getLofiSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(lofiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Lo-Fi songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getDevotionalSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(devotionalSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Devotional songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getWorkoutSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(workoutSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Workout songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getRetroSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(retroSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Retro songs", e)
            }
        }
    }
    
    /**
     * Load songs from Top 500 Indian Music Channels
     * Fetches songs from priority channels and filters them
     */
    private fun loadPriorityChannels() {
        viewModelScope.launch {
            try {
                // Get top priority channels (Top 20 - Tier 1)
                val topChannels = IndianMusicChannels.TOP_100_CHANNELS.take(20)
                val allPrioritySongs = mutableListOf<Song>()
                
                // Search for songs from top channels
                topChannels.take(5).forEach { channel ->
                    try {
                        repository.searchSongsWithLimit("${channel.name} latest songs", 10).getOrNull()?.let { songs ->
                            // Filter songs from this specific channel
                            val channelSongs = songs.filter { 
                                it.channelName.contains(channel.name, ignoreCase = true) ||
                                it.artist.contains(channel.name, ignoreCase = true)
                            }
                            allPrioritySongs.addAll(channelSongs)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading songs from ${channel.name}", e)
                    }
                }
                
                // Remove duplicates and update state
                val uniquePrioritySongs = allPrioritySongs.distinctBy { it.id }
                _uiState.value = _uiState.value.copy(
                    priorityChannelSongs = uniquePrioritySongs,
                    topTierSongs = uniquePrioritySongs.take(20)
                )
                
                Log.d(TAG, "Loaded ${uniquePrioritySongs.size} priority channel songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading priority channels", e)
            }
        }
    }
    
    /**
     * Load verified/official channel songs from Top 500 list
     */
    private fun loadVerifiedChannelSongs() {
        viewModelScope.launch {
            try {
                // Search for songs from verified Top 500 channels
                val verifiedSongs = mutableListOf<Song>()
                
                // Get top 10 channels and search their content
                IndianMusicChannels.TOP_100_CHANNELS.take(10).forEach { channel ->
                    try {
                        repository.searchSongsWithLimit("${channel.name} official", 8).getOrNull()?.let { songs ->
                            verifiedSongs.addAll(songs)
                        }
                    } catch (e: Exception) {
                        // Continue with next channel
                    }
                }
                
                // Filter for verified channels and sort by view count
                val filteredVerified = verifiedSongs
                    .filter { song ->
                        IndianMusicChannels.isPriorityChannel(song.channelName) ||
                        song.channelName.contains("official", ignoreCase = true)
                    }
                    .distinctBy { it.id }
                    .sortedByDescending { it.viewCount }
                    .take(30)
                
                _uiState.value = _uiState.value.copy(verifiedChannelSongs = filteredVerified)
                Log.d(TAG, "Loaded ${filteredVerified.size} verified channel songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading verified channel songs", e)
            }
        }
    }
    
    private fun loadArtistSpotlights() {
        viewModelScope.launch {
            try {
                repository.getArijitSinghSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(arijitSinghSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Arijit Singh songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getARRahmanSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(arRahmanSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading AR Rahman songs", e)
            }
        }
        
        viewModelScope.launch {
            try {
                repository.getShreyaGhoshalSongs().getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(shreyaGhoshalSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Shreya Ghoshal songs", e)
            }
        }
    }
    
    fun selectGenre(genre: Genre) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre)
        loadGenreSongs(genre)
    }
    
    private fun loadGenreSongs(genre: Genre) {
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("${genre.name} songs", 30).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(genreSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ${genre.name} songs", e)
            }
        }
    }
    
    fun setPreferredSource(source: MusicSource) {
        viewModelScope.launch {
            userPreferences.setPreferredSource(source)
            // Reload content with new source
            loadHomeContent()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Search songs with limit - for endless scrolling
     */
    suspend fun searchSongsForChart(query: String, limit: Int): List<Song> {
        return repository.searchSongsWithLimit(query, limit).getOrNull() ?: emptyList()
    }

    fun getPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            repository.getPlaylistDetails(playlistId).getOrNull()?.let { playlist ->
                _uiState.value = _uiState.value.copy(chartSongs = playlist.songs)
            }
        }
    }
    
    /**
     * Search songs unlimited from YouTube
     */
    fun searchSongsUnlimited(query: String, maxResults: Int = 1000): kotlinx.coroutines.flow.Flow<List<Song>> {
        return repository.searchSongsUnlimited(query, maxResults)
    }
    
    /**
     * Sort songs by option
     */
    fun sortSongs(songs: List<Song>, sortOption: com.reon.music.core.model.SongSortOption): List<Song> {
        return repository.sortSongs(songs, sortOption)
    }
    
    /**
     * Search playlists for a specific artist
     */
    suspend fun searchPlaylistsForArtist(artistName: String): List<Playlist> {
        return try {
            val queries = listOf(
                "$artistName playlist",
                "$artistName best songs",
                "$artistName hits collection",
                "$artistName jukebox"
            )
            
            val allPlaylists = mutableListOf<Playlist>()
            
            queries.forEach { query ->
                try {
                    repository.searchPlaylists(query).getOrNull()?.let { playlists ->
                        allPlaylists.addAll(playlists)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error searching playlists for query: $query", e)
                }
            }
            
            // Remove duplicates and limit
            allPlaylists
                .distinctBy { it.id }
                .filter { it.songCount > 0 }
                .take(10)
                .also {
                    Log.d(TAG, "Found ${it.size} playlists for artist: $artistName")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching playlists for artist: $artistName", e)
            emptyList()
        }
    }
    
    private fun loadRecentlyPlayed() {
        viewModelScope.launch {
            try {
                val recentHistory = historyDao.getRecentHistory(15).first()
                val recentSongIds = recentHistory.map { it.songId }
                val recentSongs = recentSongIds.mapNotNull { songId ->
                    songDao.getSongById(songId)?.toSong()
                }
                _uiState.value = _uiState.value.copy(recentlyPlayedSongs = recentSongs)
                Log.d(TAG, "Loaded ${recentSongs.size} recently played songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recently played", e)
            }
        }
    }
    
    private fun loadIndianPlaylists() {
        // Love Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("love songs hindi romantic", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(loveSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading love songs", e)
            }
        }
        
        // Romantic Hits
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("romantic hits bollywood", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(romanticHits = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading romantic hits", e)
            }
        }
        
        // Heartbreak / Love Failure Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("heartbreak sad songs hindi breakup", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(heartbreakSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading heartbreak songs", e)
            }
        }
        
        // Sad Songs Playlist
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("sad songs emotional hindi", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(sadSongPlaylist = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sad playlist", e)
            }
        }
        
        // Party Hits
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("party songs bollywood dance hits", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(partyHits = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading party hits", e)
            }
        }
        
        // DJ Remixes
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("dj remix bollywood nonstop", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(djRemixes = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading DJ remixes", e)
            }
        }
        
        // Telugu DJ Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("telugu dj songs remix folk", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(teluguDjSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Telugu DJ songs", e)
            }
        }
        
        // Wedding Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("wedding songs hindi shaadi sangeet", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(weddingSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading wedding songs", e)
            }
        }
        
        // Mix Songs (Mashup/Mix)
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("bollywood mashup mix songs", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(mixSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading mix songs", e)
            }
        }
        
        // Viral Hits / Trending Reels
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("viral songs trending reels", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(viralHits = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading viral hits", e)
            }
        }
        
        // Regional Language Songs
        loadRegionalSongs()
        
        // More Artist Spotlights
        loadMoreArtistSpotlights()
    }
    
    private fun loadRegionalSongs() {
        // Bhojpuri
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("bhojpuri songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(bhojpuriSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Bhojpuri songs", e)
            }
        }
        
        // Malayalam
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("malayalam songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(malayalamSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Malayalam songs", e)
            }
        }
        
        // Kannada
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("kannada songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(kannadaSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Kannada songs", e)
            }
        }
        
        // Marathi
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("marathi songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(marathiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Marathi songs", e)
            }
        }
        
        // Bengali
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("bengali songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(bengaliSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Bengali songs", e)
            }
        }
        
        // Gujarati
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("gujarati songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(gujaratiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Gujarati songs", e)
            }
        }
        
        // Rajasthani
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("rajasthani folk songs", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(rajasthaniSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Rajasthani songs", e)
            }
        }
    }
    
    private fun loadMoreArtistSpotlights() {
        // Sid Sriram
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Sid Sriram songs", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(sidSriram = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Sid Sriram songs", e)
            }
        }
        
        // Anirudh
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Anirudh Ravichander songs", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(anirudhSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Anirudh songs", e)
            }
        }
        
        // Kanika Kapoor
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Kanika Kapoor songs", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(kanikKapoor = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Kanika Kapoor songs", e)
            }
        }
        
        // Badshah
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Badshah songs hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(badshah = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Badshah songs", e)
            }
        }
        
        // Honey Singh
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Yo Yo Honey Singh songs", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(honeysingh = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Honey Singh songs", e)
            }
        }
    }
    
    /**
     * Load curated playlist categories
     */
    private fun loadCuratedPlaylists() {
        // Most Listening Telugu Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("most popular telugu songs 2024", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(mostListeningTeluguSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading most listening Telugu songs", e)
            }
        }
        
        // Most Listening Hindi Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("most popular hindi songs 2024", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(mostListeningHindiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading most listening Hindi songs", e)
            }
        }
        
        // All Time Favorite Telugu Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("all time best telugu songs evergreen", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(allTimeFavoriteTeluguSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all time favorite Telugu", e)
            }
        }
        
        // All Time Favorite Hindi Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("all time best hindi songs evergreen", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(allTimeFavoriteHindiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all time favorite Hindi", e)
            }
        }
        
        // Popular Indian Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("popular indian songs hits", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(popularIndianSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading popular Indian songs", e)
            }
        }
        
        // Most Listening Indian Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("most streamed indian songs 2024", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(mostListeningIndianSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading most listening Indian songs", e)
            }
        }
        
        // Evergreen Hindi Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("evergreen hindi songs 90s 2000s", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(everGreenHindiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading evergreen Hindi songs", e)
            }
        }
        
        // Evergreen Telugu Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("evergreen telugu songs old classics", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(everGreenTeluguSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading evergreen Telugu songs", e)
            }
        }
        
        // Top 100 Indian Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("top 100 indian songs best", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(top100IndianSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading top 100 Indian songs", e)
            }
        }
        
        // Telugu Playlist Collection
//        viewModelScope.launch {
//            try {
//                repository.searchPlaylists("telugu songs playlist").getOrNull()?.let { playlists ->
//                    _uiState.value = _uiState.value.copy(teluguPlaylistCollection = playlists)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading Telugu playlist collection", e)
//            }
//        }
//        
//        // Hindi Playlist Collection
//        viewModelScope.launch {
//            try {
//                repository.searchPlaylists("hindi songs playlist").getOrNull()?.let { playlists ->
//                    _uiState.value = _uiState.value.copy(hindiPlaylistCollection = playlists)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading Hindi playlist collection", e)
//            }
//        }
//        
//        // Tamil Playlist Collection
//        viewModelScope.launch {
//            try {
//                repository.searchPlaylists("tamil songs playlist").getOrNull()?.let { playlists ->
//                    _uiState.value = _uiState.value.copy(tamilPlaylistCollection = playlists)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading Tamil playlist collection", e)
//            }
//        }
//        
//        // Punjabi Playlist Collection
//        viewModelScope.launch {
//            try {
//                repository.searchPlaylists("punjabi songs playlist").getOrNull()?.let { playlists ->
//                    _uiState.value = _uiState.value.copy(punjabiPlaylistCollection = playlists)
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error loading Punjabi playlist collection", e)
//            }
//        }
        
        // Load International and additional categories
        loadInternationalSongs()
        loadAllTimeFavorites()
        loadMoreArtistCollections()
    }
    
    /**
     * Load International Songs
     */
    private fun loadInternationalSongs() {
        // Global Top 50
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("global top 50 songs 2024", 50).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(globalTop50 = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Global Top 50", e)
            }
        }
        
        // International Hits
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("international hits english pop", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(internationalHits = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading international hits", e)
            }
        }
        
        // English Pop Songs
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("english pop songs 2024", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(englishPopSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading English pop songs", e)
            }
        }
        
        // EDM
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("EDM electronic dance music hits", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(edm = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading EDM", e)
            }
        }
        
        // K-Pop
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("kpop korean pop songs BTS", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(kpopSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading K-Pop", e)
            }
        }
    }
    
    /**
     * Load All Time Favorites from database
     */
    private fun loadAllTimeFavorites() {
        viewModelScope.launch {
            try {
                // Get liked songs from database - these are the user's all-time favorites
                songDao.getLikedSongs().collect { likedSongs ->
                    val favoriteSongs = likedSongs.map { it.toSong() }
                    _uiState.value = _uiState.value.copy(allTimeFavorites = favoriteSongs)
                    Log.d(TAG, "Loaded ${favoriteSongs.size} all time favorites")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all time favorites", e)
            }
        }
        
        // Most Listening Songs (from history)
        viewModelScope.launch {
            try {
                val mostListened = historyDao.getMostPlayedFromHistory(30).first()
                val songs = mostListened.map { it.toSong() }
                _uiState.value = _uiState.value.copy(mostListeningSongs = songs)
                Log.d(TAG, "Loaded ${songs.size} most listening songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading most listening songs", e)
            }
        }
        
        // Trending Now
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("trending songs 2024 viral", 20).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(trendingNowSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading trending now songs", e)
            }
        }
    }
    
    /**
     * Load more artist collections
     */
    private fun loadMoreArtistCollections() {
        // SPB - S.P. Balasubrahmanyam
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("S.P. Balasubrahmanyam songs best", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(spbSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading SPB songs", e)
            }
        }
        
        // Lata Mangeshkar
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Lata Mangeshkar songs best", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(lataMangeshkarSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Lata Mangeshkar songs", e)
            }
        }
        
        // Kishore Kumar
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Kishore Kumar songs best evergreen", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(kishorKumarSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Kishore Kumar songs", e)
            }
        }
        
        // Mohammed Rafi
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Mohammed Rafi songs best", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(mohammedRafiSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Mohammed Rafi songs", e)
            }
        }
        
        // Pritam
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Pritam songs bollywood hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(pritam = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Pritam songs", e)
            }
        }
        
        // Harris Jayaraj
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Harris Jayaraj songs tamil", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(harishJeyaraj = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Harris Jayaraj songs", e)
            }
        }
        
        // DSP - Devi Sri Prasad
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Devi Sri Prasad songs telugu hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(dspSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading DSP songs", e)
            }
        }
        
        // Mani Sharma
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Mani Sharma songs telugu", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(manisharma = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Mani Sharma songs", e)
            }
        }
        
        // Thaman
        viewModelScope.launch {
            try {
                repository.searchSongsWithLimit("Thaman S songs telugu hits", 15).getOrNull()?.let { songs ->
                    _uiState.value = _uiState.value.copy(thamanSongs = songs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Thaman songs", e)
            }
        }
    }
    
    /**
     * Search songs by album/movie name
     */
    suspend fun searchByAlbumOrMovie(albumName: String): List<Song> {
        return try {
            repository.searchSongsWithLimit("$albumName movie songs", 20).getOrNull() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error searching by album/movie", e)
            emptyList()
        }
    }
}
