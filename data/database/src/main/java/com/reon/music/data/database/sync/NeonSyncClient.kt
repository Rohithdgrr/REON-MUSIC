/*
 * REON Music App - Neon PostgreSQL Sync Client
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 * 
 * Uses Neon's HTTP Data API for cloud sync
 */

package com.reon.music.data.database.sync

import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Neon PostgreSQL Sync Client
 * Handles cloud synchronization for user data
 */
@Singleton
class NeonSyncClient @Inject constructor(
    private val httpClient: HttpClient
) {
    companion object {
        private const val TAG = "NeonSyncClient"
        
        // Neon connection details
        // SECURITY: Credentials MUST be set via environment variables (NEON_PASSWORD)
        // or BuildConfig - never hardcode passwords in source
        private const val NEON_HOST = "ep-odd-grass-a19v097i-pooler.ap-southeast-1.aws.neon.tech"
        private const val NEON_DATABASE = "neondb"
        private const val NEON_USER = "neondb_owner"
        private val NEON_PASSWORD: String by lazy {
            System.getenv("NEON_PASSWORD") ?: error("NEON_PASSWORD environment variable not set")
        }
        
        // We'll use Neon's SQL HTTP API
        private val NEON_API_URL = "https://$NEON_HOST/sql"
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    /**
     * Initialize database tables if they don't exist
     */
    suspend fun initializeTables(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val createTablesSql = """
                -- Users table for device identification
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    device_id VARCHAR(255) UNIQUE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_sync TIMESTAMP
                );
                
                -- Synced songs (favorites/library)
                CREATE TABLE IF NOT EXISTS synced_songs (
                    id VARCHAR(255) PRIMARY KEY,
                    device_id VARCHAR(255) NOT NULL,
                    title VARCHAR(500) NOT NULL,
                    artist VARCHAR(500) NOT NULL,
                    album VARCHAR(500),
                    duration INT DEFAULT 0,
                    artwork_url TEXT,
                    source VARCHAR(50),
                    is_liked BOOLEAN DEFAULT FALSE,
                    in_library BOOLEAN DEFAULT FALSE,
                    play_count INT DEFAULT 0,
                    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                
                -- Synced playlists
                CREATE TABLE IF NOT EXISTS synced_playlists (
                    id SERIAL PRIMARY KEY,
                    device_id VARCHAR(255) NOT NULL,
                    local_id BIGINT NOT NULL,
                    title VARCHAR(500) NOT NULL,
                    description TEXT,
                    thumbnail TEXT,
                    track_count INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE(device_id, local_id)
                );
                
                -- Playlist songs
                CREATE TABLE IF NOT EXISTS synced_playlist_songs (
                    playlist_id INT REFERENCES synced_playlists(id) ON DELETE CASCADE,
                    song_id VARCHAR(255) NOT NULL,
                    position INT NOT NULL,
                    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (playlist_id, song_id)
                );
                
                -- Listen history
                CREATE TABLE IF NOT EXISTS synced_history (
                    id SERIAL PRIMARY KEY,
                    device_id VARCHAR(255) NOT NULL,
                    song_id VARCHAR(255) NOT NULL,
                    played_at TIMESTAMP NOT NULL,
                    play_duration BIGINT DEFAULT 0,
                    completed_play BOOLEAN DEFAULT FALSE
                );
                
                -- Create indexes
                CREATE INDEX IF NOT EXISTS idx_synced_songs_device ON synced_songs(device_id);
                CREATE INDEX IF NOT EXISTS idx_synced_songs_liked ON synced_songs(is_liked);
                CREATE INDEX IF NOT EXISTS idx_synced_playlists_device ON synced_playlists(device_id);
                CREATE INDEX IF NOT EXISTS idx_synced_history_device ON synced_history(device_id);
            """.trimIndent()
            
            executeQuery(createTablesSql)
            Log.d(TAG, "Database tables initialized successfully")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize tables", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync liked songs to cloud
     */
    suspend fun syncLikedSong(
        deviceId: String,
        songId: String,
        title: String,
        artist: String,
        album: String?,
        artworkUrl: String?,
        source: String,
        isLiked: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sql = """
                INSERT INTO synced_songs (id, device_id, title, artist, album, artwork_url, source, is_liked, synced_at)
                VALUES ('$songId', '$deviceId', '${title.escapeSql()}', '${artist.escapeSql()}', 
                        ${album?.let { "'${it.escapeSql()}'" } ?: "NULL"},
                        ${artworkUrl?.let { "'$it'" } ?: "NULL"},
                        '$source', $isLiked, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET
                    is_liked = $isLiked,
                    synced_at = CURRENT_TIMESTAMP;
            """.trimIndent()
            
            executeQuery(sql)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync liked song", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all liked songs from cloud
     */
    suspend fun getLikedSongs(deviceId: String): Result<List<SyncedSong>> = withContext(Dispatchers.IO) {
        try {
            val sql = "SELECT * FROM synced_songs WHERE device_id = '$deviceId' AND is_liked = true ORDER BY synced_at DESC"
            val result = executeQuery(sql)
            
            // Parse result
            val songs = parseQueryResult<SyncedSong>(result)
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get liked songs", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync playlist to cloud
     */
    suspend fun syncPlaylist(
        deviceId: String,
        localId: Long,
        title: String,
        description: String?,
        thumbnail: String?,
        trackCount: Int
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val sql = """
                INSERT INTO synced_playlists (device_id, local_id, title, description, thumbnail, track_count, updated_at)
                VALUES ('$deviceId', $localId, '${title.escapeSql()}', 
                        ${description?.let { "'${it.escapeSql()}'" } ?: "NULL"},
                        ${thumbnail?.let { "'$it'" } ?: "NULL"},
                        $trackCount, CURRENT_TIMESTAMP)
                ON CONFLICT (device_id, local_id) DO UPDATE SET
                    title = '${title.escapeSql()}',
                    description = ${description?.let { "'${it.escapeSql()}'" } ?: "NULL"},
                    thumbnail = ${thumbnail?.let { "'$it'" } ?: "NULL"},
                    track_count = $trackCount,
                    updated_at = CURRENT_TIMESTAMP
                RETURNING id;
            """.trimIndent()
            
            val result = executeQuery(sql)
            // Parse and return the playlist ID
            Result.success(1) // Simplified
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync playlist", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync listen history
     */
    suspend fun syncHistory(
        deviceId: String,
        songId: String,
        playedAt: Long,
        playDuration: Long,
        completedPlay: Boolean
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val sql = """
                INSERT INTO synced_history (device_id, song_id, played_at, play_duration, completed_play)
                VALUES ('$deviceId', '$songId', to_timestamp($playedAt / 1000.0), $playDuration, $completedPlay);
            """.trimIndent()
            
            executeQuery(sql)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync history", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get listening stats from cloud
     */
    suspend fun getListeningStats(deviceId: String): Result<ListeningStats> = withContext(Dispatchers.IO) {
        try {
            val sql = """
                SELECT 
                    COUNT(*) as total_plays,
                    SUM(play_duration) as total_duration,
                    COUNT(DISTINCT song_id) as unique_songs
                FROM synced_history 
                WHERE device_id = '$deviceId';
            """.trimIndent()
            
            val result = executeQuery(sql)
            // Parse stats
            Result.success(ListeningStats(0, 0, 0)) // Placeholder
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get stats", e)
            Result.failure(e)
        }
    }
    
    /**
     * Execute SQL query against Neon
     */
    private suspend fun executeQuery(sql: String): String {
        val response = httpClient.post(NEON_API_URL) {
            contentType(ContentType.Application.Json)
            basicAuth(NEON_USER, NEON_PASSWORD)
            setBody(json.encodeToString(NeonQueryRequest(sql)))
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("Query failed: ${response.status}")
        }
        
        return response.bodyAsText()
    }
    
    private inline fun <reified T> parseQueryResult(jsonResponse: String): List<T> {
        // Parse Neon response format
        return try {
            json.decodeFromString<NeonQueryResponse<T>>(jsonResponse).rows
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun String.escapeSql(): String {
        return this.replace("'", "''")
    }
}

@Serializable
data class NeonQueryRequest(
    val query: String
)

@Serializable
data class NeonQueryResponse<T>(
    val rows: List<T> = emptyList(),
    val rowCount: Int = 0
)

@Serializable
data class SyncedSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artwork_url: String? = null,
    val source: String = "unknown",
    val is_liked: Boolean = false,
    val play_count: Int = 0
)

@Serializable
data class ListeningStats(
    val totalPlays: Int,
    val totalDuration: Long,
    val uniqueSongs: Int
)
