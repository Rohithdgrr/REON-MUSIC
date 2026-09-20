package com.reon.innertube

import com.reon.cache.CacheType
import com.reon.cache.ReonCache
import kotlinx.serialization.json.Json

/**
 * High-level API: cache-aside orchestration over [InnerTubeClient] + [Parsers].
 * Throws [UpstreamException]; routes map it to HTTP.
 */
class MusicService(
    private val tube: InnerTubeClient = InnerTubeClient(),
    private val cache: ReonCache = ReonCache("v1"),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private suspend fun cached(type: CacheType, id: String, load: suspend () -> String): String {
        cache.get(type, id)?.let { return it }
        val fresh = load()
        cache.put(type, id, fresh)
        return fresh
    }

    private fun encode(v: Any): String = when (v) {
        is SearchResponse -> json.encodeToString(SearchResponse.serializer(), v)
        is HomeResponse -> json.encodeToString(HomeResponse.serializer(), v)
        is AlbumDetails -> json.encodeToString(AlbumDetails.serializer(), v)
        is ArtistDetails -> json.encodeToString(ArtistDetails.serializer(), v)
        is PlaylistDetails -> json.encodeToString(PlaylistDetails.serializer(), v)
        is RadioResponse -> json.encodeToString(RadioResponse.serializer(), v)
        is StreamResponse -> json.encodeToString(StreamResponse.serializer(), v)
        is LyricsResponse -> json.encodeToString(LyricsResponse.serializer(), v)
        is PlayerResponse -> json.encodeToString(PlayerResponse.serializer(), v)
        is SuggestionsResponse -> json.encodeToString(SuggestionsResponse.serializer(), v)
        else -> error("no serializer")
    }

    suspend fun searchJson(q: String, filter: String?): String = cached(CacheType.SEARCH, "$q|${filter ?: "all"}") {
        encode(Parsers.search(tube.search(q, filter)))
    }

    suspend fun suggestionsJson(q: String): String = cached(CacheType.SUGGEST, q) {
        encode(SuggestionsResponse(Parsers.suggestions(tube.suggestions(q))))
    }

    suspend fun homeJson(): String = cached(CacheType.HOME, "home") {
        encode(Parsers.home(tube.browse("FEmusic_home")))
    }

    suspend fun albumJson(id: String): String = cached(CacheType.ALBUM, id) {
        encode(Parsers.album(id, tube.browse(id)))
    }

    suspend fun artistJson(id: String): String = cached(CacheType.ARTIST, id) {
        val details = Parsers.artist(id, tube.browse(id))
        if (details.artist.name.isEmpty()) throw UpstreamException("NOT_FOUND", "artist $id not found")
        encode(details)
    }

    suspend fun playlistJson(id: String): String {
        val browseId = if (id.startsWith("VL")) id else "VL$id"
        return cached(CacheType.PLAYLIST, browseId) {
            val details = Parsers.playlist(browseId, tube.browse(browseId))
            if (details.playlist.title.isEmpty()) throw UpstreamException("NOT_FOUND", "playlist $id not found")
            encode(details)
        }
    }

    suspend fun radioJson(trackId: String): String = cached(CacheType.RADIO, trackId) {
        encode(Parsers.radio(trackId, tube.next(trackId, "RDAMVM$trackId")))
    }

    suspend fun streamJson(trackId: String, quality: String): String = cached(CacheType.STREAM, "$trackId|$quality") {
        // Try multiple clients (ANDROID_MUSIC, ANDROID, IOS, WEB, etc.) to bypass LOGIN_REQUIRED/PO token
        val playerResponses = tube.playerWithFallback(trackId)
        if (playerResponses.isEmpty()) throw UpstreamException("NO_STREAM", "no player response for $trackId")

        // Collect playable formats from all successful clients
        val allFormats = playerResponses.flatMap { Parsers.rawFormats(it) }.distinctBy { it.url }

        val picked = if (allFormats.isNotEmpty()) {
            Parsers.pickFormat(allFormats, quality) ?: Parsers.pickFormat(allFormats, "high")
        } else null

        if (picked != null) {
            encode(
                StreamResponse(
                    url = picked.url,
                    codec = Parsers.codecOf(picked.mime),
                    bitrate = picked.bitrate,
                    expires_at = Parsers.expireOf(picked.url),
                    quality = quality,
                ),
            )
        } else {
            // Check ciphered-only on any client
            val hasCiphered = playerResponses.any { Parsers.hasCipheredOnly(it) }
            if (hasCiphered) {
                throw UpstreamException("STREAM_CIPHERED", "stream requires signature decipher (Phase 4)")
            }
            // Check LOGIN_REQUIRED / UNPLAYABLE
            // If all are LOGIN_REQUIRED, surface as auth
            val anyOk = playerResponses.any { Parsers.playability(it).first == "OK" }
            if (!anyOk) {
                val firstAuth = playerResponses.firstOrNull { Parsers.playability(it).first == "LOGIN_REQUIRED" }
                if (firstAuth != null) {
                    val (_, reason) = Parsers.playability(firstAuth)
                    throw UpstreamException("UPSTREAM_AUTH", reason.ifEmpty { "login required" })
                }
                val firstUnplayable = playerResponses.firstOrNull { Parsers.playability(it).first != "OK" && Parsers.playability(it).first != "UNKNOWN" }
                if (firstUnplayable != null) {
                    val (status, reason) = Parsers.playability(firstUnplayable)
                    throw UpstreamException("UNPLAYABLE", reason.ifEmpty { "status=$status" })
                }
            }
            throw UpstreamException("NO_STREAM", "no playable audio format for $trackId")
        }
    }

    suspend fun playerJson(trackId: String): String = cached(CacheType.PLAYER, trackId) {
        val responses = tube.playerWithFallback(trackId)
        val player = responses.firstOrNull { Parsers.playability(it).first == "OK" } ?: responses.firstOrNull() ?: tryPlayer(trackId)
        val (title, artist, durationMs) = Parsers.videoMeta(player)
        val best = responses.flatMap { Parsers.rawFormats(it) }.let { Parsers.pickFormat(it, "high") } ?: Parsers.pickFormat(Parsers.rawFormats(player), "high")
        encode(
            PlayerResponse(
                trackId = trackId,
                title = title,
                artist = artist,
                durationMs = durationMs,
                codec = best?.let { Parsers.codecOf(it.mime) } ?: "",
                bitrate = best?.bitrate ?: 0,
                artUrl = "",
            ),
        )
    }

    suspend fun lyricsJson(trackId: String): String = cached(CacheType.LYRICS, trackId) {
        val browseId = Parsers.lyricsBrowseId(tube.next(trackId))
            ?: throw UpstreamException("LYRICS_NOT_FOUND", "no lyrics for $trackId")
        val text = Parsers.lyricsText(tube.browse(browseId))
            ?: throw UpstreamException("LYRICS_NOT_FOUND", "no lyrics for $trackId")
        encode(LyricsResponse(trackId = trackId, text = text, synced = false))
    }

    /** ANDROID_MUSIC first, WEB_REMIX fallback. Enforces playability OK. */
    private suspend fun tryPlayer(trackId: String): kotlinx.serialization.json.JsonObject {
        val android = try { tube.player(trackId, useAndroid = true) } catch (e: UpstreamException) { null }
        if (android != null && Parsers.playability(android).first == "OK" && Parsers.rawFormats(android).isNotEmpty()) return android
        val web = try { tube.player(trackId, useAndroid = false) } catch (e: UpstreamException) { null }
        if (web != null) {
            val (status, reason) = Parsers.playability(web)
            if (status == "LOGIN_REQUIRED") throw UpstreamException("UPSTREAM_AUTH", reason.ifEmpty { "login required" })
            if (status == "OK" && Parsers.rawFormats(web).isNotEmpty()) return web
            if (status != "OK" && status != "UNKNOWN") throw UpstreamException("UNPLAYABLE", reason.ifEmpty { "status=$status" })
            if (Parsers.rawFormats(web).isNotEmpty()) return web
        }
        // Return whichever we have, caller will inspect cipher/no-stream
        return android ?: web ?: throw UpstreamException("UNPLAYABLE", "both players failed for $trackId")
    }
}
