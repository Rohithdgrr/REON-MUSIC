/*
 * REON Music App - Download Manager
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.services

import android.content.Context
import androidx.work.*
import com.reon.music.core.model.Song
import com.reon.music.core.preferences.AudioQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Download Manager
 * Manages song downloads using WorkManager with optional yt-dlp fallback
 * 
 * When WorkManager download fails, can fallback to yt-dlp if available
 */
@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ytDlpDownloader: YtDlpDownloader
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Queue a song for download
     */
    fun downloadSong(
        song: Song,
        resolvedStreamUrl: String? = null,
        quality: AudioQuality = AudioQuality.HIGH
    ): UUID {
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_SONG_ID, song.id)
            .putString(
                DownloadWorker.KEY_STREAM_URL,
                resolvedStreamUrl ?: song.getStreamUrlWithQuality("_${quality.bitrate}")
            )
            .putString(DownloadWorker.KEY_TITLE, song.title)
            .putString(DownloadWorker.KEY_QUALITY, quality.bitrate.toString())
            .build()
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()
        
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .addTag("download")
            .addTag("download_${song.id}")
            .build()
        
        // REPLACE (not KEEP): every call here is an explicit user or
        // cache action meaning "make sure this is downloaded". KEEP would
        // silently drop the request while a previous attempt is still
        // queued or backing off, stranding stuck downloads with no retry.
        workManager.enqueueUniqueWork(
            "download_${song.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
        
        return request.id
    }
    
    /**
     * Download multiple songs
     */
    fun downloadSongs(
        songs: List<Song>,
        resolvedStreamUrls: Map<String, String?> = emptyMap(),
        quality: AudioQuality = AudioQuality.HIGH
    ): List<UUID> {
        return songs.map { song ->
            downloadSong(song, resolvedStreamUrls[song.id], quality)
        }
    }
    
    /**
     * Cancel a specific download
     */
    fun cancelDownload(songId: String) {
        workManager.cancelUniqueWork("download_$songId")
    }
    
    /**
     * Cancel all downloads
     */
    fun cancelAllDownloads() {
        workManager.cancelAllWorkByTag("download")
    }
    
    /**
     * Get download progress
     */
    fun getDownloadProgress(songId: String): Flow<DownloadProgress> {
        return workManager.getWorkInfosForUniqueWorkFlow("download_$songId")
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull()
                when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED -> DownloadProgress(songId, 0, DownloadStatus.QUEUED)
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                        DownloadProgress(songId, progress, DownloadStatus.DOWNLOADING)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val filePath = workInfo.outputData.getString(DownloadWorker.KEY_FILE_PATH)
                        DownloadProgress(songId, 100, DownloadStatus.COMPLETED, filePath)
                    }
                    WorkInfo.State.FAILED -> DownloadProgress(songId, 0, DownloadStatus.FAILED)
                    WorkInfo.State.CANCELLED -> DownloadProgress(songId, 0, DownloadStatus.CANCELLED)
                    else -> DownloadProgress(songId, 0, DownloadStatus.UNKNOWN)
                }
            }
    }
    
    /**
     * Get all active downloads
     */
    fun getActiveDownloads(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagFlow("download")
            .map { workInfos ->
                workInfos.filter { 
                    it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED 
                }
            }
    }
}

data class DownloadProgress(
    val songId: String,
    val progress: Int,
    val status: DownloadStatus,
    val filePath: String? = null
)

enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    COMPLETED,
    FAILED,
    CANCELLED,
    UNKNOWN
}
