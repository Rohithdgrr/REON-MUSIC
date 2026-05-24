/*
 * REON Music App - Content Sync Scheduler
 * Copyright (c) 2024 REON
 * Manages scheduling of background content sync
 */

package com.reon.music.workers

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Scheduler for content sync worker
 * Handles periodic sync based on user preferences
 */
object ContentSyncScheduler {

    /**
     * Schedule periodic content sync
     * 
     * @param context Application context
     * @param frequencyMinutes How often to sync (in minutes)
     * @param wifiOnly Whether to sync only on WiFi
     * @param syncCharts Whether to sync charts
     * @param syncPlaylists Whether to sync playlists
     * @param syncNewReleases Whether to sync new releases
     */
    fun scheduleSync(
        context: Context,
        frequencyMinutes: Int = 60,
        wifiOnly: Boolean = true,
        syncCharts: Boolean = true,
        syncPlaylists: Boolean = true,
        syncNewReleases: Boolean = true
    ) {
        // Build constraints — battery-aware, no idle required for periodic
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()

        // Build input data
        val inputData = Data.Builder()
            .putBoolean(ContentSyncWorker.KEY_SYNC_CHARTS, syncCharts)
            .putBoolean(ContentSyncWorker.KEY_SYNC_PLAYLISTS, syncPlaylists)
            .putBoolean(ContentSyncWorker.KEY_SYNC_NEW_RELEASES, syncNewReleases)
            .build()

        // Create periodic work request
        val syncRequest = PeriodicWorkRequestBuilder<ContentSyncWorker>(
            repeatInterval = frequencyMinutes.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 15, // Allow 15 minute flex for battery optimization
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(ContentSyncWorker.TAG)
            .build()

        // Enqueue work (replace existing if any)
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ContentSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest
        )
    }

    /**
     * Trigger immediate sync (one-time)
     * 
     * @param context Application context
     * @param wifiOnly Whether to require WiFi
     */
    fun syncNow(
        context: Context,
        wifiOnly: Boolean = false
    ): androidx.lifecycle.LiveData<WorkInfo> {
        // Build constraints — immediate sync, battery-aware
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .build()

        // Build input data
        val inputData = Data.Builder()
            .putBoolean(ContentSyncWorker.KEY_SYNC_CHARTS, true)
            .putBoolean(ContentSyncWorker.KEY_SYNC_PLAYLISTS, true)
            .putBoolean(ContentSyncWorker.KEY_SYNC_NEW_RELEASES, true)
            .build()

        // Create one-time work request
        val syncRequest = OneTimeWorkRequestBuilder<ContentSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag("sync_now")
            .build()

        // Enqueue work
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(syncRequest)

        // Return LiveData to observe progress
        return workManager.getWorkInfoByIdLiveData(syncRequest.id)
    }

    /**
     * Cancel all scheduled syncs
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ContentSyncWorker.WORK_NAME)
    }

    /**
     * Check if sync is currently scheduled
     */
    fun isSyncScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ContentSyncWorker.WORK_NAME)
            .get()
        
        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.ENQUEUED || 
            workInfo.state == WorkInfo.State.RUNNING
        }
    }

    /**
     * Get sync status
     */
    fun getSyncStatus(context: Context): androidx.lifecycle.LiveData<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(ContentSyncWorker.WORK_NAME)
    }
}
