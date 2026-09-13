package com.reon.music.ui.viewmodels

import com.reon.music.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
/**
 * JVM unit tests for home content-group mapping.
 */
class HomeGroupsTest {

    @Test
    fun `primary chart types map to primary`() {
        listOf(
            "recent", "quick-picks", "recommended", "mostplayed", "new", "albums",
            "telugu", "hindi", "tamil", "charts", "chart", "playlists", "daily-mix"
        ).forEach {
            assertEquals(listOf(HomeGroups.PRIMARY), groupsForChartType(it))
        }
    }

    @Test
    fun `extended keys map to their groups`() {
        assertEquals(listOf(HomeGroups.LANGUAGES_EXT), groupsForChartType("punjabi"))
        assertEquals(listOf(HomeGroups.LANGUAGES_EXT), groupsForChartType("english"))
        assertEquals(listOf(HomeGroups.REGIONAL), groupsForChartType("kannada"))
        assertEquals(listOf(HomeGroups.MOODS), groupsForChartType("lofi"))
        assertEquals(listOf(HomeGroups.MOODS), groupsForChartType("telugudj"))
        assertEquals(listOf(HomeGroups.SPOTLIGHTS), groupsForChartType("arijitsingh"))
        assertEquals(listOf(HomeGroups.CURATED), groupsForChartType("alltimefavorite"))
        assertEquals(listOf(HomeGroups.CURATED), groupsForChartType("mostlistening"))
        assertEquals(listOf(HomeGroups.INTERNATIONAL), groupsForChartType("international"))
        assertEquals(listOf(HomeGroups.SECONDARY), groupsForChartType("banjara"))
    }

    @Test
    fun `unknown keys fall back to all extended`() {
        assertEquals(HomeGroups.ALL_EXTENDED, groupsForChartType("whatever"))
    }

    @Test
    fun `mapping is case-insensitive`() {
        assertEquals(listOf(HomeGroups.MOODS), groupsForChartType("LoFi"))
    }
}

/**
 * JVM unit tests for Made For You mix generation.
 */
class DailyMixTest {

    private fun song(id: String, genre: String = "", artist: String = "A") =
        Song(id = id, title = "Title $id", artist = artist, genre = genre)

    @Test
    fun `empty inputs yield no mixes`() {
        assertTrue(generateDailyMixes(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun `top genres become mixes`() {
        val history = List(5) { song("p$it", genre = "Pop") } +
            List(3) { song("r$it", genre = "Rock") }
        val mixes = generateDailyMixes(history, emptyList())
        assertTrue(mixes.any { it.title == "Pop Mix" })
        assertTrue(mixes.any { it.title == "Rock Mix" })
    }

    @Test
    fun `liked songs become liked mix`() {
        val mixes = generateDailyMixes(emptyList(), listOf(song("l1"), song("l2")))
        assertTrue(mixes.any { it.id == "mix-liked" })
    }

    @Test
    fun `mixes cap songs and mix count`() {
        val history = (1..50).map { song("s$it", genre = "Pop") }
        val mixes = generateDailyMixes(history, emptyList(), limitPerMix = 5, maxMixes = 2)
        assertTrue(mixes.all { it.songs.size <= 5 })
        assertTrue(mixes.size <= 2)
    }

    @Test
    fun `history without genre still yields rediscover mix`() {
        val mixes = generateDailyMixes(listOf(song("x")), emptyList())
        assertEquals(1, mixes.size)
        assertEquals("mix-rediscover", mixes[0].id)
    }
}

/**
 * JVM unit tests for home section pagination windows.
 */
class HomePaginationTest {

    @Test
    fun `page sizes match legacy row windows`() {
        assertEquals(16, pageSizeFor(HomeSections.RECENT))
        assertEquals(6, pageSizeFor(HomeSections.QUICK_PICKS))
        assertEquals(5, pageSizeFor(HomeSections.CHARTS))
        assertEquals(10, pageSizeFor(HomeSections.NEW_RELEASES))
        assertEquals(20, pageSizeFor(HomeSections.PLAYLISTS))
        assertEquals(10, pageSizeFor(HomeSections.MOST_PLAYED))
        assertEquals(10, pageSizeFor(HomeSections.TRENDING_ALBUMS))
        assertEquals(10, pageSizeFor(HomeSections.PUNJABI))
        assertEquals(10, pageSizeFor(HomeSections.ARIJIT))
    }

    @Test
    fun `load more grows by one page and clamps to total`() {
        assertEquals(32, nextVisibleCount(16, 100, HomeSections.RECENT))
        assertEquals(30, nextVisibleCount(26, 30, HomeSections.RECENT))
        assertEquals(0, nextVisibleCount(10, 0, HomeSections.HINDI))
    }

    @Test
    fun `unknown sections fall back to default window`() {
        assertEquals(10, pageSizeFor("whatever"))
    }
}

/**
 * JVM unit tests for Jump Back In progress derivation.
 */
class JumpBackInTest {
    @Test
    fun `completed play reports full progress`() {
        assertEquals(1f, jumpBackInProgress(10_000L, true, 200_000L))
    }

    @Test
    fun `partial play reports fraction clamped to 0-1`() {
        assertEquals(0.5f, jumpBackInProgress(100_000L, false, 200_000L))
        assertEquals(1f, jumpBackInProgress(300_000L, false, 200_000L))
        assertEquals(0f, jumpBackInProgress(0L, false, 200_000L))
    }

    @Test
    fun `missing durations report zero`() {
        assertEquals(0f, jumpBackInProgress(50_000L, false, 0L))
    }
}

/**
 * JVM unit tests for freshness helpers: discovery queries must qualify
 * with the current year so "latest / trending" never goes stale.
 */
class HomeFreshnessTest {

    @Test
    fun `music year tracks the calendar`() {
        val expected = java.util.Calendar.getInstance()
            .get(java.util.Calendar.YEAR)
        assertEquals(expected, com.reon.music.core.common.currentMusicYear())
    }

    @Test
    fun `year query appends the current year`() {
        val year = com.reon.music.core.common.currentMusicYear()
        assertEquals(
            "trending songs $year",
            com.reon.music.core.common.yearQuery("trending songs")
        )
        assertEquals(
            "latest songs $year",
            com.reon.music.core.common.yearQuery("latest songs")
        )
    }
}
