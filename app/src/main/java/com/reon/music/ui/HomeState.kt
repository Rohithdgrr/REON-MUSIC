package com.reon.music.ui

import androidx.compose.runtime.Immutable

/**
 * REON Home Screen Data Models and State Definition.
 */

@Immutable
data class TrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val duration: String = "03:45",
    val rank: String = "",
    val badge: String = "",
    val plays: String = "",
    val isPlaying: Boolean = false,
    val isLiked: Boolean = false,
    val artSeed: Int = 1
)

@Immutable
data class ArtistItem(
    val id: String,
    val name: String,
    val genre: String,
    val isFollowing: Boolean = false,
    val artSeed: Int = 1
)

@Immutable
data class PlaylistItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val trackCount: String,
    val duration: String,
    val artSeed: Int = 1
)

@Immutable
data class DailyMixItem(
    val id: String,
    val mixNumber: String,
    val title: String,
    val subtitle: String,
    val trackCount: String,
    val isPrimaryGradient: Boolean = true
)

@Immutable
data class LiveEventItem(
    val id: String,
    val artist: String,
    val venue: String,
    val city: String,
    val dateMonth: String,
    val dateDay: String,
    val statusBadge: String = "UPCOMING",
    val artSeed: Int = 1
)

@Immutable
data class MoodGenreItem(
    val id: String,
    val emoji: String,
    val label: String,
    val backgroundColorHex: Long,
    val textColorHex: Long,
    val isSelected: Boolean = false
)

@Immutable
data class AlbumItem(
    val id: String,
    val title: String,
    val artist: String,
    val year: String = "2024",
    val trackCount: String = "12 tracks",
    val genre: String = "Electronic",
    val artSeed: Int = 1
)

@Immutable
data class CategoryBrowseItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val gradientColors: List<Long>,
    val queryTarget: String
)

@Immutable
data class ReonNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: String = "RELEASE", // "RELEASE", "AUDIO ENGINE", "DOWNLOADS", "LIVE", "SYSTEM"
    val isRead: Boolean = false,
    val badge: String = "NEW",
    val actionText: String = "Play Now",
    val trackId: String? = null,
    val albumId: String? = null,
    val iconType: String = "music"
)

@Immutable
data class TopMatchResult(
    val type: String, // "ARTIST", "SONG", "ALBUM"
    val id: String,
    val title: String,
    val subtitle: String,
    val extraInfo: String,
    val badge: String = "TOP MATCH",
    val isLiked: Boolean = false,
    val isFollowing: Boolean = false,
    val artSeed: Int = 1
)

enum class HomeTab {
    Home, Search, Downloads
}

@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val greetingName: String = "Rohith",
    val greetingSubtitle: String = "Your daily mix is ready. Let's explore.",
    val searchQuery: String = "",
    val selectedFilter: String = "All",
    val filterChips: List<String> = listOf("All", "Songs", "Artists", "Albums", "Playlists"),

    // Playlist Detail View State
    val selectedPlaylist: PlaylistItem? = null,
    val activePlaylistTitle: String = "Deep Focus // Electric Light",
    val activePlaylistDescription: String = "Subtle ambient textures, precision glitch rhythms, and generative modular synthesis tuned for deep flow states.",
    val activePlaylistTagline: String = "REON ORIGINAL • 2 hr 14 min",
    val activePlaylistTracks: List<TrackItem> = emptyList(),
    val isPlaylistLiked: Boolean = true,
    val isPlaylistDownloaded: Boolean = true,
    val playlistSortOption: String = "Default",

    // Album Detail View State
    val selectedAlbum: AlbumItem? = null,
    val activeAlbumTitle: String = "Refractions",
    val activeAlbumArtist: String = "Aurora Glow",
    val activeAlbumYear: String = "2024",
    val activeAlbumTrackCount: String = "9 songs, 42 min",
    val activeAlbumTracks: List<TrackItem> = emptyList(),
    val isAlbumLiked: Boolean = false,
    val isAlbumDownloaded: Boolean = true,

    // Artist Detail View State
    val selectedArtist: ArtistItem? = null,
    val activeArtistName: String = "Aurora Glow",
    val activeArtistListeners: String = "1,482,904",
    val activeArtistBio: String = "Formed in Berlin and refined through Tokyo's avant-garde analog clubs, Aurora Glow pair vintage Buchla and Eurorack modular synthesizers with pristine 96kHz acoustic spatialization.",
    val activeArtistTracks: List<TrackItem> = emptyList(),
    val isArtistFollowed: Boolean = false,

    // Liked Songs Detail View State
    val isLikedSongsOpen: Boolean = false,

    // Listening History Timeline State
    val isHistoryOpen: Boolean = false,

    // Audio Settings View State
    val isSettingsOpen: Boolean = false,

    // Analytics Dashboard View State
    val isAnalyticsOpen: Boolean = false,

    // Notifications Screen View State
    val isNotificationsOpen: Boolean = false,
    val notifications: List<ReonNotificationItem> = emptyList(),

    // Search Specific State
    val recentSearches: List<String> = listOf("Aurora Glow", "Tokyo Underground", "Refractions", "Synthwave 84", "96kHz Lossless"),
    val trendingSearches: List<String> = listOf("ISOxo & Knock2", "Midnight Prism", "Deep Focus", "Cyber Soul", "Spatial Atmos", "Liquid DnB"),
    val browseCategories: List<CategoryBrowseItem> = emptyList(),
    val isVoiceSearching: Boolean = false,
    val voiceTranscript: String = "",
    val searchResultsTracks: List<TrackItem> = emptyList(),
    val searchResultsArtists: List<ArtistItem> = emptyList(),
    val searchResultsAlbums: List<AlbumItem> = emptyList(),
    val searchResultsPlaylists: List<PlaylistItem> = emptyList(),
    val searchResultsMoods: List<MoodGenreItem> = emptyList(),
    val topMatch: TopMatchResult? = null,

    // Downloads Tab State
    val downloadedTracks: List<TrackItem> = emptyList(),
    val storageUsedMb: Float = 1840f,
    val storageTotalMb: Float = 64000f,
    val storageAudioMb: Float = 1420f,
    val storageSpatialMb: Float = 320f,
    val storageCacheMb: Float = 100f,
    val isOfflineModeOnly: Boolean = false,
    val isSmartAutoSyncEnabled: Boolean = true,
    val isCellularDownloadAllowed: Boolean = false,
    val downloadQuality: String = "24-bit / 96kHz Lossless FLAC",
    val selectedDownloadFilter: String = "All",
    val downloadFilterChips: List<String> = listOf("All", "Hi-Res FLAC", "Liked Songs", "Albums", "Playlists"),
    val isDownloadingActive: Boolean = false,
    val downloadProgress: Float = 0f,
    val activeDownloadingTrackName: String = "",
    val showQualitySelector: Boolean = false,

    // Customization & Appearance State
    val themeMode: String = "LIGHT", // "SYSTEM", "LIGHT", "DARK", "OLED"
    val accentColorIndex: Int = 0, // 0: Electric Blue, 1: Neon Cyan, 2: Cyber Purple, 3: Emerald Green, 4: Sunset Crimson, 5: Rose Magenta
    val backgroundThemeIndex: Int = 0, // 0: Modern Ice Blue, 1: Pure White, 2: Soft Slate, 3: Obsidian Dark, 4: Pure AMOLED
    val fontFamilyChoice: String = "SANS_SERIF", // "SANS_SERIF", "INTER", "MONOSPACE", "SERIF"
    val fontSizeScale: Float = 1.0f, // 0.9f, 1.0f, 1.1f, 1.2f

    // User Profile
    val userProfileBio: String = "Audiophile & 192kHz Hi-Res Enthusiast",
    val userProfileEmail: String = "rayan91greate@gmail.com",
    val isEditProfileDialogOpen: Boolean = false,
    val isAboutDialogOpen: Boolean = false,
    val isOpenSourceLicensesDialogOpen: Boolean = false,

    // Section 4: Continue Listening Hero
    val currentTrack: TrackItem = TrackItem(
        id = "track_refractions",
        title = "Refractions",
        artist = "Aurora Glow",
        album = "Refraction EP",
        duration = "03:47",
        isPlaying = true,
        isLiked = true,
        badge = "DEEP FOCUS"
    ),
    val playbackProgress: Float = 0.37f,
    val currentPositionStr: String = "01:24",
    val remainingPositionStr: String = "-02:23",
    val queueText: String = "Queue 3/4",
    val playingInText: String = "Playing in ~ 14",

    // Section 5: Quick Access (2x2)
    val likedCount: String = "248 tracks",
    val downloadsCount: String = "08 tracks",
    val historyText: String = "Recent activity",
    val flowRadioText: String = "Your station",

    // Section 6: Trending Now
    val trendingList: List<TrackItem> = emptyList(),

    // Section 7: Top Charts
    val topChartsList: List<TrackItem> = emptyList(),

    // Section 8: Featured Artists
    val featuredArtists: List<ArtistItem> = emptyList(),

    // Section 9: Artist of the Week
    val artistOfTheWeekHeadline: String = "ISOxo & Knock2: Reimagining Trap & Future Bass",
    val artistOfTheWeekSubtitle: String = "Inside the sonic laboratory behind the world tour, the DIY club sets, and their explosive collaborative sets.",
    val artistOfTheWeekTag: String = "ARTIST OF THE WEEK",

    // Section 10: New Releases
    val newReleases: List<TrackItem> = emptyList(),

    // Section 11: Moods & Genres
    val moodsGenres: List<MoodGenreItem> = emptyList(),
    val selectedMood: String = "Focus",

    // Section 12: Featured Playlists
    val featuredPlaylists: List<PlaylistItem> = emptyList(),

    // Section 13: Made For You
    val dailyMixes: List<DailyMixItem> = emptyList(),

    // Section 14: Because You Played
    val becauseYouPlayedArtist: String = "Aurora Glow",
    val becauseYouPlayedMatch: String = "69% MATCH",
    val becauseYouPlayedList: List<TrackItem> = emptyList(),

    // Section 15: Editor's Picks
    val editorsPickHeadline: String = "The breakthrough sound of Tokyo underground",
    val editorsPickDescription: String = "Exploring futuristic Shibuya ambient beat scenes with modular synthesizers and hyper-dense rhythms.",
    val editorsPickTag: String = "DEEP FOCUS",

    // Section 16: Recently Played
    val recentlyPlayedList: List<TrackItem> = emptyList(),

    // Section 17: Rediscover
    val rediscoverList: List<TrackItem> = emptyList(),

    // Section 18: Live & Upcoming
    val liveEvents: List<LiveEventItem> = emptyList(),

    // Navigation & Mini Player
    val currentTab: HomeTab = HomeTab.Home,
    val isMiniPlayerVisible: Boolean = true,
    val showNowPlayingScreen: Boolean = false,
    val toastMessage: String? = null
)
