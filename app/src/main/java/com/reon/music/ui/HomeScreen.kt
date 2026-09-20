package com.reon.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * REON — Home Screen
 * Implements the full Electric Horizon Design System with 18 consecutive sections
 * in a high-performance single root LazyColumn, rich Search Tab, Downloads Tab,
 * floating Mini Player dock, and 3-tab floating bottom navigation.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    onSearchChange: (String) -> Unit = {},
    onFilterSelect: (String) -> Unit = {},
    onSelectRecentSearch: (String) -> Unit = {},
    onRemoveRecentSearch: (String) -> Unit = {},
    onClearRecentSearches: () -> Unit = {},
    onStartVoiceSearch: () -> Unit = {},
    onCancelVoiceSearch: () -> Unit = {},
    onClearSearch: () -> Unit = {},
    onMoodSelect: (String) -> Unit = {},
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onLike: () -> Unit = {},
    onTrackSelect: (TrackItem) -> Unit = {},
    onArtistFollowToggle: (String) -> Unit = {},
    onOpenNowPlaying: () -> Unit = {},
    onTabSelected: (HomeTab) -> Unit = {},
    onDownloadAll: () -> Unit = {},
    onRemoveDownload: (String) -> Unit = {},
    onToggleOfflineMode: () -> Unit = {},
    onToggleAutoSync: () -> Unit = {},
    onToggleCellularDownload: () -> Unit = {},
    onDownloadFilterSelect: (String) -> Unit = {},
    onDownloadQualitySelect: (String) -> Unit = {},
    onToggleQualitySelector: (Boolean) -> Unit = {},
    onShuffleDownloads: () -> Unit = {},
    onClearAllDownloads: () -> Unit = {},
    onPlaylistLikeToggle: () -> Unit = {},
    onPlaylistDownloadToggle: () -> Unit = {},
    onPlaylistShuffle: () -> Unit = {},
    onPlaylistPlayAll: () -> Unit = {},
    onOpenPlaylist: (PlaylistItem?) -> Unit = {},
    onClosePlaylist: () -> Unit = {},
    onAlbumLikeToggle: () -> Unit = {},
    onAlbumDownloadToggle: () -> Unit = {},
    onAlbumShuffle: () -> Unit = {},
    onAlbumPlayAll: () -> Unit = {},
    onOpenAlbum: (AlbumItem?) -> Unit = {},
    onCloseAlbum: () -> Unit = {},
    onOpenArtist: (ArtistItem?) -> Unit = {},
    onCloseArtist: () -> Unit = {},
    onArtistFollowProfileToggle: () -> Unit = {},
    onOpenLikedSongs: () -> Unit = {},
    onCloseLikedSongs: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onCloseHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCloseSettings: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {},
    onCloseAnalytics: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onCloseNotifications: () -> Unit = {},
    onMarkNotificationAsRead: (String) -> Unit = {},
    onMarkAllNotificationsAsRead: () -> Unit = {},
    onClearAllNotifications: () -> Unit = {},
    onDeleteNotification: (String) -> Unit = {},
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    onOpenEditProfile: () -> Unit = {},
    onCloseEditProfile: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onCloseAbout: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onCloseLicenses: () -> Unit = {},
    onSetThemeMode: (String) -> Unit = {},
    onSetAccentColor: (Int) -> Unit = {},
    onSetBackgroundTheme: (Int) -> Unit = {},
    onSetFontFamily: (String) -> Unit = {},
    onSetFontSizeScale: (Float) -> Unit = {},
    onClearCache: () -> Unit = {},
    onOptimizeThumbnails: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    onDismissToast: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReonTokens.Canvas)
            .statusBarsPadding()
            .testTag("reon_home_screen")
    ) {
        if (state.isNotificationsOpen) {
            NotificationScreen(
                state = state,
                onBackClick = onCloseNotifications,
                onTrackSelect = onTrackSelect,
                onOpenSettings = onOpenSettings,
                onOpenDownloads = { onTabSelected(HomeTab.Downloads) },
                onMarkAsRead = onMarkNotificationAsRead,
                onMarkAllAsRead = onMarkAllNotificationsAsRead,
                onClearAll = onClearAllNotifications,
                onDeleteNotification = onDeleteNotification
            )
        } else if (state.isAnalyticsOpen) {
            AnalyticsScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onBackClick = onCloseAnalytics,
                onPlayPause = onPlayPause,
                onLike = onLike,
                onOpenNowPlaying = onOpenNowPlaying,
                onShowToast = onShowToast
            )
        } else if (state.isSettingsOpen) {
            SettingsScreen(
                state = state,
                onBackClick = onCloseSettings,
                onShareClick = {
                    ShareHelper.shareApp(context)
                    onShowToast("Shared REON Audio Engine Configuration")
                },
                onUpdateProfile = onUpdateProfile,
                onOpenEditProfile = onOpenEditProfile,
                onCloseEditProfile = onCloseEditProfile,
                onOpenAbout = onOpenAbout,
                onCloseAbout = onCloseAbout,
                onOpenLicenses = onOpenLicenses,
                onCloseLicenses = onCloseLicenses,
                onSetThemeMode = onSetThemeMode,
                onSetAccentColor = onSetAccentColor,
                onSetBackgroundTheme = onSetBackgroundTheme,
                onSetFontFamily = onSetFontFamily,
                onSetFontSizeScale = onSetFontSizeScale,
                onClearCache = onClearCache,
                onOptimizeThumbnails = onOptimizeThumbnails,
                onShowToast = onShowToast
            )
        } else if (state.isHistoryOpen) {
            HistoryScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onBackClick = onCloseHistory,
                onShareClick = {
                    ShareHelper.shareApp(context)
                    onShowToast("Shared Listening History Timeline")
                },
                onClearHistoryClick = { onShowToast("Listening History Cleared") },
                onToggleLike = { id -> onLike() },
                onShowToast = onShowToast
            )
        } else if (state.isLikedSongsOpen) {
            LikedSongsScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onBackClick = onCloseLikedSongs,
                onShareClick = {
                    ShareHelper.sharePlaylist(context, "Liked Masterpieces")
                    onShowToast("Shared Liked Masterpieces Library")
                },
                onPlayAllClick = { onShowToast("Playing Liked Songs") },
                onToggleLike = { track -> onLike() },
                onShowToast = onShowToast
            )
        } else if (state.selectedArtist != null) {
            ArtistScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onAlbumSelect = onOpenAlbum,
                onBackClick = onCloseArtist,
                onShareClick = {
                    ShareHelper.shareArtist(context, state.activeArtistName)
                    onShowToast("Shared '${state.activeArtistName}' profile")
                },
                onFollowToggle = onArtistFollowProfileToggle,
                onRadioClick = { onShowToast("${state.activeArtistName} Radio Started") },
                onPlayClick = { onShowToast("Playing ${state.activeArtistName}") },
                onMoreOptionsClick = { onShowToast("Artist options") },
                onShowToast = onShowToast
            )
        } else if (state.selectedAlbum != null) {
            AlbumScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onBackClick = onCloseAlbum,
                onShareClick = {
                    ShareHelper.shareAlbum(context, state.activeAlbumTitle, state.activeArtistName)
                    onShowToast("Shared '${state.activeAlbumTitle}' album")
                },
                onLikeToggle = onAlbumLikeToggle,
                onDownloadToggle = onAlbumDownloadToggle,
                onShuffleClick = onAlbumShuffle,
                onPlayAllClick = onAlbumPlayAll,
                onMoreOptionsClick = { onShowToast("Album options") },
                onShowToast = onShowToast
            )
        } else if (state.selectedPlaylist != null) {
            PlaylistScreen(
                state = state,
                onTrackSelect = onTrackSelect,
                onBackClick = onClosePlaylist,
                onShareClick = {
                    ShareHelper.sharePlaylist(context, state.activePlaylistTitle)
                    onShowToast("Shared ${state.activePlaylistTitle} playlist")
                },
                onLikeToggle = onPlaylistLikeToggle,
                onDownloadToggle = onPlaylistDownloadToggle,
                onShuffleClick = onPlaylistShuffle,
                onPlayAllClick = onPlaylistPlayAll,
                onMoreOptionsClick = { onShowToast("Playlist options") },
                onShowToast = onShowToast
            )
        } else {
            when (state.currentTab) {
                HomeTab.Search -> {
                    SearchScreen(
                        state = state,
                        onSearchChange = onSearchChange,
                        onFilterSelect = onFilterSelect,
                        onSelectRecentSearch = onSelectRecentSearch,
                        onRemoveRecentSearch = onRemoveRecentSearch,
                        onClearRecentSearches = onClearRecentSearches,
                        onStartVoiceSearch = onStartVoiceSearch,
                        onCancelVoiceSearch = onCancelVoiceSearch,
                        onClearSearch = onClearSearch,
                        onTrackSelect = onTrackSelect,
                        onArtistFollowToggle = onArtistFollowToggle,
                        onPlaylistSelect = onOpenPlaylist,
                        onAlbumSelect = onOpenAlbum,
                        onArtistSelect = onOpenArtist,
                        onShowToast = onShowToast
                    )
                }
                HomeTab.Downloads -> {
                    DownloadsScreen(
                        state = state,
                        onTrackSelect = onTrackSelect,
                        onDownloadAll = onDownloadAll,
                        onRemoveDownload = onRemoveDownload,
                        onToggleOfflineMode = onToggleOfflineMode,
                        onToggleAutoSync = onToggleAutoSync,
                        onToggleCellular = onToggleCellularDownload,
                        onFilterSelect = onDownloadFilterSelect,
                        onQualitySelect = onDownloadQualitySelect,
                        onToggleQualitySelector = onToggleQualitySelector,
                        onShuffleAll = onShuffleDownloads,
                        onClearAll = onClearAllDownloads,
                        onOpenPlaylist = onOpenPlaylist,
                        onOpenAlbum = onOpenAlbum,
                        onOpenArtist = onOpenArtist,
                        onShowToast = onShowToast
                    )
                }
                HomeTab.Home -> {
                    // Single root LazyColumn with all 18 home sections
                    LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp)
                ) {
                    // 1. Top Bar
                    item(key = "section_top_bar") {
                        HomeTopBar(
                            unreadCount = state.notifications.count { !it.isRead },
                            onNotificationClick = onOpenNotifications,
                            onAnalyticsClick = onOpenAnalytics,
                            onSettingsClick = onOpenSettings
                        )
                    }

                    // 2. Greeting Block
                    item(key = "section_greeting") {
                        HomeGreetingBlock(
                            name = state.greetingName,
                            subtitle = state.greetingSubtitle
                        )
                    }

                    // 3. Search + Filters (Clicking switches directly to Search tab or applies filter)
                    item(key = "section_search_filters") {
                        HomeSearchAndFilters(
                            searchQuery = state.searchQuery,
                            onSearchChange = {
                                onSearchChange(it)
                                onTabSelected(HomeTab.Search)
                            },
                            selectedFilter = state.selectedFilter,
                            filters = state.filterChips,
                            onFilterSelect = { filter ->
                                onFilterSelect(filter)
                                if (filter != "All") {
                                    onTabSelected(HomeTab.Search)
                                }
                            }
                        )
                    }

                    item(key = "spacer_hero") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 4. Continue Listening (Hero Bento Card)
                    item(key = "section_continue_listening") {
                        HomeContinueListeningHero(
                            track = state.currentTrack,
                            progress = state.playbackProgress,
                            currentPos = state.currentPositionStr,
                            remainingPos = state.remainingPositionStr,
                            queueText = state.queueText,
                            playingInText = state.playingInText,
                            onCardClick = onOpenNowPlaying,
                            onPlayPause = onPlayPause,
                            onLike = onLike,
                            onSeeAll = { onOpenLikedSongs() },
                            onMoreOptions = { }
                        )
                    }

                    item(key = "spacer_quick_access") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 5. Quick Access — 2×2 Bento
                    item(key = "section_quick_access") {
                        HomeQuickAccessBento(
                            likedCount = state.likedCount,
                            downloadsCount = state.downloadsCount,
                            historyText = state.historyText,
                            flowRadioText = state.flowRadioText,
                            onItemClick = { title ->
                                when (title) {
                                    "liked_songs", "Liked Songs" -> onOpenLikedSongs()
                                    "history", "History" -> onOpenHistory()
                                    "Downloads", "downloads" -> onTabSelected(HomeTab.Downloads)
                                    "Search" -> onTabSelected(HomeTab.Search)
                                    else -> onShowToast("Opening $title")
                                }
                            }
                        )
                    }

                    item(key = "spacer_trending") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 6. Trending Now
                    item(key = "section_trending") {
                        HomeTrendingNowSection(
                            tracks = state.trendingList,
                            onTrackClick = onTrackSelect,
                            onSeeAll = {
                                onFilterSelect("All")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "spacer_top_charts") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 7. Top Charts
                    item(key = "section_top_charts") {
                        HomeTopChartsSection(
                            tracks = state.topChartsList,
                            onTrackClick = onTrackSelect,
                            onSeeAll = { onShowToast("Viewing Global Top Charts") }
                        )
                    }

                    item(key = "spacer_featured_artists") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 8. Featured Artists
                    item(key = "section_featured_artists") {
                        HomeFeaturedArtistsSection(
                            artists = state.featuredArtists,
                            onArtistClick = { artist ->
                                onOpenArtist(artist)
                            },
                            onFollowToggle = onArtistFollowToggle,
                            onDiscover = {
                                onOpenArtist(null)
                            }
                        )
                    }

                    item(key = "spacer_artist_of_week") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 9. Artist of the Week (Editorial Bento)
                    item(key = "section_artist_of_week") {
                        HomeArtistOfTheWeekEditorial(
                            headline = state.artistOfTheWeekHeadline,
                            description = state.artistOfTheWeekSubtitle,
                            tag = state.artistOfTheWeekTag,
                            onReadStory = {
                                onSearchChange("ISOxo & Knock2")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "spacer_new_releases") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 10. New Releases
                    item(key = "section_new_releases") {
                        HomeNewReleasesSection(
                            releases = state.newReleases,
                            onReleaseClick = onTrackSelect,
                            onFreshDrops = {
                                onFilterSelect("All")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "spacer_moods_genres") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 11. Moods & Genres
                    item(key = "section_moods_genres") {
                        HomeMoodsAndGenresSection(
                            moods = state.moodsGenres,
                            selectedMood = state.selectedMood,
                            onMoodSelect = onMoodSelect
                        )
                    }

                    item(key = "spacer_featured_playlists") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 12. Featured Playlists
                    item(key = "section_featured_playlists") {
                        HomeFeaturedPlaylistsSection(
                            playlists = state.featuredPlaylists,
                            onPlaylistClick = { pl -> onOpenPlaylist(pl) },
                            onExplore = {
                                onFilterSelect("Playlists")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "spacer_made_for_you") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 13. Made For You
                    item(key = "section_made_for_you") {
                        HomeMadeForYouSection(
                            mixes = state.dailyMixes,
                            onMixClick = { mix -> onOpenPlaylist(PlaylistItem(mix.id, mix.title, mix.subtitle, "28 tracks", "1 hr 30 min", 1)) }
                        )
                    }

                    item(key = "spacer_because_you_played") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 14. Because You Played
                    item(key = "section_because_you_played") {
                        HomeBecauseYouPlayedSection(
                            artistName = state.becauseYouPlayedArtist,
                            matchBadge = state.becauseYouPlayedMatch,
                            tracks = state.becauseYouPlayedList,
                            onTrackClick = onTrackSelect
                        )
                    }

                    item(key = "spacer_editors_picks") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 15. Editor's Picks
                    item(key = "section_editors_picks") {
                        HomeEditorsPicksSection(
                            headline = state.editorsPickHeadline,
                            description = state.editorsPickDescription,
                            onReadStory = {
                                onSearchChange("Tokyo")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "spacer_recently_played") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 16. Recently Played
                    item(key = "section_recently_played") {
                        HomeRecentlyPlayedSection(
                            tracks = state.recentlyPlayedList,
                            onTrackClick = onTrackSelect,
                            onHistoryClick = onOpenHistory
                        )
                    }

                    item(key = "spacer_rediscover") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 17. Rediscover
                    item(key = "section_rediscover") {
                        HomeRediscoverSection(
                            tracks = state.rediscoverList,
                            onTrackClick = onTrackSelect
                        )
                    }

                    item(key = "spacer_live_upcoming") {
                        Spacer(Modifier.height(ReonTokens.SectionSpacing))
                    }

                    // 18. Live & Upcoming
                    item(key = "section_live_upcoming") {
                        HomeLiveUpcomingSection(
                            events = state.liveEvents,
                            onEventClick = { event ->
                                onSearchChange(event.artist)
                                onTabSelected(HomeTab.Search)
                            },
                            onNearYou = {
                                onSearchChange("Live")
                                onTabSelected(HomeTab.Search)
                            }
                        )
                    }

                    item(key = "bottom_end_space") {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }

        // 19. Floating Mini Player & Upside Floating Toast notification
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toast notification pill floating just upside of mini player
            AnimatedVisibility(
                visible = state.toastMessage != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                state.toastMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(2600)
                        onDismissToast()
                    }

                    Box(
                        modifier = Modifier
                            .padding(bottom = 10.dp, start = 16.dp, end = 16.dp)
                            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF0057FF).copy(alpha = 0.25f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0B1020))
                            .border(1.dp, Color(0xFF242C44), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (state.isMiniPlayerVisible) {
                MiniPlayerDock(
                    track = state.currentTrack,
                    isPlaying = state.currentTrack.isPlaying,
                    isLiked = state.currentTrack.isLiked,
                    onExpand = onOpenNowPlaying,
                    onPlayPause = onPlayPause,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onLike = onLike,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 20. Bottom Navigation (Three tabs only: Home, Search, Downloads)
                HomeBottomNav(
                    currentTab = state.currentTab,
                    onTabSelected = onTabSelected
                )
            }
        }
    }
}
