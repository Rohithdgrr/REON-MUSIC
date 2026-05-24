/*
 * REON Music App - Database Module
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.reon.music.data.database.ReonDatabase
import com.reon.music.data.database.dao.HistoryDao
import com.reon.music.data.database.dao.LyricsDao
import com.reon.music.data.database.dao.PlaylistDao
import com.reon.music.data.database.dao.SongDao
import com.reon.music.data.database.dao.YouTubeStreamCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ReonDatabase {
        return Room.databaseBuilder(
            context,
            ReonDatabase::class.java,
            ReonDatabase.DATABASE_NAME
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideSongDao(database: ReonDatabase): SongDao {
        return database.songDao()
    }
    
    @Provides
    fun providePlaylistDao(database: ReonDatabase): PlaylistDao {
        return database.playlistDao()
    }
    
    @Provides
    fun provideHistoryDao(database: ReonDatabase): HistoryDao {
        return database.historyDao()
    }
    
    @Provides
    fun provideLyricsDao(database: ReonDatabase): LyricsDao {
        return database.lyricsDao()
    }
    
    @Provides
    fun provideYouTubeStreamCacheDao(database: ReonDatabase): YouTubeStreamCacheDao {
        return database.youtubeStreamCacheDao()
    }
}
