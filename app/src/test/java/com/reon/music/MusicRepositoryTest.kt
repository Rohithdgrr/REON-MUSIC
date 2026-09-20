package com.reon.music

import com.reon.music.data.AlbumEntity
import com.reon.music.data.ArtistEntity
import com.reon.music.data.MusicRepository
import com.reon.music.data.MusicTrack
import com.reon.music.data.NotificationEntity
import com.reon.music.data.PlaylistEntity
import com.reon.music.data.ReonDao
import com.reon.music.data.SourceKind
import com.reon.music.data.TrackEntity
import com.reon.music.data.remote.AlbumDetails
import com.reon.music.data.remote.AlbumDto
import com.reon.music.data.remote.ArtistDetails
import com.reon.music.data.remote.ArtistDto
import com.reon.music.data.remote.HomeResponse
import com.reon.music.data.remote.HomeSection
import com.reon.music.data.remote.LyricsResponse
import com.reon.music.data.remote.PlayerResponse
import com.reon.music.data.remote.PlaylistDetails
import com.reon.music.data.remote.PlaylistDto
import com.reon.music.data.remote.RadioResponse
import com.reon.music.data.remote.ReonBackendApi
import com.reon.music.data.remote.SearchResponse
import com.reon.music.data.remote.StreamResponse
import com.reon.music.data.remote.SuggestionsResponse
import com.reon.music.data.remote.TrackDto
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/** In-memory fake DAO — no Android needed. */
private class FakeDao(seed: List<TrackEntity> = emptyList()) : ReonDao {
    private val tracks = seed.toMutableList()
    private val tracksFlow = MutableStateFlow<List<TrackEntity>>(tracks.toList())
    private val artists = mutableListOf<ArtistEntity>()
    private val artistsFlow = MutableStateFlow<List<ArtistEntity>>(emptyList())
    private val albums = mutableListOf<AlbumEntity>()
    private val albumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())
    private val playlists = mutableListOf<PlaylistEntity>()
    private val playlistsFlow = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    private val notifs = mutableListOf<NotificationEntity>()
    private val notifsFlow = MutableStateFlow<List<NotificationEntity>>(emptyList())

    private fun emit() {
        tracksFlow.value = tracks.toList()
        artistsFlow.value = artists.toList()
        albumsFlow.value = albums.toList()
        playlistsFlow.value = playlists.toList()
        notifsFlow.value = notifs.toList()
    }

    override fun getAllTracksFlow(): Flow<List<TrackEntity>> = tracksFlow.asStateFlow()
    override suspend fun getAllTracks(): List<TrackEntity> = tracks.toList()
    override suspend fun getTrackById(id: String): TrackEntity? = tracks.firstOrNull { it.id == id }
    override suspend fun insertTracks(tracks: List<TrackEntity>) {
        for (t in tracks) {
            val i = this.tracks.indexOfFirst { it.id == t.id }
            if (i >= 0) {
                // preserve user flags + existing stream cache unless overwritten
                val old = this.tracks[i]
                this.tracks[i] = t.copy(isLiked = old.isLiked, isDownloaded = old.isDownloaded)
            } else this.tracks.add(t)
        }
        emit()
    }
    override suspend fun updateTrackLike(id: String, isLiked: Boolean) {
        val i = tracks.indexOfFirst { it.id == id }
        if (i >= 0) tracks[i] = tracks[i].copy(isLiked = isLiked)
        emit()
    }
    override suspend fun updateTrackDownload(id: String, isDownloaded: Boolean) {
        val i = tracks.indexOfFirst { it.id == id }
        if (i >= 0) tracks[i] = tracks[i].copy(isDownloaded = isDownloaded)
        emit()
    }
    override suspend fun getTrackByVideoId(videoId: String): TrackEntity? = tracks.firstOrNull { it.videoId == videoId }
    override suspend fun updateStream(id: String, url: String, expiresAt: Long) {
        val i = tracks.indexOfFirst { it.id == id }
        if (i >= 0) tracks[i] = tracks[i].copy(streamUrl = url, streamExpiresAt = expiresAt)
        emit()
    }
    override fun getAllArtistsFlow(): Flow<List<ArtistEntity>> = artistsFlow.asStateFlow()
    override suspend fun insertArtists(artists: List<ArtistEntity>) {
        for (a in artists) {
            val i = this.artists.indexOfFirst { it.id == a.id }
            if (i >= 0) this.artists[i] = a else this.artists.add(a)
        }
        emit()
    }
    override suspend fun updateArtistFollowing(id: String, isFollowing: Boolean) {}
    override fun getAllAlbumsFlow(): Flow<List<AlbumEntity>> = albumsFlow.asStateFlow()
    override suspend fun insertAlbums(albums: List<AlbumEntity>) {
        for (a in albums) {
            val i = this.albums.indexOfFirst { it.id == a.id }
            if (i >= 0) this.albums[i] = a else this.albums.add(a)
        }
        emit()
    }
    override fun getAllPlaylistsFlow(): Flow<List<PlaylistEntity>> = playlistsFlow.asStateFlow()
    override suspend fun insertPlaylists(playlists: List<PlaylistEntity>) {
        for (p in playlists) {
            val i = this.playlists.indexOfFirst { it.id == p.id }
            if (i >= 0) this.playlists[i] = p else this.playlists.add(p)
        }
        emit()
    }
    override fun getAllNotificationsFlow(): Flow<List<NotificationEntity>> = notifsFlow.asStateFlow()
    override suspend fun getAllNotifications(): List<NotificationEntity> = notifs.toList()
    override suspend fun insertNotifications(notifications: List<NotificationEntity>) { notifs.addAll(notifications); emit() }
    override suspend fun markNotificationAsRead(id: String) {}
    override suspend fun markAllNotificationsAsRead() {}
    override suspend fun clearAllNotifications() { notifs.clear(); emit() }
}

/** Configurable fake backend. Throw IOException to simulate airplane mode. */
private class FakeApi(
    var searchResult: SearchResponse = SearchResponse(),
    var searchThrows: IOException? = null,
    var streamResult: StreamResponse = StreamResponse("https://x", "opus", 160000, 9999999999L, "high"),
    var streamThrows: IOException? = null,
) : ReonBackendApi {
    var searchCalls = 0
    var streamCalls = 0
    var homeCalls = 0
    override suspend fun search(q: String, filter: String?): SearchResponse {
        searchCalls++
        searchThrows?.let { throw it }
        return searchResult
    }
    override suspend fun suggestions(q: String) = SuggestionsResponse(emptyList())
    override suspend fun home(): HomeResponse { homeCalls++; return HomeResponse(emptyList()) }
    override suspend fun album(id: String) = AlbumDetails(AlbumDto(id, "T"), emptyList())
    override suspend fun artist(id: String) = ArtistDetails(ArtistDto(id, "N"), emptyList(), emptyList())
    override suspend fun playlist(id: String) = PlaylistDetails(PlaylistDto(id, "P"), emptyList())
    override suspend fun radio(trackId: String) = RadioResponse(trackId, emptyList())
    override suspend fun stream(trackId: String, quality: String): StreamResponse {
        streamCalls++
        streamThrows?.let { throw it }
        return streamResult.copy(quality = quality)
    }
    override suspend fun player(trackId: String) = PlayerResponse(trackId, "T", "A", 1000L, "opus", 160000, "")
    override suspend fun lyrics(trackId: String) = LyricsResponse(trackId, "la", false)
}

private fun seedEntity(id: String = "seed1", title: String = "Refractions") = TrackEntity(
    id = id, title = title, artist = "Aurora Glow", album = "EP", category = "DEEP FOCUS",
    durationMs = 200000L, albumArtUrl = "", artistImageUrl = "", source = "Direct FLAC Studio",
    quality = "96KHZ · FLAC", spatialMode = "Binaural Spatial", codec = "FLAC",
    sampleRate = "24-bit / 96kHz", monthlyListeners = "", lyricsQuote = "",
    badge = "96kHz Lossless", artSeed = 1, videoId = "", sourceKind = SourceKind.LOCAL_HIRES,
)

/**
 * P2-02 / P2-04 gates: offline fallback, stream expiry, split labeling.
 */
class MusicRepositoryTest {

    @Test
    fun airplaneMode_searchReturnsRoomSeeds() = runTest {
        val dao = FakeDao(listOf(seedEntity(), seedEntity("seed2", "Midnight Prism")))
        val api = FakeApi(searchThrows = IOException("offline"))
        val repo = MusicRepository(api, dao)
        val res = repo.search("refractions", "All")
        assertEquals(1, res.tracks.size)
        assertEquals("Refractions", res.tracks[0].title)
        assertEquals(0, dao.getAllTracks().size - 2) // no write-through on fallback
    }

    @Test
    fun onlineSearch_writeThroughCachesYtStream() = runTest {
        val dao = FakeDao()
        val dto = TrackDto(id = "vid1", title = "Aurora Song", artist = "Aurora", album = "EP", durationMs = 180000L, videoId = "vid1")
        val api = FakeApi(searchResult = SearchResponse(tracks = listOf(dto)))
        val repo = MusicRepository(api, dao)
        val res = repo.search("aurora", "songs")
        assertEquals(1, res.tracks.size)
        val cached = dao.getTrackByVideoId("vid1")
        assertNotNull(cached)
        assertEquals(SourceKind.YT_STREAM, cached!!.sourceKind)
        // P2-04: YT cache never labeled FLAC/Hi-Res
        assertFalse(cached.codec.contains("FLAC", true))
        assertFalse(cached.quality.contains("FLAC", true))
        assertFalse(cached.quality.contains("96", true))
        assertTrue(cached.codec == "Opus" || cached.codec == "AAC" || cached.codec == "Vorbis")
    }

    @Test
    fun stream_usesCacheWhenFresh_avoidsNetwork() = runTest {
        val freshExpiry = System.currentTimeMillis() / 1000 + 3600
        val entity = seedEntity("v1").copy(
            videoId = "v1", sourceKind = SourceKind.YT_STREAM, codec = "Opus",
            streamUrl = "https://cached/url", streamExpiresAt = freshExpiry,
        )
        val dao = FakeDao(listOf(entity))
        val api = FakeApi()
        val repo = MusicRepository(api, dao)
        val res = repo.stream("v1", "high")
        assertEquals("https://cached/url", res.url)
        assertEquals(0, api.streamCalls) // cache hit → no network
    }

    @Test
    fun stream_reResolvesWhenExpired() = runTest {
        val expired = System.currentTimeMillis() / 1000 - 10
        val entity = seedEntity("v1").copy(
            videoId = "v1", sourceKind = SourceKind.YT_STREAM, codec = "Opus",
            streamUrl = "https://old/url", streamExpiresAt = expired,
        )
        val dao = FakeDao(listOf(entity))
        val api = FakeApi(streamResult = StreamResponse("https://fresh/url", "opus", 160000, System.currentTimeMillis() / 1000 + 3600, "high"))
        val repo = MusicRepository(api, dao)
        val res = repo.stream("v1", "high")
        assertEquals("https://fresh/url", res.url)
        assertEquals(1, api.streamCalls)
        // persisted
        assertEquals("https://fresh/url", dao.getTrackById("v1")!!.streamUrl)
    }

    @Test
    fun stream_reResolvesWithin60sMargin() = runTest {
        val almostExpired = System.currentTimeMillis() / 1000 + 30 // inside 60s margin
        val entity = seedEntity("v1").copy(
            videoId = "v1", sourceKind = SourceKind.YT_STREAM, codec = "Opus",
            streamUrl = "https://old/url", streamExpiresAt = almostExpired,
        )
        val dao = FakeDao(listOf(entity))
        val api = FakeApi(streamResult = StreamResponse("https://fresh/url", "opus", 160000, System.currentTimeMillis() / 1000 + 3600, "high"))
        val repo = MusicRepository(api, dao)
        repo.stream("v1", "high")
        assertEquals(1, api.streamCalls)
    }

    @Test
    fun ytTrack_neverLabeledHiRes_afterNormalized() {
        val yt = MusicTrack(
            id = "v1", title = "T", artist = "A", album = "AL", durationMs = 1000L,
            codec = "FLAC", quality = "96KHZ · FLAC", sampleRate = "24-bit / 96kHz",
            sourceKind = SourceKind.YT_STREAM,
        ).normalized()
        assertFalse(yt.codec.contains("FLAC", true))
        assertFalse(yt.quality.contains("FLAC", true))
        assertFalse(yt.quality.contains("96", true))
        assertTrue(yt.codec == "Opus" || yt.codec == "AAC")
    }

    @Test
    fun localHiRes_keepsFlacLabel() {
        val local = MusicTrack(
            id = "l1", title = "T", artist = "A", album = "AL", durationMs = 1000L,
            codec = "FLAC", quality = "96KHZ · FLAC", sampleRate = "24-bit / 96kHz",
            sourceKind = SourceKind.LOCAL_HIRES,
        ).normalized()
        assertEquals("FLAC", local.codec)
        assertTrue(local.quality.contains("FLAC"))
    }

    @Test
    fun roomV2_fieldsExistWithDefaults() {
        val e = seedEntity()
        assertEquals("", e.videoId)
        assertEquals(SourceKind.LOCAL_HIRES, e.sourceKind)
        assertNull(e.streamUrl)
        assertEquals(0L, e.streamExpiresAt)
    }
}
