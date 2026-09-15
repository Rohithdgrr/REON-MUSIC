package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReonApplication
import com.example.data.ReonDatabase
import com.example.data.TrackEntity
import com.example.data.ArtistEntity
import com.example.data.AlbumEntity
import com.example.data.PlaylistEntity
import com.example.data.NotificationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db: ReonDatabase = (application as ReonApplication).database
    private val dao = db.reonDao()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        seedAndLoadDatabase()
    }

    private fun createInitialState(): HomeState {
        return HomeState(
            isLoading = true,
            greetingName = "Rohith",
            greetingSubtitle = "Your daily mix is ready. Let's explore.",
            searchQuery = "",
            selectedFilter = "All",
            filterChips = listOf("All", "Songs", "Artists", "Albums", "Playlists"),
            notifications = emptyList(),
            activePlaylistTitle = "Deep Focus // Electric Light",
            activePlaylistDescription = "Subtle ambient textures, precision glitch rhythms, and generative modular synthesis tuned for deep flow states.",
            activePlaylistTagline = "REON ORIGINAL • 2 hr 14 min",
            activePlaylistTracks = emptyList(),
            isPlaylistLiked = true,
            isPlaylistDownloaded = true,
            recentSearches = listOf("Aurora Glow", "Tokyo Underground", "Refractions", "Synthwave 84", "96kHz Lossless"),
            trendingSearches = listOf("ISOxo & Knock2", "Midnight Prism", "Deep Focus", "Cyber Soul", "Spatial Atmos", "Liquid DnB"),
            browseCategories = emptyList(),
            downloadedTracks = emptyList(),
            storageUsedMb = 8600f,
            storageTotalMb = 64000f,
            downloadFilterChips = listOf("All", "Playlists", "Albums", "Tracks"),
            selectedDownloadFilter = "All",
            playbackProgress = 0.37f,
            currentPositionStr = "01:24",
            remainingPositionStr = "-02:23",
            queueText = "Queue 3/4",
            playingInText = "Playing in ~ 14",
            likedCount = "0 tracks",
            downloadsCount = "0 tracks",
            historyText = "Recent activity",
            flowRadioText = "Your station",
            trendingList = emptyList(),
            topChartsList = emptyList(),
            featuredArtists = emptyList(),
            artistOfTheWeekHeadline = "ISOxo & Knock2: Reimagining Trap & Future Bass",
            artistOfTheWeekSubtitle = "Inside the sonic laboratory behind the world tour, the DIY club sets, and their explosive collaborative sets.",
            artistOfTheWeekTag = "ARTIST OF THE WEEK",
            newReleases = emptyList(),
            moodsGenres = listOf(
                MoodGenreItem("mg1", "🌙", "Chill", 0xFFE8EFFF, 0xFF0057FF, isSelected = false),
                MoodGenreItem("mg2", "⚡", "Energy", 0xFFFFE8EE, 0xFFCF094C, isSelected = false),
                MoodGenreItem("mg3", "🎧", "Focus", 0xFF0057FF, 0xFFFFFFFF, isSelected = true),
                MoodGenreItem("mg4", "🎉", "Party", 0xFFF3E8FF, 0xFF7A3FE0, isSelected = false),
                MoodGenreItem("mg5", "☕", "Lo-fi", 0xFFEBEDFF, 0xFF2A50CD, isSelected = false),
                MoodGenreItem("mg6", "☀️", "Morning", 0xFFFFF4E0, 0xFFB26A00, isSelected = false)
            ),
            selectedMood = "Focus",
            featuredPlaylists = emptyList(),
            dailyMixes = listOf(
                DailyMixItem("dm1", "MIX 01", "Arcturus", "50 songs · 2h 47m", "50 songs", isPrimaryGradient = true),
                DailyMixItem("dm2", "MIX 02", "Synthwave", "45 songs · 2h 15m", "45 songs", isPrimaryGradient = false)
            ),
            becauseYouPlayedArtist = "Aurora Glow",
            becauseYouPlayedMatch = "69% MATCH",
            becauseYouPlayedList = emptyList(),
            editorsPickHeadline = "The breakthrough sound of Tokyo underground",
            editorsPickDescription = "Exploring futuristic Shibuya ambient beat scenes with modular synthesizers and hyper-dense rhythms.",
            editorsPickTag = "DEEP FOCUS",
            recentlyPlayedList = emptyList(),
            rediscoverList = emptyList(),
            liveEvents = listOf(
                LiveEventItem("le1", "Kaytranada: Live", "The Forum", "Los Angeles, CA", "OCT", "12", "LIVE", artSeed = 801),
                LiveEventItem("le2", "ODESZA: The Last Goodbye", "Red Rocks Amphitheatre", "Morrison, CO", "NOV", "04", "UPCOMING", artSeed = 802)
            )
        )
    }

    private fun seedAndLoadDatabase() {
        viewModelScope.launch {
            // Seed database if empty
            val existingNotifications = dao.getAllNotifications()
            if (existingNotifications.isEmpty()) {
                val seedTracks = listOf(
                    TrackEntity("refractions", "Refractions", "Aurora Glow", "Refraction EP", "DEEP FOCUS", 227000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80", "Direct FLAC Studio", "96KHZ · FLAC", "Binaural Spatial", "FLAC", "24-bit / 96kHz", "62.4M monthly", "“Floating through shards of electric light\nCaught in the frequency of the night”", isLiked = true, isDownloaded = true, badge = "96kHz Lossless", artSeed = 101),
                    TrackEntity("tr1", "Midnight Prism", "Kavinsky", "OutRun Reimagined", "SYNTH AMBIENCE", 252000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80", "Qobuz Studio Hi-Res", "192KHZ · DSD", "Dolby Atmos", "DSD / FLAC", "32-bit / 192kHz", "18.2M monthly", "“Digital waves across the neural sky\nInfinite echoes passing by”", rank = "01", plays = "+34% plays", badge = "Hi-Res FLAC", artSeed = 102),
                    TrackEntity("tr2", "Ethereal Echoes", "Aurora Glow", "Refraction EP", "DEEP FOCUS", 198000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "Direct FLAC Studio", "96KHZ · FLAC", "Binaural Spatial", "FLAC", "24-bit / 96kHz", "62.4M monthly", "“Listening to the silent hum”", rank = "02", plays = "+28% plays", badge = "Binaural 3D", artSeed = 103),
                    TrackEntity("tr3", "Solar Flare", "ØZI", "Cybernetic Funk", "FUNK", 232000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Direct Stream", "44.1KHZ", "Stereo", "AAC", "16-bit / 44.1kHz", "5M monthly", "", rank = "03", plays = "+19% plays", badge = "Lossless", artSeed = 104),
                    TrackEntity("nr1", "Prism-Dynamics", "Nova Pulse", "Prism World", "CYBERPUNK", 245000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "MQA Studio", "88.2KHZ", "Spatial MQA", "MQA", "24-bit / 88.2kHz", "2.1M monthly", "", badge = "New Release", artSeed = 301),
                    TrackEntity("nr2", "Solar Flare II", "Cyber Soul", "Neon Orbit", "SYNTH", 210000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Direct Stream", "48KHZ", "Spatial", "FLAC", "24-bit / 48kHz", "1.2M monthly", "", badge = "New Release", artSeed = 302),
                    TrackEntity("by1", "Elysian Field", "Aurora Glow", "Astral Plane", "AMBIENT", 284000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "FLAC Master", "96KHZ", "Binaural", "FLAC", "24-bit / 96kHz", "62.4M monthly", "", badge = "Lossless", artSeed = 501),
                    TrackEntity("by2", "Sub-Zero Pulse", "Midnight Circuit", "Tokyo Nights", "ELECTRONIC", 195000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Stream", "44.1KHZ", "Stereo", "AAC", "16-bit / 44.1kHz", "800k monthly", "", badge = "Hi-Res", artSeed = 502),
                    TrackEntity("rp2", "Midnight Drift", "Stellar Echo", "Echoes", "AMBIENT", 252000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "FLAC Master", "48KHZ", "Stereo", "FLAC", "24-bit / 48kHz", "4.5M monthly", "", badge = "Lossless", artSeed = 601),
                    TrackEntity("rp3", "Celestial Resonance", "Nighthawk", "Astral Wings", "SPATIAL", 348000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Direct FLAC Studio", "96KHZ", "Binaural Spatial", "FLAC", "24-bit / 96kHz", "1.7M monthly", "", badge = "96kHz", artSeed = 602),
                    TrackEntity("rd1", "Neon Horizon", "Synthwave '84", "Retrowave Anthology", "RETRO", 235000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "Standard FLAC", "44.1KHZ", "Stereo", "FLAC", "16-bit / 44.1kHz", "9.2M monthly", "", badge = "Retro Synth", artSeed = 701),
                    TrackEntity("rd2", "Distant Memory", "Ember Skies", "Silent Dawn", "AMBIENT", 260000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Lossless FLAC", "44.1KHZ", "Stereo", "FLAC", "16-bit / 44.1kHz", "3.4M monthly", "", badge = "Ambient Flow", artSeed = 702),
                    TrackEntity("tr4", "Hyperdrive", "ISOxo & Knock2", "SNAKEPIT", "TRAP", 188000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "Direct FLAC Studio", "96KHZ", "Atmos 3D", "FLAC", "24-bit / 96kHz", "14.5M monthly", "", badge = "Club Trap", artSeed = 105),
                    TrackEntity("tr5", "Breathe Underwater", "Fred again..", "Actual Life 3", "HOUSE", 221000L, "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80", "", "Direct Stream", "48KHZ", "Stereo", "AAC", "24-bit / 48kHz", "48.6M monthly", "", badge = "House", artSeed = 106),
                    TrackEntity("tr6", "Shibuya Neon", "PinkPantheress", "Heaven Knows", "ALT-POP", 165000L, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80", "", "Lossless FLAC", "44.1KHZ", "Stereo", "FLAC", "16-bit / 44.1kHz", "22.3M monthly", "", badge = "Alt-Pop", artSeed = 107)
                )

                val seedArtists = listOf(
                    ArtistEntity("fa1", "Kavinsky", "French Electro / Synthwave", isFollowing = false, artSeed = 201),
                    ArtistEntity("fa2", "Aurora Glow", "Ambient / Deep Focus", isFollowing = true, artSeed = 202),
                    ArtistEntity("fa3", "Fred again..", "House / UK Garage", isFollowing = false, artSeed = 203),
                    ArtistEntity("fa4", "Drake", "Hip-Hop / R&B", isFollowing = false, artSeed = 204),
                    ArtistEntity("fa5", "PinkPantheress", "Alt-Pop / Drum & Bass", isFollowing = false, artSeed = 205),
                    ArtistEntity("fa6", "ØZI", "R&B / Future Soul", isFollowing = false, artSeed = 206),
                    ArtistEntity("fa7", "ISOxo & Knock2", "Trap / Future Bass", isFollowing = true, artSeed = 207),
                    ArtistEntity("fa8", "Nova Pulse", "Cyberpunk / Synth", isFollowing = false, artSeed = 208)
                )

                val seedAlbums = listOf(
                    AlbumEntity("al1", "Refraction EP", "Aurora Glow", "2024", "6 tracks · 22m", "Ambient", artSeed = 101),
                    AlbumEntity("al2", "OutRun Reimagined", "Kavinsky", "2024", "14 tracks · 52m", "Synthwave", artSeed = 102),
                    AlbumEntity("al3", "SNAKEPIT", "ISOxo & Knock2", "2024", "10 tracks · 34m", "Trap & Bass", artSeed = 207),
                    AlbumEntity("al4", "Actual Life 3", "Fred again..", "2023", "12 tracks · 44m", "Electronic", artSeed = 203),
                    AlbumEntity("al5", "Prism World", "Nova Pulse", "2024", "8 tracks · 31m", "Cyberpunk", artSeed = 301)
                )

                val seedPlaylists = listOf(
                    PlaylistEntity("fp1", "Electric Nights", "32 tracks · High energy synth", "32 tracks", "1h 32m", artSeed = 401),
                    PlaylistEntity("fp2", "Deep Resonance", "24 tracks · Deep focus flow", "24 tracks", "Deep Focus", artSeed = 402),
                    PlaylistEntity("fp3", "Tokyo Underground 2025", "28 tracks · Shibuya electronic scene", "28 tracks", "1h 18m", artSeed = 403),
                    PlaylistEntity("fp4", "Lossless Hi-Res Master Studio", "40 tracks · 24-bit 96kHz pure audio", "40 tracks", "2h 45m", artSeed = 404),
                    PlaylistEntity("fp5", "Late Night Cyberpunk Drive", "35 tracks · Modular neon beats", "35 tracks", "2h 10m", artSeed = 405)
                )

                val seedNotifications = listOf(
                    NotificationEntity("notif_1", "New Master Release: Refractions (Live Spatial)", "Aurora Glow has published an exclusive 24-bit/96kHz spatial binaural master recording.", "10m ago", "RELEASE", isRead = false, badge = "MASTER FLAC", trackId = "refractions", iconType = "music"),
                    NotificationEntity("notif_2", "Bit-Perfect USB DAC Mode Active", "Hardware DAC detected. Sample rate dynamically locked to 192kHz bit-perfect playback without OS resampling.", "1h ago", "AUDIO ENGINE", isRead = false, badge = "192kHz / 32-BIT", iconType = "bolt"),
                    NotificationEntity("notif_3", "Offline Batch Download Complete", "8 Lossless tracks from your Liked Songs library have been stored to high-speed offline cache.", "3h ago", "DOWNLOADS", isRead = true, badge = "OFFLINE READY", iconType = "download"),
                    NotificationEntity("notif_4", "ISOxo & Knock2 World Tour Added", "Upcoming concert dates added for Tokyo, Berlin, and Los Angeles. Tap to view tickets & live setlists.", "Yesterday", "LIVE", isRead = true, badge = "TOUR 2025", iconType = "event"),
                    NotificationEntity("notif_5", "Acoustic Ear Calibration Ready", "Your personalized HRTF spatial profile has been calibrated for ultra-low latency binaural monitoring.", "2d ago", "SYSTEM", isRead = true, badge = "DSP SOUND LAB", iconType = "settings")
                )

                dao.insertTracks(seedTracks)
                dao.insertArtists(seedArtists)
                dao.insertAlbums(seedAlbums)
                dao.insertPlaylists(seedPlaylists)
                dao.insertNotifications(seedNotifications)
            }

            // Real DB observe and load
            observeDatabase()
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            // Setup browse categories
            val masterCategories = listOf(
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

            launch {
                dao.getAllTracksFlow().collect { dbTracks ->
                    val tracks = dbTracks.map { it.toTrackItem() }
                    val currentTrackId = _uiState.value.currentTrack.id
                    val updatedCurrentTrack = tracks.firstOrNull { it.id == currentTrackId } ?: tracks.firstOrNull() ?: _uiState.value.currentTrack

                    val liked = tracks.filter { it.isLiked }
                    val downloaded = dbTracks.filter { it.isDownloaded }.map { it.toTrackItem() }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            browseCategories = masterCategories,
                            currentTrack = updatedCurrentTrack,
                            likedCount = "${liked.size} tracks",
                            downloadsCount = "${downloaded.size} tracks",
                            downloadedTracks = downloaded,
                            trendingList = tracks.take(3),
                            topChartsList = tracks.filter { it.rank.isNotEmpty() },
                            newReleases = tracks.filter { it.badge == "New Release" || it.id.startsWith("nr") },
                            becauseYouPlayedList = tracks.filter { it.artist == state.becauseYouPlayedArtist },
                            recentlyPlayedList = tracks.take(5).shuffled(),
                            rediscoverList = tracks.takeLast(3),
                            activePlaylistTracks = tracks.take(7)
                        )
                    }
                }
            }

            launch {
                dao.getAllArtistsFlow().collect { dbArtists ->
                    val artists = dbArtists.map { it.toArtistItem() }
                    _uiState.update { state ->
                        state.copy(
                            featuredArtists = artists.take(6)
                        )
                    }
                }
            }

            launch {
                dao.getAllAlbumsFlow().collect { dbAlbums ->
                    val albums = dbAlbums.map { it.toAlbumItem() }
                    _uiState.update { state ->
                        state.copy(
                            selectedAlbum = if (state.selectedAlbum != null) albums.firstOrNull { it.id == state.selectedAlbum.id } else null
                        )
                    }
                }
            }

            launch {
                dao.getAllPlaylistsFlow().collect { dbPlaylists ->
                    val playlists = dbPlaylists.map { it.toPlaylistItem() }
                    _uiState.update { state ->
                        state.copy(
                            featuredPlaylists = playlists.take(2),
                            selectedPlaylist = if (state.selectedPlaylist != null) playlists.firstOrNull { it.id == state.selectedPlaylist.id } else null
                        )
                    }
                }
            }

            launch {
                dao.getAllNotificationsFlow().collect { dbNotifs ->
                    val notifications = dbNotifs.map { it.toNotificationItem() }
                    _uiState.update { state ->
                        state.copy(
                            notifications = notifications
                        )
                    }
                }
            }
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun TrackEntity.toTrackItem(): TrackItem = TrackItem(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = formatDuration(durationMs),
        rank = rank,
        plays = plays,
        badge = badge,
        isPlaying = false,
        isLiked = isLiked,
        artSeed = artSeed
    )

    private fun ArtistEntity.toArtistItem(): ArtistItem = ArtistItem(
        id = id,
        name = name,
        genre = genre,
        isFollowing = isFollowing,
        artSeed = artSeed
    )

    private fun AlbumEntity.toAlbumItem(): AlbumItem = AlbumItem(
        id = id,
        title = title,
        artist = artist,
        year = year,
        trackCount = trackCount,
        genre = genre,
        artSeed = artSeed
    )

    private fun PlaylistEntity.toPlaylistItem(): PlaylistItem = PlaylistItem(
        id = id,
        title = title,
        subtitle = subtitle,
        trackCount = trackCount,
        duration = duration,
        artSeed = artSeed
    )

    private fun NotificationEntity.toNotificationItem(): ReonNotificationItem = ReonNotificationItem(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        category = category,
        isRead = isRead,
        badge = badge,
        actionText = actionText,
        trackId = trackId,
        albumId = albumId,
        iconType = iconType
    )

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

        viewModelScope.launch {
            val q = trimmed.lowercase()
            val allTracks = dao.getAllTracks().map { it.toTrackItem() }

            val matchedTracks = allTracks.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q)
            }

            val topTrack = matchedTracks.firstOrNull { it.title.lowercase().startsWith(q) || it.title.lowercase() == q }

            val topMatchResult: TopMatchResult? = when {
                topTrack != null -> TopMatchResult(
                    type = "SONG",
                    id = topTrack.id,
                    title = topTrack.title,
                    subtitle = "Song · ${topTrack.artist}",
                    extraInfo = "${topTrack.album} · ${topTrack.duration}",
                    isLiked = topTrack.isLiked,
                    artSeed = topTrack.artSeed
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
                else -> null
            }

            val finalTracks = if (filter == "All" || filter == "Songs") matchedTracks else emptyList()

            _uiState.update { state ->
                val updatedRecent = if (trimmed.length > 2 && !state.recentSearches.contains(trimmed)) {
                    (listOf(trimmed) + state.recentSearches).take(8)
                } else state.recentSearches

                state.copy(
                    recentSearches = updatedRecent,
                    searchResultsTracks = finalTracks,
                    searchResultsArtists = emptyList(),
                    searchResultsAlbums = emptyList(),
                    searchResultsPlaylists = emptyList(),
                    searchResultsMoods = emptyList(),
                    topMatch = topMatchResult
                )
            }
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
        viewModelScope.launch {
            val trackId = _uiState.value.currentTrack.id
            val isCurrentlyLiked = _uiState.value.currentTrack.isLiked
            val targetLiked = !isCurrentlyLiked
            dao.updateTrackLike(trackId, targetLiked)
            showToast(if (targetLiked) "Added to Liked Songs" else "Removed from Liked Songs")
        }
    }

    fun toggleFollowArtist(artistId: String) {
        viewModelScope.launch {
            val currentFollowing = _uiState.value.featuredArtists.firstOrNull { it.id == artistId }?.isFollowing ?: false
            dao.updateArtistFollowing(artistId, !currentFollowing)
            showToast(if (!currentFollowing) "Following Artist" else "Unfollowed Artist")
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

    fun openPlaylist(playlist: PlaylistItem?) {
        val playlistToOpen = playlist ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    selectedPlaylist = playlistToOpen,
                    activePlaylistTitle = playlistToOpen.title,
                    activePlaylistDescription = playlistToOpen.subtitle,
                    activePlaylistTagline = "REON PLAYLIST • ${playlistToOpen.trackCount} • ${playlistToOpen.duration}"
                )
            }
        }
    }

    fun closePlaylist() {
        _uiState.update { state ->
            state.copy(selectedPlaylist = null)
        }
    }

    fun openAlbum(album: AlbumItem?) {
        viewModelScope.launch {
            val albumToOpen = album ?: AlbumItem("al1", "Refractions", "Aurora Glow")
            _uiState.update { state ->
                state.copy(
                    selectedAlbum = albumToOpen,
                    activeAlbumTitle = albumToOpen.title,
                    activeAlbumArtist = albumToOpen.artist,
                    activeAlbumYear = albumToOpen.year,
                    activeAlbumTrackCount = albumToOpen.trackCount
                )
            }
        }
    }

    fun closeAlbum() {
        _uiState.update { state ->
            state.copy(selectedAlbum = null)
        }
    }

    fun setDownloadFilter(filter: String) {
        _uiState.update { it.copy(selectedDownloadFilter = filter) }
    }

    fun setDownloadQuality(quality: String) {
        _uiState.update { it.copy(downloadQuality = quality, showQualitySelector = false) }
    }

    fun toggleQualitySelector(open: Boolean) {
        _uiState.update { it.copy(showQualitySelector = open) }
    }

    fun shufflePlayDownloads() {
        val tracks = _uiState.value.downloadedTracks
        if (tracks.isNotEmpty()) {
            playTrack(tracks.shuffled().first())
            showToast("Shuffling downloads")
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            val tracks = dao.getAllTracks()
            tracks.forEach {
                dao.updateTrackDownload(it.id, false)
            }
            _uiState.update { state ->
                state.copy(
                    downloadedTracks = emptyList(),
                    downloadsCount = "0 tracks",
                    storageUsedMb = 1200f,
                    storageAudioMb = 0f,
                    storageSpatialMb = 0f,
                    toastMessage = "All offline downloads cleared"
                )
            }
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
                activeArtistTracks = emptyList() // populated reactively on db callback
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

            val tracks = dao.getAllTracks()
            tracks.forEach {
                if (it.isLiked) {
                    dao.updateTrackDownload(it.id, true)
                }
            }

            _uiState.update { state ->
                state.copy(
                    isDownloadingActive = false,
                    downloadProgress = 1f,
                    activeDownloadingTrackName = "",
                    storageUsedMb = 2450f,
                    storageAudioMb = 1980f,
                    storageSpatialMb = 370f,
                    toastMessage = "Successfully downloaded tracks in 24-bit/96kHz FLAC"
                )
            }
        }
    }

    fun removeDownload(trackId: String) {
        viewModelScope.launch {
            dao.updateTrackDownload(trackId, false)
            showToast("Removed from offline storage")
        }
    }

    fun openNotifications() {
        _uiState.update { state ->
            state.copy(isNotificationsOpen = true)
        }
    }

    fun closeNotifications() {
        _uiState.update { state ->
            state.copy(isNotificationsOpen = false)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            dao.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            dao.markAllNotificationsAsRead()
            showToast("All notifications marked as read")
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            dao.clearAllNotifications()
            showToast("Notification feed cleared")
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updated = state.notifications.filter { it.id != id }
                state.copy(notifications = updated)
            }
        }
    }

    fun updateProfile(name: String, bio: String) {
        val trimmedName = name.trim().ifEmpty { "User" }
        _uiState.update { state ->
            state.copy(
                greetingName = trimmedName,
                userProfileBio = bio.trim(),
                isEditProfileDialogOpen = false,
                toastMessage = "Profile updated for $trimmedName"
            )
        }
    }

    fun openEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogOpen = true) }
    }

    fun closeEditProfileDialog() {
        _uiState.update { it.copy(isEditProfileDialogOpen = false) }
    }

    fun openAboutDialog() {
        _uiState.update { it.copy(isAboutDialogOpen = true) }
    }

    fun closeAboutDialog() {
        _uiState.update { it.copy(isAboutDialogOpen = false) }
    }

    fun openLicensesDialog() {
        _uiState.update { it.copy(isOpenSourceLicensesDialogOpen = true) }
    }

    fun closeLicensesDialog() {
        _uiState.update { it.copy(isOpenSourceLicensesDialogOpen = false) }
    }

    fun setThemeMode(mode: String) {
        _uiState.update {
            it.copy(
                themeMode = mode,
                toastMessage = "Theme set to ${mode.lowercase().replaceFirstChar { char -> char.uppercase() }}"
            )
        }
    }

    fun setAccentColor(index: Int) {
        _uiState.update {
            val colorNames = listOf("Electric Blue", "Neon Cyan", "Cyber Purple", "Emerald Green", "Sunset Crimson", "Rose Magenta")
            val name = colorNames.getOrElse(index) { "Electric Blue" }
            it.copy(
                accentColorIndex = index,
                toastMessage = "Accent color changed to $name"
            )
        }
    }

    fun setBackgroundTheme(index: Int) {
        _uiState.update {
            val bgNames = listOf("Ice Blue", "Pure White", "Soft Slate", "Obsidian Dark", "Pure AMOLED")
            val name = bgNames.getOrElse(index) { "Default" }
            it.copy(
                backgroundThemeIndex = index,
                toastMessage = "Canvas style set to $name"
            )
        }
    }

    fun setFontFamily(choice: String) {
        _uiState.update {
            val label = when (choice) {
                "INTER" -> "Inter (Clean)"
                "MONOSPACE" -> "Monospace (Technical)"
                "SERIF" -> "Serif (Editorial)"
                else -> "Plus Jakarta Sans (Modern Geometric)"
            }
            it.copy(
                fontFamilyChoice = choice,
                toastMessage = "Font family set to $label"
            )
        }
    }

    fun setFontSizeScale(scale: Float) {
        _uiState.update {
            val percentage = (scale * 100).toInt()
            it.copy(
                fontSizeScale = scale,
                toastMessage = "Text scale set to $percentage%"
            )
        }
    }

    fun clearCache() {
        _uiState.update {
            it.copy(
                storageUsedMb = 1200f,
                toastMessage = "Recovered 7.4 GB of lossless hi-res cached audio buffer"
            )
        }
    }

    fun optimizeThumbnails() {
        _uiState.update {
            it.copy(
                toastMessage = "Compressed artwork cache footprints by 65%"
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
