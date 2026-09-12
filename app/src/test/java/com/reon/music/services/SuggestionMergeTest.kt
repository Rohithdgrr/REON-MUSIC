package com.reon.music.services

import com.reon.music.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for the pure [mergeSuggestions] helper.
 * Same module as the implementation, so `internal` is visible.
 */
class SuggestionMergeTest {

    private fun song(id: String) = Song(id = id, title = "Title $id", artist = "Artist")

    @Test
    fun `deduplicates across candidate lists`() {
        val result = mergeSuggestions(
            listOf(song("1"), song("2")),
            listOf(song("2"), song("3"))
        )
        assertEquals(listOf("1", "2", "3"), result.map { it.id }.sorted())
    }

    @Test
    fun `excludes current song when id given`() {
        val result = mergeSuggestions(
            listOf(song("1"), song("2")),
            excludeSongId = "1"
        )
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `caps results at limit`() {
        val many = (1..30).map { song(it.toString()) }
        val result = mergeSuggestions(many, limit = 5)
        assertEquals(5, result.size)
    }

    @Test
    fun `empty input yields empty output`() {
        assertTrue(mergeSuggestions(emptyList(), emptyList()).isEmpty())
    }
}
