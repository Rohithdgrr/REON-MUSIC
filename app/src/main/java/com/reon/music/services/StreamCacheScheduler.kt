/*
 * REON Music App - Stream Cache Scheduler
 * Copyright (c) 2024 REON
 * Schedules periodic maintenance tasks for YouTube stream caching
 */

package com.reon.music.services

import android.content.Context
import androidx.work.*
import com.reon.music.workers.YouTubeStreamMaintenanceWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages scheduling of periodic tasks for stream cache maintenance
 */
@Singleton
class StreamCacheScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic stream cache maintenance
     * Runs every hour to refresh expiring URLs and cleanup
     */
    fun schedulePeriodicMaintenance() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
        
        val maintenanceRequest = PeriodicWorkRequestBuilder<YouTubeStreamMaintenanceWorker>(
            repeatInterval = 1, // Repeat every 1 hour
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 15, // Flex window of 15 minutes
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            YouTubeStreamMaintenanceWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            maintenanceRequest
        )
    }
    
    /**
     * Cancel all scheduled maintenance tasks
     */
    fun cancelMaintenance() {
        workManager.cancelUniqueWork(YouTubeStreamMaintenanceWorker.WORK_NAME)
    }
    
    /**
     * Trigger manual maintenance immediately
     */
    fun triggerManualMaintenance() {
        val maintenanceRequest = OneTimeWorkRequestBuilder<YouTubeStreamMaintenanceWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        
        workManager.enqueue(maintenanceRequest)
    }
    
    /**
     * Get maintenance work status
     */
    fun getMaintenanceWorkInfo() = workManager
        .getWorkInfosForUniqueWorkLiveData(YouTubeStreamMaintenanceWorker.WORK_NAME)
}
