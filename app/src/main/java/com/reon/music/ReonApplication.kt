/*
 * REON Music App
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.reon.music.workers.ContentSyncWorker
import com.reon.music.workers.DailyMusicRefreshWorker
import com.reon.music.workers.YouTubeStreamMaintenanceWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * REON Application class
 * Entry point for the application with Hilt DI
 * Schedules background maintenance tasks on startup
 */
@HiltAndroidApp
class ReonApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Sync notifications are silently dropped on Android 8+ without this.
        com.reon.music.workers.SyncNotificationManager.createNotificationChannel(this)
        scheduleBackgroundTasks()
    }

    private fun scheduleBackgroundTasks() {
        val workManager = WorkManager.getInstance(this)

        // Stream cache maintenance every hour.
        // Kept to network-only constraints: battery/storage gating is
        // evaluated inside the worker so maintenance still runs (possibly
        // deferred) instead of never firing on constrained devices.
        val maintenanceConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val maintenanceRequest = PeriodicWorkRequestBuilder<YouTubeStreamMaintenanceWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setConstraints(maintenanceConstraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            YouTubeStreamMaintenanceWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            maintenanceRequest
        )

        // Content sync every 2 hours.
        // UNMETERED is kept to honor the Wi-Fi-only sync setting; battery /
        // storage gating is left to the worker so sync degrades gracefully
        // instead of never running on constrained devices.
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<ContentSyncWorker>(
            2, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        )
            .setConstraints(syncConstraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ContentSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        // Daily dynamic-content refresh (Tamil hits, trending, new releases).
        // Network-only constraints on purpose: battery/storage gating is
        // evaluated inside the worker so refresh degrades gracefully
        // instead of never firing on constrained devices.
        val dailyConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dailyRequest = PeriodicWorkRequestBuilder<DailyMusicRefreshWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(dailyConstraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            DailyMusicRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyRequest
        )
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
