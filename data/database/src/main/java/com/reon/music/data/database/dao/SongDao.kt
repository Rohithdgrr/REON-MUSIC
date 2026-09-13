/*
 * REON Music App - Song DAO
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.database.dao

import androidx.room.*
import com.reon.music.data.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    
    // Insert/Update
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)
    
    @Update
    suspend fun update(song: SongEntity)
    
    @Delete
    suspend fun delete(song: SongEntity)
    
    // Query by ID
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?
    
    @Query("SELECT * FROM songs WHERE id = :id")
    fun getSongByIdFlow(id: String): Flow<SongEntity?>
    
    // Liked songs
    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY addedAt DESC")
    fun getLikedSongs(): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY addedAt DESC LIMIT :limit")
    suspend fun getLikedSongsList(limit: Int = 100): List<SongEntity>
    
    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY addedAt DESC")
    suspend fun getLikedSongsOnce(): List<SongEntity>
    
    @Query("UPDATE songs SET isLiked = :isLiked, needsSync = 1 WHERE id = :songId")
    suspend fun setLiked(songId: String, isLiked: Boolean)
    
    @Query("SELECT COUNT(*) FROM songs WHERE isLiked = 1")
    fun getLikedCount(): Flow<Int>
    
    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE id = :songId AND isLiked = 1)")
    suspend fun isLiked(songId: String): Boolean
    
    // Library songs
    @Query("SELECT * FROM songs WHERE inLibrary = 1 ORDER BY addedAt DESC")
    fun getLibrarySongs(): Flow<List<SongEntity>>
    
    @Query("UPDATE songs SET inLibrary = :inLibrary, needsSync = 1 WHERE id = :songId")
    suspend fun setInLibrary(songId: String, inLibrary: Boolean)
    
    // Downloaded songs
    @Query("SELECT * FROM songs WHERE downloadState = 2 ORDER BY addedAt DESC")
    fun getDownloadedSongs(): Flow<List<SongEntity>>
    
    @Query("UPDATE songs SET downloadState = :state, localPath = :path WHERE id = :songId")
    suspend fun updateDownloadState(songId: String, state: Int, path: String? = null)
    
    @Query("SELECT COUNT(*) FROM songs WHERE downloadState = 2")
    fun getDownloadedCount(): Flow<Int>
    
    // Most played
    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int = 50): Flow<List<SongEntity>>
    
    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, timestamp: Long = System.currentTimeMillis())
    
    // Recently played
    @Query("SELECT * FROM songs WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<SongEntity>>
    
    // Sync
    @Query("SELECT * FROM songs WHERE needsSync = 1")
    suspend fun getSongsNeedingSync(): List<SongEntity>
    
    @Query("UPDATE songs SET syncedAt = :timestamp, needsSync = 0 WHERE id = :songId")
    suspend fun markSynced(songId: String, timestamp: Long = System.currentTimeMillis())
    
    // Search
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' LIMIT :limit")
    suspend fun searchSongs(query: String, limit: Int = 50): List<SongEntity>
    
    // Stats
    @Query("SELECT SUM(playCount) FROM songs")
    fun getTotalPlayCount(): Flow<Int?>
    
    @Query("SELECT COUNT(*) FROM songs WHERE isLiked = 1 OR inLibrary = 1")
    fun getLibrarySize(): Flow<Int>
}
