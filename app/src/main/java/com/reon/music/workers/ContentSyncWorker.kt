/*
 * REON Music App - Content Sync Worker
 * Copyright (c) 2024 REON
 * Background worker for auto-updating charts, playlists, and new releases
 */

package com.reon.music.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reon.music.data.repository.MusicRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for syncing content in the background
 * Runs periodically based on user settings
 */
class ContentSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "ContentSyncWorker"
        const val WORK_NAME = "content_sync_work"
        
        // Input data keys
        const val KEY_SYNC_CHARTS = "sync_charts"
        const val KEY_SYNC_PLAYLISTS = "sync_playlists"
        const val KEY_SYNC_NEW_RELEASES = "sync_new_releases"
        
        // Output data keys
        const val KEY_CHARTS_UPDATED = "charts_updated"
        const val KEY_PLAYLISTS_UPDATED = "playlists_updated"
        const val KEY_NEW_RELEASES_UPDATED = "new_releases_updated"
        const val KEY_SYNC_TIME = "sync_time"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ContentSyncWorkerEntryPoint {
        fun musicRepository(): MusicRepository
        fun userPreferences(): com.reon.music.core.preferences.UserPreferences
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting content sync...")
        
        // Show sync in progress notification
        SyncNotificationManager.showSyncInProgress(context)
        
        try {
            // Get dependencies via Hilt
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                ContentSyncWorkerEntryPoint::class.java
            )
            val repository = entryPoint.musicRepository()
            val userPreferences = entryPoint.userPreferences()
            
            // Get sync preferences from input data
            val syncCharts = inputData.getBoolean(KEY_SYNC_CHARTS, true)
            val syncPlaylists = inputData.getBoolean(KEY_SYNC_PLAYLISTS, true)
            val syncNewReleases = inputData.getBoolean(KEY_SYNC_NEW_RELEASES, true)
            
            var chartsUpdated = 0
            var playlistsUpdated = 0
            var newReleasesUpdated = 0
            
            // Sync charts
            if (syncCharts) {
                Log.d(TAG, "Syncing charts...")
                chartsUpdated = syncCharts(repository)
                Log.d(TAG, "Charts synced: $chartsUpdated updated")
            }
            
            // Sync playlists
            if (syncPlaylists) {
                Log.d(TAG, "Syncing playlists...")
                playlistsUpdated = syncPlaylists(repository)
                Log.d(TAG, "Playlists synced: $playlistsUpdated updated")
            }
            
            // Sync new releases
            if (syncNewReleases) {
                Log.d(TAG, "Syncing new releases...")
                newReleasesUpdated = syncNewReleases(repository)
                Log.d(TAG, "New releases synced: $newReleasesUpdated updated")
            }
            
            // Show completion notification
            SyncNotificationManager.showSyncCompleted(
                context,
                chartsUpdated,
                playlistsUpdated,
                newReleasesUpdated
            )
            
            // Record the sync so the UI can show freshness and skip
            // redundant reloads when content was just synced in background.
            userPreferences.setLastSyncTime(System.currentTimeMillis())

            // Create output data
            val outputData = androidx.work.Data.Builder()
                .putInt(KEY_CHARTS_UPDATED, chartsUpdated)
                .putInt(KEY_PLAYLISTS_UPDATED, playlistsUpdated)
                .putInt(KEY_NEW_RELEASES_UPDATED, newReleasesUpdated)
                .putLong(KEY_SYNC_TIME, System.currentTimeMillis())
                .build()
            
            Log.d(TAG, "Content sync completed successfully")
            Result.success(outputData)
            
        } catch (e: Exception) {
            Log.e(TAG, "Content sync failed", e)
            
            // Show failure notification
            SyncNotificationManager.showSyncFailed(context, e.message)
            
            // Retry on failure (max 3 attempts)
            if (runAttemptCount < 3) {
                Log.d(TAG, "Retrying... (attempt ${runAttemptCount + 1}/3)")
                Result.retry()
            } else {
                Log.e(TAG, "Max retry attempts reached")
                Result.failure()
            }
        }
    }

    /**
     * Sync charts from YouTube Music (InnerTube). Each fetch warms the
     * HTTP caches and proves the endpoint is still returning fresh data.
     */
    private suspend fun syncCharts(repository: MusicRepository): Int {
        return try {
            var updated = 0

            suspend fun fetch(label: String, block: suspend () -> com.reon.music.core.common.Result<List<*>>) {
                try {
                    Log.d(TAG, "Fetching chart: $label")
                    if (block().isSuccess) updated++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync chart: $label", e)
                }
            }

            fetch("trending") { repository.getTrendingSongs() }
            fetch("top_hindi") { repository.getTop50Hindi() }
            fetch("top_telugu") { repository.getTeluguSongs() }
            fetch("top_tamil") { repository.getTamilSongs() }
            fetch("top_english") { repository.getEnglishSongs() }
            fetch("top_punjabi") { repository.getPunjabiSongs() }

            updated
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync charts", e)
            0
        }
    }

    /**
     * Sync playlists from YouTube Music (InnerTube).
     */
    private suspend fun syncPlaylists(repository: MusicRepository): Int {
        return try {
            var updated = 0

            try {
                Log.d(TAG, "Fetching featured playlists")
                if (repository.getFeaturedPlaylists().isSuccess) updated++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync featured playlists", e)
            }

            try {
                Log.d(TAG, "Fetching mood playlists")
                if (repository.searchPlaylists("mood chill relax").isSuccess) updated++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync mood playlists", e)
            }

            try {
                Log.d(TAG, "Fetching genre playlists")
                if (repository.searchPlaylists("pop hits playlist").isSuccess) updated++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync genre playlists", e)
            }

            updated
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync playlists", e)
            0
        }
    }

    /**
     * Sync new releases from YouTube Music (InnerTube).
     */
    private suspend fun syncNewReleases(repository: MusicRepository): Int {
        return try {
            var updated = 0

            try {
                Log.d(TAG, "Fetching new releases")
                if (repository.getNewReleases().isSuccess) updated++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync new releases", e)
            }

            try {
                Log.d(TAG, "Fetching trending albums")
                if (repository.getTrendingAlbums().isSuccess) updated++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync trending albums", e)
            }
            
            updated
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync new releases", e)
            0
        }
    }
}
