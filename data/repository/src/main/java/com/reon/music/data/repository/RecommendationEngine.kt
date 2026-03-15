package com.reon.music.data.repository

import com.reon.music.core.model.Song
import com.reon.music.data.database.dao.HistoryDao
import com.reon.music.data.database.dao.SongDao
import com.reon.music.data.database.entities.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction for recommendation data access so we can
 * swap in a cloud-backed implementation later without
 * changing UI/ViewModel code.
 */
interface RecommendationDataSource {
    fun getQuickPicks(limit: Int = 20): Flow<List<Song>>
    fun getMostPlayed(limit: Int = 50): Flow<List<Song>>
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Song>>
}

/**
 * Local-first recommendation engine backed by listening history and song stats.
 *
 * This is intentionally simple for now and designed so that a future
 * cloud-backed implementation can plug in behind the same API.
 */
@Singleton
class RecommendationEngine @Inject constructor(
    private val historyDao: HistoryDao,
    private val songDao: SongDao
) : RecommendationDataSource {

    /**
     * Quick picks based on a mix of recent and most-played songs.
     */
    override fun getQuickPicks(limit: Int): Flow<List<Song>> {
        return songDao.getRecentlyPlayed(limit * 2)
            .map { entities ->
                entities
                    .sortedByDescending { it.lastPlayedAt ?: 0L }
                    .take(limit)
                    .map(SongEntity::toSong)
            }
    }

    /**
     * Most played songs on this device.
     */
    override fun getMostPlayed(limit: Int): Flow<List<Song>> {
        return songDao.getMostPlayed(limit)
            .map { entities -> entities.map(SongEntity::toSong) }
    }

    /**
     * Recently played unique songs using history + song table.
     */
    override fun getRecentlyPlayed(limit: Int): Flow<List<Song>> {
        return historyDao.getRecentlyPlayedSongs(limit)
            .map { entities -> entities.map(SongEntity::toSong) }
    }
}

