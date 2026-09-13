/*
 * REON Music App - Player Controller
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.reon.music.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Player State
 */
data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val radioModeEnabled: Boolean = false
)

/**
 * Player Controller
 * Manages media playback using Media3
 */
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var mediaController: MediaController? = null
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private var currentQueue: MutableList<Song> = mutableListOf()
    private var radioModeCallback: ((Song) -> Unit)? = null
    private var isRadioModeEnabled = false
    
    init {
        connectToService()
    }
    
    private fun connectToService() {
        scope.launch {
            try {
                val sessionToken = SessionToken(
                    context,
                    ComponentName(context, MusicService::class.java)
                )
                
                mediaController = MediaController.Builder(context, sessionToken)
                    .buildAsync()
                    .await()
                
                mediaController?.addListener(playerListener)
                _isConnected.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _isConnected.value = false
            }
        }
    }
    
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState()
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            scope.launch {
                val index = mediaController?.currentMediaItemIndex ?: -1
                val currentSong = currentQueue.getOrNull(index)
                _playerState.value = _playerState.value.copy(
                    currentSong = currentSong,
                    currentIndex = index
                )
                
                // Radio mode: when near end of queue, add more songs
                if (isRadioModeEnabled && currentSong != null) {
                    val remainingSongs = currentQueue.size - index
                    if (remainingSongs < 5) {
                        radioModeCallback?.invoke(currentSong)
                    }
                }
            }
            updateState()
        }
        
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateState()
        }
        
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playerState.value = _playerState.value.copy(shuffleEnabled = shuffleModeEnabled)
        }
        
        override fun onRepeatModeChanged(repeatMode: Int) {
            _playerState.value = _playerState.value.copy(repeatMode = repeatMode)
        }
    }
    
    private fun updateState() {
        val controller = mediaController ?: return
        _playerState.value = _playerState.value.copy(
            isPlaying = controller.isPlaying,
            position = controller.currentPosition,
            duration = controller.duration.coerceAtLeast(0)
        )
    }
    
    /**
     * Play a single song
     */
    fun playSong(song: Song, streamUrl: String) {
        scope.launch {
            val controller = mediaController ?: return@launch
            
            currentQueue.clear()
            currentQueue.add(song)
            
            val mediaItem = buildMediaItem(song, streamUrl)
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
            
            _playerState.value = _playerState.value.copy(
                currentSong = song,
                queue = currentQueue.toList(),
                currentIndex = 0
            )
        }
    }
    
    /**
     * Play a list of songs starting at index
     */
    fun playQueue(songs: List<Song>, startIndex: Int = 0, streamUrls: Map<String, String>) {
        scope.launch {
            val controller = mediaController ?: return@launch
            
            currentQueue.clear()
            currentQueue.addAll(songs)
            
            val mediaItems = songs.map { song ->
                buildMediaItem(song, streamUrls[song.id] ?: song.streamUrl ?: "")
            }
            
            controller.setMediaItems(mediaItems, startIndex, 0L)
            controller.prepare()
            controller.play()
            
            _playerState.value = _playerState.value.copy(
                currentSong = songs.getOrNull(startIndex),
                queue = currentQueue.toList(),
                currentIndex = startIndex
            )
        }
    }
    
    /**
     * Add song to queue
     */
    fun addToQueue(song: Song, streamUrl: String) {
        scope.launch {
            val controller = mediaController ?: return@launch
            
            currentQueue.add(song)
            controller.addMediaItem(buildMediaItem(song, streamUrl))
            
            _playerState.value = _playerState.value.copy(
                queue = currentQueue.toList()
            )
        }
    }
    
    /**
     * Play/Pause toggle
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    /**
     * Play
     */
    fun play() {
        mediaController?.play()
    }
    
    /**
     * Pause
     */
    fun pause() {
        mediaController?.pause()
    }
    
    /**
     * Skip to next
     */
    fun skipToNext() {
        mediaController?.seekToNext()
    }
    
    /**
     * Skip to previous
     */
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    /**
     * Toggle shuffle
     */
    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }
    
    /**
     * Toggle repeat mode
     */
    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }
    
    /**
     * Skip to specific index in queue
     */
    fun skipToIndex(index: Int) {
        mediaController?.seekTo(index, 0L)
    }
    
    /**
     * Get current position (for UI updates)
     */
    fun getCurrentPosition(): Long = mediaController?.currentPosition ?: 0L
    
    /**
     * Get current duration
     */
    fun getDuration(): Long = mediaController?.duration?.coerceAtLeast(0) ?: 0L
    
    /**
     * Enable radio mode - automatically adds more songs when queue is running low
     */
    fun enableRadioMode(callback: (Song) -> Unit) {
        isRadioModeEnabled = true
        radioModeCallback = callback
        _playerState.value = _playerState.value.copy(radioModeEnabled = true)
    }
    
    /**
     * Disable radio mode
     */
    fun disableRadioMode() {
        isRadioModeEnabled = false
        radioModeCallback = null
        _playerState.value = _playerState.value.copy(radioModeEnabled = false)
    }
    
    /**
     * Play from queue at specific index
     */
    fun playFromQueue(index: Int) {
        skipToIndex(index)
    }
    
    /**
     * Remove song from queue at specific index
     */
    fun removeFromQueue(index: Int) {
        scope.launch {
            val controller = mediaController ?: return@launch
            if (index >= 0 && index < currentQueue.size) {
                val currentIndex = controller.currentMediaItemIndex
                currentQueue.removeAt(index)
                controller.removeMediaItem(index)
                
                // Update queue in state
                val newIndex = if (index <= currentIndex && currentIndex > 0) currentIndex - 1 else currentIndex
                _playerState.value = _playerState.value.copy(
                    queue = currentQueue.toList(),
                    currentIndex = newIndex
                )
            }
        }
    }
    
    /**
     * Swap a freshly resolved stream URL into a queued item.
     * No-op if the queue moved on and the index now holds a different
     * song, so stale resolutions can never corrupt playback.
     */
    fun replaceStreamUrl(index: Int, song: Song, streamUrl: String) {
        scope.launch {
            val controller = mediaController ?: return@launch
            if (index < 0 || index >= currentQueue.size) return@launch
            if (controller.mediaItemCount <= index) return@launch
            if (currentQueue.getOrNull(index)?.id != song.id) return@launch
            controller.replaceMediaItem(index, buildMediaItem(song, streamUrl))
        }
    }

    private fun buildMediaItem(song: Song, streamUrl: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.artworkUrl?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()
    }
    
    /**
     * Set playback speed (0.5x to 2.0x)
     */
    fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed.coerceIn(0.5f, 2.0f))
    }
    
    fun release() {
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
    }
}
