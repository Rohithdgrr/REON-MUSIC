/*
 * REON Music App - Android Auto Service
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.services

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android Auto / Media Browser Service
 * Provides media browsing and playback for Android Auto and other clients
 */
@AndroidEntryPoint
class ReonMediaBrowserService : MediaBrowserServiceCompat() {
    
    companion object {
        private const val MEDIA_ROOT_ID = "reon_root"
        private const val EMPTY_ROOT_ID = "empty_root"
        
        // Browsable categories
        private const val CATEGORY_LIKED = "liked_songs"
        private const val CATEGORY_RECENT = "recent"
        private const val CATEGORY_PLAYLISTS = "playlists"
        private const val CATEGORY_DOWNLOADED = "downloaded"
        private const val CATEGORY_SEARCH = "search"
    }
    
    private lateinit var mediaSession: MediaSessionCompat
    
    override fun onCreate() {
        super.onCreate()
        
        // Create media session
        mediaSession = MediaSessionCompat(this, "ReonMusicService").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
            )
            
            setCallback(MediaSessionCallback())
            
            // Set initial playback state
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
                    )
                    .build()
            )
        }
        
        sessionToken = mediaSession.sessionToken
    }
    
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // Check if the client is allowed to browse
        // In production, verify package signature for auto clients
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // Detach result to load asynchronously
        result.detach()
        
        when (parentId) {
            MEDIA_ROOT_ID -> {
                // Return root categories
                result.sendResult(getRootCategories())
            }
            CATEGORY_LIKED -> {
                // Return liked songs
                loadLikedSongs(result)
            }
            CATEGORY_RECENT -> {
                // Return recently played
                loadRecentSongs(result)
            }
            CATEGORY_PLAYLISTS -> {
                // Return playlists
                loadPlaylists(result)
            }
            CATEGORY_DOWNLOADED -> {
                // Return downloaded songs
                loadDownloadedSongs(result)
            }
            else -> {
                // Try to load playlist content or return empty
                if (parentId.startsWith("playlist_")) {
                    loadPlaylistSongs(parentId, result)
                } else {
                    result.sendResult(mutableListOf())
                }
            }
        }
    }
    
    private fun getRootCategories(): MutableList<MediaBrowserCompat.MediaItem> {
        return mutableListOf(
            createBrowsableItem(
                CATEGORY_LIKED,
                "Liked Songs",
                "Your favorite tracks"
            ),
            createBrowsableItem(
                CATEGORY_RECENT,
                "Recently Played",
                "Songs you've listened to recently"
            ),
            createBrowsableItem(
                CATEGORY_PLAYLISTS,
                "Playlists",
                "Your playlists"
            ),
            createBrowsableItem(
                CATEGORY_DOWNLOADED,
                "Downloads",
                "Offline songs"
            )
        )
    }
    
    private fun createBrowsableItem(
        id: String,
        title: String,
        subtitle: String
    ): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        
        return MediaBrowserCompat.MediaItem(
            description,
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        )
    }
    
    private fun createPlayableItem(
        id: String,
        title: String,
        artist: String,
        artworkUrl: String? = null
    ): MediaBrowserCompat.MediaItem {
        val builder = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist)
        
        // Icon URI would be set here in production
        
        return MediaBrowserCompat.MediaItem(
            builder.build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
    
    private fun loadLikedSongs(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // In production, load from database
        result.sendResult(mutableListOf())
    }
    
    private fun loadRecentSongs(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // In production, load from database
        result.sendResult(mutableListOf())
    }
    
    private fun loadPlaylists(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // In production, load from database
        result.sendResult(mutableListOf())
    }
    
    private fun loadDownloadedSongs(result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        // In production, load from database
        result.sendResult(mutableListOf())
    }
    
    private fun loadPlaylistSongs(
        playlistId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // In production, load playlist songs from database
        result.sendResult(mutableListOf())
    }
    
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        // In production, search through songs/playlists
        result.sendResult(mutableListOf())
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }
    
    /**
     * Media Session Callback
     * Handles playback control commands
     */
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        
        override fun onPlay() {
            // Handle play
        }
        
        override fun onPause() {
            // Handle pause
        }
        
        override fun onStop() {
            // Handle stop
        }
        
        override fun onSkipToNext() {
            // Handle skip next
        }
        
        override fun onSkipToPrevious() {
            // Handle skip prev
        }
        
        override fun onSeekTo(pos: Long) {
            // Handle seek
        }
        
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            // Handle play from media ID
        }
        
        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            // Handle voice search
        }
        
        override fun onCustomAction(action: String?, extras: Bundle?) {
            // Handle custom actions (like, shuffle, repeat)
        }
    }
}
