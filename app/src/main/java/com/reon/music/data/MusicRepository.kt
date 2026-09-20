package com.reon.music.data

import com.reon.music.data.remote.AlbumDetails
import com.reon.music.data.remote.ArtistDetails
import com.reon.music.data.remote.HomeResponse
import com.reon.music.data.remote.LyricsResponse
import com.reon.music.data.remote.PlayerResponse
import com.reon.music.data.remote.PlaylistDetails
import com.reon.music.data.remote.RadioResponse
import com.reon.music.data.remote.ReonBackendApi
import com.reon.music.data.remote.SearchResponse
import com.reon.music.data.remote.StreamResponse
import com.reon.music.data.remote.SuggestionsResponse
import com.reon.music.data.remote.TrackDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Phase 2 — Single Source of Truth: Backend → Room → UI.
 *
 * - Tries backend first, falls back to Room on IOException (offline).
 * - Write-through: successful backend catalog results are cached as YT_STREAM entities for offline fallback.
 * - Stream: re-resolves when now > streamExpiresAt - 60s.
 * - Labeling enforcement: YT_STREAM tracks are forced to Opus/AAC (never FLAC/Hi-Res).
 */
class MusicRepository(
    private val api: ReonBackendApi,
    private val dao: ReonDao,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) {
    val repositoryDao: ReonDao get() = dao

    // Fallback for emulator networking: try 10.0.2.2, 127.0.0.1 (adb reverse), and host LAN IPs
    private val fallbackApis: List<ReonBackendApi> by lazy {
        val primary = ReonBackendApi.resolveBaseUrl()
        val candidates = mutableListOf<String>()
        // Primary is already tried, so fallbacks are the others
        val allBases = listOf(
            "http://10.0.2.2:8080",
            "http://127.0.0.1:8080",
            "http://10.76.209.211:8080",
            "http://172.16.0.2:8080",
            "http://172.27.128.1:8080"
        )
        for (base in allBases) {
            if (!primary.contains(base.substringAfter("://").substringBefore(":")) && base != primary) {
                candidates.add(base)
            }
        }
        // Also add the opposite of primary as first fallback
        val firstFallback = if (primary.contains("10.0.2.2")) "http://127.0.0.1:8080" else "http://10.0.2.2:8080"
        val ordered = (listOf(firstFallback) + candidates).distinct().filter { it != primary }
        ordered.map { ReonBackendApi.create(baseUrl = it, apiKey = ReonBackendApi.resolveApiKey()) }
    }

    private fun isEmulatorNetworkError(e: IOException): Boolean {
        val msg = e.message ?: ""
        return msg.contains("Failed to connect", true) ||
            msg.contains("unexpected end of stream", true) ||
            msg.contains("Unable to resolve host", true) ||
            msg.contains("Software caused connection abort", true) ||
            msg.contains("Connection reset", true) ||
            msg.contains("Connection refused", true)
    }

    private suspend fun <T> withFallback(block: suspend (ReonBackendApi) -> T): T {
        try {
            return block(api)
        } catch (e: IOException) {
            if (isEmulatorNetworkError(e)) {
                for (fallback in fallbackApis) {
                    try {
                        return block(fallback)
                    } catch (fe: IOException) {
                        if (!isEmulatorNetworkError(fe)) throw fe
                        // try next fallback
                    }
                }
            }
            throw e
        }
    }

    // ---- helpers: labeling enforcement ----

    private fun enforceYtCodec(raw: String?): String = when {
        raw?.contains("opus", true) == true -> "Opus"
        raw?.contains("aac", true) == true || raw?.contains("mp4a", true) == true -> "AAC"
        raw?.contains("vorbis", true) == true -> "Vorbis"
        else -> "Opus"
    }

    private fun ytQualityLabel(codec: String, bitrate: Int): String = when {
        codec == "Opus" && bitrate >= 150_000 -> "Opus · 160kbps"
        codec == "Opus" -> "Opus · 128kbps"
        codec == "AAC" && bitrate >= 150_000 -> "AAC · 128kbps"
        codec == "AAC" -> "AAC · 128kbps"
        else -> "$codec · 128kbps"
    }

    private fun ytBadge(codec: String): String = codec // never "FLAC" / "Hi-Res" / "96kHz"

    /** TrackDto (from backend) → TrackEntity persisted as YT_STREAM with enforced labels. */
    fun TrackDto.toYtEntity(): TrackEntity {
        val codec = enforceYtCodec(null) // DTO has no codec; default to Opus — stream will refine
        return TrackEntity(
            id = videoId.takeIf { it.isNotBlank() } ?: id,
            title = title,
            artist = artist,
            album = album,
            category = "YT Stream",
            durationMs = durationMs,
            albumArtUrl = artUrl,
            artistImageUrl = "",
            source = "YT Stream",
            quality = ytQualityLabel(codec, 160_000),
            spatialMode = "Stereo",
            codec = codec,
            sampleRate = "48kHz",
            monthlyListeners = "",
            lyricsQuote = "",
            isLiked = false,
            isDownloaded = false,
            rank = "",
            plays = "",
            badge = ytBadge(codec),
            artSeed = (id.hashCode().and(0x7fffffff) % 900) + 100,
            videoId = videoId.takeIf { it.isNotBlank() } ?: id,
            sourceKind = SourceKind.YT_STREAM,
            streamUrl = null,
            streamExpiresAt = 0L,
        )
    }

    private suspend fun persistSearchResult(response: SearchResponse) = withContext(io) {
        val toInsert = response.tracks.map { it.toYtEntity() }
        if (toInsert.isNotEmpty()) {
            dao.insertTracks(toInsert)
        }
        val artists = response.artists.map { dto ->
            ArtistEntity(id = dto.id, name = dto.name, genre = dto.genre, artSeed = (dto.id.hashCode().and(0x7fffffff) % 900) + 200)
        }
        if (artists.isNotEmpty()) dao.insertArtists(artists)
        val albums = response.albums.map { dto ->
            AlbumEntity(id = dto.id, title = dto.title, artist = dto.artist, year = dto.year, trackCount = dto.trackCount, genre = "YouTube", artSeed = (dto.id.hashCode().and(0x7fffffff) % 900) + 300)
        }
        if (albums.isNotEmpty()) dao.insertAlbums(albums)
        val playlists = response.playlists.map { dto ->
            PlaylistEntity(id = dto.id, title = dto.title, subtitle = dto.subtitle, trackCount = dto.trackCount, duration = "", artSeed = (dto.id.hashCode().and(0x7fffffff) % 900) + 400)
        }
        if (playlists.isNotEmpty()) dao.insertPlaylists(playlists)
    }

    // ---- fallback builders (offline) ----

    private fun TrackEntity.toTrackDto(): TrackDto = TrackDto(
        id = id,
        title = title,
        artist = artist,
        album = album,
        duration = "${(durationMs / 60000)}:${String.format("%02d", (durationMs % 60000) / 1000)}",
        durationMs = durationMs,
        artUrl = albumArtUrl,
        videoId = videoId,
        badge = badge,
    )

    // Full fallback that includes artists/albums/playlists by querying Room tables directly
    private suspend fun fallbackSearchFull(query: String, filter: String?): SearchResponse = withContext(io) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@withContext SearchResponse()
        val allTracks = dao.getAllTracks()
        val matchedTracks = allTracks.filter {
            it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
        }.map { it.toTrackDto() }

        // Load artists/albums/playlists via collecting first value from Flow
        val artistsFiltered: List<com.reon.music.data.remote.ArtistDto> = try {
            val allArtists = dao.getAllArtistsFlow().first()
            allArtists.filter { it.name.lowercase().contains(q) || it.genre.lowercase().contains(q) }
                .map { com.reon.music.data.remote.ArtistDto(id = it.id, name = it.name, genre = it.genre, artUrl = "") }
        } catch (_: Exception) { emptyList() }

        val albumsFiltered = try {
            val allAlbums = dao.getAllAlbumsFlow().first()
            allAlbums.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }
                .map { com.reon.music.data.remote.AlbumDto(id = it.id, title = it.title, artist = it.artist, year = it.year, trackCount = it.trackCount, artUrl = "") }
        } catch (_: Exception) { emptyList() }

        val playlistsFiltered = try {
            val allPlaylists = dao.getAllPlaylistsFlow().first()
            allPlaylists.filter { it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) }
                .map { com.reon.music.data.remote.PlaylistDto(id = it.id, title = it.title, subtitle = it.subtitle, trackCount = it.trackCount, artUrl = "") }
        } catch (_: Exception) { emptyList() }

        val f = filter?.lowercase()
        SearchResponse(
            tracks = if (f == null || f == "all" || f == "songs") matchedTracks else emptyList(),
            artists = if (f == null || f == "all" || f == "artists") artistsFiltered else emptyList(),
            albums = if (f == null || f == "all" || f == "albums") albumsFiltered else emptyList(),
            playlists = if (f == null || f == "all" || f == "playlists") playlistsFiltered else emptyList(),
        )
    }

    private suspend fun fallbackHome(): HomeResponse = withContext(io) {
        val tracks = dao.getAllTracks().take(12).map { it.toTrackDto() }
        if (tracks.isEmpty()) return@withContext HomeResponse(emptyList())
        HomeResponse(
            sections = listOf(
                com.reon.music.data.remote.HomeSection(title = "Trending now", items = tracks.take(6)),
                com.reon.music.data.remote.HomeSection(title = "Because you listened to Aurora Glow", items = tracks.shuffled().take(4)),
            )
        )
    }

    // ---- public API ----

    private fun isNetworkError(e: Throwable): Boolean = e is IOException || e is HttpException

    suspend fun search(query: String, filter: String?): SearchResponse {
        return try {
            val result = withFallback { it.search(query, filter?.lowercase()?.takeIf { it != "all" }) }
            // write-through for next offline use
            try { persistSearchResult(result) } catch (_: Exception) { /* ignore persist failures */ }
            result
        } catch (e: IOException) {
            fallbackSearchFull(query, filter)
        } catch (e: HttpException) {
            // 5xx/429 fall back to offline, 4xx return empty
            if (e.code() in 500..599 || e.code() == 429) fallbackSearchFull(query, filter)
            else SearchResponse()
        }
    }

    suspend fun suggestions(query: String): SuggestionsResponse {
        return try {
            withFallback { it.suggestions(query) }
        } catch (e: IOException) {
            SuggestionsResponse(emptyList())
        } catch (e: HttpException) {
            SuggestionsResponse(emptyList())
        } catch (_: Exception) {
            SuggestionsResponse(emptyList())
        }
    }

    suspend fun home(): HomeResponse {
        return try {
            val res = withFallback { it.home() }
            // persist first section's items as YT cache
            try {
                val all = res.sections.flatMap { it.items }
                if (all.isNotEmpty()) persistSearchResult(SearchResponse(tracks = all))
            } catch (_: Exception) {}
            res
        } catch (e: IOException) {
            fallbackHome()
        } catch (e: HttpException) {
            if (e.code() in 500..599 || e.code() == 429) fallbackHome()
            else HomeResponse(emptyList())
        }
    }

    suspend fun album(id: String): AlbumDetails {
        return try {
            withFallback { it.album(id) }
        } catch (e: IOException) {
            // fallback to Room album + tracks with that album name
            val allAlbums = dao.getAllAlbumsFlow().first()
            val found = allAlbums.firstOrNull { it.id == id }
                ?: throw e
            val tracks = dao.getAllTracks().filter { it.album == found.title }.map { it.toTrackDto() }
            AlbumDetails(
                album = com.reon.music.data.remote.AlbumDto(id = found.id, title = found.title, artist = found.artist, year = found.year, trackCount = found.trackCount, artUrl = ""),
                tracks = tracks,
            )
        } catch (e: HttpException) {
            val allAlbums = dao.getAllAlbumsFlow().first()
            val found = allAlbums.firstOrNull { it.id == id }
                ?: throw e
            val tracks = dao.getAllTracks().filter { it.album == found.title }.map { it.toTrackDto() }
            AlbumDetails(
                album = com.reon.music.data.remote.AlbumDto(id = found.id, title = found.title, artist = found.artist, year = found.year, trackCount = found.trackCount, artUrl = ""),
                tracks = tracks,
            )
        }
    }

    suspend fun artist(id: String): ArtistDetails {
        return try {
            withFallback { it.artist(id) }
        } catch (e: IOException) {
            val allArtists = dao.getAllArtistsFlow().first()
            val found = allArtists.firstOrNull { it.id == id } ?: throw e
            val topTracks = dao.getAllTracks().filter { it.artist == found.name }.take(5).map { it.toTrackDto() }
            val allAlbums = dao.getAllAlbumsFlow().first()
            val albums = allAlbums.filter { it.artist == found.name }.map { com.reon.music.data.remote.AlbumDto(it.id, it.title, it.artist, it.year, it.trackCount, "") }
            ArtistDetails(
                artist = com.reon.music.data.remote.ArtistDto(id = found.id, name = found.name, genre = found.genre, artUrl = ""),
                topTracks = topTracks,
                albums = albums,
            )
        } catch (e: HttpException) {
            val allArtists = dao.getAllArtistsFlow().first()
            val found = allArtists.firstOrNull { it.id == id } ?: throw e
            val topTracks = dao.getAllTracks().filter { it.artist == found.name }.take(5).map { it.toTrackDto() }
            val allAlbums = dao.getAllAlbumsFlow().first()
            val albums = allAlbums.filter { it.artist == found.name }.map { com.reon.music.data.remote.AlbumDto(it.id, it.title, it.artist, it.year, it.trackCount, "") }
            ArtistDetails(
                artist = com.reon.music.data.remote.ArtistDto(id = found.id, name = found.name, genre = found.genre, artUrl = ""),
                topTracks = topTracks,
                albums = albums,
            )
        }
    }

    suspend fun playlist(id: String): PlaylistDetails {
        return try {
            withFallback { it.playlist(id) }
        } catch (e: IOException) {
            val allPlaylists = dao.getAllPlaylistsFlow().first()
            val found = allPlaylists.firstOrNull { it.id == id } ?: throw e
            val tracks = dao.getAllTracks().take(6).map { it.toTrackDto() }
            PlaylistDetails(
                playlist = com.reon.music.data.remote.PlaylistDto(id = found.id, title = found.title, subtitle = found.subtitle, trackCount = found.trackCount, artUrl = ""),
                tracks = tracks,
            )
        } catch (e: HttpException) {
            val allPlaylists = dao.getAllPlaylistsFlow().first()
            val found = allPlaylists.firstOrNull { it.id == id } ?: throw e
            val tracks = dao.getAllTracks().take(6).map { it.toTrackDto() }
            PlaylistDetails(
                playlist = com.reon.music.data.remote.PlaylistDto(id = found.id, title = found.title, subtitle = found.subtitle, trackCount = found.trackCount, artUrl = ""),
                tracks = tracks,
            )
        }
    }

    suspend fun radio(trackId: String): RadioResponse {
        return try {
            withFallback { it.radio(trackId) }
        } catch (e: IOException) {
            val tracks = dao.getAllTracks().shuffled().take(8).map { it.toTrackDto() }
            RadioResponse(seedTrackId = trackId, tracks = tracks)
        } catch (e: HttpException) {
            val tracks = dao.getAllTracks().shuffled().take(8).map { it.toTrackDto() }
            RadioResponse(seedTrackId = trackId, tracks = tracks)
        }
    }

    /**
     * Resolve stream URL, using cached Room value if not expired (60s margin).
     * On cache hit, returns synthetic StreamResponse without network.
     * Handles both IOException (offline) and HttpException (502 cipher etc) with graceful fallback.
     * Tries quality fallback high->medium->low if backend returns 502.
     */
    suspend fun stream(trackId: String, quality: String = "high"): StreamResponse {
        require(trackId.isNotBlank()) { "trackId blank" }
        // check cache first
        val cached = withContext(io) {
            dao.getTrackById(trackId) ?: dao.getTrackByVideoId(trackId)
        }
        if (cached?.streamUrl != null && cached.streamExpiresAt > 0) {
            val nowSec = System.currentTimeMillis() / 1000
            if (nowSec < cached.streamExpiresAt - 60) {
                // return cached — infer codec/bitrate from entity
                val codec = when {
                    cached.codec.contains("opus", true) -> "opus"
                    cached.codec.contains("aac", true) -> "aac"
                    else -> "opus"
                }
                val bitrate = when (codec) {
                    "opus" -> 160_000
                    else -> 128_000
                }
                return StreamResponse(
                    url = cached.streamUrl,
                    codec = codec,
                    bitrate = bitrate,
                    expiresAt = cached.streamExpiresAt,
                    quality = quality,
                )
            }
        }

        // helper to attempt one quality with emulator fallback (10.0.2.2 <-> 127.0.0.1)
        suspend fun tryQuality(q: String): StreamResponse = withFallback { it.stream(trackId, q) }

        // fetch fresh with fallback qualities on server errors
        val qualities = when (quality.lowercase()) {
            "high" -> listOf("high", "medium", "low")
            "medium" -> listOf("medium", "high", "low")
            else -> listOf(quality, "high", "medium")
        }.distinct()

        var lastError: Exception? = null
        var fresh: StreamResponse? = null
        for (q in qualities) {
            try {
                fresh = tryQuality(q)
                break
            } catch (e: IOException) {
                lastError = e
                // if we have expired cached URL, return it as last resort for offline
                if (cached?.streamUrl != null) {
                    val codec = when {
                        cached.codec.contains("opus", true) -> "opus"
                        cached.codec.contains("aac", true) -> "aac"
                        else -> "opus"
                    }
                    return StreamResponse(
                        url = cached.streamUrl,
                        codec = codec,
                        bitrate = 160_000,
                        expiresAt = cached.streamExpiresAt,
                        quality = q,
                    )
                }
                // For IOException, don't try other qualities — network offline, break
                throw e
            } catch (e: HttpException) {
                lastError = e
                val code = e.code()
                // Retry other qualities only for gateway/cipher errors
                if (code == 502 || code == 500 || code == 429 || code == 503) {
                    // if last quality, try fallback to cached expired if available
                    if (q == qualities.last() && cached?.streamUrl != null) {
                        val codec = when {
                            cached.codec.contains("opus", true) -> "opus"
                            cached.codec.contains("aac", true) -> "aac"
                            else -> "opus"
                        }
                        return StreamResponse(
                            url = cached.streamUrl,
                            codec = codec,
                            bitrate = 160_000,
                            expiresAt = cached.streamExpiresAt,
                            quality = q,
                        )
                    }
                    // otherwise try next quality
                    continue
                }
                // 4xx -> not retryable
                if (cached?.streamUrl != null) {
                    val codec = when {
                        cached.codec.contains("opus", true) -> "opus"
                        cached.codec.contains("aac", true) -> "aac"
                        else -> "opus"
                    }
                    return StreamResponse(
                        url = cached.streamUrl,
                        codec = codec,
                        bitrate = 160_000,
                        expiresAt = cached.streamExpiresAt,
                        quality = q,
                    )
                }
                throw e
            } catch (e: Exception) {
                lastError = e
                continue
            }
        }
        val result = fresh ?: throw lastError ?: IOException("stream failed for $trackId")
        // persist
        withContext(io) {
            val targetId = cached?.id ?: trackId
            if (cached != null) {
                dao.updateStream(targetId, result.url, result.expiresAt)
            } else {
                // Insert minimal YT entity so future cache hits work (title unknown -> use id)
                try {
                    val minimal = TrackEntity(
                        id = trackId,
                        title = trackId,
                        artist = "",
                        album = "",
                        category = "YT Stream",
                        durationMs = 0L,
                        albumArtUrl = "",
                        artistImageUrl = "",
                        source = "YT Stream",
                        quality = "Opus · 160kbps",
                        spatialMode = "Stereo",
                        codec = result.codec.ifBlank { "Opus" },
                        sampleRate = "48kHz",
                        monthlyListeners = "",
                        lyricsQuote = "",
                        badge = result.codec.ifBlank { "Opus" },
                        artSeed = (trackId.hashCode().and(0x7fffffff) % 900) + 100,
                        videoId = trackId,
                        sourceKind = SourceKind.YT_STREAM,
                        streamUrl = result.url,
                        streamExpiresAt = result.expiresAt,
                    )
                    dao.insertTracks(listOf(minimal))
                } catch (_: Exception) { /* ignore */ }
            }
        }
        return result
    }

    suspend fun player(trackId: String): PlayerResponse {
        return try {
            withFallback { it.player(trackId) }
        } catch (e: IOException) {
            val cached = dao.getTrackById(trackId) ?: dao.getTrackByVideoId(trackId) ?: throw e
            PlayerResponse(
                trackId = trackId,
                title = cached.title,
                artist = cached.artist,
                durationMs = cached.durationMs,
                codec = cached.codec,
                bitrate = 160_000,
                artUrl = cached.albumArtUrl,
            )
        }
    }

    suspend fun lyrics(trackId: String): LyricsResponse {
        return try {
            withFallback { it.lyrics(trackId) }
        } catch (e: IOException) {
            val cached = dao.getTrackById(trackId) ?: dao.getTrackByVideoId(trackId)
            LyricsResponse(trackId = trackId, text = cached?.lyricsQuote ?: "", synced = false)
        }
    }

    // Expose Flows for UI (Room as offline source)
    fun allTracksFlow(): Flow<List<TrackEntity>> = dao.getAllTracksFlow()
    fun allArtistsFlow(): Flow<List<ArtistEntity>> = dao.getAllArtistsFlow()
    fun allAlbumsFlow(): Flow<List<AlbumEntity>> = dao.getAllAlbumsFlow()
    fun allPlaylistsFlow(): Flow<List<PlaylistEntity>> = dao.getAllPlaylistsFlow()
}

