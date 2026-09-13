/*
 * REON Music App - Home Cache Manager
 * 24-hour TTL for dynamic home / playlist content.
 */

package com.reon.music.data.repository

import com.reon.music.core.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises the "real, latest data every day" rule:
 * home content is stale after [ttlMs] (default 24h) since the last
 * successful sync. Backed by DataStore via [UserPreferences] so the
 * TTL survives process death, unlike the old in-memory timestamp.
 */
@Singleton
class HomeCacheManager @Inject constructor(
    private val userPreferences: UserPreferences
) {
    companion object {
        val DEFAULT_TTL_MS: Long = TimeUnit.DAYS.toMillis(1)
    }

    suspend fun isCacheStale(ttlMs: Long = DEFAULT_TTL_MS): Boolean {
        val lastSync = try {
            userPreferences.lastSyncTime.first()
        } catch (_: Exception) {
            0L
        }
        if (lastSync <= 0L) return true
        return System.currentTimeMillis() - lastSync >= ttlMs
    }

    suspend fun markFresh(timestamp: Long = System.currentTimeMillis()) {
        userPreferences.setLastSyncTime(timestamp)
    }

    suspend fun lastUpdated(): Long {
        return try {
            userPreferences.lastSyncTime.first()
        } catch (_: Exception) {
            0L
        }
    }
}
