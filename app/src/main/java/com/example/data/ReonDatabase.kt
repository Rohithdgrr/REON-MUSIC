package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val category: String,
    val durationMs: Long,
    val albumArtUrl: String,
    val artistImageUrl: String,
    val source: String,
    val quality: String,
    val spatialMode: String,
    val codec: String,
    val sampleRate: String,
    val monthlyListeners: String,
    val lyricsQuote: String,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val rank: String = "",
    val plays: String = "",
    val badge: String = "",
    val artSeed: Int = 1
)

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val genre: String,
    val isFollowing: Boolean = false,
    val artSeed: Int = 1
)

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val year: String = "2024",
    val trackCount: String = "12 tracks",
    val genre: String = "Electronic",
    val artSeed: Int = 1
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val trackCount: String,
    val duration: String,
    val artSeed: Int = 1
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: String,
    val isRead: Boolean = false,
    val badge: String = "NEW",
    val actionText: String = "Play Now",
    val trackId: String? = null,
    val albumId: String? = null,
    val iconType: String = "music"
)

@Dao
interface ReonDao {
    @Query("SELECT * FROM tracks")
    fun getAllTracksFlow(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :id")
    suspend fun updateTrackLike(id: String, isLiked: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateTrackDownload(id: String, isDownloaded: Boolean)

    @Query("SELECT * FROM artists")
    fun getAllArtistsFlow(): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Query("UPDATE artists SET isFollowing = :isFollowing WHERE id = :id")
    suspend fun updateArtistFollowing(id: String, isFollowing: Boolean)

    @Query("SELECT * FROM albums")
    fun getAllAlbumsFlow(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

@Database(
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ReonDatabase : RoomDatabase() {
    abstract fun reonDao(): ReonDao

    companion object {
        @Volatile
        private var INSTANCE: ReonDatabase? = null

        fun getDatabase(context: Context): ReonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReonDatabase::class.java,
                    "reon_music_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
