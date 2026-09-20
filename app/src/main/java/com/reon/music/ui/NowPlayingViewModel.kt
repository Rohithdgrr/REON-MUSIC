package com.reon.music.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.reon.music.ReonApplication
import com.reon.music.data.MusicRepository
import com.reon.music.data.MusicTrack
import com.reon.music.data.TrackEntity
import com.reon.music.data.remote.ReonBackendApi
import com.reon.music.playback.ACTION_RESTORE
import com.reon.music.playback.PlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NowPlayingViewModel(application: Application) : AndroidViewModel(application) {

    private val db: com.reon.music.data.ReonDatabase = (application as com.reon.music.ReonApplication).database
    private val dao = db.reonDao()
    private val repository = MusicRepository(
        api = ReonBackendApi.create(),
        dao = dao
    )

    private var allTracks: List<com.reon.music.data.MusicTrack> = com.reon.music.data.MusicTrack.sampleTracks
    private var currentTrackIndex = 0

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<NowPlayingState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var positionPollJob: Job? = null
    private var pendingPlayIndex: Int? = null

    init {
        loadTracksFromDb()
        observeDb()
    }

    fun setMediaController(controller: MediaController) {
        mediaController = controller
        controller.addListener(mediaListener)
        startPositionPoll()
        updateFromController()
        // If user tapped track before controller was ready, play it now
        pendingPlayIndex?.let { idx ->
            pendingPlayIndex = null
            if (idx in allTracks.indices) {
                viewModelScope.launch { resolveAndPlay(allTracks[idx]) }
            }
        }
    }

    fun releaseMediaController() {
        positionPollJob?.cancel()
        positionPollJob = null
        mediaController?.removeListener(mediaListener)
        mediaController = null
    }

    private val mediaListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val playing = playbackState == Player.STATE_READY && mediaController?.isPlaying == true
                    || playbackState == Player.STATE_BUFFERING && mediaController?.isPlaying == true
            _uiState.update { it.copy(isPlaying = playing) }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            updatePosition()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            val msg = error.message ?: "unknown"
            // Auto retry on stream expiry (403/410) by re-resolving
            if (error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ||
                msg.contains("403", true) || msg.contains("410", true)) {
                _uiState.update { it.copy(toastMessage = "Stream expired, re-resolving...") }
                viewModelScope.launch {
                    val current = _uiState.value.currentTrack
                    resolveAndPlay(current)
                }
            } else {
                _uiState.update { it.copy(toastMessage = "Playback error, retrying... ($msg)") }
            }
        }
    }

    private fun startPositionPoll() {
        positionPollJob?.cancel()
        positionPollJob = viewModelScope.launch {
            while (true) {
                delay(500L)
                updatePosition()
            }
        }
    }

    private fun updatePosition() {
        val ctrl = mediaController ?: return
        val duration = ctrl.duration
        val currentPos = ctrl.currentPosition
        val progress = if (duration > 0) (currentPos.toFloat() / duration) else 0f
        _uiState.update {
            it.copy(
                positionMs = currentPos,
                progress = progress.coerceIn(0f, 1f),
                isPlaying = ctrl.playbackState == Player.STATE_READY && ctrl.isPlaying
            )
        }
    }

    private fun updateFromController() {
        val ctrl = mediaController ?: return
        updatePosition()
    }

    private fun loadTracksFromDb() {
        viewModelScope.launch {
            dao.getAllTracksFlow().collect { dbTracks ->
                if (dbTracks.isNotEmpty()) {
                    allTracks = dbTracks.map { it.toMusicTrack() }
                    val currentTrackId = _uiState.value.currentTrack.id
                    val matchedIndex = allTracks.indexOfFirst { it.id == currentTrackId }
                    if (matchedIndex != -1) {
                        currentTrackIndex = matchedIndex
                    }
                    updateTracksState()
                }
            }
        }
    }

    private fun observeDb() {
        viewModelScope.launch {
            dao.getAllTracksFlow().collect { dbTracks ->
                if (dbTracks.isNotEmpty()) {
                    allTracks = dbTracks.map { it.toMusicTrack() }
                }
            }
        }
    }

    private fun TrackEntity.toMusicTrack(): com.reon.music.data.MusicTrack = com.reon.music.data.MusicTrack(
        id = id,
        title = title,
        artist = artist,
        album = album,
        category = category,
        durationMs = durationMs,
        albumArtUrl = albumArtUrl,
        artistImageUrl = artistImageUrl,
        source = source,
        quality = quality,
        spatialMode = spatialMode,
        codec = codec,
        sampleRate = sampleRate,
        monthlyListeners = monthlyListeners,
        lyricsQuote = lyricsQuote,
        isLiked = isLiked,
        videoId = videoId,
        sourceKind = sourceKind,
        streamUrl = streamUrl,
        streamExpiresAt = streamExpiresAt,
    )

    private fun updateTracksState() {
        if (allTracks.isEmpty()) return
        val current = allTracks.getOrNull(currentTrackIndex) ?: allTracks[0]
        val next = allTracks.getOrNull((currentTrackIndex + 1) % allTracks.size) ?: allTracks[0]
        val related = allTracks.filter { it.id != current.id }

        _uiState.update { state ->
            state.copy(
                currentTrack = current,
                nextTrack = next,
                isLiked = current.isLiked,
                relatedTracks = related,
                queueTracks = allTracks,
            )
        }
    }

    private fun createInitialState(): NowPlayingState {
        val current = allTracks[currentTrackIndex]
        val next = allTracks[(currentTrackIndex + 1) % allTracks.size]
        val related = allTracks.filter { it.id != current.id }

        return NowPlayingState(
            currentTrack = current,
            nextTrack = next,
            isPlaying = false,
            progress = 0f,
            positionMs = 0L,
            shuffleOn = false,
            repeatOn = false,
            isLiked = current.isLiked,
            relatedTracks = related,
            queueTracks = allTracks,
            isLoading = false,
            streamUrl = null,
        )
    }

    fun togglePlayPause() {
        val ctrl = mediaController ?: return
        if (ctrl.isPlaying) {
            ctrl.pause()
        } else {
            ctrl.play()
        }
        updatePosition()
    }

    fun playNext() {
        if (mediaController != null) {
            mediaController?.seekToNext()
        } else {
            currentTrackIndex = (currentTrackIndex + 1) % allTracks.size
            loadTrackAtIndex(currentTrackIndex)
        }
    }

    fun playPrevious() {
        val ctrl = mediaController
        if (ctrl != null && ctrl.currentPosition > 3000L) {
            ctrl.seekTo(0)
        } else {
            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else allTracks.size - 1
            loadTrackAtIndex(currentTrackIndex)
        }
    }

    fun seekTo(fraction: Float) {
        val ctrl = mediaController ?: return
        val duration = ctrl.duration
        val clampedFraction = fraction.coerceIn(0f, 1f)
        ctrl.seekTo((duration * clampedFraction).toLong())
        updatePosition()
    }

    fun toggleShuffle() {
        _uiState.update {
            val nextShuffle = !it.shuffleOn
            it.copy(shuffleOn = nextShuffle)
        }
    }

    fun toggleRepeat() {
        _uiState.update {
            val nextRepeat = !it.repeatOn
            it.copy(repeatOn = nextRepeat)
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val trackId = _uiState.value.currentTrack.id
            val isCurrentlyLiked = _uiState.value.isLiked
            val targetLiked = !isCurrentlyLiked
            dao.updateTrackLike(trackId, targetLiked)
            _uiState.update {
                it.copy(
                    isLiked = targetLiked,
                    toastMessage = if (targetLiked) "Added to Favorites ❤️" else "Removed from Favorites"
                )
            }
        }
    }

    fun selectTrack(track: com.reon.music.data.MusicTrack) {
        val index = allTracks.indexOfFirst { it.id == track.id }
        if (index != -1) {
            currentTrackIndex = index
            loadTrackAtIndex(index)
        }
    }

    fun playTrackById(trackId: String) {
        val index = allTracks.indexOfFirst { it.id == trackId }
        if (index != -1) {
            currentTrackIndex = index
            loadTrackAtIndex(index)
        } else {
            // Track not in current list — look up from DB and play
            viewModelScope.launch {
                val entity = dao.getTrackById(trackId)
                    ?: dao.getTrackByVideoId(trackId)
                    ?: return@launch
                val musicTrack = entity.toMusicTrack()
                allTracks = allTracks + musicTrack
                currentTrackIndex = allTracks.size - 1
                loadTrackAtIndex(currentTrackIndex)
            }
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
            it.copy(isMinimized = minimized)
        }
    }

    fun expandPlayer() {
        _uiState.update { it.copy(isMinimized = false) }
    }

    fun downloadTrack() {
        viewModelScope.launch {
            val trackId = _uiState.value.currentTrack.id
            val isCurrentlyDownloaded = _uiState.value.isDownloaded
            val targetDownloaded = !isCurrentlyDownloaded
            dao.updateTrackDownload(trackId, targetDownloaded)
            _uiState.update {
                it.copy(
                    isDownloaded = targetDownloaded,
                    toastMessage = if (targetDownloaded) "Downloaded '${it.title}'" else "Removed offline download"
                )
            }
        }
    }

    fun addToQueue() {
        _uiState.update {
            it.copy(isMenuOpen = false, toastMessage = "Added '${it.title}' to Playback Queue")
        }
    }

    fun queuePlayNext() {
        _uiState.update {
            it.copy(isMenuOpen = false, toastMessage = "'${it.title}' queued to play next")
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun loadTrackAtIndex(index: Int) {
        if (allTracks.isEmpty()) return
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

        // If track is local hires mock with no videoId, don't attempt network stream
        val streamId = track.videoId.takeIf { it.isNotBlank() } ?: track.id
        val isLocalMock = track.sourceKind == com.reon.music.data.SourceKind.LOCAL_HIRES && track.videoId.isBlank() && !track.id.matches(Regex("[A-Za-z0-9_-]{11}"))
        if (isLocalMock) {
            _uiState.update { it.copy(toastMessage = "Local preview – search for '${track.title}' to stream", isPlaying = false) }
            return
        }

        // Trigger actual audio playback via MediaController
        if (mediaController != null) {
            viewModelScope.launch {
                resolveAndPlay(track)
            }
        } else {
            // Queue until controller binds (MainActivity LaunchedEffect)
            pendingPlayIndex = index
            _uiState.update { it.copy(toastMessage = "Queuing '${track.title}'...") }
        }
    }

    /**
     * Resolve a stream URL for the given track via the backend,
     * then set it as the MediaItem on ExoPlayer and start playback.
     * Retries with quality fallback already handled in repository.stream.
     */
    private suspend fun resolveAndPlay(track: MusicTrack) {
        val ctrl = mediaController ?: run {
            pendingPlayIndex = allTracks.indexOfFirst { it.id == track.id }.takeIf { it >= 0 }
            return
        }
        // Don't stream LOCAL_HIRES mock without real videoId
        if (track.sourceKind == com.reon.music.data.SourceKind.LOCAL_HIRES && track.videoId.isBlank() && !track.id.matches(Regex("[A-Za-z0-9_-]{11}"))) {
            _uiState.update { it.copy(toastMessage = "Local preview – search to stream", isPlaying = false) }
            return
        }
        try {
            val streamId = track.videoId.takeIf { it.isNotBlank() } ?: track.id

            _uiState.update { it.copy(isLoading = true) }
            val streamResult = repository.stream(streamId, "high")
            val url = streamResult.url
            if (url.isNullOrBlank()) {
                _uiState.update { it.copy(toastMessage = "Stream URL unavailable for '${track.title}'", isLoading = false, isPlaying = false) }
                return
            }

            // Persist the resolved URL so it can be reused without re-resolving
            try { dao.updateStream(track.id, url, streamResult.expiresAt) } catch (_: Exception) {}
            // also store under videoId if different
            if (track.videoId.isNotBlank() && track.videoId != track.id) {
                try { dao.updateStream(track.videoId, url, streamResult.expiresAt) } catch (_: Exception) {}
            }

            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .build()
                )
                .build()

            withContext(Dispatchers.Main) {
                ctrl.setMediaItem(mediaItem)
                ctrl.prepare()
                ctrl.playWhenReady = true
            }
            _uiState.update { it.copy(isLoading = false, toastMessage = "Streaming '${track.title}' • ${streamResult.codec} • ${streamResult.bitrate/1000}kbps") }

            // Ensure the foreground service + notification is active
            val intent = Intent(getApplication(), PlaybackService::class.java).apply {
                action = ACTION_RESTORE
            }
            getApplication<Application>().startForegroundService(intent)
        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val msg = when (code) {
                502 -> "Stream ciphered/unavailable (try another track)"
                429 -> "Rate limited, try again"
                404 -> "Track not found"
                else -> "Server error $code"
            }
            _uiState.update { it.copy(toastMessage = "Playback failed: $msg", isLoading = false, isPlaying = false) }
        } catch (e: java.io.IOException) {
            _uiState.update { it.copy(toastMessage = "Offline – no cached stream for '${track.title}'", isLoading = false, isPlaying = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(toastMessage = "Playback error: ${e.message ?: "unknown"}", isLoading = false, isPlaying = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        positionPollJob?.cancel()
    }
}
