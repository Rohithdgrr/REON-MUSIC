/*
 * REON Music App - Player ViewModel
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reon.music.core.model.Song
import com.reon.music.data.network.StreamResolver
import com.reon.music.data.repository.MusicRepository
import com.reon.music.playback.PlayerController
import com.reon.music.playback.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.reon.music.services.DownloadProgress
import com.reon.music.services.SuggestionResult

data class PlayerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showPlayer: Boolean = false,
    val isLiked: Boolean = false,
    val userPlaylists: List<com.reon.music.data.database.entities.PlaylistEntity> = emptyList()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val streamResolver: StreamResolver,
    private val repository: MusicRepository,
    private val songDao: com.reon.music.data.database.dao.SongDao,
    private val playlistDao: com.reon.music.data.database.dao.PlaylistDao,
    private val downloadManager: com.reon.music.services.DownloadManager,
    private val discordRichPresence: com.reon.music.services.DiscordRichPresence,
    private val aiSuggestions: com.reon.music.services.AISongSuggestions,
    private val smartOfflineCache: com.reon.music.services.SmartOfflineCache,
    private val sleepTimerManager: com.reon.music.services.SleepTimerManager,
    private val userPreferences: com.reon.music.core.preferences.UserPreferences
) : ViewModel() {
    
    companion object {
        private const val TAG = "PlayerViewModel"
    }

    // Regex for removing bracketed parts and non-alphanumeric characters
    private val BRACKETS_REGEX = "\\[.*?\\]|\\(.*?\\)".toRegex()
    private val NON_ALNUM_REGEX = "[^a-z0-9\\s]".toRegex()
    private val MULTI_WHITESPACE_REGEX = "\\s+".toRegex()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    val playerState: StateFlow<PlayerState> = playerController.playerState
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
private val _downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, DownloadProgress>> = _downloadProgress.asStateFlow()

    private val _downloadedSongs = MutableStateFlow<List<Song>>(emptyList())
    val downloadedSongs: StateFlow<List<Song>> = _downloadedSongs.asStateFlow()
    
    private var positionUpdateJob: Job? = null
    
init {
        startPositionUpdates()
        observeCurrentSong()
        loadPlaylists()
        observeDiscordPresence()
        observeDownloadedSongs()
    }

    private fun observeDownloadedSongs() {
        viewModelScope.launch {
            songDao.getDownloadedSongs().collect { entities ->
                _downloadedSongs.value = entities.map { entity -> entity.toSong() }
            }
        }
    }

    // ===== QUEUE DEDUP HELPERS =====

    private fun normalizeTitle(raw: String): String {
        if (raw.isBlank()) return ""
        val lower = raw.lowercase(Locale.US)
        val withoutBrackets = BRACKETS_REGEX.replace(lower, " ")
        val cleaned = NON_ALNUM_REGEX.replace(withoutBrackets, " ")
        return MULTI_WHITESPACE_REGEX.replace(cleaned, " ").trim()
    }

    private fun canonicalKey(song: Song): String {
        val t = normalizeTitle(song.title)
        val a = normalizeTitle(song.artist)
        return "$t::$a"
    }

    private fun isBetterSource(candidate: Song, current: Song?): Boolean {
        if (current == null) return true
        fun score(s: Song): Int {
            var score = 0
            when (s.source.lowercase(Locale.US)) {
                "local" -> score += 3
                "youtube" -> score += 2
                "jiosaavn" -> score += 1
            }
            if (s.is320kbps) score += 3
            if (s.quality.contains("4k", ignoreCase = true)) score += 2
            if (s.quality.contains("hd", ignoreCase = true)) score += 1
            score += (s.viewCount / 1_000_000).toInt().coerceAtMost(3)
            return score
        }
        return score(candidate) > score(current)
    }

    private fun mergeDuplicateSongs(songs: List<Song>): List<Song> {
        val bestByKey = linkedMapOf<String, Song>()
        songs.forEach { song ->
            val key = canonicalKey(song)
            val existing = bestByKey[key]
            if (isBetterSource(song, existing)) {
                bestByKey[key] = song
            }
        }
        return bestByKey.values.toList()
    }

    private fun filterForQueueWindow(
        existingQueue: List<Song>,
        candidates: List<Song>,
        windowSize: Int = 50
    ): List<Song> {
        if (candidates.isEmpty()) return emptyList()
        val window = existingQueue.takeLast(windowSize)
        val existingTitles = window
            .map { normalizeTitle(it.title) }
            .filter { it.isNotBlank() }
            .toMutableSet()
        val existingIds = existingQueue.map { it.id }.toMutableSet()
        val result = mutableListOf<Song>()
        candidates.forEach { song ->
            val normTitle = normalizeTitle(song.title)
            if (existingIds.contains(song.id)) return@forEach
            if (normTitle.isNotBlank() && existingTitles.contains(normTitle)) return@forEach
            result += song
            existingIds += song.id
            if (normTitle.isNotBlank()) existingTitles += normTitle
        }
        return result
    }
    
    /**
     * Update Discord Rich Presence when song changes
     */
    private fun observeDiscordPresence() {
        viewModelScope.launch {
            playerController.playerState.collect { state ->
                state.currentSong?.let { song ->
                    discordRichPresence.updatePresence(
                        songTitle = song.title,
                        artist = song.artist,
                        albumArtUrl = song.getHighQualityArtwork(),
                        isPlaying = state.isPlaying,
                        position = _currentPosition.value,
                        duration = (song.duration * 1000).toLong()
                    )
                } ?: discordRichPresence.clearPresence()
            }
        }
    }
    
    /**
     * Get AI song suggestions for current song.
     * Errors are surfaced as [SuggestionResult.Error] so the UI can
     * show feedback instead of silently receiving an empty list.
     */
    fun getAISuggestions(): Flow<SuggestionResult> = flow {
        val currentSong = playerState.value.currentSong
        if (currentSong != null) {
            emit(aiSuggestions.getSuggestions(currentSong))
        } else {
            emit(SuggestionResult.Success(emptyList()))
        }
    }
    
    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (isActive) {
                val position = playerController.getCurrentPosition()
                _currentPosition.value = position
                
                // Track playback for smart offline caching
                playerState.value.currentSong?.let { song ->
                    val duration = song.duration * 1000L
                    if (duration > 0) {
                        smartOfflineCache.trackPlayback(song, position, duration)
                    }
                }
                
                delay(500) // Update every 500ms
            }
        }
    }
    
    private fun observeCurrentSong() {
        viewModelScope.launch {
            var previousSong: Song? = null
            
            playerController.playerState.collect { state ->
                state.currentSong?.let { song ->
                    checkIfLiked(song.id)
                    
                    // Update duration in DB if it was 0 but now we have it
                    if (song.duration == 0 && state.duration > 0) {
                        val actualDuration = (state.duration / 1000).toInt()
                        updateSongDuration(song.id, actualDuration)
                    }

                    // If song changed, mark previous song as completed
                    if (previousSong != null && previousSong?.id != song.id) {
                        previousSong?.let { completedSong ->
                            smartOfflineCache.markSongCompleted(completedSong)
                        }
                    }
                    
                    previousSong = song
                }
            }
        }
    }

    /**
     * Update song duration in database
     */
    private fun updateSongDuration(songId: String, duration: Int) {
        viewModelScope.launch {
            try {
                val song = songDao.getSongById(songId)
                if (song != null && song.duration == 0) {
                    songDao.update(song.copy(duration = duration))
                    Log.d(TAG, "Updated duration for $songId to $duration seconds")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update duration", e)
            }
        }
    }
    
    /**
     * Handle song completion (called when song ends)
     */
    fun onSongCompleted() {
        viewModelScope.launch {
            playerState.value.currentSong?.let { song ->
                smartOfflineCache.markSongCompleted(song)
            }
        }
    }
    
    private fun loadPlaylists() {
        viewModelScope.launch {
            playlistDao.getAllPlaylists().collect { playlists ->
                _uiState.value = _uiState.value.copy(userPlaylists = playlists)
            }
        }
    }
    
    private suspend fun checkIfLiked(songId: String) {
        val isLiked = songDao.isLiked(songId)
        _uiState.value = _uiState.value.copy(isLiked = isLiked)
    }

    fun toggleLike() {
        val song = playerState.value.currentSong ?: return
        viewModelScope.launch {
            val current = songDao.getSongById(song.id)
            val newLikedState = !(current?.isLiked ?: false)
            
            if (current == null) {
                songDao.insert(com.reon.music.data.database.entities.SongEntity.fromSong(song, isLiked = true))
            } else {
                songDao.setLiked(song.id, newLikedState)
            }
            // Update state immediately
            _uiState.value = _uiState.value.copy(isLiked = newLikedState)
        }
    }
    
    fun addToPlaylist(playlistId: Long) {
        val song = playerState.value.currentSong ?: return
        viewModelScope.launch {
            if (songDao.getSongById(song.id) == null) {
                songDao.insert(com.reon.music.data.database.entities.SongEntity.fromSong(song))
            }
            
            val maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
            playlistDao.addSongToPlaylist(
                com.reon.music.data.database.entities.PlaylistSongCrossRef(
                    playlistId = playlistId,
                    songId = song.id,
                    position = maxPos + 1
                )
            )
            playlistDao.updatePlaylistStats(playlistId)
        }
    }
    
    /**
     * Play a song - resolves stream URL and plays with retry logic
     * Supports offline mode by checking for downloaded songs first, but allows streaming as fallback
     */
    fun playSong(song: Song, retryCount: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                Log.d(TAG, "Playing song: ${song.title} (${song.source}) - attempt ${retryCount + 1}")
                
                // First, try to play downloaded song if available
                val songEntity = songDao.getSongById(song.id)
                val downloadState = com.reon.music.data.database.entities.DownloadState
                
                if (songEntity != null) {
                    Log.d(TAG, "Song found in DB - downloadState: ${songEntity.downloadState}, localPath: ${songEntity.localPath}")
                    
                    val localPath = songEntity.localPath
                    if (songEntity.downloadState == downloadState.DOWNLOADED && 
                        localPath != null && 
                        java.io.File(localPath).exists()) {
                        
                        Log.d(TAG, "Playing downloaded song from local path: $localPath")
                        Log.d(TAG, "File exists: ${java.io.File(localPath).exists()}")
                        Log.d(TAG, "File size: ${java.io.File(localPath).length()} bytes")
                        
                        playerController.playSong(song, localPath)
                        _uiState.value = _uiState.value.copy(isLoading = false, showPlayer = true)
                        Log.d(TAG, "Offline playback started successfully")
                        return@launch
                    }
                }
                
                // If not downloaded, try to stream online
                Log.d(TAG, "Song not downloaded or file missing. Attempting to resolve stream URL...")
                Log.d(TAG, "Existing streamUrl: ${song.streamUrl?.take(100) ?: "null"}")
                
                val streamUrl = streamResolver.resolveStreamUrl(song)
                
                if (streamUrl != null && streamUrl.isNotBlank()) {
                    Log.d(TAG, "Resolved stream URL: ${streamUrl.take(100)}...")
                    Log.d(TAG, "Starting playback...")
                    playerController.playSong(song, streamUrl)
                    _uiState.value = _uiState.value.copy(isLoading = false, showPlayer = true)
                    Log.d(TAG, "Playback started successfully")
                } else {
                    Log.e(TAG, "Could not resolve stream URL")
                    
                    // Retry up to 2 times with exponential backoff
                    if (retryCount < 2) {
                        Log.d(TAG, "Retrying (attempt ${retryCount + 2})...")
                        delay((retryCount + 1) * 1000L) // Wait 1s, then 2s for retries
                        playSong(song, retryCount + 1)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Could not play: ${song.title}. Please check your internet connection and try again."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing song", e)
                
                // Retry on exception with exponential backoff
                if (retryCount < 2) {
                    Log.d(TAG, "Retrying after error (attempt ${retryCount + 2})...")
                    delay((retryCount + 1) * 1000L)
                    playSong(song, retryCount + 1)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error playing: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }
    
    /**
     * Play a list of songs
     */
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        viewModelScope.launch {
            if (songs.isEmpty()) return@launch
            // Merge duplicates across sources before starting playback
            val mergedSongs = mergeDuplicateSongs(songs)
            if (mergedSongs.isEmpty()) return@launch
            val safeStartIndex = startIndex.coerceIn(0, mergedSongs.lastIndex)
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // First, resolve and play the current song
                val currentSong = mergedSongs[safeStartIndex]
                val streamUrl = streamResolver.resolveStreamUrl(currentSong)
                
                if (streamUrl != null) {
                    val streamUrls = mutableMapOf(currentSong.id to streamUrl)
                    playerController.playQueue(mergedSongs, safeStartIndex, streamUrls)
                    _uiState.value = _uiState.value.copy(isLoading = false, showPlayer = true)
                    
                    // Pre-resolve next songs in background and swap the URLs
                    // into the queued items so advancing never hits an
                    // empty-URI placeholder.
                    mergedSongs.forEachIndexed { index, song ->
                        if (index != safeStartIndex && index < safeStartIndex + 5) {
                            launch {
                                streamResolver.resolveStreamUrl(song)?.let { url ->
                                    playerController.replaceStreamUrl(index, song, url)
                                }
                            }
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Could not play: ${currentSong.title}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }
    
    fun togglePlayPause() {
        playerController.togglePlayPause()
    }
    
    fun play() {
        playerController.play()
    }
    
    fun pause() {
        playerController.pause()
    }
    
    fun skipToNext() {
        viewModelScope.launch {
            val state = playerState.value
            val nextIndex = state.currentIndex + 1
            val nextSong = state.queue.getOrNull(nextIndex)
            
            if (nextSong != null) {
                // Resolve the URL first and swap it into the queued item;
                // controller commands dispatch in order, so the queued
                // replace lands before the seek. Always advance afterwards
                // instead of stranding playback on an unresolvable URL.
                streamResolver.resolveStreamUrl(nextSong)?.let { url ->
                    playerController.replaceStreamUrl(nextIndex, nextSong, url)
                }
                playerController.skipToNext()
            } else {
                playerController.skipToNext()
            }
        }
    }
    
    fun skipToPrevious() {
        playerController.skipToPrevious()
    }
    
    fun seekTo(position: Long) {
        playerController.seekTo(position)
        _currentPosition.value = position
    }
    
    fun seekToPercent(percent: Float) {
        val duration = playerState.value.duration
        if (duration > 0) {
            val position = (duration * percent).toLong()
            seekTo(position)
        }
    }
    
    fun toggleShuffle() {
        playerController.toggleShuffle()
    }
    
    fun toggleRepeat() {
        playerController.toggleRepeatMode()
    }
    
    fun showPlayer() {
        _uiState.value = _uiState.value.copy(showPlayer = true)
    }
    
    fun hidePlayer() {
        _uiState.value = _uiState.value.copy(showPlayer = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get progress as percentage (0-1)
     */
    fun getProgress(): Float {
        val position = currentPosition.value
        val duration = playerState.value.duration
        return if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    }
    
    override fun onCleared() {
        super.onCleared()
        positionUpdateJob?.cancel()
    }
    
    fun downloadSong() {
        val song = playerState.value.currentSong ?: return
        downloadSong(song)
    }
    
    fun downloadSongs(songs: List<Song>) {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            try {
                val resolvedUrls = mutableMapOf<String, String?>()
                songs.forEach { song ->
                    resolvedUrls[song.id] = streamResolver.resolveStreamUrl(song)
                }
                
                downloadManager.downloadSongs(songs, resolvedUrls)
                
                // Ensure each song is in DB and mark state as DOWNLOADING so it only appears in Downloads after completion
                songs.forEach { song ->
                    val existing = songDao.getSongById(song.id)
                    if (existing == null) {
                        songDao.insert(com.reon.music.data.database.entities.SongEntity.fromSong(song))
                    }
                    songDao.updateDownloadState(
                        songId = song.id,
                        state = com.reon.music.data.database.entities.DownloadState.DOWNLOADING,
                        path = null
                    )
                }
                
                // Track progress for UI animation
                songs.forEach { trackDownloadProgress(it.id) }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting bulk download", e)
            }
        }
    }
    
    fun downloadSong(song: Song) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting download for: ${song.title}")
                
                val streamUrl = streamResolver.resolveStreamUrl(song)
                if (streamUrl.isNullOrBlank()) {
                    Log.e(TAG, "No stream URL available for download: ${song.title}")
                    _uiState.value = _uiState.value.copy(
                        error = "Cannot download: No stream URL found"
                    )
                    return@launch
                }
                
                Log.d(TAG, "Resolved stream URL for download: ${streamUrl.take(100)}...")
                
                // Ensure song is in database with download pending state
                val existingSong = songDao.getSongById(song.id)
                if (existingSong == null) {
                    Log.d(TAG, "Creating new song entry in database")
                    val entity = com.reon.music.data.database.entities.SongEntity.fromSong(song)
                    songDao.insert(entity)
                } else {
                    Log.d(TAG, "Song already in database, updating download state")
                }
                
                // Update to downloading state
                songDao.updateDownloadState(
                    songId = song.id,
                    state = com.reon.music.data.database.entities.DownloadState.DOWNLOADING,
                    path = null
                )
                
                Log.d(TAG, "Initiating download via DownloadManager")
                
                // Initiate download via DownloadManager
                downloadManager.downloadSong(song, streamUrl)
                
                Log.d(TAG, "Download queued, tracking progress...")
                
                // Track progress for UI updates
                trackDownloadProgress(song.id)
                
                Log.d(TAG, "Download initiated for: ${song.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting download", e)
                _uiState.value = _uiState.value.copy(
                    error = "Download failed: ${e.message ?: "Unknown error"}"
                )
                
                // Mark as failed in database
                try {
                    songDao.updateDownloadState(
                        songId = song.id,
                        state = com.reon.music.data.database.entities.DownloadState.FAILED,
                        path = null
                    )
                } catch (dbError: Exception) {
                    Log.e(TAG, "Failed to update failed state", dbError)
                }
            }
        }
    }
    
    /**
     * Play song from queue at specific index
     */
    fun playFromQueue(index: Int) {
        viewModelScope.launch {
            val song = playerState.value.queue.getOrNull(index)
            if (song != null) {
                streamResolver.resolveStreamUrl(song)?.let { url ->
                    playerController.replaceStreamUrl(index, song, url)
                }
            }
            playerController.playFromQueue(index)
        }
    }
    
    /**
     * Add song to queue - either at end or as next song
     * Note: playNext currently adds to queue end (player controller feature pending)
     */
    fun addToQueue(song: Song, playNext: Boolean = false) {
        viewModelScope.launch {
            try {
                val queue = playerState.value.queue
                // Prevent duplicates by ID and by recent title window
                val normTitle = normalizeTitle(song.title)
                val recentWindow = queue.takeLast(50)
                val titleClash = recentWindow.any { normalizeTitle(it.title) == normTitle && normTitle.isNotBlank() }
                if (queue.any { it.id == song.id } || titleClash) {
                    Log.d(TAG, "Song already in queue: ${song.title}")
                    return@launch
                }
                val streamUrl = streamResolver.resolveStreamUrl(song)
                if (streamUrl != null) {
                    playerController.addToQueue(song, streamUrl)
                    Log.d(TAG, if (playNext) "Added as next: ${song.title}" else "Added to queue: ${song.title}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding to queue", e)
            }
        }
    }
    
    /**
     * Remove song from queue at specific index
     */
    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }
    
    /**
     * Set playback speed (0.5x to 2.0x)
     */
    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }
    
    /**
     * Set sleep timer in minutes. Delegates to [SleepTimerManager] so
     * repeated calls replace (never stack) the timer and it can be
     * cancelled. Volume fade is unsupported through MediaController,
     * so completion simply pauses playback.
     */
    fun setSleepTimer(minutes: Int) {
        sleepTimerManager.setCallbacks(
            onComplete = { playerController.pause() },
            onVolume = { /* no-op: controller has no per-stream volume */ }
        )
        sleepTimerManager.start(minutes * 60 * 1000L)
    }

    /**
     * Cancel the active sleep timer, if any.
     */
    fun cancelSleepTimer() {
        sleepTimerManager.cancel()
    }

    val sleepTimerState = sleepTimerManager.state
    
    /**
     * Create a new playlist
     */
    fun createPlaylist(name: String, description: String? = null) {
        viewModelScope.launch {
            val playlist = com.reon.music.data.database.entities.PlaylistEntity(
                title = name,
                description = description
            )
            playlistDao.insert(playlist)
            loadPlaylists() // Reload playlists
        }
    }
    
    /**
     * Cycle through repeat modes: OFF -> ALL -> ONE -> OFF
     */
    fun cycleRepeatMode() {
        playerController.toggleRepeatMode()
    }
    
    /**
     * Enable radio mode - plays endless songs automatically with smart auto-queue
     * Mixes both YouTube and JioSaavn sources for maximum variety
     */
    fun enableRadioMode(seedSongs: List<Song>) {
        viewModelScope.launch {
            val currentState = playerState.value
            val currentSong = currentState.currentSong
            
            // IMPROVEMENT: If a song is already playing, STAY on it. 
            // Just add related songs to the queue instead of starting over.
            if (currentSong == null && seedSongs.isNotEmpty()) {
                playQueue(seedSongs.shuffled())
            } else if (currentSong != null) {
                // We are already playing a song, so just trigger the related songs logic immediately
                // to populate the queue for endless playback from the CURRENT song.
                try {
                    val relatedSongs = repository.getRelatedSongs(currentSong, 40).getOrNull() ?: emptyList()
                    val combined = relatedSongs.filter { it.id != currentSong.id }
                    val merged = mergeDuplicateSongs(combined)
                    val filtered = filterForQueueWindow(playerState.value.queue, merged, windowSize = 50)
                    filtered.shuffled().take(20).forEach { addToQueue(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Set up automatic queue extension with smart queue management
            playerController.enableRadioMode { song ->
                val songToUse = song ?: playerState.value.currentSong
                if (songToUse != null) {
                    viewModelScope.launch {
                        try {
                            val relatedSongs = repository.getRelatedSongs(songToUse, 40).getOrNull() ?: emptyList()
                            val combined = relatedSongs.filter { it.id != songToUse.id }
                            val merged = mergeDuplicateSongs(combined)
                            val filtered = filterForQueueWindow(playerState.value.queue, merged, windowSize = 50)
                            filtered.shuffled().take(20).forEach { addToQueue(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    /**
     * Add similar songs to queue based on current song
     */
    fun addSimilarSongs() {
        val currentSong = playerState.value.currentSong ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch related artists' songs, album songs, movie songs, genre songs
                val relatedSongs = repository.getRelatedSongs(currentSong, 50).getOrNull() ?: emptyList()
                
                val merged = mergeDuplicateSongs(relatedSongs)
                val filtered = filterForQueueWindow(playerState.value.queue, merged, windowSize = 50)
                
                filtered.forEach { addToQueue(it) }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to fetch similar songs")
            }
        }
    }
    
    /**
     * Disable radio mode
     */
    fun disableRadioMode() {
        playerController.disableRadioMode()
    }
    
    /**
     * Track download progress for a song and surface to UI
     */
    private fun trackDownloadProgress(songId: String) {
        viewModelScope.launch {
            downloadManager.getDownloadProgress(songId).collect { progress ->
                _downloadProgress.value = _downloadProgress.value + (songId to progress)
                
                Log.d(TAG, "Download progress for $songId: ${progress.progress}% - ${progress.status}")
                
                // Clean up completed/failed to avoid endless spinners
                when (progress.status) {
                    com.reon.music.services.DownloadStatus.COMPLETED -> {
                        Log.d(TAG, "Download completed for song $songId")
                        Log.d(TAG, "File path: ${progress.filePath}")
                        
                        // Verify file exists
                        if (progress.filePath != null && java.io.File(progress.filePath).exists()) {
                            Log.d(TAG, "File verified to exist: ${progress.filePath}")
                            Log.d(TAG, "File size: ${java.io.File(progress.filePath).length()} bytes")
                        } else {
                            Log.w(TAG, "Downloaded file does not exist at: ${progress.filePath}")
                        }
                        
                        // Clean up from tracking after a delay to allow UI to show 100%
                        delay(2000)
                        _downloadProgress.value = _downloadProgress.value - songId
                    }
                    
                    com.reon.music.services.DownloadStatus.FAILED -> {
                        Log.e(TAG, "Download failed for song $songId")
                        
                        // Mark as failed in database
                        try {
                            songDao.updateDownloadState(
                                songId = songId,
                                state = com.reon.music.data.database.entities.DownloadState.FAILED,
                                path = null
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to update failed state", e)
                        }
                        
                        _downloadProgress.value = _downloadProgress.value - songId
                    }
                    
                    com.reon.music.services.DownloadStatus.CANCELLED -> {
                        Log.d(TAG, "Download cancelled for song $songId")
                        _downloadProgress.value = _downloadProgress.value - songId
                    }
                    
                    else -> {
                        // Keep tracking for other states
                    }
                }
            }
        }
    }
}
