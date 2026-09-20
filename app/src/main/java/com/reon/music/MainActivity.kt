package com.reon.music

import android.os.Bundle
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.reon.music.playback.PlaybackService
import com.reon.music.ui.HomeScreen
import com.reon.music.ui.HomeViewModel
import com.reon.music.ui.NowPlayingScreen
import com.reon.music.ui.NowPlayingViewModel
import com.reon.music.ui.ReonTokens
import com.reon.music.ui.ShareHelper
import com.reon.music.ui.theme.ReonTheme

class MainActivity : ComponentActivity() {
  private var mediaController: MediaController? = null
  private var nowPlayingViewModel: NowPlayingViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = androidx.compose.ui.platform.LocalContext.current
      val app = context.applicationContext as ReonApplication
      val homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
          initializer {
            HomeViewModel(app)
          }
        }
      )
      val nowPlayingViewModel: NowPlayingViewModel = viewModel(
        factory = viewModelFactory {
          initializer {
            NowPlayingViewModel(app)
          }
        }
      )
      this.nowPlayingViewModel = nowPlayingViewModel
      val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
      val nowPlayingState by nowPlayingViewModel.uiState.collectAsStateWithLifecycle()

      ReonTheme(
        themeMode = homeState.themeMode,
        accentColorIndex = homeState.accentColorIndex,
        backgroundThemeIndex = homeState.backgroundThemeIndex,
        fontFamilyChoice = homeState.fontFamilyChoice,
        fontScale = homeState.fontSizeScale
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = ReonTokens.Canvas
        ) {
          DisposableEffect(Unit) {
            onDispose {
              mediaController?.release()
              mediaController = null
              nowPlayingViewModel?.releaseMediaController()
            }
          }
          LaunchedEffect(Unit) {
            val token = SessionToken(this@MainActivity, ComponentName(this@MainActivity, PlaybackService::class.java))
            val controller = MediaController.Builder(this@MainActivity, token).buildAsync().get()
            mediaController = controller
            nowPlayingViewModel?.setMediaController(controller)
          }
          AnimatedContent(
            targetState = homeState.showNowPlayingScreen,
            transitionSpec = {
              if (targetState) {
                (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it / 3 } + fadeOut())
              } else {
                (slideInVertically { -it / 3 } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
              }
            },
            label = "screen_transition"
          ) { isNowPlayingOpen ->
            if (isNowPlayingOpen) {
              BackHandler {
                homeViewModel.closeNowPlaying()
              }

              NowPlayingScreen(
                state = nowPlayingState,
                onBack = { homeViewModel.closeNowPlaying() },
                onPlayPause = {
                  nowPlayingViewModel.togglePlayPause()
                  homeViewModel.togglePlayPause()
                },
                onNext = { nowPlayingViewModel.playNext() },
                onPrevious = { nowPlayingViewModel.playPrevious() },
                onSeek = { fraction -> nowPlayingViewModel.seekTo(fraction) },
                onShuffle = { nowPlayingViewModel.toggleShuffle() },
                onRepeat = { nowPlayingViewModel.toggleRepeat() },
                onLike = {
                  nowPlayingViewModel.toggleLike()
                  homeViewModel.toggleLike()
                },
                onMenuToggle = { nowPlayingViewModel.toggleMenu() },
                onMenuClose = { nowPlayingViewModel.closeMenu() },
                onDownload = { nowPlayingViewModel.downloadTrack() },
                onQueueClick = { nowPlayingViewModel.toggleQueueExpanded() },
                onPlaylistClick = { nowPlayingViewModel.togglePlaylistDialog() },
                onAddToPlaylist = { playlist -> nowPlayingViewModel.addTrackToPlaylist(playlist) },
                onPlayNextMenu = { nowPlayingViewModel.queuePlayNext() },
                onAddToQueue = { nowPlayingViewModel.addToQueue() },
                onViewAlbum = {
                  homeViewModel.closeNowPlaying()
                  homeViewModel.openAlbum(null)
                },
                onShareTrack = {
                  ShareHelper.shareTrack(
                    context = context,
                    title = nowPlayingState.title,
                    artist = nowPlayingState.artist,
                    album = nowPlayingState.album
                  )
                  homeViewModel.showToast("Sharing \"${nowPlayingState.title}\"")
                },
                onSleepTimerClick = { },
                onTrackSelect = { track -> nowPlayingViewModel.selectTrack(track) },
                onExpandPlayer = { /* Already expanded */ },
                onDismissToast = { nowPlayingViewModel.dismissToast() }
              )
            } else {
              if (homeState.isNotificationsOpen) {
                BackHandler {
                  homeViewModel.closeNotifications()
                }
              } else if (homeState.isAnalyticsOpen) {
                BackHandler {
                  homeViewModel.closeAnalytics()
                }
              } else if (homeState.isSettingsOpen) {
                BackHandler {
                  homeViewModel.closeSettings()
                }
              } else if (homeState.isHistoryOpen) {
                BackHandler {
                  homeViewModel.closeHistory()
                }
              } else if (homeState.isLikedSongsOpen) {
                BackHandler {
                  homeViewModel.closeLikedSongs()
                }
              } else if (homeState.selectedArtist != null) {
                BackHandler {
                  homeViewModel.closeArtist()
                }
              } else if (homeState.selectedAlbum != null) {
                BackHandler {
                  homeViewModel.closeAlbum()
                }
              } else if (homeState.selectedPlaylist != null) {
                BackHandler {
                  homeViewModel.closePlaylist()
                }
              }

              HomeScreen(
                state = homeState,
                onSearchChange = { homeViewModel.onSearchQueryChanged(it) },
                onFilterSelect = { homeViewModel.onFilterSelected(it) },
                onSelectRecentSearch = { homeViewModel.selectRecentSearch(it) },
                onRemoveRecentSearch = { homeViewModel.removeRecentSearch(it) },
                onClearRecentSearches = { homeViewModel.clearRecentSearches() },
                onStartVoiceSearch = { homeViewModel.startVoiceSearch() },
                onCancelVoiceSearch = { homeViewModel.cancelVoiceSearch() },
                onClearSearch = { homeViewModel.clearSearch() },
                onMoodSelect = { homeViewModel.onMoodSelected(it) },
                onPlayPause = {
                  homeViewModel.togglePlayPause()
                  nowPlayingViewModel.togglePlayPause()
                },
                onPrevious = { nowPlayingViewModel.playPrevious() },
                onNext = { nowPlayingViewModel.playNext() },
                onLike = {
                  homeViewModel.toggleLike()
                  nowPlayingViewModel.toggleLike()
                },
                onTrackSelect = { track ->
                  homeViewModel.playTrack(track)
                  nowPlayingViewModel.playTrackById(track.id)
                },
                onArtistFollowToggle = { homeViewModel.toggleFollowArtist(it) },
                onOpenNowPlaying = {
                  nowPlayingViewModel.expandPlayer()
                  homeViewModel.openNowPlaying()
                },
                onTabSelected = { homeViewModel.selectTab(it) },
                onDownloadAll = { homeViewModel.downloadAllLiked() },
                onRemoveDownload = { homeViewModel.removeDownload(it) },
                onToggleOfflineMode = { homeViewModel.toggleOfflineMode() },
                onToggleAutoSync = { homeViewModel.toggleAutoSync() },
                onToggleCellularDownload = { homeViewModel.toggleCellularDownload() },
                onDownloadFilterSelect = { homeViewModel.setDownloadFilter(it) },
                onDownloadQualitySelect = { homeViewModel.setDownloadQuality(it) },
                onToggleQualitySelector = { homeViewModel.toggleQualitySelector(it) },
                onShuffleDownloads = { homeViewModel.shufflePlayDownloads() },
                onClearAllDownloads = { homeViewModel.clearAllDownloads() },
                onPlaylistLikeToggle = { homeViewModel.togglePlaylistLiked() },
                onPlaylistDownloadToggle = { homeViewModel.togglePlaylistDownloaded() },
                onPlaylistShuffle = { homeViewModel.shufflePlaylist() },
                onPlaylistPlayAll = { homeViewModel.playPlaylist() },
                onOpenPlaylist = { homeViewModel.openPlaylist(it) },
                onClosePlaylist = { homeViewModel.closePlaylist() },
                onAlbumLikeToggle = { homeViewModel.toggleAlbumLiked() },
                onAlbumDownloadToggle = { homeViewModel.toggleAlbumDownloaded() },
                onAlbumShuffle = { homeViewModel.shufflePlaylist() },
                onAlbumPlayAll = { homeViewModel.playPlaylist() },
                onOpenAlbum = { homeViewModel.openAlbum(it) },
                onCloseAlbum = { homeViewModel.closeAlbum() },
                onOpenArtist = { homeViewModel.openArtist(it) },
                onCloseArtist = { homeViewModel.closeArtist() },
                onArtistFollowProfileToggle = { homeViewModel.toggleFollowArtistFromProfile() },
                onOpenLikedSongs = { homeViewModel.openLikedSongs() },
                onCloseLikedSongs = { homeViewModel.closeLikedSongs() },
                onOpenHistory = { homeViewModel.openHistory() },
                onCloseHistory = { homeViewModel.closeHistory() },
                onOpenSettings = { homeViewModel.openSettings() },
                onCloseSettings = { homeViewModel.closeSettings() },
                onOpenAnalytics = { homeViewModel.openAnalytics() },
                onCloseAnalytics = { homeViewModel.closeAnalytics() },
                onOpenNotifications = { homeViewModel.openNotifications() },
                onCloseNotifications = { homeViewModel.closeNotifications() },
                onMarkNotificationAsRead = { homeViewModel.markNotificationAsRead(it) },
                onMarkAllNotificationsAsRead = { homeViewModel.markAllNotificationsAsRead() },
                onClearAllNotifications = { homeViewModel.clearAllNotifications() },
                onDeleteNotification = { homeViewModel.deleteNotification(it) },
                onUpdateProfile = { name, bio -> homeViewModel.updateProfile(name, bio) },
                onOpenEditProfile = { homeViewModel.openEditProfileDialog() },
                onCloseEditProfile = { homeViewModel.closeEditProfileDialog() },
                onOpenAbout = { homeViewModel.openAboutDialog() },
                onCloseAbout = { homeViewModel.closeAboutDialog() },
                onOpenLicenses = { homeViewModel.openLicensesDialog() },
                onCloseLicenses = { homeViewModel.closeLicensesDialog() },
                onSetThemeMode = { homeViewModel.setThemeMode(it) },
                onSetAccentColor = { homeViewModel.setAccentColor(it) },
                onSetBackgroundTheme = { homeViewModel.setBackgroundTheme(it) },
                onSetFontFamily = { homeViewModel.setFontFamily(it) },
                onSetFontSizeScale = { homeViewModel.setFontSizeScale(it) },
                onClearCache = { homeViewModel.clearCache() },
                onOptimizeThumbnails = { homeViewModel.optimizeThumbnails() },
                onShowToast = { homeViewModel.showToast(it) },
                onDismissToast = { homeViewModel.dismissToast() }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}


