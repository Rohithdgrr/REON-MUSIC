/*
 * REON Music App - History Entity
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.data.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Listening history entity
 */
@Entity(
    tableName = "listen_history",
    indices = [
        Index(value = ["songId"]),
        Index(value = ["playedAt"]),
        Index(value = ["source"]),
        Index(value = ["songId", "playedAt"]),
        Index(value = ["playedAt", "source"])
    ]
)
data class ListenHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val playDuration: Long = 0, // How long they listened in ms
    val completedPlay: Boolean = false, // Did they finish the song?
    val source: String = "unknown", // "search", "playlist", "radio", "queue", "home"
    val syncedAt: Long? = null,
    val needsSync: Boolean = true
)

/**
 * History with embedded song details using Room relations
 */
data class HistoryWithSong(
    @Embedded
    val history: ListenHistoryEntity,
    
    @Relation(
        parentColumn = "songId",
        entityColumn = "id"
    )
    val song: SongEntity?
)
