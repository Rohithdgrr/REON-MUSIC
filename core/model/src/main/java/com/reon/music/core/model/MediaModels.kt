/*
 * REON Music App - Media Models
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a song/track
 * Clean-room design - not copied from any GPL source
 */
@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val albumId: String? = null,
    val duration: Int = 0, // in seconds
    val artworkUrl: String? = null,
    val streamUrl: String? = null,
    val source: String = "unknown", // "jiosaavn", "youtube", "local"
    val hasLyrics: Boolean = false,
    val language: String = "",
    val year: String = "",
    val releaseDate: String = "",
    val is320kbps: Boolean = false,
    val permaUrl: String? = null,
    val genre: String = "", // Genre category (Pop, Rock, Classical, etc.)
    val description: String = "", // Song description/notes
    val type: String = "", // Type of song (Single, Album Track, etc.)
    val extras: Map<String, String> = emptyMap(),
    // YouTube metadata
    val viewCount: Long = 0L, // Video view count
    val likeCount: Long = 0L, // Video like count
    val channelName: String = "", // YouTube channel name
    val channelId: String = "", // YouTube channel ID
    val channelSubscriberCount: Long = 0L, // Channel subscriber count
    val quality: String = "", // Video quality (HD, 4K, etc.)
    val uploadDate: String = "", // Video upload date
    // Movie/Film metadata
    val movieName: String = "", // Movie name if song is from a film
    val movieGenre: String = "", // Movie genre (Action, Romance, etc.)
    val heroName: String = "", // Male lead actor/hero name
    val heroineName: String = "", // Female lead actor/actress/heroine name
    val director: String = "", // Movie director name
    val producer: String = "" // Movie producer name
) {
    /**
     * Get high quality artwork URL
     * Returns the highest quality version of the thumbnail URL
     * For YouTube: uses maxresdefault.jpg (1920x1080) or sddefault.jpg (640x480) as fallback
     */
    fun getHighQualityArtwork(): String? {
        if (artworkUrl.isNullOrBlank()) return null
        
        // Validate URL
        if (!artworkUrl.startsWith("http://") && !artworkUrl.startsWith("https://")) {
            return null
        }
        
        // Handle YouTube URLs (ytimg.com or googleusercontent.com)
        if (artworkUrl.contains("ytimg.com") || artworkUrl.contains("googleusercontent.com") || 
            artworkUrl.contains("ggpht.com")) {
            
            // If already maxresdefault, return as-is
            if (artworkUrl.contains("maxresdefault")) {
                return artworkUrl.replace("http:", "https:")
            }
            
            // If it's a standard YouTube thumbnail URL with quality suffix,
            // upgrade to maxresdefault. Specific variants first: the generic
            // "default.jpg" replacement would otherwise corrupt "hqdefault"
            // into "hqmaxresdefault" (or double-upgrade an already upgraded
            // "maxresdefault" into "maxresmaxresdefault") — both 404.
            if (artworkUrl.contains("default.jpg")) {
                // Upgrade to maxresdefault for best quality
                // Note: Not all videos have maxresdefault, the UI should handle 404s with fallback
                val upgraded = artworkUrl
                    .replace("hqdefault.jpg", "maxresdefault.jpg")
                    .replace("mqdefault.jpg", "maxresdefault.jpg")
                    .replace("sddefault.jpg", "maxresdefault.jpg")
                return if (upgraded != artworkUrl) {
                    upgraded.replace("http:", "https:")
                } else {
                    artworkUrl
                        .replace("default.jpg", "maxresdefault.jpg")
                        .replace("http:", "https:")
                }
            }
            
            // Handle googleusercontent URLs with dynamic sizing (w60-h60, w120-h120, etc.)
            if (artworkUrl.contains(Regex("w\\d+-h\\d+"))) {
                // Request high resolution version
                return artworkUrl
                    .replace(Regex("w\\d+-h\\d+"), "w1200-h1200")
                    .replace("=w\\d+", "=w1200")
                    .replace("http:", "https:")
            }
        }
        
        // For JioSaavn and other providers - upgrade low-res URLs
        var optimizedUrl = artworkUrl
            .replace("http:", "https:")
            .replace("50x50", "500x500")
            .replace("150x150", "500x500")
            .replace("/50/", "/500/")
            .replace("/150/", "/500/")
        
        return optimizedUrl
    }
    
    /**
     * Get medium quality artwork URL (good balance of quality and load speed)
     * For YouTube: uses hqdefault.jpg (480x360) which is more reliably available than maxresdefault
     */
    fun getMediumQualityArtwork(): String? {
        val highQuality = getHighQualityArtwork() ?: return null
        
        // If it's a YouTube maxresdefault URL, provide a medium quality alternative
        if (highQuality.contains("maxresdefault")) {
            return highQuality.replace("maxresdefault.jpg", "hqdefault.jpg")
        }
        
        return highQuality
    }
    
    /**
     * Get stream URL with quality variant
     */
    fun getStreamUrlWithQuality(quality: String = "_320"): String? {
        return streamUrl?.let { url ->
            when {
                url.contains("_96") -> url.replace("_96", quality)
                url.contains("_160") -> url.replace("_160", quality)
                url.contains("_320") -> url.replace("_320", quality)
                else -> url
            }
        }
    }
    
    /**
     * Format duration as MM:SS
     */
    fun formattedDuration(): String {
        val minutes = duration / 60
        val seconds = duration % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

/**
 * Represents an album
 */
@Serializable
data class Album(
    val id: String,
    val name: String,
    val artist: String,
    val artworkUrl: String? = null,
    val year: String = "",
    val songCount: Int = 0,
    val songs: List<Song> = emptyList(),
    val permaUrl: String? = null,
    val language: String = "",
    val description: String = "", // Album description
    val genre: String = "" // Album genre
)

/**
 * Represents an artist
 */
@Serializable
data class Artist(
    val id: String,
    val name: String,
    val artworkUrl: String? = null,
    val followerCount: Int = 0,
    val topSongs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val permaUrl: String? = null,
    val description: String = "", // Artist bio
    val genres: List<String> = emptyList() // Artist genres
)

/**
 * Represents a playlist
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val artworkUrl: String? = null,
    val songCount: Int = 0,
    val songs: List<Song> = emptyList(),
    val isLocal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val permaUrl: String? = null
)

/**
 * Lyrics data
 */
@Serializable
data class Lyrics(
    val plainLyrics: String? = null,
    val syncedLyrics: String? = null
) {
    fun getSyncedLines(): List<LyricLine> {
        if (syncedLyrics == null) return emptyList()
        
        val regex = """\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""".toRegex()
        return syncedLyrics.lines().mapNotNull { line ->
            regex.find(line)?.let { match ->
                val (min, sec, ms) = match.destructured
                val timestamp = min.toLong() * 60000 + 
                               sec.toLong() * 1000 + 
                               ms.padEnd(3, '0').toLong()
                LyricLine(timestamp, match.groupValues[4].trim())
            }
        }
    }
}

/**
 * Single lyric line with timestamp
 */
@Serializable
data class LyricLine(
    val timestamp: Long, // in milliseconds
    val text: String
)

/**
 * Search result container
 */
@Serializable
data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)
