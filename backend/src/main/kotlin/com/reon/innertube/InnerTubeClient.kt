package com.reon.innertube

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.coroutines.delay

class UpstreamException(val code: String, override val message: String) : Exception(message)

/**
 * Low-level InnerTube HTTP. Anonymous-only. Throws [UpstreamException] on
 * transport/upstream failures; parsing never throws (see [Parsers]).
 */
class InnerTubeClient(
    val webRemixVersion: String = System.getenv("YT_WEB_VERSION") ?: "1.20260707.12.00",
    val androidVersion: String = System.getenv("YT_ANDROID_VERSION") ?: "7.27.52",
    private val http: HttpClient = defaultHttp(),
) {
    companion object {
        const val API_KEY = "AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
        const val ANDROID_MUSIC_API_KEY = "AIzaSyAOghZGza2MQSZkY_zfZ370N-PUdXEo8AI"
        const val BASE = "https://music.youtube.com/youtubei/v1"

        // ytmusicapi search filters (stable for years).
        val FILTER_PARAMS = mapOf(
            "songs" to "EgWKAQIIAWoQEAMQBBAJEAoQBRAg",
            "albums" to "EgWKAQIYAWoQEAMQBBAJEAoQBRAg",
            "artists" to "EgWKAQIgAWoQEAMQBBAJEAoQBRAg",
            "playlists" to "EgWKAQIoAWoQEAMQBBAJEAoQBRAg",
            "videos" to "EgWKAQIQAWoQEAMQBBAJEAoQBRAg",
        )

        fun defaultHttp(): HttpClient = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 8_000
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        }
    }

    @Serializable
    data class WebContext(
        val context: Ctx = Ctx(),
        val query: String? = null,
        val params: String? = null,
        val browseId: String? = null,
        val videoId: String? = null,
        val playlistId: String? = null,
        val input: String? = null,
    )

    @Serializable
    data class Ctx(val client: Client = Client())

    @Serializable
    data class Client(
        val clientName: String = "WEB_REMIX",
        val clientVersion: String = "1.20260707.12.00",
        val hl: String = "en",
        val gl: String = "US",
        val androidSdkVersion: Int? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val userAgent: String? = null,
    )

    private fun webBody(
        query: String? = null,
        params: String? = null,
        browseId: String? = null,
        videoId: String? = null,
        playlistId: String? = null,
        input: String? = null,
    ): WebContext = WebContext(
        context = Ctx(Client("WEB_REMIX", webRemixVersion)),
        query = query,
        params = params,
        browseId = browseId,
        videoId = videoId,
        playlistId = playlistId,
        input = input,
    )

    private suspend fun postJson(endpoint: String, body: Any, apiKey: String = API_KEY): JsonObject {
        val url = "$BASE/$endpoint?key=$apiKey&prettyPrint=false"
        var last: Exception? = null
        repeat(3) { attempt ->
            try {
                val res = http.post(url) {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Accept, "application/json")
                    setBody(body)
                }
                if (res.status == HttpStatusCode.TooManyRequests) {
                    throw UpstreamException("UPSTREAM_RATE_LIMITED", "YouTube rate-limited the request")
                }
                if (res.status.value >= 500) {
                    throw UpstreamException("UPSTREAM_BAD_GATEWAY", "YouTube returned ${res.status.value}")
                }
                if (res.status.value != 200) {
                    throw UpstreamException("UPSTREAM_ERROR", "YouTube returned ${res.status.value}")
                }
                return Json.parseToJsonElement(res.bodyAsText()).jsonObject
            } catch (e: UpstreamException) {
                last = e
                if (e.code == "UPSTREAM_RATE_LIMITED") {
                    delay(1000L shl attempt)
                } else if (attempt >= 2) {
                    throw e
                }
            } catch (e: Exception) {
                last = e
                if (attempt >= 2) throw UpstreamException("UPSTREAM_UNREACHABLE", e.message ?: "network error")
                delay(500L shl attempt)
            }
        }
        throw UpstreamException("UPSTREAM_UNREACHABLE", last?.message ?: "network error")
    }

    suspend fun search(query: String, filter: String? = null): JsonObject {
        val params = filter?.lowercase()?.let { FILTER_PARAMS[it] }
        return postJson("search", webBody(query = query, params = params))
    }

    suspend fun suggestions(input: String): JsonObject =
        postJson("music/get_search_suggestions", webBody(input = input))

    suspend fun browse(browseId: String): JsonObject =
        postJson("browse", webBody(browseId = browseId))

    suspend fun next(videoId: String, playlistId: String? = null): JsonObject =
        postJson("next", webBody(videoId = videoId, playlistId = playlistId))

    /** Player via ANDROID_MUSIC first (often direct URLs), caller falls back to WEB_REMIX. */
    suspend fun player(videoId: String, useAndroid: Boolean = true): JsonObject {
        val body = if (useAndroid) {
            WebContext(
                context = Ctx(Client(
                    clientName = "ANDROID_MUSIC",
                    clientVersion = androidVersion,
                    androidSdkVersion = 30,
                    osName = "Android",
                    osVersion = "11",
                    userAgent = "com.google.android.apps.youtube.music/$androidVersion (Linux; U; Android 11) gzip",
                )),
                videoId = videoId,
            )
        } else {
            webBody(videoId = videoId)
        }
        return postJson("player", body, apiKey = if (useAndroid) ANDROID_MUSIC_API_KEY else API_KEY)
    }
}
