/*
 * REON Music App - History DAO
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.database.dao

import androidx.room.*
import com.reon.music.data.database.entities.HistoryWithSong
import com.reon.music.data.database.entities.ListenHistoryEntity
import com.reon.music.data.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: ListenHistoryEntity): Long
    
    @Delete
    suspend fun delete(history: ListenHistoryEntity)
    
    @Query("DELETE FROM listen_history")
    suspend fun clearAll()
    
    @Query("DELETE FROM listen_history WHERE playedAt < :olderThan AND completedPlay = 1")
    suspend fun deleteOldCompleted(olderThan: Long): Int
    
    @Query("DELETE FROM listen_history WHERE playedAt < :olderThan")
    suspend fun deleteOlderThan(olderThan: Long): Int
    
    @Query("DELETE FROM listen_history WHERE songId = :songId")
    suspend fun deleteBySongId(songId: String)
    
    // Get recent history
    @Query("SELECT * FROM listen_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 100): Flow<List<ListenHistoryEntity>>
    
    // Get with song details - needs @Transaction for @Relation to work
    @Transaction
    @Query("SELECT * FROM listen_history ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentWithSongs(limit: Int = 50): Flow<List<HistoryWithSong>>
    
    // Get unique recently played songs
    @Query("""
        SELECT s.* FROM songs s
        WHERE s.id IN (
            SELECT DISTINCT songId FROM listen_history 
            ORDER BY playedAt DESC 
            LIMIT :limit
        )
    """)
    fun getRecentlyPlayedSongs(limit: Int = 50): Flow<List<SongEntity>>
    
    // Get history by date range
    @Query("""
        SELECT * FROM listen_history 
        WHERE playedAt >= :startTime AND playedAt <= :endTime
        ORDER BY playedAt DESC
    """)
    suspend fun getHistoryBetween(startTime: Long, endTime: Long): List<ListenHistoryEntity>
    
    // Get play count for a song
    @Query("SELECT COUNT(*) FROM listen_history WHERE songId = :songId")
    suspend fun getPlayCountForSong(songId: String): Int
    
    // Get total listening time
    @Query("SELECT SUM(playDuration) FROM listen_history")
    fun getTotalListeningTime(): Flow<Long?>
    
    // Get listening time by date
    @Query("""
        SELECT SUM(playDuration) FROM listen_history
        WHERE playedAt >= :startTime AND playedAt <= :endTime
    """)
    suspend fun getListeningTimeBetween(startTime: Long, endTime: Long): Long?
    
    // Get most played songs from history - simplified query
    @Query("""
        SELECT s.* FROM songs s
        WHERE s.id IN (
            SELECT songId FROM listen_history
            GROUP BY songId
            ORDER BY COUNT(*) DESC
            LIMIT :limit
        )
    """)
    fun getMostPlayedFromHistory(limit: Int = 50): Flow<List<SongEntity>>
    
    // Get completed plays only
    @Query("SELECT COUNT(*) FROM listen_history WHERE completedPlay = 1")
    fun getCompletedPlaysCount(): Flow<Int>
    
    // Sync
    @Query("SELECT * FROM listen_history WHERE needsSync = 1")
    suspend fun getHistoryNeedingSync(): List<ListenHistoryEntity>
    
    @Query("UPDATE listen_history SET syncedAt = :timestamp, needsSync = 0 WHERE id = :historyId")
    suspend fun markSynced(historyId: Long, timestamp: Long = System.currentTimeMillis())
    
    // Total songs played today
    @Query("""
        SELECT COUNT(DISTINCT songId) FROM listen_history
        WHERE playedAt >= :startOfDay
    """)
    suspend fun getSongsPlayedToday(startOfDay: Long): Int
}
