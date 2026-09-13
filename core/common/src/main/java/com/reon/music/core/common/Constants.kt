/*
 * REON Music App - Constants
 * Copyright (c) 2024 REON
 * Clean-room implementation - No GPL code included
 */

package com.reon.music.core.common

/**
 * API Configuration Constants
 * Clean-room implementation based on publicly documented API patterns
 */
object ApiConstants {
    // JioSaavn API (clean-room implementation)
    const val JIOSAAVN_BASE_URL = "www.jiosaavn.com"
    const val JIOSAAVN_API_PATH = "/api.php"
    const val JIOSAAVN_API_PARAMS = "_format=json&_marker=0&api_version=4&ctx=web6dot0"
    
    // YouTube Music API (clean-room implementation)
    const val YOUTUBE_MUSIC_BASE_URL = "https://music.youtube.com/youtubei/v1/"
    
    // Piped API (fallback for streams)
    const val PIPED_API_BASE_URL = "https://pipedapi.kavin.rocks"
    
    // LrcLib (lyrics)
    const val LRCLIB_BASE_URL = "https://lrclib.net/api"
}

/**
 * Audio Quality Options
 */
enum class AudioQuality(val kbps: Int, val suffix: String) {
    LOW(96, "_96"),
    MEDIUM(160, "_160"),
    HIGH(320, "_320")
}

/**
 * Music Source Types
 */
enum class MusicSource {
    JIOSAAVN,
    YOUTUBE,
    LOCAL
}

/**
 * Cache Constants
 */
object CacheConstants {
    const val STREAM_CACHE_SIZE = 512L * 1024 * 1024  // 512 MB
    const val IMAGE_CACHE_SIZE = 100L * 1024 * 1024   // 100 MB
    const val URL_CACHE_EXPIRY_HOURS = 6L
}

/**
 * Current calendar year (device local time). Discovery queries qualify
 * with this instead of a hardcoded year so "latest / trending" searches
 * keep returning current music as years roll over.
 */
fun currentMusicYear(): Int =
    java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

/**
 * Qualifies a discovery query with the current year, e.g.
 * `yearQuery("trending songs")` -> `"trending songs 2026"`.
 * Pure function so it is unit-testable on the JVM.
 */
fun yearQuery(base: String): String = "$base ${currentMusicYear()}"
