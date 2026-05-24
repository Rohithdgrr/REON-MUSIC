package com.reon.music.data.network

import android.util.Log
import com.reon.music.core.model.Song
import com.reon.music.data.network.jiosaavn.JioSaavnClient
import com.reon.music.data.network.youtube.PipedClient
import com.reon.music.data.network.youtube.YouTubeMusicClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamResolver @Inject constructor(
    private val httpClient: HttpClient,
    private val jiosaavnClient: JioSaavnClient,
    private val youtubeMusicClient: YouTubeMusicClient,
    private val pipedClient: PipedClient
) {
    companion object {
        private const val TAG = "StreamResolver"
    }

    enum class AdaptiveQuality { LOW, MEDIUM, HIGH }

    suspend fun resolveStreamUrl(song: Song, quality: AdaptiveQuality = AdaptiveQuality.HIGH): String? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Resolving stream for: ${song.title} (${song.source}) quality=$quality")

        val existingUrl = song.streamUrl
        if (!existingUrl.isNullOrBlank()) {
            return@withContext applyQuality(existingUrl, quality)
        }

        return@withContext when (song.source) {
            "youtube" -> resolveYouTubeUrl(song, quality)
            "local" -> song.streamUrl
            else -> resolveYouTubeUrl(song, quality)
        }
    }

    private fun applyQuality(url: String, quality: AdaptiveQuality): String {
        val suffix = when (quality) {
            AdaptiveQuality.LOW -> "_96"
            AdaptiveQuality.MEDIUM -> "_160"
            AdaptiveQuality.HIGH -> "_320"
        }
        return url
            .replace("_96", suffix)
            .replace("_160", suffix)
            .replace("_320", suffix)
    }

    private suspend fun resolveYouTubeUrl(song: Song, quality: AdaptiveQuality): String? {
        val videoId = song.id

        pipedClient.getStreamUrl(videoId).getOrNull()?.let { return it }

        youtubeMusicClient.getStreamUrl(videoId).getOrNull()?.let { return it }

        val altInstances = listOf(
            "https://api.piped.privacydev.net",
            "https://pipedapi.in.projectsegfau.lt",
            "https://pipedapi.adminforge.de",
            "https://piped-api.hostux.net",
            "https://api.piped.yt"
        )
        for (instance in altInstances) {
            try {
                val response: HttpResponse = httpClient.get("$instance/streams/$videoId")
                if (response.status.isSuccess()) {
                    val json = Json.parseToJsonElement(response.bodyAsText())
                    val audioStreams = json.jsonObject["audioStreams"]?.jsonArray
                    val best = audioStreams?.filter {
                        it.jsonObject["mimeType"]?.jsonPrimitive?.content?.contains("audio") == true
                    }?.maxByOrNull { it.jsonObject["bitrate"]?.jsonPrimitive?.int ?: 0 }
                    best?.jsonObject?.get("url")?.jsonPrimitive?.content?.let { return it }
                }
            } catch (_: Exception) { }
        }

        return null
    }
}
