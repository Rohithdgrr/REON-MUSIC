/*
 * REON Music App - Enhanced Power Search ViewModel
 * Copyright (c) 2024 REON
 * YouTube Music Only - Multi-Language Real-time Search
 */

package com.reon.music.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reon.music.core.common.Result
import com.reon.music.core.model.Album
import com.reon.music.core.model.Artist
import com.reon.music.core.model.Song
import com.reon.music.data.network.youtube.YouTubeMusicClient
import com.reon.music.data.network.youtube.IndianMusicChannels
import com.reon.music.data.repository.MusicRepository
import com.reon.music.ui.search.SearchSuggestion
import com.reon.music.ui.search.SuggestionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ln
import kotlin.math.max

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val movies: List<Album> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val suggestions: List<SearchSuggestion> = emptyList(),
    val trendingSearches: List<SearchSuggestion> = emptyList(),
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val error: String? = null,
    val hasSearched: Boolean = false,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
    val isUnlimitedMode: Boolean = false
)

enum class SearchFilter {
    ALL, SONGS, ALBUMS, ARTISTS, MOVIES
}

// Indian language search priorities
private val INDIAN_LANGUAGES = listOf(
    "telugu", "hindi", "tamil", "malayalam", "kannada", 
    "punjabi", "marathi", "bengali", "gujarati", "bhojpuri",
    "banjara", "rajasthani", "odia"
)

private data class ChannelPriorityMeta(
    val primaryName: String,
    val aliases: Set<String> = emptySet(),
    val languages: Set<String>,
    val weight: Int
)

private val CHANNEL_PRIORITY_LIST = listOf(
    // === T-Series Empire (Hindi/Bollywood/Punjabi) ===
    ChannelPriorityMeta("T-Series", aliases = setOf("tseries"), languages = setOf("hindi", "bollywood", "punjabi", "global"), weight = 100),
    ChannelPriorityMeta("T-Series Apna Punjab", languages = setOf("punjabi"), weight = 90),
    ChannelPriorityMeta("T-Series Hamaar Bhojpuri", languages = setOf("bhojpuri"), weight = 85),
    ChannelPriorityMeta("T-Series Bhakti Sagar", languages = setOf("devotional"), weight = 80),
    ChannelPriorityMeta("T-Series Regional", languages = setOf("marathi", "bengali", "gujarati"), weight = 80),
    ChannelPriorityMeta("T-Series Telugu", languages = setOf("telugu"), weight = 95),
    ChannelPriorityMeta("T-Series Tamil", languages = setOf("tamil"), weight = 92),
    ChannelPriorityMeta("T-Series Kannada", languages = setOf("kannada"), weight = 88),

    // === Major Indian Music Labels ===
    ChannelPriorityMeta("Zee Music Company", languages = setOf("hindi", "bollywood"), weight = 98),
    ChannelPriorityMeta("Sony Music India", languages = setOf("hindi", "bollywood"), weight = 97),
    ChannelPriorityMeta("YRF", aliases = setOf("yash raj films", "yrf music"), languages = setOf("hindi", "bollywood"), weight = 96),
    ChannelPriorityMeta("Tips Official", languages = setOf("hindi", "bollywood"), weight = 95),
    ChannelPriorityMeta("Saregama Music", aliases = setOf("saregama"), languages = setOf("hindi", "bollywood"), weight = 94),
    ChannelPriorityMeta("Venus Movies", aliases = setOf("venus"), languages = setOf("hindi"), weight = 90),
    ChannelPriorityMeta("Speed Records", languages = setOf("punjabi"), weight = 92),
    ChannelPriorityMeta("White Hill Music", languages = setOf("punjabi"), weight = 88),
    ChannelPriorityMeta("Desi Melodies", languages = setOf("punjabi"), weight = 86),
    ChannelPriorityMeta("Aditya Music", languages = setOf("telugu"), weight = 98),
    ChannelPriorityMeta("Lahari Music", languages = setOf("telugu", "tamil", "kannada"), weight = 97),
    ChannelPriorityMeta("MRT Music", languages = setOf("telugu"), weight = 90),
    ChannelPriorityMeta("Mango Music", languages = setOf("telugu"), weight = 92),
    ChannelPriorityMeta("Sony Music South", languages = setOf("tamil", "telugu", "malayalam", "kannada"), weight = 96),
    ChannelPriorityMeta("Think Music India", aliases = setOf("think music"), languages = setOf("tamil", "malayalam"), weight = 95),
    ChannelPriorityMeta("Divo Music", aliases = setOf("divo tamil"), languages = setOf("tamil"), weight = 90),
    ChannelPriorityMeta("Muzik247", languages = setOf("malayalam", "tamil"), weight = 88),
    ChannelPriorityMeta("Junglee Music", languages = setOf("hindi", "kannada"), weight = 85),
    ChannelPriorityMeta("Eros Now Music", aliases = setOf("eros now"), languages = setOf("hindi", "bollywood"), weight = 90),

    // === Top Telugu Channels ===
    ChannelPriorityMeta("Geetha Arts", languages = setOf("telugu"), weight = 94),
    ChannelPriorityMeta("Volga Video", aliases = setOf("volga music"), languages = setOf("telugu"), weight = 90),
    ChannelPriorityMeta("Telugu Filmnagar", languages = setOf("telugu"), weight = 88),
    ChannelPriorityMeta("Nivetha Audios", languages = setOf("telugu"), weight = 85),
    ChannelPriorityMeta("Madhura Audio", languages = setOf("telugu"), weight = 86),
    ChannelPriorityMeta("Haasini Music", languages = setOf("telugu"), weight = 84),
    ChannelPriorityMeta("TeluguOne Music", languages = setOf("telugu"), weight = 82),

    // === Top Tamil Channels ===
    ChannelPriorityMeta("Star Music India", languages = setOf("tamil"), weight = 88),
    ChannelPriorityMeta("Pyramid Music", languages = setOf("tamil"), weight = 86),
    ChannelPriorityMeta("Saregama Tamil", languages = setOf("tamil"), weight = 92),
    ChannelPriorityMeta("U1 Records", aliases = setOf("yuvan shankar raja"), languages = setOf("tamil"), weight = 94),
    ChannelPriorityMeta("Five Star Audio", languages = setOf("tamil"), weight = 85),

    // === Top Bollywood / Hindi Singers ===
    ChannelPriorityMeta("Arijit Singh", languages = setOf("hindi"), weight = 98),
    ChannelPriorityMeta("A R Rahman", aliases = setOf("arrahman"), languages = setOf("global", "hindi", "tamil"), weight = 99),
    ChannelPriorityMeta("Shreya Ghoshal", languages = setOf("hindi", "tamil", "telugu"), weight = 97),
    ChannelPriorityMeta("Neha Kakkar", languages = setOf("hindi"), weight = 95),
    ChannelPriorityMeta("Guru Randhawa", languages = setOf("hindi", "punjabi"), weight = 94),
    ChannelPriorityMeta("Badshah", languages = setOf("hindi", "punjabi"), weight = 93),
    ChannelPriorityMeta("Jubin Nautiyal", languages = setOf("hindi"), weight = 92),
    ChannelPriorityMeta("Diljit Dosanjh", languages = setOf("punjabi", "hindi"), weight = 94),
    ChannelPriorityMeta("Atif Aslam", languages = setOf("hindi", "punjabi"), weight = 92),

    // === Top Telugu Singers ===
    ChannelPriorityMeta("Sid Sriram", languages = setOf("telugu", "tamil"), weight = 96),
    ChannelPriorityMeta("Anirudh Ravichander", aliases = setOf("anirudh official"), languages = setOf("tamil", "telugu"), weight = 98),
    ChannelPriorityMeta("Thaman S", languages = setOf("telugu"), weight = 95),
    ChannelPriorityMeta("Devi Sri Prasad", aliases = setOf("dsp"), languages = setOf("telugu"), weight = 95),
    ChannelPriorityMeta("Karthik", languages = setOf("telugu", "tamil"), weight = 90),
    ChannelPriorityMeta("Mangli", languages = setOf("telugu"), weight = 88),

    // === International Icons ===
    ChannelPriorityMeta("Vevo", aliases = setOf("vevo global"), languages = setOf("global", "english"), weight = 95),
    ChannelPriorityMeta("Taylor Swift", languages = setOf("global", "english"), weight = 98),
    ChannelPriorityMeta("Billie Eilish", languages = setOf("global", "english"), weight = 96),
    ChannelPriorityMeta("The Weeknd", languages = setOf("global", "english"), weight = 97),
    ChannelPriorityMeta("Ed Sheeran", languages = setOf("global", "english"), weight = 97),
    ChannelPriorityMeta("Justin Bieber", languages = setOf("global", "english"), weight = 96),
    ChannelPriorityMeta("EminemMusic", aliases = setOf("eminem"), languages = setOf("global", "english"), weight = 96),
    ChannelPriorityMeta("Rihanna", languages = setOf("global", "english"), weight = 95),
    ChannelPriorityMeta("Beyoncé", languages = setOf("global", "english"), weight = 95),

    // === LoFi & Chill ===
    ChannelPriorityMeta("Lofi Girl", languages = setOf("lofi", "chill"), weight = 90),
    ChannelPriorityMeta("Chillhop Music", languages = setOf("lofi", "chill"), weight = 85),
    ChannelPriorityMeta("Hindi LoFi Songs", languages = setOf("hindi", "lofi"), weight = 80),
    ChannelPriorityMeta("Telugu LoFi Hub", languages = setOf("telugu", "lofi"), weight = 80)
)


private val CHANNEL_PRIORITY_MAP: Map<String, ChannelPriorityMeta> = buildMap {
    CHANNEL_PRIORITY_LIST.forEach { meta ->
        (setOf(meta.primaryName) + meta.aliases).forEach { alias ->
            val key = normalizeChannelName(alias)
            val existing = this[key]
            if (existing == null || meta.weight > existing.weight) {
                this[key] = meta
            }
        }
    }
}

private val CHANNEL_PRIORITY_BY_LANGUAGE: Map<String, List<ChannelPriorityMeta>> =
    CHANNEL_PRIORITY_LIST.flatMap { meta ->
        meta.languages.map { language -> language to meta }
    }.groupBy({ it.first }, { it.second })

private val DETECTABLE_LANGUAGES = setOf(
    "hindi", "telugu", "tamil", "english", "malayalam", "kannada", "punjabi"
)

private val NON_ALPHANUMERIC_REGEX = Regex("[^a-z0-9 ]")
private val MULTI_WHITESPACE_REGEX = Regex("\\s+")

private fun normalizeChannelName(name: String): String {
    if (name.isBlank()) return ""
    val lower = name.lowercase(Locale.US)
    val stripped = NON_ALPHANUMERIC_REGEX.replace(lower, " ")
    return MULTI_WHITESPACE_REGEX.replace(stripped, " ").trim()
}

private fun detectLanguageFromQuery(normalizedQuery: String): String? {
    return when {
        normalizedQuery.contains("telugu") || normalizedQuery.contains("tollywood") -> "telugu"
        normalizedQuery.contains("hindi") || normalizedQuery.contains("bollywood") -> "hindi"
        normalizedQuery.contains("tamil") -> "tamil"
        normalizedQuery.contains("malayalam") || normalizedQuery.contains("mollywood") -> "malayalam"
        normalizedQuery.contains("kannada") -> "kannada"
        normalizedQuery.contains("punjabi") -> "punjabi"
        normalizedQuery.contains("english") || normalizedQuery.contains("international") -> "english"
        else -> null
    }
}

private fun findChannelMetaFromQuery(normalizedQuery: String): ChannelPriorityMeta? {
    CHANNEL_PRIORITY_MAP[normalizedQuery]?.let { return it }
    return CHANNEL_PRIORITY_LIST.firstOrNull { meta ->
        val candidates = (setOf(meta.primaryName) + meta.aliases).map { normalizeChannelName(it) }
        candidates.any { candidate -> candidate.isNotBlank() && normalizedQuery.contains(candidate) }
    }
}

private fun computeChannelPriorityScore(
    song: Song,
    queryLanguage: String?,
    queryChannelMeta: ChannelPriorityMeta?
): Double {
    val normalizedChannelName = normalizeChannelName(song.channelName)
    val normalizedArtistName = normalizeChannelName(song.artist)
    val meta = CHANNEL_PRIORITY_MAP[normalizedChannelName]
    var score = 0.0

    if (meta != null) {
        score += meta.weight.toDouble()
        if (queryLanguage != null && meta.languages.contains(queryLanguage)) {
            score += 25.0
        }
    }

    if (queryChannelMeta != null) {
        val queryChannelName = normalizeChannelName(queryChannelMeta.primaryName)
        if (meta != null && meta.primaryName == queryChannelMeta.primaryName) {
            score += 40.0
        } else if (queryChannelName.isNotBlank() && normalizedArtistName.contains(queryChannelName)) {
            score += 15.0
        }
    }

    return score
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val youTubeClient: YouTubeMusicClient,
    private val repository: MusicRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "SearchViewModel"
        private const val DEBOUNCE_MS = 400L
        private const val SUGGESTION_DEBOUNCE_MS = 300L
        private const val INITIAL_SEARCH_LIMIT = 100
        private const val MAX_RESULTS = 400
        private const val PAGE_SIZE = 30
    }
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    private var suggestionJob: Job? = null
    private var loadMoreJob: Job? = null
    private val searchHistoryList = mutableListOf<String>()
    private var allSearchResults = mutableListOf<Song>()
    
    init {
        loadTrendingSearches()
    }
    
    private fun loadTrendingSearches() {
        viewModelScope.launch {
            val trending = listOf(
                "Telugu Songs", "Hindi Songs", "Tamil Hits",
                "Arijit Singh", "Devi Sri Prasad", "AR Rahman",
                "Pushpa", "RRR", "Pathaan", "Animal",
                "Romantic Songs", "Party Songs", "Sad Songs",
                "English Pop", "BTS", "Punjabi Hits"
            )
            _uiState.value = _uiState.value.copy(
                trendingSearches = trending.map { SearchSuggestion(text = it, type = SuggestionType.TRENDING) }
            )
        }
    }
    
    /**
     * Update query and trigger real-time search and suggestions
     */
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        
        if (query.isBlank()) {
            // Clear results when query is empty
            _uiState.value = _uiState.value.copy(
                songs = emptyList(),
                albums = emptyList(),
                artists = emptyList(),
                suggestions = emptyList<SearchSuggestion>(),
                hasSearched = false,
                isLoading = false
            )
            allSearchResults.clear()
            return
        }
        
        // Get suggestions as user types
        suggestionJob?.cancel()
        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            loadSuggestions(query)
        }
        
        // Debounced search - search as user types
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            performPowerSearch(query)
        }
    }
    
    /**
     * Load search suggestions using autocomplete
     */
    private suspend fun loadSuggestions(query: String) {
        try {
            val suggestions = mutableListOf<SearchSuggestion>()
            
            val result = repository.autocomplete(query)
            
            result.getOrNull()?.let { searchResult ->
                searchResult.artists.take(3).forEach {
                    suggestions.add(SearchSuggestion(text = it.name, type = SuggestionType.ARTIST))
                }
                searchResult.albums.take(3).forEach {
                    suggestions.add(SearchSuggestion(text = it.name, type = SuggestionType.PERSONALIZED))
                }
                searchResult.playlists.take(2).forEach {
                    suggestions.add(SearchSuggestion(text = it.name, type = SuggestionType.PERSONALIZED))
                }
            }
            
            searchHistoryList
                .filter { it.contains(query, ignoreCase = true) }
                .take(3)
                .forEach {
                    suggestions.add(SearchSuggestion(text = it, type = SuggestionType.RECENT))
                }
            
            _uiState.value = _uiState.value.copy(suggestions = suggestions.distinct().take(10))
        } catch (e: Exception) {
            Log.e(TAG, "Error loading suggestions", e)
        }
    }
    
    /**
     * Perform immediate search (e.g., when user presses search button)
     */
    fun search(query: String) {
        if (query.isBlank()) return
        
        searchJob?.cancel()
        addToHistory(query)
        
        searchJob = viewModelScope.launch {
            performPowerSearch(query)
        }
    }
    
    /**
     * POWER SEARCH - Multi-language, multi-source comprehensive search
     * Prioritizes Indian languages and searches by song, artist, album/movie
     * Now with live streaming and better null safety
     */
    private suspend fun performPowerSearch(query: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            hasSearched = true,
            hasMore = true,
            currentPage = 1,
            error = null,
            songs = emptyList()
        )
        allSearchResults.clear()
        
        try {
            repository.searchSongsLive(query, limit = INITIAL_SEARCH_LIMIT)
                .collect { liveResults ->
                    try {
                        val filteredResults = liveResults.filter {
                            it.id.isNotBlank() && it.title.isNotBlank()
                        }
                        
                        if (filteredResults.isNotEmpty()) {
                            val rankedSongs = filteredResults.sortedByDescending { song ->
                                val relevanceScore = calculateBasicRelevanceScore(query, song)
                                val viewScore = (song.viewCount / 1000000).coerceAtMost(50)
                                relevanceScore + viewScore
                            }
                            
                            allSearchResults.clear()
                            allSearchResults.addAll(rankedSongs.take(MAX_RESULTS))
                            
                            updateUiWithFilteredResults(query, rankedSongs.take(MAX_RESULTS))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing live search results", e)
                    }
                }
            
            if (allSearchResults.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No results found. Try searching with different keywords."
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Search error", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Search failed: ${e.message ?: "Unknown error"}"
            )
        }
    }
    
    private fun updateUiWithFilteredResults(query: String, rankedSongs: List<Song>) {
        val filter = _uiState.value.activeFilter
        
        val artists = if (filter == SearchFilter.ALL || filter == SearchFilter.ARTISTS) {
            rankedSongs
                .groupBy { it.artist.lowercase() }
                .mapNotNull { (name, songs) ->
                    if (songs.isEmpty() || name.isBlank()) return@mapNotNull null
                    val representative = songs.first()
                    Artist(
                        id = name.hashCode().toString(),
                        name = representative.artist.ifBlank { "Unknown" },
                        artworkUrl = representative.artworkUrl ?: "",
                        followerCount = representative.channelSubscriberCount.toInt().coerceAtLeast(0),
                        topSongs = songs.take(5)
                    )
                }
                .sortedByDescending { it.followerCount }
                .take(10)
        } else emptyList()
        
        val albums = if (filter == SearchFilter.ALL || filter == SearchFilter.ALBUMS) {
            rankedSongs
                .filter { it.album.isNotBlank() }
                .groupBy { it.album.lowercase() }
                .mapNotNull { (name, songs) ->
                    if (songs.isEmpty() || name.isBlank()) return@mapNotNull null
                    val representative = songs.first()
                    Album(
                        id = name.hashCode().toString(),
                        name = representative.album.ifBlank { "Unknown Album" },
                        artist = representative.artist.ifBlank { "Unknown" },
                        artworkUrl = representative.artworkUrl ?: "",
                        songs = songs.take(5)
                    )
                }
                .sortedByDescending { it.songs.size }
                .take(10)
        } else emptyList()
        
        val movies = if (filter == SearchFilter.ALL || filter == SearchFilter.MOVIES) {
            rankedSongs
                .filter { it.movieName.isNotBlank() }
                .groupBy { it.movieName.lowercase() }
                .mapNotNull { (name, songs) ->
                    if (songs.isEmpty() || name.isBlank()) return@mapNotNull null
                    val representative = songs.first()
                    Album(
                        id = ("movie_" + name).hashCode().toString(),
                        name = representative.movieName.ifBlank { "Unknown Movie" },
                        artist = representative.heroName.ifBlank { representative.artist },
                        artworkUrl = representative.artworkUrl ?: "",
                        songs = songs.take(5)
                    )
                }
                .sortedByDescending { it.songs.size }
                .take(5)
        } else emptyList()
        
        val songs = if (filter == SearchFilter.SONGS) rankedSongs
        else if (filter == SearchFilter.ALL) rankedSongs
        else emptyList()
        
        _uiState.value = _uiState.value.copy(
            songs = songs.take(INITIAL_SEARCH_LIMIT),
            albums = albums,
            artists = artists,
            movies = movies,
            isLoading = false,
            hasMore = rankedSongs.size > INITIAL_SEARCH_LIMIT,
            error = null
        )
    }
    
    /**
     * Build comprehensive search queries for power search
     * Supports searching by: song name, artist, album, movie, metadata, hero/heroine names
     * Prioritizes Indian languages and includes movie/album searches
     */
    private fun buildPowerSearchQueries(query: String): List<String> {
        val queries = mutableListOf<String>()
        val normalizedQuery = normalizeChannelName(query)
        val queryLanguage = detectLanguageFromQuery(normalizedQuery)
        val queryChannelMeta = findChannelMetaFromQuery(normalizedQuery)
        
        // Primary query
        queries.add(query)
        
        // === SONG & ALBUM SEARCHES ===
        queries.add("$query song")
        queries.add("$query songs")
        queries.add("$query album")
        queries.add("$query track")
        
        // === MOVIE SEARCHES (for film songs) ===
        queries.add("$query movie songs")
        queries.add("$query movie soundtrack")
        queries.add("$query film songs")
        queries.add("$query cinema songs")
        
        // === ARTIST SEARCHES ===
        queries.add("$query artist")
        queries.add("$query singer")
        queries.add("$query performer")
        
        // === ACTOR/HERO/HEROINE SEARCHES ===
        queries.add("$query hero songs")
        queries.add("$query heroine songs")
        queries.add("$query actor songs")
        queries.add("$query actress songs")
        
        // === METADATA & CATEGORY SEARCHES ===
        queries.add("$query romantic")
        queries.add("$query love songs")
        queries.add("$query sad songs")
        queries.add("$query devotional")
        queries.add("$query classical")
        
        // === CHANNEL-FOCUSED VARIATIONS ===
        queryChannelMeta?.let { meta ->
            queries.add("${meta.primaryName} $query")
            queries.add("$query ${meta.primaryName}")
            meta.aliases.take(3).forEach { alias ->
                queries.add("$alias $query")
                queries.add("$query $alias")
            }
        }
        
        val detectedLanguage = queryLanguage ?: run {
            DETECTABLE_LANGUAGES.firstOrNull { language ->
                normalizedQuery.contains(language)
            }
        }
        
        val isIndianLanguageQuery = detectedLanguage != null || INDIAN_LANGUAGES.any {
            query.lowercase().contains(it)
        }
        
        if (detectedLanguage != null) {
            CHANNEL_PRIORITY_BY_LANGUAGE[detectedLanguage]
                ?.distinctBy { it.primaryName }
                ?.take(4)
                ?.forEach { meta ->
                    queries.add("${meta.primaryName} $query")
                    queries.add("$query ${meta.primaryName}")
                }
        }
        
        // Add Indian language variations for better coverage when language not specified
        if (!isIndianLanguageQuery) {
            queries.add("$query telugu")
            queries.add("$query telugu songs")
            queries.add("$query telugu movie songs")
            
            queries.add("$query hindi")
            queries.add("$query hindi songs")
            queries.add("$query bollywood $query")
            
            queries.add("$query tamil")
            queries.add("$query tamil songs")
            
            queries.add("$query kannada")
            queries.add("$query kannada songs")
            
            queries.add("$query punjabi")
            queries.add("$query punjabi songs")
            
            queries.add("$query malayalam")
            queries.add("$query malayalam songs")
        }
        
        return queries.distinct().take(20)
    }
    
    /**
     * Simplified relevance score for basic sorting
     * Focuses on title and artist matching with basic popularity scoring
     * Enhanced with Top 500 Indian Music Channels priority ranking
     */
    private fun calculateBasicRelevanceScore(query: String, song: Song): Int {
        var score = 0
        val lowerQuery = query.lowercase()
        
        // Safe field access with null checks
        val lowerTitle = song.title?.lowercase() ?: ""
        val lowerArtist = song.artist?.lowercase() ?: ""
        val lowerAlbum = song.album?.lowercase() ?: ""
        val lowerChannel = song.channelName?.lowercase() ?: ""
        
        // === TITLE MATCHING (Highest Priority) ===
        when {
            lowerTitle == lowerQuery -> score += 100 // Exact match
            lowerTitle.startsWith(lowerQuery) -> score += 80 // Starts with query
            lowerTitle.contains(lowerQuery) -> score += 60 // Contains query
        }
        
        // === ARTIST MATCHING ===
        when {
            lowerArtist == lowerQuery -> score += 90 // Exact artist match
            lowerArtist.startsWith(lowerQuery) -> score += 70 // Artist starts with query
            lowerArtist.contains(lowerQuery) -> score += 50 // Artist contains query
        }
        
        // === ALBUM MATCHING ===
        when {
            lowerAlbum == lowerQuery -> score += 70 // Exact album match
            lowerAlbum.startsWith(lowerQuery) -> score += 50 // Album starts with query
            lowerAlbum.contains(lowerQuery) -> score += 30 // Album contains query
        }
        
        // === TOP 500 INDIAN MUSIC CHANNELS PRIORITY BOOST ===
        val channelBoost = IndianMusicChannels.getChannelPriorityBoost(song.channelName)
        score += channelBoost.toInt()
        
        // Check if channel is official/verified
        if (IndianMusicChannels.isPriorityChannel(song.channelName)) {
            score += 25 // Verified channel bonus
        }
        
        // === BASIC POPULARITY BONUS ===
        // Safe access to view count with default value
        val viewCount = try {
            song.viewCount.coerceAtLeast(0)
        } catch (e: Exception) {
            0L
        }
        
        // Add view count bonus (normalized)
        val viewBonus = (viewCount / 1000000).toInt().coerceAtMost(20)
        score += viewBonus
        
        // === OFFICIAL CHANNEL BONUS ===
        if (song.channelName?.contains("official", ignoreCase = true) == true) {
            score += 10
        }
        
        return score
    }
    
    /**
     * Load more results for endless scrolling
     */
    fun loadMoreResults() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            if (!isActive) return@launch
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            val currentPage = _uiState.value.currentPage
            val startIndex = currentPage * PAGE_SIZE
            
            if (startIndex < allSearchResults.size) {
                val reRanked = reRankWithYouTubeSignals(_uiState.value.query, allSearchResults)
                allSearchResults = reRanked.toMutableList()
                val sliceEnd = (startIndex + PAGE_SIZE).coerceAtMost(reRanked.size)
                val nextBatch = reRanked.subList(startIndex, sliceEnd)
                
                _uiState.value = _uiState.value.copy(
                    songs = _uiState.value.songs + nextBatch,
                    currentPage = currentPage + 1,
                    isLoadingMore = false,
                    hasMore = sliceEnd < reRanked.size
                )
            } else {
                val query = _uiState.value.query
                if (query.isNotBlank()) {
                    try {
                        val moreQueries = listOf(
                            "$query ${currentPage + 1}",
                            "$query latest",
                            "$query hits"
                        )
                        val existingIds = HashSet<String>(allSearchResults.size)
                        allSearchResults.forEach { existingIds.add(it.id) }
                        
                        moreQueries.forEach { searchQuery ->
                            if (!isActive) return@launch
                            val result = youTubeClient.searchSongs(searchQuery)
                            if (result is Result.Success) {
                                val newSongs = result.data.filter { it.id !in existingIds }
                                newSongs.forEach { existingIds.add(it.id) }
                                val roomLeft = MAX_RESULTS - allSearchResults.size
                                if (roomLeft > 0) {
                                    val batch = newSongs.take(roomLeft)
                                    allSearchResults.addAll(batch)
                                }
                                val reRanked = reRankWithYouTubeSignals(query, allSearchResults)
                                allSearchResults = reRanked.toMutableList()
                                _uiState.value = _uiState.value.copy(
                                    songs = _uiState.value.songs + reRanked.drop(_uiState.value.songs.size),
                                    currentPage = currentPage + 1,
                                    hasMore = reRanked.size > _uiState.value.songs.size && allSearchResults.size < MAX_RESULTS
                                )
                            }
                        }
                    } catch (e: Exception) {
                        if (isActive) Log.e(TAG, "Error loading more results", e)
                    }
                }
                _uiState.value = _uiState.value.copy(isLoadingMore = false)
            }
        }
    }
    
    /**
     * Toggle unlimited search mode
     */
    fun toggleUnlimitedMode() {
        val newMode = !_uiState.value.isUnlimitedMode
        _uiState.value = _uiState.value.copy(isUnlimitedMode = newMode)
        
        if (newMode && _uiState.value.query.isNotBlank()) {
            performUnlimitedSearch(_uiState.value.query)
        }
    }
    
    /**
     * Unlimited search - fetches maximum results
     */
    private fun performUnlimitedSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Perform extensive search across all variations
                val allQueries = buildPowerSearchQueries(query) + listOf(
                    "$query full album", "$query jukebox", "$query all songs",
                    "$query collection", "$query best of", "$query top hits"
                )
                
                val allSongs = mutableListOf<Song>()
                allQueries.forEach { searchQuery ->
                    try {
                        val result = repository.searchSongsWithLimit(searchQuery, 100)
                        result.getOrNull()?.let { songs ->
                            allSongs.addAll(songs)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unlimited search query failed: $searchQuery", e)
                    }
                }
                
                val uniqueSongs = allSongs.distinctBy { it.id }
                val rankedSongs = reRankWithYouTubeSignals(query, uniqueSongs)
                
                allSearchResults.clear()
                allSearchResults.addAll(rankedSongs)
                
                _uiState.value = _uiState.value.copy(
                    songs = rankedSongs,
                    isLoading = false,
                    hasMore = false // All results loaded
                )
                
                Log.d(TAG, "Unlimited search: ${rankedSongs.size} total songs found")
                
            } catch (e: Exception) {
                Log.e(TAG, "Unlimited search error", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun setFilter(filter: SearchFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
        if (allSearchResults.isNotEmpty()) {
            val reRanked = reRankWithYouTubeSignals(_uiState.value.query, allSearchResults.toList())
            updateUiWithFilteredResults(_uiState.value.query, reRanked)
        }
    }
    
    private fun addToHistory(query: String) {
        if (query.isNotBlank() && !searchHistoryList.contains(query)) {
            searchHistoryList.add(0, query)
            if (searchHistoryList.size > 15) {
                searchHistoryList.removeLast()
            }
            _uiState.value = _uiState.value.copy(searchHistory = searchHistoryList.toList())
        }
    }
    
    fun removeFromHistory(query: String) {
        searchHistoryList.remove(query)
        _uiState.value = _uiState.value.copy(searchHistory = searchHistoryList.toList())
    }
    
    fun clearHistory() {
        searchHistoryList.clear()
        _uiState.value = _uiState.value.copy(searchHistory = emptyList())
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        suggestionJob?.cancel()
        loadMoreJob?.cancel()
        allSearchResults.clear()
        _uiState.value = SearchUiState(searchHistory = searchHistoryList.toList())
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Rank songs using YouTube-style signals (relevance + engagement + recency)
     * Enhanced with Top 500 Indian Music Channels priority ranking
     */
    private fun reRankWithYouTubeSignals(query: String, songs: List<Song>): List<Song> {
        val nowMillis = System.currentTimeMillis()
        
        val normalizedQuery = normalizeChannelName(query)
        val queryLanguage = detectLanguageFromQuery(normalizedQuery)
        val queryChannelMeta = findChannelMetaFromQuery(normalizedQuery)
        val languageTopChannels = queryLanguage?.let { language ->
            CHANNEL_PRIORITY_BY_LANGUAGE[language]?.mapIndexed { index, meta ->
                normalizeChannelName(meta.primaryName) to (40.0 - index * 6)
            }?.toMap()
        } ?: emptyMap()

        return songs
            .map { song ->
                val relevance = calculateRelevanceScore(query, song).toDouble()
                
                val engagement = (
                    ln(song.viewCount.coerceAtLeast(1).toDouble()) * 12 +
                    ln(song.likeCount.coerceAtLeast(1).toDouble()) * 8 +
                    ln(song.channelSubscriberCount.coerceAtLeast(1).toDouble()) * 6
                )
                
                val recencyMillis = parseUploadDateMillis(song.uploadDate)
                val monthsOld = recencyMillis?.let { (nowMillis - it).coerceAtLeast(0) / (1000L * 60 * 60 * 24 * 30) } ?: 48L
                val recencyScore = max(0.0, 24.0 - monthsOld.toDouble()) * 3
                
                val officialBoost = when {
                    song.channelName.contains("official", ignoreCase = true) -> 25.0
                    song.channelName.contains("music", ignoreCase = true) -> 10.0
                    else -> 0.0
                }
                
                // Prefer typical music durations (~2-7 minutes) to mimic YouTube ordering
                val durationScore = when {
                    song.duration in 120..420 -> 18.0
                    song.duration in 90..540 -> 8.0
                    else -> 0.0
                }
                
                val qualityScore = when {
                    song.is320kbps -> 6.0
                    song.quality.contains("4K", true) -> 8.0
                    song.quality.contains("HD", true) -> 4.0
                    else -> 0.0
                }
                
                // === TOP 500 INDIAN MUSIC CHANNELS BOOST ===
                val indianChannelBoost = IndianMusicChannels.getChannelPriorityBoost(song.channelName)
                val verifiedChannelBoost = if (IndianMusicChannels.isPriorityChannel(song.channelName)) 35.0 else 0.0
                
                val channelPriorityScore = computeChannelPriorityScore(song, queryLanguage, queryChannelMeta)
                val topChannelLanguageBoost = languageTopChannels[normalizeChannelName(song.channelName)] ?: 0.0
                
                val score = relevance + engagement + recencyScore + officialBoost + durationScore + qualityScore + 
                           channelPriorityScore + topChannelLanguageBoost + indianChannelBoost + verifiedChannelBoost
                song to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
    }
    
    /**
     * Calculate relevance score for a song based on query match
     * Higher score = more relevant
     * Enhanced with Top 500 Indian Music Channels priority ranking
     */
    private fun calculateRelevanceScore(query: String, song: Song): Int {
        var score = 0
        val lowerQuery = query.lowercase().trim()
        
        // Safe field access with null checks
        val lowerTitle = song.title?.lowercase()?.trim() ?: ""
        val lowerArtist = song.artist?.lowercase()?.trim() ?: ""
        val lowerChannel = song.channelName?.lowercase()?.trim() ?: ""
        
        // === EXACT TITLE MATCH (Highest Score) ===
        if (lowerTitle == lowerQuery) {
            score += 100
        }
        
        // === TITLE STARTS WITH QUERY ===
        if (lowerTitle.startsWith(lowerQuery)) {
            score += 80
        }
        
        // === PARTIAL TITLE MATCH ===
        if (lowerTitle.contains(lowerQuery)) {
            score += 60
        }
        
        // === ARTIST EXACT MATCH ===
        if (lowerArtist == lowerQuery) {
            score += 90
        }
        
        // === ARTIST PARTIAL MATCH ===
        if (lowerArtist.contains(lowerQuery)) {
            score += 50
        }
        
        // === CHANNEL NAME MATCH ===
        if (lowerChannel.contains(lowerQuery)) {
            score += 40
        }
        
        // === WORD-BY-WORD MATCHING ===
        val queryWords = lowerQuery.split(" ").filter { it.length > 2 }
        val titleWords = lowerTitle.split(" ")
        val artistWords = lowerArtist.split(" ")
        
        queryWords.forEach { word ->
            // Word in title
            if (titleWords.any { it == word }) {
                score += 20
            } else if (titleWords.any { it.contains(word) }) {
                score += 10
            }
            
            // Word in artist
            if (artistWords.any { it == word }) {
                score += 15
            } else if (artistWords.any { it.contains(word) }) {
                score += 8
            }
        }
        
        // === TOP 500 INDIAN MUSIC CHANNELS PRIORITY BOOST ===
        // Priority tiers: Top 20 get +100, 21-50 get +80, 51-100 get +60, 101-200 get +40, 201-500 get +20
        val channelPriorityBoost = IndianMusicChannels.getChannelPriorityBoost(song.channelName)
        score += channelPriorityBoost.toInt()
        
        // === VERIFIED CHANNEL BONUS ===
        if (IndianMusicChannels.isPriorityChannel(song.channelName)) {
            score += 30 // Verified/Official Top 500 channel bonus
        }
        
        // === CHANNEL RANK DISPLAY INFO ===
        val channel = IndianMusicChannels.getChannelByName(song.channelName)
        if (channel != null) {
            // Additional boost based on channel tier
            when {
                channel.rank <= 20 -> score += 50 // Top tier
                channel.rank <= 50 -> score += 40
                channel.rank <= 100 -> score += 30
                channel.rank <= 200 -> score += 20
                channel.rank <= 500 -> score += 10
            }
        }
        
        // === POPULARITY BOOST ===
        val viewBonus = (song.viewCount / 1000000).toInt().coerceAtMost(30)
        score += viewBonus
        
        // === LIKES BOOST ===
        val likesBonus = (song.likeCount / 100000).toInt().coerceAtMost(20)
        score += likesBonus
        
        // === OFFICIAL CHANNEL BONUS ===
        if (lowerChannel.contains("official") || lowerChannel.contains("music")) {
            score += 15
        }
        
        return score
    }
    
    /**
     * Get channel rank info for display in search results
     */
    fun getChannelRankInfo(channelName: String): Pair<Int, Boolean>? {
        val channel = IndianMusicChannels.getChannelByName(channelName) ?: return null
        return Pair(channel.rank, true)
    }
    
    /**
     * Parse upload date string to millis for recency scoring
     */
    private fun parseUploadDateMillis(uploadDate: String): Long? {
        if (uploadDate.isBlank()) return null
        val formats = listOf(
            "yyyy-MM-dd",
            "dd MMM yyyy",
            "MMM dd, yyyy",
            "yyyy"
        )
        formats.forEach { pattern ->
            runCatching {
                val formatter = SimpleDateFormat(pattern, Locale.US)
                formatter.parse(uploadDate)?.time
            }.getOrNull()?.let { return it }
        }
        return null
    }
}
