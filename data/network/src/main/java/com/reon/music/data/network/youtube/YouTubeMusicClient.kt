/*
 * REON Music App - YouTube Music (InnerTube) API Client
 * Copyright (c) 2024 REON
 * 
 * CLEAN-ROOM IMPLEMENTATION
 * This code is independently written based on publicly observable API behavior.
 * No GPL-licensed code has been copied. All implementation logic is original.
 */

package com.reon.music.data.network.youtube

import com.reon.music.core.common.Result
import com.reon.music.core.common.safeApiCall
import com.reon.music.core.model.Song
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Video metadata for YouTube videos
 */
data class VideoMetadata(
    val videoId: String,
    val viewCount: Long = 0L,
    val likeCount: Long = 0L,
    val channelName: String = "",
    val channelId: String = "",
    val uploadDate: String = "",
    val description: String = "",
    val composer: String? = null,
    val lyricist: String? = null,
    val producer: String? = null,
    val musicLabel: String? = null,
    val movieName: String? = null,
    val releaseYear: Int? = null
)

/**
 * YouTube Music API Client using InnerTube
 * Clean-room implementation - independently written
 */
@Singleton
class YouTubeMusicClient @Inject constructor(
    private val httpClient: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val INNERTUBE_API_URL = "https://music.youtube.com/youtubei/v1"
        private const val API_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
        
        private val CLIENT_CONTEXT = buildJsonObject {
            put("client", buildJsonObject {
                put("clientName", "WEB_REMIX")
                put("clientVersion", "1.20231204.01.00")
                put("hl", "en")
                put("gl", "IN")
                put("experimentIds", JsonArray(emptyList()))
                put("experimentsToken", "")
                put("browserName", "Chrome")
                put("browserVersion", "120.0.0.0")
                put("osName", "Windows")
                put("osVersion", "10.0")
                put("platform", "DESKTOP")
            })
            put("user", buildJsonObject {
                put("lockedSafetyMode", false)
            })
        }
        
        private val ANDROID_CLIENT_CONTEXT = buildJsonObject {
            put("client", buildJsonObject {
                put("clientName", "ANDROID_MUSIC")
                put("clientVersion", "6.42.52")
                put("androidSdkVersion", 30)
                put("hl", "en")
                put("gl", "IN")
            })
        }
        
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
    
    /**
     * Search for songs on YouTube Music with pagination
     */
    suspend fun searchSongs(query: String, continuation: String? = null): Result<List<Song>> = safeApiCall {
        val requestBody = buildJsonObject {
            put("query", query)
            put("context", CLIENT_CONTEXT)
            put("params", "EgWKAQIIAWoKEAMQBBAKEAkQBQ%3D%3D") // Filter for songs
            if (continuation != null) {
                put("continuation", continuation)
            }
        }
        
        val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/search?key=$API_KEY") {
            contentType(ContentType.Application.Json)
            header("User-Agent", USER_AGENT)
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(requestBody.toString())
        }
        
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        parseSearchResults(responseJson)
    }
    
    /**
     * Search for songs prioritizing Top 500 Indian Music Channels
     * Performs multiple searches with priority channel names for better results
     */
    suspend fun searchSongsWithPriorityChannels(
        query: String, 
        limit: Int = 50
    ): Result<List<Song>> = safeApiCall {
        val allSongs = mutableListOf<Song>()
        
        // Base search
        val baseResult = searchSongs(query)
        if (baseResult is Result.Success) {
            allSongs.addAll(baseResult.data)
        }
        
        // Search with top priority channels (Top 20)
        IndianMusicChannels.TOP_100_CHANNELS.take(20).forEach { channel ->
            if (allSongs.size >= limit) return@safeApiCall allSongs.distinctBy { it.id }
            
            try {
                val channelQuery = "${channel.name} $query"
                val result = searchSongs(channelQuery)
                if (result is Result.Success) {
                    allSongs.addAll(result.data)
                }
            } catch (e: Exception) {
                // Continue with next channel
            }
        }
        
        // Remove duplicates and return
        allSongs.distinctBy { it.id }.take(limit)
    }
    
    /**
     * Search for songs from a specific Top 500 channel
     */
    suspend fun searchSongsFromChannel(
        channelName: String,
        query: String = "",
        limit: Int = 30
    ): Result<List<Song>> = safeApiCall {
        val searchQuery = if (query.isBlank()) {
            "$channelName official songs"
        } else {
            "$channelName $query"
        }
        
        val result = searchSongs(searchQuery)
        if (result is Result.Success) {
            // Filter results to prioritize songs from the specified channel
            val songs = result.data
            val channelSongs = songs.filter { song ->
                song.channelName.contains(channelName, ignoreCase = true) ||
                song.artist.contains(channelName, ignoreCase = true)
            }
            // Return channel songs first, then other results
            (channelSongs + songs).distinctBy { it.id }.take(limit)
        } else {
            emptyList()
        }
    }
    
    /**
     * Get Top 500 Indian Music Channels list for reference
     */
    fun getTopIndianMusicChannels(limit: Int = 100): List<MusicChannel> {
        return IndianMusicChannels.TOP_100_CHANNELS.take(limit)
    }
    
    /**
     * Check if a channel is in the Top 500 Indian Music Channels
     */
    fun isTopIndianMusicChannel(channelName: String): Boolean {
        return IndianMusicChannels.isPriorityChannel(channelName)
    }
    
    /**
     * Get channel priority score for ranking
     */
    fun getChannelPriorityScore(channelName: String): Double {
        return IndianMusicChannels.getChannelPriorityBoost(channelName)
    }
    
    /**
     * Get video metadata (views, likes, channel info)
     */
    suspend fun getVideoMetadata(videoId: String): Result<VideoMetadata> = safeApiCall {
        val response: HttpResponse = httpClient.get("https://www.youtube.com/watch?v=$videoId") {
            header("User-Agent", USER_AGENT)
        }
        
        val html = response.bodyAsText()
        parseVideoMetadata(html, videoId)
    }
    
    /**
     * Parse video metadata from HTML
     */
    private fun parseVideoMetadata(html: String, videoId: String): VideoMetadata {
        // Extract view count
        val viewCountRegex = """\"viewCount\":\"(\d+)\"""".toRegex()
        val viewCount = viewCountRegex.find(html)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        
        // Extract like count
        val likeCountRegex = """\"likeCount\":(\d+)""".toRegex()
        val likeCount = likeCountRegex.find(html)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        
        // Extract channel name
        val channelNameRegex = """\"channelName\":\"([^\"]+)\"""".toRegex()
        val channelName = channelNameRegex.find(html)?.groupValues?.get(1) ?: ""
        
        // Extract channel ID
        val channelIdRegex = """\"channelId\":\"([^\"]+)\"""".toRegex()
        val channelId = channelIdRegex.find(html)?.groupValues?.get(1) ?: ""
        
        // Extract upload date
        val uploadDateRegex = """\"uploadDate\":\"([^\"]+)\"""".toRegex()
        val uploadDate = uploadDateRegex.find(html)?.groupValues?.get(1) ?: ""
        
        // Extract description for metadata parsing
        val descriptionRegex = """\"shortDescription\":\{\"simpleText\":\"([^\"]+)\"""".toRegex()
        val description = descriptionRegex.find(html)?.groupValues?.get(1) ?: ""
        
        // Parse metadata from description
        val metadata = parseMetadataFromDescription(description)
        
        return VideoMetadata(
            videoId = videoId,
            viewCount = viewCount,
            likeCount = likeCount,
            channelName = channelName,
            channelId = channelId,
            uploadDate = uploadDate,
            description = description,
            composer = metadata["composer"],
            lyricist = metadata["lyricist"],
            producer = metadata["producer"],
            musicLabel = metadata["label"],
            movieName = metadata["movie"],
            releaseYear = metadata["year"]?.toIntOrNull()
        )
    }
    
    /**
     * Parse metadata from video description
     * Extracts: Composer, Lyricist, Producer, Music Label, Movie Name, Year
     */
    private fun parseMetadataFromDescription(description: String): Map<String, String> {
        if (description.isBlank()) return emptyMap()
        
        val metadata = mutableMapOf<String, String>()
        
        // Patterns for Indian music context (Bollywood, Regional)
        val patterns = mapOf(
            "composer" to listOf(
                Regex("(?:Music|Composer|Composed by)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE),
                Regex("Music by[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "lyricist" to listOf(
                Regex("(?:Lyrics|Lyricist|Lyrics by)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "producer" to listOf(
                Regex("(?:Producer|Produced by)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "label" to listOf(
                Regex("(?:Label|©)[:\\s]*(.+?(?:Records|Music|Entertainment))", RegexOption.IGNORE_CASE),
                Regex("©.*?([A-Z][a-zA-Z ]+(?:Records|Music|Entertainment))", RegexOption.IGNORE_CASE)
            ),
            "singer" to listOf(
                Regex("(?:Singer|Vocals|Voice)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "movie" to listOf(
                Regex("(?:Movie|Film)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "album" to listOf(
                Regex("(?:Album)[:\\s]+(.+?)(?:\\n|$)", RegexOption.IGNORE_CASE)
            ),
            "year" to listOf(
                Regex("(?:Released|Release Date|Year)[:\\s]+(\\d{4})", RegexOption.IGNORE_CASE)
            )
        )
        
        patterns.forEach { (key, regexList) ->
            for (regex in regexList) {
                regex.find(description)?.let { match ->
                    metadata[key] = match.groupValues[1].trim()
                    return@forEach // Found match, move to next key
                }
            }
        }
        
        return metadata
    }
    
    /**
     * Search with unlimited results using pagination
     */
    fun searchSongsUnlimited(query: String, maxResults: Int = 1000): Flow<List<Song>> = flow {
        var continuation: String? = null
        var totalFetched = 0
        val allSongs = mutableListOf<Song>()
        
        while (totalFetched < maxResults) {
            val result = searchSongs(query, continuation)
            when (result) {
                is Result.Success -> {
                    val songs = result.data
                    allSongs.addAll(songs)
                    totalFetched += songs.size
                    emit(allSongs.toList())
                    
                    // Try to get continuation token for next page
                    continuation = extractContinuationToken(result.data)
                    if (continuation == null || songs.isEmpty()) break
                }
                is Result.Error -> {
                    break
                }
                is Result.Loading -> continue
            }
        }
    }
    
    private fun extractContinuationToken(songs: List<Song>): String? {
        // This would need to be extracted from the API response
        // For now, return null to indicate no more pages
        // In a full implementation, this would parse the continuation token from the JSON response
        return null
    }
    
    /**
     * Get stream URL for a video ID
     */
    suspend fun getStreamUrl(videoId: String): Result<String?> = safeApiCall {
        android.util.Log.d("YouTubeMusicClient", "Getting stream URL for video: $videoId")
        
        // Calculate current signature timestamp
        val signatureTimestamp = (System.currentTimeMillis() / 1000 / 86400).toInt()
        
        var audioUrl: String? = null
        
        // Try Android Music client first (best for audio)
        try {
            android.util.Log.d("YouTubeMusicClient", "  Trying ANDROID_MUSIC client...")
            val androidRequestBody = buildJsonObject {
                put("videoId", videoId)
                put("context", buildJsonObject {
                    put("client", buildJsonObject {
                        put("clientName", "ANDROID_MUSIC")
                        put("clientVersion", "7.02.52")
                        put("androidSdkVersion", 34)
                        put("hl", "en")
                        put("gl", "US")
                        put("utcOffsetMinutes", 330)
                    })
                })
                put("playbackContext", buildJsonObject {
                    put("contentPlaybackContext", buildJsonObject {
                        put("signatureTimestamp", signatureTimestamp)
                    })
                })
                put("racyCheckOk", true)
                put("contentCheckOk", true)
            }
            
            val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/player?key=$API_KEY") {
                contentType(ContentType.Application.Json)
                header("User-Agent", "com.google.android.apps.youtube.music/7.02.52 (Linux; U; Android 14; en_US) gzip")
                header("X-Youtube-Client-Name", "21")
                header("X-Youtube-Client-Version", "7.02.52")
                header("Accept-Language", "en-US,en;q=0.9")
                setBody(androidRequestBody.toString())
            }
            
            val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            audioUrl = extractAudioUrl(responseJson)
            
            if (audioUrl != null) {
                android.util.Log.d("YouTubeMusicClient", "  ✓ ANDROID_MUSIC client success!")
            } else {
                android.util.Log.w("YouTubeMusicClient", "  ANDROID_MUSIC client returned no URL")
            }
        } catch (e: Exception) {
            android.util.Log.e("YouTubeMusicClient", "  ANDROID_MUSIC client error: ${e.message}", e)
        }
        
        // If Android didn't work, try iOS Music client
        if (audioUrl == null) {
            try {
                android.util.Log.d("YouTubeMusicClient", "  Trying IOS_MUSIC client...")
                val iosRequestBody = buildJsonObject {
                    put("videoId", videoId)
                    put("context", buildJsonObject {
                        put("client", buildJsonObject {
                            put("clientName", "IOS_MUSIC")
                            put("clientVersion", "7.02")
                            put("deviceModel", "iPhone16,2")
                            put("osVersion", "18.0.0.22A3351")
                            put("hl", "en")
                            put("gl", "US")
                            put("utcOffsetMinutes", 330)
                        })
                    })
                    put("playbackContext", buildJsonObject {
                        put("contentPlaybackContext", buildJsonObject {
                            put("signatureTimestamp", signatureTimestamp)
                        })
                    })
                    put("racyCheckOk", true)
                    put("contentCheckOk", true)
                }
                
                val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/player?key=$API_KEY") {
                    contentType(ContentType.Application.Json)
                    header("User-Agent", "com.google.ios.youtube.music/7.02 (iPhone16,2; U; CPU iOS 18_0 like Mac OS X; en_US)")
                    header("X-Youtube-Client-Name", "26")
                    header("X-Youtube-Client-Version", "7.02")
                    header("Accept-Language", "en-US,en;q=0.9")
                    setBody(iosRequestBody.toString())
                }
                
                val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                audioUrl = extractAudioUrl(responseJson)
                
                if (audioUrl != null) {
                    android.util.Log.d("YouTubeMusicClient", "  ✓ IOS_MUSIC client success!")
                } else {
                    android.util.Log.w("YouTubeMusicClient", "  IOS_MUSIC client returned no URL")
                }
            } catch (e: Exception) {
                android.util.Log.e("YouTubeMusicClient", "  IOS_MUSIC client error: ${e.message}", e)
            }
        }
        
        // Try WEB client as last resort
        if (audioUrl == null) {
            try {
                android.util.Log.d("YouTubeMusicClient", "  Trying WEB client as fallback...")
                val webRequestBody = buildJsonObject {
                    put("videoId", videoId)
                    put("context", CLIENT_CONTEXT)
                    put("playbackContext", buildJsonObject {
                        put("contentPlaybackContext", buildJsonObject {
                            put("signatureTimestamp", signatureTimestamp)
                        })
                    })
                    put("racyCheckOk", true)
                    put("contentCheckOk", true)
                }
                
                val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/player?key=$API_KEY") {
                    contentType(ContentType.Application.Json)
                    header("User-Agent", USER_AGENT)
                    header("Origin", "https://music.youtube.com")
                    header("Referer", "https://music.youtube.com/")
                    setBody(webRequestBody.toString())
                }
                
                val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                audioUrl = extractAudioUrl(responseJson)
                
                if (audioUrl != null) {
                    android.util.Log.d("YouTubeMusicClient", "  ✓ WEB client success!")
                } else {
                    android.util.Log.w("YouTubeMusicClient", "  WEB client returned no URL")
                }
            } catch (e: Exception) {
                android.util.Log.e("YouTubeMusicClient", "  WEB client error: ${e.message}", e)
            }
        }
        
        if (audioUrl == null) {
            android.util.Log.e("YouTubeMusicClient", "✗ All clients failed for video: $videoId")
        }
        
        audioUrl
    }
    
    /**
     * Get song details
     */
    suspend fun getSongDetails(videoId: String): Result<Song?> = safeApiCall {
        val requestBody = buildJsonObject {
            put("videoId", videoId)
            put("context", CLIENT_CONTEXT)
        }
        
        val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/player?key=$API_KEY") {
            contentType(ContentType.Application.Json)
            header("User-Agent", USER_AGENT)
            header("Origin", "https://music.youtube.com")
            setBody(requestBody.toString())
        }
        
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        parseSongDetails(responseJson, videoId)
    }
    
    /**
     * Get enhanced song details with metadata from next endpoint
     * Provides richer information including album, related content, etc.
     */
    suspend fun getEnhancedSongDetails(videoId: String): Result<Song?> = safeApiCall {
        // Get basic song details from player endpoint
        val baseSong = getSongDetails(videoId).getOrNull() ?: return@safeApiCall null
        
        // Get additional metadata from next endpoint
        val nextRequestBody = buildJsonObject {
            put("videoId", videoId)
            put("context", CLIENT_CONTEXT)
            put("isAudioOnly", true)
            put("enablePersistentPlaylistPanel", true)
        }
        
        val nextResponse: HttpResponse = httpClient.post("$INNERTUBE_API_URL/next?key=$API_KEY") {
            contentType(ContentType.Application.Json)
            header("User-Agent", USER_AGENT)
            header("Origin", "https://music.youtube.com")
            setBody(nextRequestBody.toString())
        }
        
        val nextJson = Json.parseToJsonElement(nextResponse.bodyAsText()).jsonObject
        
        // Parse additional metadata from next response
        val enhancedMetadata = parseNextMetadata(nextJson)
        
        // Merge metadata
        baseSong.copy(
            album = enhancedMetadata["album"] ?: baseSong.album,
            artist = enhancedMetadata["artist"] ?: baseSong.artist,
            year = enhancedMetadata["year"] ?: baseSong.year,
            description = (enhancedMetadata["description"] ?: baseSong.description).ifEmpty { baseSong.description },
            extras = baseSong.extras + enhancedMetadata.filterKeys { 
                it !in setOf("album", "artist", "year", "description") 
            }
        )
    }
    
    /**
     * Parse additional metadata from next endpoint response
     */
    private fun parseNextMetadata(json: JsonObject): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        try {
            // Try to extract description from structured metadata
            val tabs = json["contents"]?.jsonObject
                ?.get("singleColumnMusicWatchNextResultsRenderer")?.jsonObject
                ?.get("tabbedRenderer")?.jsonObject
                ?.get("watchNextTabbedResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray
            
            tabs?.forEach { tab ->
                val tabContent = tab.jsonObject["tabRenderer"]?.jsonObject
                    ?.get("content")?.jsonObject
                    ?.get("sectionListRenderer")?.jsonObject
                    ?.get("contents")?.jsonArray
                
                tabContent?.forEach { section ->
                    // Look for description shelf
                    val descShelf = section.jsonObject["musicDescriptionShelfRenderer"]?.jsonObject
                    val description = descShelf?.get("description")?.jsonObject
                        ?.get("runs")?.jsonArray?.joinToString("") {
                            it.jsonObject["text"]?.jsonPrimitive?.content ?: ""
                        }
                    
                    if (!description.isNullOrBlank()) {
                        metadata["description"] = description
                        // Parse metadata from description
                        val parsedMeta = parseMetadataFromDescription(description)
                        metadata.putAll(parsedMeta)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        
        return metadata
    }
    
    /**
     * Get next/related songs
     */
    suspend fun getRelatedSongs(videoId: String, playlistId: String? = null): Result<List<Song>> = safeApiCall {
        val requestBody = buildJsonObject {
            put("videoId", videoId)
            put("context", CLIENT_CONTEXT)
            put("isAudioOnly", true)
            put("tunerSettingValue", "AUTOMIX_SETTING_NORMAL")
            if (playlistId != null) {
                put("playlistId", playlistId)
            }
        }
        
        val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/next?key=$API_KEY") {
            contentType(ContentType.Application.Json)
            header("User-Agent", USER_AGENT)
            header("Origin", "https://music.youtube.com")
            setBody(requestBody.toString())
        }
        
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        parseRelatedSongs(responseJson)
    }
    
    // --- Parsing helpers ---
    
    private fun parseSearchResults(json: JsonObject): List<Song> {
        val songs = mutableListOf<Song>()
        
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("tabbedSearchResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("tabRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray
            
            contents?.forEach { section ->
                val musicShelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
                val items = musicShelf?.get("contents")?.jsonArray
                
                items?.forEach { item ->
                    val musicItem = item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject
                    parseMusicItem(musicItem)?.let { songs.add(it) }
                }
            }
        } catch (e: Exception) {
            // Fallback - try alternate parsing
        }
        
        return songs
    }
    
    private fun parseMusicItem(item: JsonObject?): Song? {
        if (item == null) return null
        
        return try {
            val playItem = item["overlay"]?.jsonObject
                ?.get("musicItemThumbnailOverlayRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("musicPlayButtonRenderer")?.jsonObject
            
            val videoId = playItem?.get("playNavigationEndpoint")?.jsonObject
                ?.get("watchEndpoint")?.jsonObject
                ?.get("videoId")?.jsonPrimitive?.content ?: return null
            
            val flexColumns = item["flexColumns"]?.jsonArray
            
            val title = flexColumns?.getOrNull(0)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "Unknown"
            
            val artistRuns = flexColumns?.getOrNull(1)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray
            
            val artist = artistRuns?.mapNotNull { 
                it.jsonObject["text"]?.jsonPrimitive?.content 
            }?.firstOrNull { it != " • " && it != " & " } ?: "Unknown Artist"
            
            // Extract channel name (usually in artist field for YouTube)
            val channelName = artist
            
            val thumbnail = item["thumbnail"]?.jsonObject
                ?.get("musicThumbnailRenderer")?.jsonObject
                ?.get("thumbnail")?.jsonObject
                ?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.content
            
            val durationText = flexColumns?.lastOrNull()?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
            
            // Try to extract view count and additional metadata from subtitle
            val subtitle = flexColumns?.getOrNull(2)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: ""
            
            // Parse view count from subtitle (e.g., "1.2M views")
            val viewCount = parseViewCount(subtitle)
            
            // Try to extract album/movie name from second column
            val albumOrMovie = flexColumns?.getOrNull(1)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.getOrNull(2)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
            
            // Extract year if present
            val yearMatch = Regex("\\d{4}").find(subtitle)
            val year = yearMatch?.value ?: ""
            
            Song(
                id = videoId,
                title = title,
                artist = artist,
                album = albumOrMovie ?: "",
                artworkUrl = validateThumbnailUrl(thumbnail)?.replace("w60-h60", "w500-h500"),
                duration = parseDuration(durationText),
                source = "youtube",
                channelName = channelName,
                viewCount = viewCount,
                year = year
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseViewCount(text: String): Long {
        return try {
            val regex = """([\d.]+)([KMB]?)""".toRegex()
            val match = regex.find(text) ?: return 0L
            val number = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val multiplier = when (match.groupValues[2]) {
                "K" -> 1000L
                "M" -> 1000000L
                "B" -> 1000000000L
                else -> 1L
            }
            (number * multiplier).toLong()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun parseSongDetails(json: JsonObject, videoId: String): Song? {
        return try {
            val videoDetails = json["videoDetails"]?.jsonObject ?: return null
            
            val title = videoDetails["title"]?.jsonPrimitive?.content ?: "Unknown"
            val author = videoDetails["author"]?.jsonPrimitive?.content ?: "Unknown Artist"
            val lengthSeconds = videoDetails["lengthSeconds"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val channelId = videoDetails["channelId"]?.jsonPrimitive?.content ?: ""
            val viewCount = videoDetails["viewCount"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
            
            val thumbnail = videoDetails["thumbnail"]?.jsonObject
                ?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.content
            
            // Extract description for metadata
            val description = videoDetails["shortDescription"]?.jsonPrimitive?.content ?: ""
            val metadata = parseMetadataFromDescription(description)
            
Song(
                id = videoId,
                title = title,
                artist = author,
                album = metadata["album"] ?: metadata["movie"] ?: "",
                artworkUrl = validateThumbnailUrl(thumbnail),
                duration = lengthSeconds,
                source = "youtube",
                channelName = author,
                channelId = channelId,
                viewCount = viewCount,
                description = description,
                year = metadata["year"] ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Audio quality mode for streaming
     */
    enum class AudioQualityMode {
        LOW,      // ~48-96 kbps - Maximum data savings
        MEDIUM,   // ~128-160 kbps - Balanced
        HIGH      // ~256-320 kbps - Best quality
    }
    
    // Default quality mode (can be changed via settings)
    var currentQualityMode: AudioQualityMode = AudioQualityMode.MEDIUM
    
    private fun extractAudioUrl(json: JsonObject, quality: AudioQualityMode = currentQualityMode): String? {
        return try {
            val streamingData = json["streamingData"]?.jsonObject
            if (streamingData == null) return null
            
            // Try adaptive formats first (audio only)
            val adaptiveFormats = streamingData["adaptiveFormats"]?.jsonArray
            
            // Filter to audio-only formats
            val audioFormats = adaptiveFormats?.filter { format ->
                val mimeType = format.jsonObject["mimeType"]?.jsonPrimitive?.content ?: ""
                mimeType.contains("audio")
            }?.sortedByDescending { format ->
                format.jsonObject["bitrate"]?.jsonPrimitive?.long ?: 0L
            }
            
            // Select format based on quality preference
            val targetBitrate = when (quality) {
                AudioQualityMode.LOW -> 96_000L     // ~96 kbps
                AudioQualityMode.MEDIUM -> 160_000L // ~160 kbps
                AudioQualityMode.HIGH -> 320_000L   // ~320 kbps
            }
            
            // Find format closest to target bitrate (prefer lower to save data)
            val audioFormat = audioFormats?.minByOrNull { format ->
                val bitrate = format.jsonObject["bitrate"]?.jsonPrimitive?.long ?: Long.MAX_VALUE
                kotlin.math.abs(bitrate - targetBitrate)
            } ?: audioFormats?.firstOrNull()
            
            var url = audioFormat?.jsonObject?.get("url")?.jsonPrimitive?.content
            
            // Check for signatureCipher (encrypted URL that needs decoding)
            // If signatureCipher is present, we can't use it directly - return null to trigger Piped fallback
            val signatureCipher = audioFormat?.jsonObject?.get("signatureCipher")?.jsonPrimitive?.content
            if (url == null && signatureCipher != null) {
                // YouTube is using encrypted URLs - we'll need Piped to handle this
                return null
            }
            
            // If no direct URL found in adaptive formats, try regular formats
            if (url == null) {
                val formats = streamingData["formats"]?.jsonArray
                val fallbackFormat = formats?.filter { format ->
                    val mimeType = format.jsonObject["mimeType"]?.jsonPrimitive?.content ?: ""
                    mimeType.contains("audio") || mimeType.contains("mp4")
                }?.minByOrNull { format ->
                    val bitrate = format.jsonObject["bitrate"]?.jsonPrimitive?.long ?: Long.MAX_VALUE
                    kotlin.math.abs(bitrate - targetBitrate)
                }
                url = fallbackFormat?.jsonObject?.get("url")?.jsonPrimitive?.content
                
                // Check for signatureCipher in regular formats too
                val fallbackCipher = fallbackFormat?.jsonObject?.get("signatureCipher")?.jsonPrimitive?.content
                if (url == null && fallbackCipher != null) {
                    return null
                }
            }
            
            // Check for HLS or DASH if still no URL
            if (url == null) {
                url = streamingData["hlsManifestUrl"]?.jsonPrimitive?.content
            }
            
            url
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseRelatedSongs(json: JsonObject): List<Song> {
        val songs = mutableListOf<Song>()
        
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("singleColumnMusicWatchNextResultsRenderer")?.jsonObject
                ?.get("tabbedRenderer")?.jsonObject
                ?.get("watchNextTabbedResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("tabRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("musicQueueRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("playlistPanelRenderer")?.jsonObject
                ?.get("contents")?.jsonArray
            
            contents?.forEach { item ->
                val renderer = item.jsonObject["playlistPanelVideoRenderer"]?.jsonObject
                parsePlaylistPanelItem(renderer)?.let { songs.add(it) }
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
        
        return songs
    }
    
    private fun parsePlaylistPanelItem(item: JsonObject?): Song? {
        if (item == null) return null
        
        return try {
            val videoId = item["videoId"]?.jsonPrimitive?.content ?: return null
            val title = item["title"]?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "Unknown"
            
            val artist = item["shortBylineText"]?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "Unknown Artist"
            
            val thumbnail = item["thumbnail"]?.jsonObject
                ?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.content
            
            val durationText = item["lengthText"]?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
            
            Song(
                id = videoId,
                title = title,
                artist = artist,
                artworkUrl = validateThumbnailUrl(thumbnail)?.replace("w60-h60", "w500-h500"),
                duration = parseDuration(durationText),
                source = "youtube"
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDuration(durationText: String?): Int {
        if (durationText == null) return 0
        val parts = durationText.split(":")
        return when (parts.size) {
            2 -> parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0) ?: 0
            3 -> parts[0].toIntOrNull()?.times(3600)
                ?.plus(parts[1].toIntOrNull()?.times(60) ?: 0)
                ?.plus(parts[2].toIntOrNull() ?: 0) ?: 0
            else -> 0
        }
    }
    
    // ===== Sync Methods (stubs - require authentication in production) =====
    
    /**
     * Get user's liked songs
     * Note: Requires authentication in production
     */
    suspend fun getLikedSongs(): Result<List<Song>> = safeApiCall {
        // In production, would use authenticated request
        emptyList()
    }
    
    /**
     * Get user's playlists
     * Note: Requires authentication in production
     */
    suspend fun getUserPlaylists(): Result<List<com.reon.music.core.model.Playlist>> = safeApiCall {
        // In production, would use authenticated request
        emptyList()
    }
    
    /**
     * Get songs from a playlist
     */
    suspend fun getPlaylistSongs(playlistId: String): Result<List<Song>> = safeApiCall {
        // In production, would parse playlist contents
        emptyList()
    }
    
    /**
     * Like a song
     * Note: Requires authentication in production
     */
    suspend fun likeSong(videoId: String): Result<Boolean> = safeApiCall {
        // In production, would send like request
        true
    }
    
    /**
     * Create a playlist
     * Note: Requires authentication in production
     */
    suspend fun createPlaylist(title: String, description: String): Result<String?> = safeApiCall {
        // In production, would create playlist and return ID
        null
    }
    
    /**
     * Add song to playlist
     * Note: Requires authentication in production
     */
    suspend fun addToPlaylist(playlistId: String, videoId: String): Result<Boolean> = safeApiCall {
        // In production, would add to playlist
        true
    }
    
    /**
     * Search for playlists on YouTube Music
     */
    suspend fun searchPlaylists(query: String, page: Int = 1, limit: Int = 20): Result<List<com.reon.music.core.model.Playlist>> = safeApiCall {
        val requestBody = buildJsonObject {
            put("query", query)
            put("context", CLIENT_CONTEXT)
            put("params", "EgWKAQIoAWoKEAkQBRAKEAoQBQ%3D%3D") // Filter for playlists
        }
        
        val response: HttpResponse = httpClient.post("$INNERTUBE_API_URL/search?key=$API_KEY") {
            contentType(ContentType.Application.Json)
            header("User-Agent", USER_AGENT)
            header("Origin", "https://music.youtube.com")
            header("Referer", "https://music.youtube.com/")
            setBody(requestBody.toString())
        }
        
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        parsePlaylistSearchResults(responseJson)
    }
    
    /**
     * Parse playlist search results
     */
    private fun parsePlaylistSearchResults(json: JsonObject): List<com.reon.music.core.model.Playlist> {
        val playlists = mutableListOf<com.reon.music.core.model.Playlist>()
        
        try {
            val contents = json["contents"]?.jsonObject
                ?.get("tabbedSearchResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("tabRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject
                ?.get("contents")?.jsonArray
            
            contents?.forEach { section ->
                val musicShelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
                val items = musicShelf?.get("contents")?.jsonArray
                
                items?.forEach { item ->
                    val playlistItem = item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject
                    parsePlaylistItem(playlistItem)?.let { playlists.add(it) }
                }
            }
        } catch (e: Exception) {
            // Fallback parsing
        }
        
        return playlists
    }
    
    private fun parsePlaylistItem(item: JsonObject?): com.reon.music.core.model.Playlist? {
        if (item == null) return null
        
        return try {
            val navigation = item["navigationEndpoint"]?.jsonObject
                ?.get("watchPlaylistEndpoint")?.jsonObject
            
            val playlistId = navigation?.get("playlistId")?.jsonPrimitive?.content ?: return null
            
            val flexColumns = item["flexColumns"]?.jsonArray
            
            val title = flexColumns?.getOrNull(0)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: "Unknown Playlist"
            
            val thumbnail = item["thumbnail"]?.jsonObject
                ?.get("musicThumbnailRenderer")?.jsonObject
                ?.get("thumbnail")?.jsonObject
                ?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.content
            
            // Try to get song count
            val subtitle = flexColumns?.getOrNull(1)?.jsonObject
                ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                ?.get("text")?.jsonObject
                ?.get("runs")?.jsonArray?.firstOrNull()?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: ""
            
            val songCount = subtitle.filter { it.isDigit() }.toIntOrNull() ?: 0
            
com.reon.music.core.model.Playlist(
                id = playlistId,
                name = title,
                artworkUrl = validateThumbnailUrl(thumbnail),
                songCount = songCount,
                description = subtitle
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * General search returning mixed results
     */
    suspend fun search(query: String): Result<com.reon.music.core.model.SearchResult> = safeApiCall {
        val songsResult = searchSongs(query)
        val playlistsResult = searchPlaylists(query, 1, 10)
        val songs = if (songsResult is Result.Success) songsResult.data else emptyList()
        val playlists = if (playlistsResult is Result.Success) playlistsResult.data else emptyList()
        com.reon.music.core.model.SearchResult(
            songs = songs,
albums = emptyList(),
            artists = emptyList(),
            playlists = playlists
        )
    }

    private fun validateThumbnailUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return if (url.startsWith("http://") || url.startsWith("https://")) url else null
    }
}

