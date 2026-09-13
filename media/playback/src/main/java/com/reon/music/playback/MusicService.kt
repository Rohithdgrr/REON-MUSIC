/*
 * REON Music App - Music Service
 * Copyright (c) 2024 REON
 * 
 * CLEAN-ROOM IMPLEMENTATION
 * This service is independently written using official Media3 APIs.
 * No GPL-licensed code has been copied.
 */

package com.reon.music.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Music Playback Service
 * Clean-room implementation using Media3 APIs
 */
@AndroidEntryPoint
class MusicService : MediaLibraryService() {
    
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaLibrarySession
    private var mediaCache: SimpleCache? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var userPreferences: com.reon.music.core.preferences.UserPreferences

    // Crossfade/gapless prefs applied to the real player. ExoPlayer plays
    // queued items gaplessly by default; the crossfade duration adds a
    // fade-out at track end + fade-in on the next track. When gapless is
    // off, fades are skipped so track boundaries stay hard cuts.
    @Volatile private var crossfadeSec: Int = 0
    @Volatile private var gaplessEnabled: Boolean = true
    private var fadeJob: Job? = null
    private var fadeWatcherJob: Job? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Music-optimized buffering: fast start, deep enough to ride out
        // network dips without rebuffering mid-song.
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 30_000,
                /* maxBufferMs = */ 120_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 3_000
            )
            .build()

        // Disk cache for streamed audio (500 MB LRU). NOTE: signed stream
        // URLs expire, so entries keyed by stale URLs rot until evicted —
        // the evictor bounds the waste.
        val cacheDir = File(cacheDir, "media").apply { mkdirs() }
        mediaCache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(500L * 1024 * 1024),
            StandaloneDatabaseProvider(this)
        )
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(mediaCache!!)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())

        // Initialize ExoPlayer with audio focus handling
        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this)
                    .setDataSourceFactory(cacheDataSourceFactory)
            )
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // handleAudioFocus
            )
            .setHandleAudioBecomingNoisy(true) // Pause when headphones disconnected
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        
        // Notification tap target: implicit intent avoids a hard reference
        // to the app module (which would be a circular dependency) and
        // can never throw ClassNotFoundException under R8/renaming.
        val sessionActivityIntent = Intent(Intent.ACTION_MAIN).apply {
            setPackage(packageName)
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Create MediaLibrarySession
        mediaSession = MediaLibrarySession.Builder(this, player, MediaLibraryCallback())
            .setSessionActivity(pendingIntent)
            .build()

        // Apply gapless/crossfade prefs to the player and pre-keep the next
        // queued item ready: the queue itself is set via setMediaItems +
        // prepare() by PlayerController (inherent next-track preload), and
        // this watcher adds the track-boundary fades.
        serviceScope.launch {
            userPreferences.crossfadeDuration.collect { crossfadeSec = it }
        }
        serviceScope.launch {
            userPreferences.gaplessPlayback.collect { gaplessEnabled = it }
        }
        player.addListener(CrossfadeListener())
        startFadeWatcher()
        
        // SponsorBlock integration is handled in PlayerViewModel
        // to avoid dependency injection issues in the service
    }
    
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession.release()
        player.release()
        mediaCache?.release()
        mediaCache = null
        serviceScope.cancel()
        super.onDestroy()
    }
    
    /**
     * Fades in on every track change so a faded-out ending flows into the
     * next song when crossfade + gapless are enabled.
     */
    private inner class CrossfadeListener : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (!gaplessEnabled || crossfadeSec <= 0) {
                player.volume = 1f
                return
            }
            fadeVolume(1f, (crossfadeSec * 1000L).coerceAtMost(2000L))
        }
    }

    /**
     * Watches the playing position and fades out across the last
     * [crossfadeSec] seconds of each track.
     */
    private fun startFadeWatcher() {
        fadeWatcherJob?.cancel()
        fadeWatcherJob = serviceScope.launch {
            while (true) {
                delay(500)
                try {
                    if (!::player.isInitialized) continue
                    val fadeMs = crossfadeSec * 1000L
                    if (!gaplessEnabled || fadeMs <= 0 || !player.isPlaying) continue
                    val duration = player.duration
                    if (duration <= 0) continue
                    val remaining = duration - player.currentPosition
                    if (remaining in 1..fadeMs) {
                        player.volume = (remaining.toFloat() / fadeMs)
                            .coerceIn(0.15f, 1f)
                    }
                } catch (_: Exception) {
                    // Playback state races during teardown are benign.
                }
            }
        }
    }

    /**
     * Ramps [player.volume] toward [target] over [durationMs].
     */
    private fun fadeVolume(target: Float, durationMs: Long) {
        fadeJob?.cancel()
        fadeJob = serviceScope.launch {
            try {
                val steps = 10
                val start = player.volume
                val stepMs = (durationMs / steps).coerceAtLeast(30L)
                repeat(steps) { i ->
                    delay(stepMs)
                    val t = (i + 1).toFloat() / steps
                    player.volume = (start + (target - start) * t).coerceIn(0f, 1f)
                }
                player.volume = target
            } catch (_: Exception) {
                // Ignored: player released mid-fade.
            }
        }
    }

    /**
     * MediaLibrarySession callback
     */
    private inner class MediaLibraryCallback : MediaLibrarySession.Callback {
        // Default implementations are sufficient for basic playback
    }
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "reon_playback_channel"
    }
}
