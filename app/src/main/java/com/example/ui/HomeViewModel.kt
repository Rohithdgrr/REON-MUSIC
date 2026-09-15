package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val masterTracks = listOf(
        TrackItem("refractions", "Refractions", "Aurora Glow", "Refraction EP", "03:47", isPlaying = true, isLiked = true, badge = "96kHz Lossless", artSeed = 101),
        TrackItem("tr1", "Midnight Prism", "Kavinsky", "OutRun Reimagined", "04:12", rank = "01", plays = "+34% plays", badge = "Hi-Res FLAC", artSeed = 102),
        TrackItem("tr2", "Ethereal Echoes", "Aurora Glow", "Refraction EP", "03:18", rank = "02", plays = "+28% plays", badge = "Binaural 3D", artSeed = 103),
        TrackItem("tr3", "Solar Flare", "ØZI", "Cybernetic Funk", "03:52", rank = "03", plays = "+19% plays", badge = "Lossless", artSeed = 104),
        TrackItem("nr1", "Prism-Dynamics", "Nova Pulse", "Prism World", "04:05", badge = "New Release", artSeed = 301),
        TrackItem("nr2", "Solar Flare II", "Cyber Soul", "Neon Orbit", "03:30", badge = "New Release", artSeed = 302),
        TrackItem("by1", "Elysian Field", "Aurora Glow", "Astral Plane", "04:44", badge = "Lossless", artSeed = 501),
        TrackItem("by2", "Sub-Zero Pulse", "Midnight Circuit", "Tokyo Nights", "03:15", badge = "Hi-Res", artSeed = 502),
        TrackItem("rp2", "Midnight Drift", "Stellar Echo", "Echoes", "04:12", badge = "Lossless", artSeed = 601),
        TrackItem("rp3", "Celestial Resonance", "Nighthawk", "Astral Wings", "05:48", badge = "96kHz", artSeed = 602),
        TrackItem("rd1", "Neon Horizon", "Synthwave '84", "Retrowave Anthology", "03:55", badge = "Retro Synth", artSeed = 701),
        TrackItem("rd2", "Distant Memory", "Ember Skies", "Silent Dawn", "04:20", badge = "Ambient Flow", artSeed = 702),
        TrackItem("tr4", "Hyperdrive", "ISOxo & Knock2", "SNAKEPIT", "03:08", badge = "Club Trap", artSeed = 105),
        TrackItem("tr5", "Breathe Underwater", "Fred again..", "Actual Life 3", "03:41", badge = "House", artSeed = 106),
        TrackItem("tr6", "Shibuya Neon", "PinkPantheress", "Heaven Knows", "02:45", badge = "Alt-Pop", artSeed = 107)
    )

    private val masterArtists = listOf(
        ArtistItem("fa1", "Kavinsky", "French Electro / Synthwave", isFollowing = false, artSeed = 201),
        ArtistItem("fa2", "Aurora Glow", "Ambient / Deep Focus", isFollowing = true, artSeed = 202),
        ArtistItem("fa3", "Fred again..", "House / UK Garage", isFollowing = false, artSeed = 203),
        ArtistItem("fa4", "Drake", "Hip-Hop / R&B", isFollowing = false, artSeed = 204),
        ArtistItem("fa5", "PinkPantheress", "Alt-Pop / Drum & Bass", isFollowing = false, artSeed = 205),
        ArtistItem("fa6", "ØZI", "R&B / Future Soul", isFollowing = false, artSeed = 206),
        ArtistItem("fa7", "ISOxo & Knock2", "Trap / Future Bass", isFollowing = true, artSeed = 207),
        ArtistItem("fa8", "Nova Pulse", "Cyberpunk / Synth", isFollowing = false, artSeed = 208)
    )

    private val masterAlbums = listOf(
        AlbumItem("al1", "Refraction EP", "Aurora Glow", "2024", "6 tracks · 22m", "Ambient", artSeed = 101),
        AlbumItem("al2", "OutRun Reimagined", "Kavinsky", "2024", "14 tracks · 52m", "Synthwave", artSeed = 102),
        AlbumItem("al3", "SNAKEPIT", "ISOxo & Knock2", "2024", "10 tracks · 34m", "Trap & Bass", artSeed = 207),
        AlbumItem("al4", "Actual Life 3", "Fred again..", "2023", "12 tracks · 44m", "Electronic", artSeed = 203),
        AlbumItem("al5", "Prism World", "Nova Pulse", "2024", "8 tracks · 31m", "Cyberpunk", artSeed = 301)
    )

    private val masterPlaylists = listOf(
        PlaylistItem("fp1", "Electric Nights", "32 tracks · High energy synth", "32 tracks", "1h 32m", artSeed = 401),
        PlaylistItem("fp2", "Deep Resonance", "24 tracks · Deep focus flow", "24 tracks", "Deep Focus", artSeed = 402),
        PlaylistItem("fp3", "Tokyo Underground 2025", "28 tracks · Shibuya electronic scene", "28 tracks", "1h 18m", artSeed = 403),
        PlaylistItem("fp4", "Lossless Hi-Res Master Studio", "40 tracks · 24-bit 96kHz pure audio", "40 tracks", "2h 45m", artSeed = 404),
        PlaylistItem("fp5", "Late Night Cyberpunk Drive", "35 tracks · Modular neon beats", "35 tracks", "2h 10m", artSeed = 405)
    )

    private val masterCategories = listOf(
        CategoryBrowseItem("cat1", "Deep Focus", "Binaural waves & ambient study", "🎧", listOf(0xFF0057FF, 0xFF002277), "Deep Focus"),
        CategoryBrowseItem("cat2", "Synthwave & 80s", "Neon analog synthesizers", "🏎️", listOf(0xFFCF094C, 0xFF7A0028), "Synthwave"),
        CategoryBrowseItem("cat3", "Tokyo Underground", "Shibuya modular electronic", "⚡", listOf(0xFF6B21A8, 0xFF3B0764), "Tokyo"),
        CategoryBrowseItem("cat4", "Hi-Res Lossless (96k)", "Master quality FLAC audio", "💎", listOf(0xFF0D9488, 0xFF115E59), "Lossless"),
        CategoryBrowseItem("cat5", "Club Trap & Bass", "Explosive festival energy", "💥", listOf(0xFFEA580C, 0xFF9A3412), "Trap"),
        CategoryBrowseItem("cat6", "Ambient & Space", "Zero gravity meditation", "🌙", listOf(0xFF1E293B, 0xFF0F172A), "Ambient"),
        CategoryBrowseItem("cat7", "Lo-Fi Beats", "Warm vinyl & study vibes", "☕", listOf(0xFF854D0E, 0xFF451A03), "Lo-fi"),
        CategoryBrowseItem("cat8", "Cyberpunk 2099", "Dystopian industrial rhythm", "🤖", listOf(0xFF0284C7, 0xFF0369A1), "Cyber"),
        CategoryBrowseItem("cat9", "Spatial Atmos 3D", "Binaural headphone 360", "🌌", listOf(0xFF4F46E5, 0xFF312E81), "Spatial"),
        CategoryBrowseItem("cat10", "Liquid DnB", "Fast tempo soulful rollers", "🌊", listOf(0xFF059669, 0xFF064E3B), "Drum & Bass")
    )

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun createInitialState(): HomeState {
        val initialDownloaded = listOf(
            TrackItem(
                id = "track_refractions",
                title = "Refractions",
                artist = "Aurora Glow",
                album = "Refraction EP",
                duration = "04:18",
                badge = "96kHz FLAC",
                isPlaying = true,
                isLiked = true,
                artSeed = 1
            ),
            TrackItem(
                id = "track_nightcall",
                title = "Nightcall (Neon Re-edit)",
                artist = "Kavinsky",
                album = "OutRun",
                duration = "04:45",
                badge = "320 KBPS",
                isPlaying = false,
                isLiked = true,
                artSeed = 2
            ),
            TrackItem(
                id = "track_usb002",
                title = "USB 002 Live Set",
                artist = "Fred again..",
                album = "USB Records",
                duration = "1h 12m",
                badge = "24-BIT MASTER",
                isPlaying = false,
                isLiked = true,
                artSeed = 3
            ),
            TrackItem(
                id = "track_chroma",
                title = "Chroma 004",
                artist = "Bicep",
                album = "Chroma Series",
                duration = "06:12",
                badge = "LOSSLESS",
                isPlaying = false,
                isLiked = false,
                artSeed = 4
            ),
            TrackItem(
                id = "track_theta",
                title = "Deep Theta Waves",
                artist = "REON Focus",
                album = "Spatial Waves",
                duration = "45:00",
                badge = "SPATIAL 3D",
                isPlaying = false,
                isLiked = true,
                artSeed = 5
            )
        )

        val playlistTracks = listOf(
            TrackItem("pl_1", "Refractions", "Aurora Glow", "Refraction EP", "04:18", badge = "96kHz FLAC", isPlaying = true, isLiked = true, artSeed = 1),
            TrackItem("pl_2", "Nightcall (Neon Re-edit)", "Kavinsky", "OutRun", "04:45", badge = "320 KBPS", isPlaying = false, isLiked = true, artSeed = 2),
            TrackItem("pl_3", "USB 002 Live Set (Pt. 1)", "Fred again..", "USB Records", "06:12", badge = "24-Bit Master", isPlaying = false, isLiked = true, artSeed = 3),
            TrackItem("pl_4", "Chroma 004", "Bicep", "Chroma Series", "05:40", badge = "Lossless", isPlaying = false, isLiked = false, artSeed = 4),
            TrackItem("pl_5", "Deep Theta Waves", "REON Focus", "Spatial Waves", "08:30", badge = "Spatial 3D", isPlaying = false, isLiked = true, artSeed = 5),
            TrackItem("pl_6", "Solaris Motion", "Kiasmos", "Solaris", "05:15", badge = "Hi-Res FLAC", isPlaying = false, isLiked = false, artSeed = 6),
            TrackItem("pl_7", "Vapour Trails", "Tycho", "Epoch", "04:22", badge = "24-Bit / 48kHz", isPlaying = false, isLiked = true, artSeed = 7)
        )

        return HomeState(
            isLoading = false,
            greetingName = "Rohith",
            greetingSubtitle = "Your daily mix is ready. Let's explore.",
            searchQuery = "",
            selectedFilter = "All",
            filterChips = listOf("All", "Songs", "Artists", "Albums", "Playlists"),

            activePlaylistTitle = "Deep Focus // Electric Light",
            activePlaylistDescription = "Subtle ambient textures, precision glitch rhythms, and generative modular synthesis tuned for deep flow states.",
            activePlaylistTagline = "REON ORIGINAL • 2 hr 14 min",
            activePlaylistTracks = playlistTracks,
            isPlaylistLiked = true,
            isPlaylistDownloaded = true,

            recentSearches = listOf("Aurora Glow", "Tokyo Underground", "Refractions", "Synthwave 84", "96kHz Lossless"),
            trendingSearches = listOf("ISOxo & Knock2", "Midnight Prism", "Deep Focus", "Cyber Soul", "Spatial Atmos", "Liquid DnB"),
            browseCategories = masterCategories,
            downloadedTracks = initialDownloaded,
            storageUsedMb = 8600f,
            storageTotalMb = 64000f,
            downloadFilterChips = listOf("All", "Playlists", "Albums", "Tracks"),
            selectedDownloadFilter = "All",

            currentTrack = masterTracks[0],
            playbackProgress = 0.37f,
            currentPositionStr = "01:24",
            remainingPositionStr = "-02:23",
            queueText = "Queue 3/4",
            playingInText = "Playing in ~ 14",

            likedCount = "248 tracks",
            downloadsCount = "08 tracks",
            historyText = "Recent activity",
            flowRadioText = "Your station",

            trendingList = masterTracks.take(3),
            topChartsList = listOf(masterTracks[1], masterTracks[0]),
            featuredArtists = masterArtists.take(6),

            artistOfTheWeekHeadline = "ISOxo & Knock2: Reimagining Trap & Future Bass",
            artistOfTheWeekSubtitle = "Inside the sonic laboratory behind the world tour, the DIY club sets, and their explosive collaborative sets.",
            artistOfTheWeekTag = "ARTIST OF THE WEEK",

            newReleases = listOf(masterTracks[4], masterTracks[5]),

            moodsGenres = listOf(
                MoodGenreItem("mg1", "🌙", "Chill", 0xFFE8EFFF, 0xFF0057FF, isSelected = false),
                MoodGenreItem("mg2", "⚡", "Energy", 0xFFFFE8EE, 0xFFCF094C, isSelected = false),
                MoodGenreItem("mg3", "🎧", "Focus", 0xFF0057FF, 0xFFFFFFFF, isSelected = true),
                MoodGenreItem("mg4", "🎉", "Party", 0xFFF3E8FF, 0xFF7A3FE0, isSelected = false),
                MoodGenreItem("mg5", "☕", "Lo-fi", 0xFFEBEDFF, 0xFF2A50CD, isSelected = false),
                MoodGenreItem("mg6", "☀️", "Morning", 0xFFFFF4E0, 0xFFB26A00, isSelected = false)
            ),
            selectedMood = "Focus",

            featuredPlaylists = masterPlaylists.take(2),
            dailyMixes = listOf(
                DailyMixItem("dm1", "MIX 01", "Arcturus", "50 songs · 2h 47m", "50 songs", isPrimaryGradient = true),
                DailyMixItem("dm2", "MIX 02", "Synthwave", "45 songs · 2h 15m", "45 songs", isPrimaryGradient = false)
            ),

            becauseYouPlayedArtist = "Aurora Glow",
            becauseYouPlayedMatch = "69% MATCH",
            becauseYouPlayedList = listOf(masterTracks[6], masterTracks[7]),

            editorsPickHeadline = "The breakthrough sound of Tokyo underground",
            editorsPickDescription = "Exploring futuristic Shibuya ambient beat scenes with modular synthesizers and hyper-dense rhythms.",
            editorsPickTag = "DEEP FOCUS",

            recentlyPlayedList = listOf(masterTracks[0], masterTracks[8], masterTracks[9]),
            rediscoverList = listOf(masterTracks[10], masterTracks[11]),
            liveEvents = listOf(
                LiveEventItem("le1", "Kaytranada: Live", "The Forum", "Los Angeles, CA", "OCT", "12", "LIVE", artSeed = 801),
                LiveEventItem("le2", "ODESZA: The Last Goodbye", "Red Rocks Amphitheatre", "Morrison, CO", "NOV", "04", "UPCOMING", artSeed = 802)
            )
        )
    }

    private fun loadHomeData() {
        // Run initial empty filter
        filterResults("", "All")
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterResults(query, _uiState.value.selectedFilter)
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        filterResults(_uiState.value.searchQuery, filter)
    }

    fun selectRecentSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterResults(query, _uiState.value.selectedFilter)
    }

    fun removeRecentSearch(query: String) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filter { it != query })
        }
    }

    fun clearRecentSearches() {
        _uiState.update { it.copy(recentSearches = emptyList()) }
    }

    fun startVoiceSearch() {
        _uiState.update { it.copy(isVoiceSearching = true, voiceTranscript = "Listening...") }
        viewModelScope.launch {
            delay(1200)
            _uiState.update { it.copy(voiceTranscript = "Recognizing: 'Aurora Glow Refractions'") }
            delay(1000)
            val recognizedQuery = "Aurora Glow"
            _uiState.update {
                it.copy(
                    isVoiceSearching = false,
                    searchQuery = recognizedQuery,
                    toastMessage = "Voice match: '$recognizedQuery'"
                )
            }
            filterResults(recognizedQuery, _uiState.value.selectedFilter)
        }
    }

    fun cancelVoiceSearch() {
        _uiState.update { it.copy(isVoiceSearching = false, voiceTranscript = "") }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        filterResults("", _uiState.value.selectedFilter)
    }

    private fun filterResults(query: String, filter: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    searchResultsTracks = emptyList(),
                    searchResultsArtists = emptyList(),
                    searchResultsAlbums = emptyList(),
                    searchResultsPlaylists = emptyList(),
                    searchResultsMoods = emptyList(),
                    topMatch = null
                )
            }
            return
        }

        val q = trimmed.lowercase()

        // Match songs (tracks)
        val matchedTracks = masterTracks.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.album.lowercase().contains(q)
        }

        // Match artists
        val matchedArtists = masterArtists.filter {
            it.name.lowercase().contains(q) ||
            it.genre.lowercase().contains(q)
        }

        // Match albums
        val matchedAlbums = masterAlbums.filter {
            it.title.lowercase().contains(q) ||
            it.artist.lowercase().contains(q) ||
            it.genre.lowercase().contains(q)
        }

        // Match playlists
        val matchedPlaylists = masterPlaylists.filter {
            it.title.lowercase().contains(q) ||
            it.subtitle.lowercase().contains(q)
        }

        // Compute top match across artists, songs, albums, playlists
        val topArtist = matchedArtists.firstOrNull { it.name.lowercase().startsWith(q) || it.name.lowercase() == q }
        val topTrack = matchedTracks.firstOrNull { it.title.lowercase().startsWith(q) || it.title.lowercase() == q }
        val topAlbum = matchedAlbums.firstOrNull { it.title.lowercase().startsWith(q) || it.title.lowercase() == q }
        val topPlaylist = matchedPlaylists.firstOrNull { it.title.lowercase().startsWith(q) || it.title.lowercase() == q }

        val topMatchResult: TopMatchResult? = when {
            topArtist != null -> TopMatchResult(
                type = "ARTIST",
                id = topArtist.id,
                title = topArtist.name,
                subtitle = "Artist · ${topArtist.genre}",
                extraInfo = "Featured Artist",
                isFollowing = topArtist.isFollowing,
                artSeed = topArtist.artSeed
            )
            topTrack != null -> TopMatchResult(
                type = "SONG",
                id = topTrack.id,
                title = topTrack.title,
                subtitle = "Song · ${topTrack.artist}",
                extraInfo = "${topTrack.album} · ${topTrack.duration}",
                isLiked = topTrack.isLiked,
                artSeed = topTrack.artSeed
            )
            topAlbum != null -> TopMatchResult(
                type = "ALBUM",
                id = topAlbum.id,
                title = topAlbum.title,
                subtitle = "Album · ${topAlbum.artist}",
                extraInfo = "${topAlbum.year} · ${topAlbum.trackCount}",
                artSeed = topAlbum.artSeed
            )
            topPlaylist != null -> TopMatchResult(
                type = "PLAYLIST",
                id = topPlaylist.id,
                title = topPlaylist.title,
                subtitle = "Playlist",
                extraInfo = "${topPlaylist.trackCount} · ${topPlaylist.duration}",
                artSeed = topPlaylist.artSeed
            )
            matchedTracks.isNotEmpty() -> {
                val first = matchedTracks.first()
                TopMatchResult(
                    type = "SONG",
                    id = first.id,
                    title = first.title,
                    subtitle = "Song · ${first.artist}",
                    extraInfo = "${first.album} · ${first.duration}",
                    isLiked = first.isLiked,
                    artSeed = first.artSeed
                )
            }
            matchedArtists.isNotEmpty() -> {
                val first = matchedArtists.first()
                TopMatchResult(
                    type = "ARTIST",
                    id = first.id,
                    title = first.name,
                    subtitle = "Artist · ${first.genre}",
                    extraInfo = "Featured Artist",
                    isFollowing = first.isFollowing,
                    artSeed = first.artSeed
                )
            }
            matchedAlbums.isNotEmpty() -> {
                val first = matchedAlbums.first()
                TopMatchResult(
                    type = "ALBUM",
                    id = first.id,
                    title = first.title,
                    subtitle = "Album · ${first.artist}",
                    extraInfo = "${first.year} · ${first.trackCount}",
                    artSeed = first.artSeed
                )
            }
            matchedPlaylists.isNotEmpty() -> {
                val first = matchedPlaylists.first()
                TopMatchResult(
                    type = "PLAYLIST",
                    id = first.id,
                    title = first.title,
                    subtitle = "Playlist",
                    extraInfo = "${first.trackCount} · ${first.duration}",
                    artSeed = first.artSeed
                )
            }
            else -> null
        }

        // Apply filter tab strictly for Songs, Artists, Albums, Playlists
        val finalTracks = if (filter == "All" || filter == "Songs") matchedTracks else emptyList()
        val finalArtists = if (filter == "All" || filter == "Artists") matchedArtists else emptyList()
        val finalAlbums = if (filter == "All" || filter == "Albums") matchedAlbums else emptyList()
        val finalPlaylists = if (filter == "All" || filter == "Playlists") matchedPlaylists else emptyList()

        // Update state and add query to recent searches
        _uiState.update { state ->
            val updatedRecent = if (trimmed.length > 2 && !state.recentSearches.contains(trimmed)) {
                (listOf(trimmed) + state.recentSearches).take(8)
            } else state.recentSearches

            state.copy(
                recentSearches = updatedRecent,
                searchResultsTracks = finalTracks,
                searchResultsArtists = finalArtists,
                searchResultsAlbums = finalAlbums,
                searchResultsPlaylists = finalPlaylists,
                searchResultsMoods = emptyList(),
                topMatch = topMatchResult
            )
        }
    }

    fun onMoodSelected(moodLabel: String) {
        _uiState.update { state ->
            val updated = state.moodsGenres.map {
                it.copy(isSelected = it.label == moodLabel)
            }
            state.copy(moodsGenres = updated, selectedMood = moodLabel)
        }
    }

    fun togglePlayPause() {
        _uiState.update { state ->
            val playing = !state.currentTrack.isPlaying
            state.copy(
                currentTrack = state.currentTrack.copy(isPlaying = playing)
            )
        }
    }

    fun toggleLike() {
        _uiState.update { state ->
            val newLiked = !state.currentTrack.isLiked
            val msg = if (newLiked) "Added to Liked Songs" else "Removed from Liked Songs"
            state.copy(
                currentTrack = state.currentTrack.copy(isLiked = newLiked),
                toastMessage = msg
            )
        }
    }

    fun toggleFollowArtist(artistId: String) {
        _uiState.update { state ->
            val updated = state.featuredArtists.map {
                if (it.id == artistId) it.copy(isFollowing = !it.isFollowing) else it
            }
            val updatedSearchResults = state.searchResultsArtists.map {
                if (it.id == artistId) it.copy(isFollowing = !it.isFollowing) else it
            }
            val updatedTopMatch = if (state.topMatch?.id == artistId) {
                state.topMatch.copy(isFollowing = !state.topMatch.isFollowing)
            } else state.topMatch

            state.copy(
                featuredArtists = updated,
                searchResultsArtists = updatedSearchResults,
                topMatch = updatedTopMatch,
                toastMessage = "Artist followed"
            )
        }
    }

    fun playTrack(track: TrackItem) {
        _uiState.update { state ->
            state.copy(
                currentTrack = track.copy(isPlaying = true),
                toastMessage = "Playing ${track.title}"
            )
        }
    }

    fun selectTab(tab: HomeTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun openNowPlaying() {
        _uiState.update { it.copy(showNowPlayingScreen = true) }
    }

    fun closeNowPlaying() {
        _uiState.update { it.copy(showNowPlayingScreen = false) }
    }

    fun toggleOfflineMode() {
        _uiState.update {
            val next = !it.isOfflineModeOnly
            it.copy(
                isOfflineModeOnly = next,
                toastMessage = if (next) "Offline Mode: Only playing stored lossless FLAC" else "Online Mode Restored"
            )
        }
    }

    fun toggleAutoSync() {
        _uiState.update {
            val next = !it.isSmartAutoSyncEnabled
            it.copy(
                isSmartAutoSyncEnabled = next,
                toastMessage = if (next) "Smart Auto-Sync Enabled (Wi-Fi)" else "Smart Auto-Sync Paused"
            )
        }
    }

    fun toggleCellularDownload() {
        _uiState.update {
            val next = !it.isCellularDownloadAllowed
            it.copy(
                isCellularDownloadAllowed = next,
                toastMessage = if (next) "Cellular Downloads Allowed (High Data Usage)" else "Cellular Downloads Blocked (Wi-Fi Only)"
            )
        }
    }

    fun setDownloadFilter(filter: String) {
        _uiState.update { it.copy(selectedDownloadFilter = filter) }
    }

    fun setDownloadQuality(quality: String) {
        _uiState.update {
            it.copy(
                downloadQuality = quality,
                showQualitySelector = false,
                toastMessage = "Download Quality set to $quality"
            )
        }
    }

    fun toggleQualitySelector(show: Boolean) {
        _uiState.update { it.copy(showQualitySelector = show) }
    }

    fun shufflePlayDownloads() {
        val downloads = _uiState.value.downloadedTracks
        if (downloads.isNotEmpty()) {
            val randomTrack = downloads.shuffled().first()
            playTrack(randomTrack)
            showToast("Shuffling ${downloads.size} downloaded tracks")
        } else {
            showToast("No downloaded tracks to shuffle")
        }
    }

    fun clearAllDownloads() {
        _uiState.update { state ->
            state.copy(
                downloadedTracks = emptyList(),
                downloadsCount = "00 tracks",
                storageUsedMb = 120f,
                storageAudioMb = 0f,
                storageSpatialMb = 0f,
                toastMessage = "All offline downloads cleared (1.72 GB freed)"
            )
        }
    }

    fun openPlaylist(playlist: PlaylistItem? = null) {
        _uiState.update { state ->
            val title = playlist?.title ?: "Deep Focus // Electric Light"
            val desc = playlist?.subtitle ?: "Subtle ambient textures, precision glitch rhythms, and generative modular synthesis tuned for deep flow states."
            state.copy(
                selectedPlaylist = playlist ?: PlaylistItem(
                    id = "pl_default",
                    title = title,
                    subtitle = desc,
                    trackCount = "28 tracks",
                    duration = "1 hr 45 min",
                    artSeed = 1
                ),
                activePlaylistTitle = title,
                activePlaylistDescription = desc
            )
        }
    }

    fun closePlaylist() {
        _uiState.update { state ->
            state.copy(selectedPlaylist = null)
        }
    }

    fun openAlbum(album: AlbumItem? = null) {
        _uiState.update { state ->
            val albumToSelect = album ?: AlbumItem(
                id = "alb_refractions",
                title = "Refractions",
                artist = "Aurora Glow",
                year = "2024",
                trackCount = "9 songs",
                genre = "Electronic",
                artSeed = 1
            )
            state.copy(
                selectedAlbum = albumToSelect,
                activeAlbumTitle = albumToSelect.title,
                activeAlbumArtist = albumToSelect.artist,
                activeAlbumYear = albumToSelect.year,
                activeAlbumTrackCount = "${albumToSelect.trackCount}, 42 min",
                activeAlbumTracks = listOf(
                    TrackItem("tr_ref_1", "Refractions", albumToSelect.artist, albumToSelect.title, "04:18", badge = "96kHz FLAC", isPlaying = true, artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_2", "Nightfall Prism", albumToSelect.artist, albumToSelect.title, "04:45", badge = "Lossless", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_3", "Subtle Drift", albumToSelect.artist, albumToSelect.title, "05:12", badge = "24-Bit", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_4", "Electric Horizon", albumToSelect.artist, albumToSelect.title, "03:58", badge = "FLAC", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_5", "Static Bloom", albumToSelect.artist, albumToSelect.title, "04:30", badge = "Lossless", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_6", "Chamber Float", albumToSelect.artist, albumToSelect.title, "06:14", badge = "96kHz", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_7", "Atomic Velocity", albumToSelect.artist, albumToSelect.title, "03:22", badge = "Dolby Atmos", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_8", "Crystalline", albumToSelect.artist, albumToSelect.title, "04:55", badge = "FLAC", artSeed = albumToSelect.artSeed),
                    TrackItem("tr_ref_9", "Signal Return", albumToSelect.artist, albumToSelect.title, "04:46", badge = "Master", artSeed = albumToSelect.artSeed)
                )
            )
        }
    }

    fun closeAlbum() {
        _uiState.update { state ->
            state.copy(selectedAlbum = null)
        }
    }

    fun toggleAlbumLiked() {
        _uiState.update { state ->
            val newLiked = !state.isAlbumLiked
            state.copy(
                isAlbumLiked = newLiked,
                toastMessage = if (newLiked) "Saved album to Your Library" else "Removed album from Library"
            )
        }
    }

    fun toggleAlbumDownloaded() {
        _uiState.update { state ->
            val newDownloaded = !state.isAlbumDownloaded
            state.copy(
                isAlbumDownloaded = newDownloaded,
                toastMessage = if (newDownloaded) "Album downloaded for offline play" else "Removed album downloads"
            )
        }
    }

    fun openArtist(artist: ArtistItem? = null) {
        _uiState.update { state ->
            val artistToSelect = artist ?: ArtistItem(
                id = "art_aurora",
                name = "Aurora Glow",
                genre = "Electronic",
                isFollowing = false,
                artSeed = 1
            )
            state.copy(
                selectedArtist = artistToSelect,
                activeArtistName = artistToSelect.name,
                activeArtistListeners = "1,482,904",
                isArtistFollowed = artistToSelect.isFollowing,
                activeArtistTracks = listOf(
                    TrackItem("tr_ref_1", "Refractions", artistToSelect.name, "Refractions", "04:18", badge = "96k FLAC", plays = "8,924,103 plays", isPlaying = true, artSeed = 1),
                    TrackItem("tr_ref_2", "Nightfall Prism", artistToSelect.name, "Refractions", "04:45", badge = "LOSSLESS", plays = "6,412,890 plays", artSeed = 1),
                    TrackItem("tr_ref_4", "Electric Horizon", artistToSelect.name, "Refractions", "03:58", badge = "24-BIT", plays = "4,891,012 plays", artSeed = 1),
                    TrackItem("tr_ref_3", "Subtle Drift", artistToSelect.name, "Refractions", "05:12", badge = "ATMOS", plays = "3,170,440 plays", artSeed = 1),
                    TrackItem("tr_ref_8", "Crystalline", artistToSelect.name, "Refractions", "04:55", badge = "FLAC", plays = "2,852,990 plays", artSeed = 1)
                )
            )
        }
    }

    fun closeArtist() {
        _uiState.update { state ->
            state.copy(selectedArtist = null)
        }
    }

    fun toggleFollowArtistFromProfile() {
        _uiState.update { state ->
            val newFollowState = !state.isArtistFollowed
            state.copy(
                isArtistFollowed = newFollowState,
                toastMessage = if (newFollowState) "Following ${state.activeArtistName}" else "Unfollowed ${state.activeArtistName}"
            )
        }
    }

    fun openLikedSongs() {
        _uiState.update { state ->
            state.copy(isLikedSongsOpen = true)
        }
    }

    fun closeLikedSongs() {
        _uiState.update { state ->
            state.copy(isLikedSongsOpen = false)
        }
    }

    fun openHistory() {
        _uiState.update { state ->
            state.copy(isHistoryOpen = true)
        }
    }

    fun closeHistory() {
        _uiState.update { state ->
            state.copy(isHistoryOpen = false)
        }
    }

    fun openSettings() {
        _uiState.update { state ->
            state.copy(isSettingsOpen = true)
        }
    }

    fun closeSettings() {
        _uiState.update { state ->
            state.copy(isSettingsOpen = false)
        }
    }

    fun openAnalytics() {
        _uiState.update { state ->
            state.copy(isAnalyticsOpen = true)
        }
    }

    fun closeAnalytics() {
        _uiState.update { state ->
            state.copy(isAnalyticsOpen = false)
        }
    }

    fun togglePlaylistLiked() {
        _uiState.update { state ->
            val newLiked = !state.isPlaylistLiked
            state.copy(
                isPlaylistLiked = newLiked,
                toastMessage = if (newLiked) "Added playlist to Your Library" else "Removed playlist from Library"
            )
        }
    }

    fun togglePlaylistDownloaded() {
        _uiState.update { state ->
            val newDownloaded = !state.isPlaylistDownloaded
            state.copy(
                isPlaylistDownloaded = newDownloaded,
                toastMessage = if (newDownloaded) "Downloading playlist in 96kHz FLAC" else "Playlist downloads removed"
            )
        }
    }

    fun shufflePlaylist() {
        val tracks = _uiState.value.activePlaylistTracks
        if (tracks.isNotEmpty()) {
            val randomTrack = tracks.shuffled().first()
            playTrack(randomTrack)
            showToast("Shuffling playlist (${tracks.size} tracks)")
        }
    }

    fun playPlaylist() {
        val tracks = _uiState.value.activePlaylistTracks
        if (tracks.isNotEmpty()) {
            playTrack(tracks.first())
            showToast("Playing ${_uiState.value.activePlaylistTitle}")
        }
    }

    fun downloadAllLiked() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloadingActive = true,
                    downloadProgress = 0.1f,
                    activeDownloadingTrackName = "Preparing Master 96kHz FLAC Batch…"
                )
            }
            delay(500)
            _uiState.update {
                it.copy(
                    downloadProgress = 0.45f,
                    activeDownloadingTrackName = "Downloading: ISOxo & Knock2 - radiate.fm (24-bit/96kHz)"
                )
            }
            delay(600)
            _uiState.update {
                it.copy(
                    downloadProgress = 0.85f,
                    activeDownloadingTrackName = "Verifying FLAC Bitstream Integrity…"
                )
            }
            delay(400)
            val newDownloaded = masterTracks.take(8).map {
                it.copy(
                    badge = "FLAC 96kHz · 38 MB",
                    isLiked = true
                )
            }
            _uiState.update { state ->
                state.copy(
                    isDownloadingActive = false,
                    downloadProgress = 1f,
                    activeDownloadingTrackName = "",
                    downloadedTracks = newDownloaded,
                    downloadsCount = "${newDownloaded.size} tracks",
                    storageUsedMb = 2450f,
                    storageAudioMb = 1980f,
                    storageSpatialMb = 370f,
                    toastMessage = "Successfully downloaded ${newDownloaded.size} tracks in 24-bit/96kHz FLAC"
                )
            }
        }
    }

    fun removeDownload(trackId: String) {
        _uiState.update { state ->
            val trackToRemove = state.downloadedTracks.firstOrNull { it.id == trackId }
            val updated = state.downloadedTracks.filter { it.id != trackId }
            val newAudioMb = (state.storageAudioMb - 40f).coerceAtLeast(0f)
            val newTotalMb = (state.storageUsedMb - 40f).coerceAtLeast(100f)
            state.copy(
                downloadedTracks = updated,
                downloadsCount = "${updated.size} tracks",
                storageUsedMb = newTotalMb,
                storageAudioMb = newAudioMb,
                toastMessage = "Removed ${trackToRemove?.title ?: "track"} from offline storage"
            )
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}

