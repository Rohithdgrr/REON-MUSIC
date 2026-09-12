package com.reon.music.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [Lyrics.getSyncedLines].
 */
class LyricsTest {

    @Test
    fun `null synced lyrics returns empty list`() {
        assertTrue(Lyrics().getSyncedLines().isEmpty())
    }

    @Test
    fun `parses timestamps to milliseconds`() {
        val lyrics = Lyrics(syncedLyrics = "[00:12.34]Hello\n[01:02.500]World")
        val lines = lyrics.getSyncedLines()
        assertEquals(2, lines.size)
        assertEquals(LyricLine(12_340L, "Hello"), lines[0])
        assertEquals(LyricLine(62_500L, "World"), lines[1])
    }

    @Test
    fun `skips malformed lines`() {
        val lyrics = Lyrics(syncedLyrics = "no timestamp\n[00:05.00]Ok")
        val lines = lyrics.getSyncedLines()
        assertEquals(1, lines.size)
        assertEquals(LyricLine(5_000L, "Ok"), lines[0])
    }
}
