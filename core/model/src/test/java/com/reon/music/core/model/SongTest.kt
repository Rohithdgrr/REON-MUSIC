package com.reon.music.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * JVM unit tests for [Song] pure helpers.
 * No Android framework involved — safe to run with plain JUnit.
 */
class SongTest {

    private fun songWithArtwork(url: String?) = Song(
        id = "s1",
        title = "Test",
        artist = "Artist",
        artworkUrl = url
    )

    @Test
    fun `high quality artwork returns null for blank urls`() {
        assertNull(songWithArtwork(null).getHighQualityArtwork())
        assertNull(songWithArtwork("").getHighQualityArtwork())
        assertNull(songWithArtwork("   ").getHighQualityArtwork())
    }

    @Test
    fun `high quality artwork rejects non-http urls`() {
        assertNull(songWithArtwork("notaurl").getHighQualityArtwork())
        assertNull(songWithArtwork("ftp://x.com/a.jpg").getHighQualityArtwork())
    }

    @Test
    fun `youtube hqdefault upgrades to maxresdefault`() {
        val result = songWithArtwork("http://i.ytimg.com/vi/abc/hqdefault.jpg")
            .getHighQualityArtwork()
        assertEquals("https://i.ytimg.com/vi/abc/maxresdefault.jpg", result)
    }

    @Test
    fun `youtube maxresdefault passes through with https`() {
        val result = songWithArtwork("http://i.ytimg.com/vi/abc/maxresdefault.jpg")
            .getHighQualityArtwork()
        assertEquals("https://i.ytimg.com/vi/abc/maxresdefault.jpg", result)
    }

    @Test
    fun `googleusercontent sized url upgrades to w1200`() {
        val result = songWithArtwork("https://lh3.googleusercontent.com/x=w60-h60")
            .getHighQualityArtwork()
        assertEquals("https://lh3.googleusercontent.com/x=w1200-h1200", result)
    }

    @Test
    fun `saavn low-res url upgrades to 500x500`() {
        val result = songWithArtwork("https://c.saavncdn.com/123/50x50.jpg")
            .getHighQualityArtwork()
        assertEquals("https://c.saavncdn.com/123/500x500.jpg", result)
    }

    @Test
    fun `medium quality artwork downgrades maxres to hqdefault`() {
        val result = songWithArtwork("https://i.ytimg.com/vi/abc/maxresdefault.jpg")
            .getMediumQualityArtwork()
        assertEquals("https://i.ytimg.com/vi/abc/hqdefault.jpg", result)
    }

    @Test
    fun `stream url quality variant swaps bitrate marker`() {
        val song = Song(id = "s1", title = "T", artist = "A", streamUrl = "https://x.com/a_96.mp3")
        assertEquals("https://x.com/a_320.mp3", song.getStreamUrlWithQuality("_320"))
    }

    @Test
    fun `stream url without marker returns unchanged`() {
        val song = Song(id = "s1", title = "T", artist = "A", streamUrl = "https://x.com/a.mp3")
        assertEquals("https://x.com/a.mp3", song.getStreamUrlWithQuality())
    }

    @Test
    fun `stream url null returns null`() {
        val song = Song(id = "s1", title = "T", artist = "A", streamUrl = null)
        assertNull(song.getStreamUrlWithQuality())
    }

    @Test
    fun `duration formats as mm-ss`() {
        assertEquals("0:00", Song(id = "s", title = "T", artist = "A", duration = 0).formattedDuration())
        assertEquals("1:05", Song(id = "s", title = "T", artist = "A", duration = 65).formattedDuration())
        assertEquals("59:59", Song(id = "s", title = "T", artist = "A", duration = 3599).formattedDuration())
    }
}
