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
        const val ANDROID_API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
        const val IOS_API_KEY = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yGCNg"
        const val BASE = "https://music.youtube.com/youtubei/v1"
        const val BASE_YT = "https://www.youtube.com/youtubei/v1"

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

    private suspend fun postJson(endpoint: String, body: Any, apiKey: String = API_KEY, base: String = BASE): JsonObject {
        val url = "$base/$endpoint?key=$apiKey&prettyPrint=false"
        var last: Exception? = null
        repeat(3) { attempt ->
            try {
                // Manually serialize to JSON string to ensure exact payload (Ktor's ContentNegotiation with explicitNulls=false was sending WebContext as toString)
                val jsonForBody = Json { explicitNulls = false; encodeDefaults = true; ignoreUnknownKeys = true }
                val jsonString = when (body) {
                    is WebContext -> jsonForBody.encodeToString(WebContext.serializer(), body)
                    else -> body.toString()
                }

                val res = http.post(url) {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Accept, "application/json")
                    header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
                    header("Origin", "https://music.youtube.com")
                    header("Referer", "https://music.youtube.com/")
                    setBody(jsonString)
                }
                val bodyText = res.bodyAsText()
                if (res.status == HttpStatusCode.TooManyRequests) {
                    throw UpstreamException("UPSTREAM_RATE_LIMITED", "YouTube rate-limited the request body=${bodyText.take(500)}")
                }
                if (res.status.value >= 500) {
                    throw UpstreamException("UPSTREAM_BAD_GATEWAY", "YouTube returned ${res.status.value} body=${bodyText.take(500)}")
                }
                if (res.status == HttpStatusCode.Forbidden || res.status == HttpStatusCode.Unauthorized) {
                    throw UpstreamException("UPSTREAM_AUTH", "YouTube returned ${res.status.value} body=${bodyText.take(500)}")
                }
                if (res.status.value != 200) {
                    throw UpstreamException("UPSTREAM_ERROR", "YouTube returned ${res.status.value} body=${bodyText.take(800)}")
                }
                return Json.parseToJsonElement(bodyText).jsonObject
            } catch (e: UpstreamException) {
                last = e
                if (e.code == "UPSTREAM_RATE_LIMITED") {
                    delay(1000L shl attempt)
                } else if (attempt >= 2) {
                    throw e
                } else {
                    delay(500L shl attempt)
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

    data class PlayerClientConfig(
        val clientName: String,
        val clientVersion: String,
        val apiKey: String,
        val base: String,
        val androidSdkVersion: Int? = null,
        val osName: String? = null,
        val osVersion: String? = null,
        val userAgent: String? = null,
    )

    /** Try multiple client configs to bypass PO token / LOGIN_REQUIRED. */
    suspend fun playerWithFallback(videoId: String): List<JsonObject> {
        val configs = listOf(
            PlayerClientConfig("ANDROID_MUSIC", androidVersion, ANDROID_MUSIC_API_KEY, BASE, 30, "Android", "11", "com.google.android.apps.youtube.music/$androidVersion (Linux; U; Android 11) gzip"),
            PlayerClientConfig("ANDROID", "20.10.38", ANDROID_API_KEY, BASE_YT, 30, "Android", "11", "com.google.android.youtube/20.10.38 (Linux; U; Android 11) gzip"),
            PlayerClientConfig("IOS", "20.10.38", IOS_API_KEY, BASE_YT, null, "iPhone", "17.5.1", "com.google.ios.youtube/20.10.38 (iPhone14,3; U; CPU iPhone OS 17_5_1 like Mac OS X; en_US)"),
            PlayerClientConfig("WEB_REMIX", webRemixVersion, API_KEY, BASE),
            PlayerClientConfig("WEB", "2.20250219.01.00", API_KEY, BASE_YT),
            PlayerClientConfig("MWEB", "2.20250219.01.00", API_KEY, BASE_YT),
            PlayerClientConfig("TVHTML5", "7.20240702.00.00", API_KEY, BASE_YT),
        )
        val results = mutableListOf<JsonObject>()
        for (cfg in configs) {
            try {
                val client = Client(
                    clientName = cfg.clientName,
                    clientVersion = cfg.clientVersion,
                    hl = "en",
                    gl = "US",
                    androidSdkVersion = cfg.androidSdkVersion,
                    osName = cfg.osName,
                    osVersion = cfg.osVersion,
                    userAgent = cfg.userAgent,
                )
                val body = WebContext(context = Ctx(client), videoId = videoId)
                val json = postJson("player", body, apiKey = cfg.apiKey, base = cfg.base)
                results.add(json)
                val (status, _) = Parsers.playability(json)
                val fmts = Parsers.rawFormats(json)
                if (status == "OK" && fmts.isNotEmpty()) {
                    // Found playable immediately; return early with this + previous
                    return results
                }
                // Don't throw on LOGIN_REQUIRED yet; try next client
            } catch (e: UpstreamException) {
                // Continue to next client unless rate limited
                if (e.code == "UPSTREAM_RATE_LIMITED") throw e
            } catch (_: Exception) { /* try next */ }
        }
        return results
    }
}
