/*
 * REON Music App - YouTube Stream URL Manager
 * Copyright (c) 2024 REON
 * Handles automatic refresh of expiring YouTube stream URLs
 */

package com.reon.music.data.repository

import androidx.collection.LruCache
import com.reon.music.data.database.dao.YouTubeStreamCacheDao
import com.reon.music.data.database.entities.YouTubeStreamCacheEntity
import com.reon.music.data.network.youtube.YouTubeMusicClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages YouTube stream URLs with automatic caching and refresh
 * Handles 6-hour expiry and proactive refresh
 * Uses bounded LRU cache to prevent OOM from unbounded map growth
 */
@Singleton
class YouTubeStreamUrlManager @Inject constructor(
    private val youtubeMusicClient: YouTubeMusicClient,
    private val streamCacheDao: YouTubeStreamCacheDao
) {
    private val mutex = Mutex()
    private val inMemoryCache = object : LruCache<String, YouTubeStreamCacheEntity>(200) {
        override fun sizeOf(key: String, value: YouTubeStreamCacheEntity): Int = 1
    }
    
    /**
     * Get valid stream URL for a video with automatic refresh
     * Returns cached URL if valid, otherwise fetches new one
     */
    suspend fun getStreamUrl(
        videoId: String,
        title: String = "",
        channelName: String = ""
    ): String? = mutex.withLock {
        // Step 1: Check in-memory cache first (bounded LRU, no OOM risk)
        inMemoryCache.get(videoId)?.let { cached ->
            if (!cached.isExpired() && !cached.willExpireSoon()) {
                streamCacheDao.updateAccess(videoId)
                return@withLock cached.audioStreamUrl
            }
        }
        
        // Step 2: Check database cache
        val dbCached = streamCacheDao.getStreamCache(videoId)
        if (dbCached != null && !dbCached.isExpired() && !dbCached.willExpireSoon()) {
            inMemoryCache.put(videoId, dbCached)
            streamCacheDao.updateAccess(videoId)
            return@withLock dbCached.audioStreamUrl
        }
        
        // Step 3: Fetch new stream URL
        val streamUrl = youtubeMusicClient.getStreamUrl(videoId).getOrNull()
        
        if (streamUrl != null) {
            val cacheEntry = YouTubeStreamCacheEntity(
                videoId = videoId,
                audioStreamUrl = streamUrl,
                title = title,
                channelName = channelName,
                codec = detectCodec(streamUrl),
                bitrate = detectBitrate(streamUrl)
            )
            
            // Cache in both memory (bounded LRU) and database
            inMemoryCache.put(videoId, cacheEntry)
            streamCacheDao.insertStreamCache(cacheEntry)
            
            return@withLock streamUrl
        }
        
        // Step 4: Fallback - return expired cache if available
        dbCached?.audioStreamUrl
    }
    
    /**
     * Proactively refresh URLs that will expire soon
     * Should be called periodically (e.g., every hour)
     */
    suspend fun refreshExpiringSoon() {
        val expiring = streamCacheDao.getExpiringSoon()
        
        expiring.forEach { cached ->
            try {
                youtubeMusicClient.getStreamUrl(cached.videoId).getOrNull()?.let { newUrl ->
                    val updated = cached.copy(
                        audioStreamUrl = newUrl,
                        fetchedAt = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + (6 * 60 * 60 * 1000)
                    )
                    streamCacheDao.insertStreamCache(updated)
                    inMemoryCache.put(cached.videoId, updated)
                }
            } catch (e: Exception) {
                // Continue with next entry
            }
        }
    }
    
    /**
     * Clean up expired cache entries
     */
    suspend fun cleanupExpired() {
        val deleted = streamCacheDao.deleteExpired()
        
        // Also clean in-memory cache (LRU handles eviction automatically via trimToSize)
    }
    
    /**
     * Prefetch stream URLs for a list of videos
     * Useful for queue management
     */
    suspend fun prefetchUrls(videoIds: List<String>) {
        videoIds.forEach { videoId ->
            // Only prefetch if not already cached
            val cached = streamCacheDao.getStreamCache(videoId)
            if (cached == null || cached.isExpired() || cached.willExpireSoon()) {
                getStreamUrl(videoId)
            }
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): StreamCacheStats {
        val dbStats = streamCacheDao.getCacheStats()
        return StreamCacheStats(
            totalEntries = dbStats.total,
            validEntries = dbStats.valid,
            expiredEntries = dbStats.expired,
            inMemorySize = inMemoryCache.size(),
            hitRate = dbStats.hitRate
        )
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAll() = mutex.withLock {
        inMemoryCache.evictAll()
        streamCacheDao.deleteAll()
    }
    
    // Helper methods
    
    private fun detectCodec(url: String): String {
        return when {
            url.contains("mime=audio%2Fwebm", ignoreCase = true) -> "opus"
            url.contains("mime=audio%2Fmp4", ignoreCase = true) -> "m4a"
            url.contains("opus") -> "opus"
            url.contains("m4a") -> "m4a"
            else -> "unknown"
        }
    }
    
    private fun detectBitrate(url: String): Int {
        // Try to extract bitrate from URL parameters
        val bitrateRegex = Regex("clen=(\\d+)")
        val match = bitrateRegex.find(url)
        return match?.groupValues?.get(1)?.toIntOrNull()?.div(1000) ?: 160
    }
}

/**
 * Stream cache statistics
 */
data class StreamCacheStats(
    val totalEntries: Int,
    val validEntries: Int,
    val expiredEntries: Int,
    val inMemorySize: Int,
    val hitRate: Float
) {
    val cacheEfficiency: Float
        get() = if (totalEntries > 0) (validEntries.toFloat() / totalEntries.toFloat()) else 0f
}
