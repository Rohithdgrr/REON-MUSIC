package com.reon.music.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.reon.music.MainActivity
import com.reon.music.data.MusicRepository
import com.reon.music.data.TrackEntity
import com.reon.music.data.remote.ReonBackendApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val CHANNEL_ID = "reon_playback"
const val NOTIFICATION_ID = 1001
const val ACTION_RESTORE = "com.reon.music.ACTION_RESTORE"
const val ACTION_PAUSE = "com.reon.music.ACTION_PAUSE"
const val ACTION_PLAY = "com.reon.music.ACTION_PLAY"
const val ACTION_NEXT = "com.reon.music.ACTION_NEXT"
const val ACTION_PREVIOUS = "com.reon.music.ACTION_PREVIOUS"

class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var currentTrackId: String? = null
    private var currentTrackTitle: String? = null
    private var currentArtist: String? = null
    private var streamUrl: String? = null
    private var streamExpiresAt: Long = 0L

    private val serviceScope = CoroutineScope(Job() + Dispatchers.IO)
    private val repository by lazy {
        MusicRepository(
            api = ReonBackendApi.create(),
            dao = (application as com.reon.music.ReonApplication).database.reonDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("REON/1.1 (Linux; Android 13)")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(1) // USAGE_MEDIA
                    .setContentType(3) // CONTENT_TYPE_MUSIC
                    .build(),
                true
            )
            .build()
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent)
            .build()
        player?.addListener(playerListener)
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(playbackReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(playbackReceiver, filter)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        unregisterReceiver(playbackReceiver)
        mediaSession?.release()
        player?.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Media3 handles foreground via this callback — ensures no ANR from missing startForeground
    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        val notification = buildNotification()
        if (startInForegroundRequired) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_RESTORE) {
            // Ensure notification exists even before MediaSession triggers onUpdateNotification
            try {
                startForeground(NOTIFICATION_ID, buildNotification())
            } catch (_: Exception) {}
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Don't stop playback when task swiped — keep service alive for background playback
        // System will call onDestroy when needed
    }

    /**
     * Primary entry for UI via MediaController or direct call.
     * Resolves fresh stream via repository (which handles cache + 60s margin + quality fallback)
     * and starts ExoPlayer. Safe to call from any thread.
     */
    fun playTrack(track: TrackEntity, streamUrl: String? = null, expiresAt: Long = 0L) {
        currentTrackId = track.id
        currentTrackTitle = track.title
        currentArtist = track.artist
        if (!streamUrl.isNullOrBlank() && expiresAt > 0) {
            this.streamUrl = streamUrl
            this.streamExpiresAt = expiresAt
        }
        serviceScope.launch {
            val url = resolveStreamUrl(track)
            if (url != null) {
                withContext(Dispatchers.Main) {
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
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.playWhenReady = true
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
            }
        }
    }

    /** Back-compat: repository-only resolve (suspend) */
    suspend fun resolveFreshStream(track: TrackEntity): String? {
        val id = track.videoId.takeIf { it.isNotBlank() } ?: track.id
        return try {
            val res = repository.stream(id, "high")
            streamUrl = res.url
            streamExpiresAt = res.expiresAt
            currentTrackId = track.id
            currentTrackTitle = track.title
            currentArtist = track.artist
            res.url
        } catch (_: Exception) {
            null
        }
    }

    fun pause() {
        player?.playWhenReady = false
        updateNotification()
    }

    fun play() {
        player?.playWhenReady = true
        updateNotification()
    }

    fun playNext() {
        player?.seekToNext()
    }

    fun playPrevious() {
        player?.seekToPrevious()
    }

    fun seekTo(fraction: Float) {
        val duration = player?.duration ?: return
        player?.seekTo((duration * fraction).coerceIn(0f, 1f).toLong())
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) pause() else play()
    }

    private suspend fun resolveStreamUrl(track: TrackEntity): String? {
        val id = track.videoId.takeIf { it.isNotBlank() } ?: track.id
        // 1) fast path: check local expiry without network
        val now = System.currentTimeMillis() / 1000L
        if (!streamUrl.isNullOrEmpty() && now < streamExpiresAt - 60) {
            return streamUrl
        }
        // 2) delegate to repository which handles cache + quality fallback + HttpException
        return try {
            val result = repository.stream(id, "high")
            streamUrl = result.url
            streamExpiresAt = result.expiresAt
            result.url
        } catch (e: Exception) {
            // retry via handleStreamExpiry will attempt again; return stale if available
            if (!streamUrl.isNullOrBlank()) return streamUrl
            null
        }
    }

    private fun handleStreamExpiry(track: TrackEntity) {
        serviceScope.launch {
            try {
                val id = track.videoId.takeIf { it.isNotBlank() } ?: track.id
                val result = repository.stream(id, "high")
                streamUrl = result.url
                streamExpiresAt = result.expiresAt
                withContext(Dispatchers.Main) {
                    val mediaItem = MediaItem.Builder()
                        .setUri(result.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(track.title)
                                .setArtist(track.artist)
                                .build()
                        )
                        .build()
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.playWhenReady = true
                }
            } catch (_: Exception) {}
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNotification()
        }

        override fun onPlayerError(error: PlaybackException) {
            // 403/410 or generic upstream -> re-resolve stream (likely expired)
            val code = (error.cause as? androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException)?.responseCode
            val shouldRetry = code == 403 || code == 410 || code == 401 || error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            if (!shouldRetry && error.errorCodeName != "ERROR_CODE_IO_BAD_HTTP_STATUS") {
                // still retry for any io error
            }
            currentTrackId?.let { id ->
                serviceScope.launch {
                    // try both id and videoId lookup
                    val track = repository.repositoryDao.getTrackById(id)
                        ?: repository.repositoryDao.getTrackByVideoId(id)
                    if (track != null) {
                        handleStreamExpiry(track)
                    } else {
                        // fallback: construct minimal entity from id
                        handleStreamExpiry(
                            TrackEntity(
                                id = id, title = currentTrackTitle ?: id, artist = currentArtist ?: "",
                                album = "", category = "YT Stream", durationMs = 0L,
                                albumArtUrl = "", artistImageUrl = "", source = "YT Stream",
                                quality = "Opus · 160kbps", spatialMode = "Stereo", codec = "Opus",
                                sampleRate = "48kHz", monthlyListeners = "", lyricsQuote = "",
                                videoId = id, sourceKind = "YT_STREAM"
                            )
                        )
                    }
                }
            }
        }
    }

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAY -> play()
                ACTION_PAUSE -> pause()
                ACTION_NEXT -> playNext()
                ACTION_PREVIOUS -> playPrevious()
            }
        }
    }

    private val pendingIntent: PendingIntent
        get() {
            val intent = Intent(this, MainActivity::class.java)
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "REON Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val isPlaying = player?.isPlaying == true
        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_rew, "Previous",
            PendingIntent.getBroadcast(this, 1, Intent(ACTION_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        )
        val pauseAction = NotificationCompat.Action(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "Pause" else "Play",
            PendingIntent.getBroadcast(this, 2, Intent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY), PendingIntent.FLAG_IMMUTABLE)
        )
        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_ff, "Next",
            PendingIntent.getBroadcast(this, 3, Intent(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        )

        val title = currentTrackTitle ?: currentTrackId ?: "REON Music"
        val artist = currentArtist ?: "Playing"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setSilent(true)
            .addAction(prevAction)
            .addAction(pauseAction)
            .addAction(nextAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .build()
    }

    private fun updateNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification())
    }


}

class PlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Delegated to PlaybackService via MediaButtonReceiver
    }
}
