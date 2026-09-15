package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NowPlayingViewModel : ViewModel() {

    private val allTracks = MusicTrack.sampleTracks
    private var currentTrackIndex = 0

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<NowPlayingState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        startPlaybackTimer()
    }

    private fun createInitialState(): NowPlayingState {
        val current = allTracks[currentTrackIndex]
        val next = allTracks[(currentTrackIndex + 1) % allTracks.size]
        val related = allTracks.filter { it.id != current.id }

        return NowPlayingState(
            currentTrack = current,
            nextTrack = next,
            isPlaying = true,
            progress = 84000f / current.durationMs,
            positionMs = 84000L,
            shuffleOn = false,
            repeatOn = false,
            isLiked = false,
            selectedEqPreset = "Binaural Spatial",
            sleepTimer = "45m",
            relatedTracks = related,
            queueTracks = allTracks
        )
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                if (_uiState.value.isPlaying) {
                    _uiState.update { current ->
                        val duration = current.currentTrack.durationMs
                        val newPos = (current.positionMs + 1000L).coerceAtMost(duration)
                        if (newPos >= duration) {
                            if (current.repeatOn) {
                                current.copy(positionMs = 0L, progress = 0f)
                            } else {
                                // Auto next
                                val nextIndex = (currentTrackIndex + 1) % allTracks.size
                                currentTrackIndex = nextIndex
                                val newTrack = allTracks[nextIndex]
                                val nextTrack = allTracks[(nextIndex + 1) % allTracks.size]
                                val related = allTracks.filter { it.id != newTrack.id }
                                current.copy(
                                    currentTrack = newTrack,
                                    nextTrack = nextTrack,
                                    positionMs = 0L,
                                    progress = 0f,
                                    isLiked = newTrack.isLiked,
                                    relatedTracks = related
                                )
                            }
                        } else {
                            current.copy(
                                positionMs = newPos,
                                progress = (newPos.toFloat() / duration).coerceIn(0f, 1f)
                            )
                        }
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { current ->
            val playing = !current.isPlaying
            current.copy(
                isPlaying = playing,
                toastMessage = if (playing) "Playing · ${current.title}" else "Playback paused"
            )
        }
    }

    fun playNext() {
        currentTrackIndex = (currentTrackIndex + 1) % allTracks.size
        loadTrackAtIndex(currentTrackIndex)
    }

    fun playPrevious() {
        if (_uiState.value.positionMs > 3000L) {
            // Restart current track if played past 3 seconds
            _uiState.update {
                it.copy(
                    positionMs = 0L,
                    progress = 0f,
                    toastMessage = "Restarted '${it.title}'"
                )
            }
        } else {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else allTracks.size - 1
            loadTrackAtIndex(currentTrackIndex)
        }
    }

    fun seekTo(fraction: Float) {
        _uiState.update { current ->
            val duration = current.currentTrack.durationMs
            val clampedFraction = fraction.coerceIn(0f, 1f)
            val newPos = (duration * clampedFraction).toLong()
            current.copy(
                progress = clampedFraction,
                positionMs = newPos
            )
        }
    }

    fun toggleShuffle() {
        _uiState.update {
            val nextShuffle = !it.shuffleOn
            it.copy(
                shuffleOn = nextShuffle,
                toastMessage = if (nextShuffle) "Shuffle enabled" else "Shuffle disabled"
            )
        }
    }

    fun toggleRepeat() {
        _uiState.update {
            val nextRepeat = !it.repeatOn
            it.copy(
                repeatOn = nextRepeat,
                toastMessage = if (nextRepeat) "Repeat track enabled" else "Repeat off"
            )
        }
    }

    fun toggleLike() {
        _uiState.update {
            val nextLiked = !it.isLiked
            it.copy(
                isLiked = nextLiked,
                toastMessage = if (nextLiked) "Added to Favorites ❤️" else "Removed from Favorites"
            )
        }
    }

    fun cycleSleepTimer() {
        _uiState.update { current ->
            val nextTimer = when (current.sleepTimer) {
                "45m" -> "60m"
                "60m" -> "30m"
                "30m" -> "15m"
                "15m" -> "Off"
                else -> "45m"
            }
            current.copy(
                sleepTimer = nextTimer,
                toastMessage = if (nextTimer == "Off") "Sleep timer turned off" else "Sleep timer set to $nextTimer"
            )
        }
    }

    fun cycleSpatialMode() {
        _uiState.update { current ->
            val nextMode = when (current.selectedEqPreset) {
                "Binaural Spatial" -> "Stereo Master"
                "Stereo Master" -> "Dolby Atmos"
                else -> "Binaural Spatial"
            }
            current.copy(selectedEqPreset = nextMode)
        }
    }

    fun setEqPreset(preset: String) {
        _uiState.update { it.copy(selectedEqPreset = preset) }
    }

    fun selectTrack(track: MusicTrack) {
        val index = allTracks.indexOfFirst { it.id == track.id }
        if (index != -1) {
            currentTrackIndex = index
            loadTrackAtIndex(index)
        }
    }

    fun toggleLyricsExpanded() {
        _uiState.update { it.copy(isLyricsExpanded = !it.isLyricsExpanded) }
    }

    fun toggleQueueExpanded() {
        _uiState.update { it.copy(isQueueExpanded = !it.isQueueExpanded) }
    }

    fun toggleMenu() {
        _uiState.update { it.copy(isMenuOpen = !it.isMenuOpen) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(isMenuOpen = false) }
    }

    fun togglePlaylistDialog() {
        _uiState.update { it.copy(isPlaylistDialogOpen = !it.isPlaylistDialogOpen, isMenuOpen = false) }
    }

    fun addTrackToPlaylist(playlistName: String) {
        _uiState.update {
            it.copy(
                isPlaylistDialogOpen = false,
                toastMessage = "Added '${it.title}' to $playlistName"
            )
        }
    }

    fun toggleMinimized() {
        _uiState.update {
            val minimized = !it.isMinimized
            it.copy(
                isMinimized = minimized,
                toastMessage = if (minimized) "Player minimized · Tap bottom bar to expand" else "Player expanded"
            )
        }
    }

    fun expandPlayer() {
        _uiState.update { it.copy(isMinimized = false) }
    }

    fun downloadTrack() {
        _uiState.update {
            val wasDownloaded = it.isDownloaded
            it.copy(
                isDownloaded = !wasDownloaded,
                isMenuOpen = false,
                toastMessage = if (!wasDownloaded) "Downloaded '${it.title}' in Lossless FLAC (96kHz)" else "Removed offline download"
            )
        }
    }

    fun addToQueue() {
        _uiState.update {
            it.copy(
                isMenuOpen = false,
                toastMessage = "Added '${it.title}' to Playback Queue"
            )
        }
    }

    fun queuePlayNext() {
        _uiState.update {
            it.copy(
                isMenuOpen = false,
                toastMessage = "'${it.title}' queued to play next"
            )
        }
    }

    fun shareTrack() {
        _uiState.update {
            it.copy(
                isMenuOpen = false,
                toastMessage = "Link copied for '${it.title}' by ${it.artist}"
            )
        }
    }

    fun viewAlbum() {
        _uiState.update {
            it.copy(
                isMenuOpen = false,
                toastMessage = "Album '${it.album}' by ${it.artist}"
            )
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun loadTrackAtIndex(index: Int) {
        val track = allTracks[index]
        val next = allTracks[(index + 1) % allTracks.size]
        val related = allTracks.filter { it.id != track.id }

        _uiState.update {
            it.copy(
                currentTrack = track,
                nextTrack = next,
                positionMs = 0L,
                progress = 0f,
                isPlaying = true,
                isLiked = track.isLiked,
                relatedTracks = related,
                toastMessage = "Now playing '${track.title}'"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
