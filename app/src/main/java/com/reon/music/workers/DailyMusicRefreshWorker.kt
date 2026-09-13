/*
 * REON Music App - Daily Music Refresh Worker
 * Refreshes dynamic playlist content (Tamil hits, trending, new releases)
 * once per day so home always shows real, latest data.
 */

package com.reon.music.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reon.music.data.repository.HomeCacheManager
import com.reon.music.data.repository.MusicRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Daily refresh: network-only constraints (no battery/storage gating, which
 * prevents the worker from ever running on many devices). Retries up to
 * 3 times with WorkManager backoff, then fails gracefully — cached
 * content stays visible.
 */
class DailyMusicRefreshWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_music_refresh"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DailyRefreshEntryPoint {
        fun musicRepository(): MusicRepository
        fun homeCacheManager(): HomeCacheManager
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(WORK_NAME, "Starting daily music refresh...")
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                DailyRefreshEntryPoint::class.java
            )
            val repository = entryPoint.musicRepository()
            val cache = entryPoint.homeCacheManager()

            // Warm the dynamic endpoints; each call refreshes HTTP caches
            // and proves the source still returns fresh data.
            try { repository.getLatestTamilHits(20) } catch (e: Exception) {
                Log.w(WORK_NAME, "Tamil refresh failed", e)
            }
            try { repository.getTrendingNow("IN", 20) } catch (e: Exception) {
                Log.w(WORK_NAME, "Trending refresh failed", e)
            }
            try { repository.getNewReleases() } catch (e: Exception) {
                Log.w(WORK_NAME, "New releases refresh failed", e)
            }
            try { repository.getFeaturedPlaylists() } catch (e: Exception) {
                Log.w(WORK_NAME, "Playlists refresh failed", e)
            }

            cache.markFresh(System.currentTimeMillis())
            Log.d(WORK_NAME, "Daily music refresh completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(WORK_NAME, "Daily refresh failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
