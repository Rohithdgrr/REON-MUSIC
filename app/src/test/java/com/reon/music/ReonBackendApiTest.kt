package com.reon.music

import com.reon.music.data.remote.ReonBackendApi
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * P2-01 gate: MockWebServer proves Retrofit paths + query params + snake_case + error mapping.
 */
class ReonBackendApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ReonBackendApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()
        api = ReonBackendApi.create(
            baseUrl = server.url("/").toString(),
            apiKey = null,
            moshi = ReonBackendApi.defaultMoshi(),
            client = client,
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun search_usesCorrectPathAndQueryParams() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"tracks":[{"id":"vid123","title":"Aurora","artist":"Aurora Glow","album":"EP","duration":"03:47","durationMs":227000,"artUrl":"http://x/y.jpg","videoId":"vid123","badge":""}],"artists":[],"albums":[],"playlists":[]}"""
            )
        )
        val res = api.search("aurora", "songs")
        val req = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertTrue(req.path!!.startsWith("/api/v1/search"))
        assertTrue(req.path!!.contains("q=aurora"))
        assertTrue(req.path!!.contains("filter=songs"))
        assertEquals("GET", req.method)
        assertEquals(1, res.tracks.size)
        assertEquals("vid123", res.tracks[0].videoId)
    }

    @Test
    fun search_withoutFilter_omitsFilterParam() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"tracks":[],"artists":[],"albums":[],"playlists":[]}"""))
        api.search("aurora", null)
        val req = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertTrue(req.path!!.contains("q=aurora"))
        assertFalse(req.path!!.contains("filter="))
    }

    @Test
    fun stream_parsesSnakeCaseExpiresAt() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"url":"https://googlevideo.com/v?expire=9999999","codec":"opus","bitrate":160000,"expires_at":9999999,"quality":"high"}"""
            )
        )
        val res = api.stream("vid123", "high")
        val req = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertTrue(req.path!!.startsWith("/api/v1/stream/vid123"))
        assertTrue(req.path!!.contains("quality=high"))
        assertEquals("opus", res.codec)
        assertEquals(160000, res.bitrate)
        assertEquals(9999999L, res.expiresAt)
    }

    @Test
    fun home_hitsHomePath() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"sections":[{"title":"Trending","items":[]}]}"""))
        val res = api.home()
        val req = server.takeRequest(5, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/home", req.path)
        assertEquals(1, res.sections.size)
    }

    @Test
    fun catalogPaths_matchContract() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"album":{"id":"a1","title":"T"},"tracks":[]}"""))
        api.album("a1")
        assertEquals("/api/v1/albums/a1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)

        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"artist":{"id":"ar1","name":"N"},"topTracks":[],"albums":[]}"""))
        api.artist("ar1")
        assertEquals("/api/v1/artists/ar1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)

        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"playlist":{"id":"p1","title":"P"},"tracks":[]}"""))
        api.playlist("p1")
        assertEquals("/api/v1/playlists/p1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)

        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"seedTrackId":"v1","tracks":[]}"""))
        api.radio("v1")
        assertEquals("/api/v1/radio/v1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)

        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"trackId":"v1","text":"la","synced":false}"""))
        api.lyrics("v1")
        assertEquals("/api/v1/lyrics/v1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)

        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"trackId":"v1","title":"T"}"""))
        api.player("v1")
        assertEquals("/api/v1/player/v1", server.takeRequest(5, TimeUnit.SECONDS)!!.path)
    }

    @Test
    fun serverError_mapsToHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(502).setBody("""{"error":"STREAM_CIPHERED"}"""))
        try {
            api.stream("vidX", "high")
            fail("expected HttpException")
        } catch (e: HttpException) {
            assertEquals(502, e.code())
        }
    }
}
