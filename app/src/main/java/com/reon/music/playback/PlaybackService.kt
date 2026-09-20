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
import com.reon.music.MainActivity
import com.reon.music.data.MusicRepository
import com.reon.music.data.ReonDatabase
import com.reon.music.data.TrackEntity
import com.reon.music.data.remote.ReonBackendApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException

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
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(1)
                    .setContentType(3)
                    .build(),
                true
            )
            .build()
        mediaSession = MediaSession.Builder(this, player!!)
            .setSessionActivity(pendingIntent)
            .build()
        player?.addListener(playerListener)
        registerReceiver(playbackReceiver, IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
        })
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESTORE) {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        return START_STICKY
    }

    fun playTrack(track: TrackEntity, streamUrl: String?, expiresAt: Long) {
        currentTrackId = track.id
        this.streamUrl = streamUrl
        this.streamExpiresAt = expiresAt
        val resolvedUrl = resolveStreamUrl(track)
        if (resolvedUrl != null) {
            player?.setMediaItem(MediaItem.fromUri(resolvedUrl))
            player?.prepare()
            player?.playWhenReady = true
            startForeground(NOTIFICATION_ID, buildNotification())
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

    private fun resolveStreamUrl(track: TrackEntity): String? {
        val now = System.currentTimeMillis() / 1000L
        if (!streamUrl.isNullOrEmpty() && now < streamExpiresAt - 60) {
            return streamUrl
        }
        serviceScope.launch {
            try {
                val result = repository.stream(track.id, "high")
                if (result.url != null) {
                    repository.repositoryDao.updateStream(track.id, result.url, result.expiresAt)
                    streamUrl = result.url
                    streamExpiresAt = result.expiresAt
                    player?.setMediaItem(MediaItem.fromUri(result.url))
                    player?.prepare()
                    player?.playWhenReady = true
                }
            } catch (_: IOException) {
                handleStreamExpiry(track)
            } catch (_: Exception) {
                handleStreamExpiry(track)
            }
        }
        return streamUrl
    }

    private fun handleStreamExpiry(track: TrackEntity) {
        serviceScope.launch {
            try {
                val result = repository.stream(track.id, "high")
                if (result.url != null) {
                    repository.repositoryDao.updateStream(track.id, result.url, result.expiresAt)
                    streamUrl = result.url
                    streamExpiresAt = result.expiresAt
                    player?.setMediaItem(MediaItem.fromUri(result.url))
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

        override fun onPlayerError(error: PlaybackException) {
            currentTrackId?.let { id ->
                serviceScope.launch {
                    val track = repository.repositoryDao.getTrackById(id)
                    if (track != null) {
                        handleStreamExpiry(track)
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
        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_rew, "Previous",
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_PREVIOUS), PendingIntent.FLAG_IMMUTABLE)
        )
        val pauseAction = NotificationCompat.Action(
            android.R.drawable.ic_media_pause, "Pause",
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_PAUSE), PendingIntent.FLAG_IMMUTABLE)
        )
        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_ff, "Next",
            PendingIntent.getBroadcast(this, 0, Intent(ACTION_NEXT), PendingIntent.FLAG_IMMUTABLE)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentTrackId ?: "REON Music")
            .setContentText("Playing")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(prevAction)
            .addAction(pauseAction)
            .addAction(nextAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
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
