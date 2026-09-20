package com.reon

import com.reon.innertube.Parsers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val json = Json { ignoreUnknownKeys = true }

private fun run(text: String, videoId: String? = null, browseId: String? = null) = buildJsonObject {
    put("text", text)
    if (videoId != null || browseId != null) {
        put("navigationEndpoint", buildJsonObject {
            if (videoId != null) put("watchEndpoint", buildJsonObject { put("videoId", videoId) })
            if (browseId != null) put("browseEndpoint", buildJsonObject { put("browseId", browseId) })
        })
    }
}

private fun sep() = buildJsonObject { put("text", " • ") }

private fun flex(vararg runs: JsonObject) = buildJsonObject {
    put("musicResponsiveListItemFlexColumnRenderer", buildJsonObject {
        put("text", buildJsonObject { put("runs", buildJsonArray { runs.forEach { add(it) } }) })
    })
}

private fun thumbs(url: String) = buildJsonObject {
    put("musicThumbnailRenderer", buildJsonObject {
        put("thumbnail", buildJsonObject {
            put("thumbnails", buildJsonArray { add(buildJsonObject { put("url", url); put("width", 540) }) })
        })
    })
}

private fun songItem(title: String, videoId: String, artist: String, album: String, duration: String) =
    buildJsonObject {
        put("musicResponsiveListItemRenderer", buildJsonObject {
            put("flexColumns", buildJsonArray {
                add(flex(run(title, videoId = videoId)))
                add(flex(run(artist), sep(), run(album), sep(), run(duration)))
            })
            put("thumbnail", thumbs("https://art/l.jpg"))
            put("overlay", buildJsonObject {
                put("musicItemThumbnailOverlayRenderer", buildJsonObject {
                    put("content", buildJsonObject {
                        put("musicPlayButtonRenderer", buildJsonObject {
                            put("playNavigationEndpoint", buildJsonObject {
                                put("watchEndpoint", buildJsonObject { put("videoId", videoId) })
                            })
                        })
                    })
                })
            })
        })
    }

private fun shelfObj(kind: String, vararg items: JsonObject) = buildJsonObject {
    put("musicShelfRenderer", buildJsonObject {
        put("title", buildJsonObject {
            put("runs", buildJsonArray { add(buildJsonObject { put("text", kind) }) })
        })
        put("contents", buildJsonArray { items.forEach { add(it) } })
    })
}

private fun tabbedSearch(vararg sections: JsonObject) = buildJsonObject {
    put("contents", buildJsonObject {
        put("tabbedSearchResultsRenderer", buildJsonObject {
            put("tabs", buildJsonArray {
                add(buildJsonObject {
                    put("tabRenderer", buildJsonObject {
                        put("content", buildJsonObject {
                            put("sectionListRenderer", buildJsonObject {
                                put("contents", buildJsonArray { sections.forEach { add(it) } })
                            })
                        })
                    })
                })
            })
        })
    })
}.jsonObject

class ParserTest {

    @Test
    fun `search parses song and artist shelves`() {
        val artistRow = buildJsonObject {
            put("musicResponsiveListItemRenderer", buildJsonObject {
                put("flexColumns", buildJsonArray {
                    add(flex(run("Aurora Glow", browseId = "UCabc")))
                    add(flex(run("62.4M monthly listeners")))
                })
                put("thumbnail", thumbs("https://a.jpg"))
            })
        }
        val root = tabbedSearch(
            shelfObj("Songs", songItem("Refractions", "vid123", "Aurora Glow", "Refraction EP", "3:47")),
            shelfObj("Artists", artistRow),
        )
        val res = Parsers.search(root)
        assertEquals(1, res.tracks.size)
        val t = res.tracks[0]
        assertEquals("Refractions", t.title)
        assertEquals("Aurora Glow", t.artist)
        assertEquals("Refraction EP", t.album)
        assertEquals("3:47", t.duration)
        assertEquals(227_000L, t.durationMs)
        assertEquals("vid123", t.videoId)
        assertEquals("https://art/l.jpg", t.artUrl)
        assertEquals(1, res.artists.size)
        assertEquals("UCabc", res.artists[0].id)
        assertEquals("Aurora Glow", res.artists[0].name)
    }

    @Test
    fun `search ignores unknown shelf kinds and bad items`() {
        val root = tabbedSearch(shelfObj("Podcasts", buildJsonObject { put("junk", 1) }))
        val res = Parsers.search(root)
        assertTrue(res.tracks.isEmpty() && res.artists.isEmpty())
    }

    @Test
    fun `player picks opus high and lowest low`() {
        val root = json.parseToJsonElement("""
{"playabilityStatus":{"status":"OK"},
"streamingData":{"adaptiveFormats":[
{"itag":251,"url":"https://g/videoplayback?expire=1700000000&x=1","mimeType":"audio/webm; codecs=\"opus\"","bitrate":160000,"audioSampleRate":"48000"},
{"itag":140,"url":"https://g/videoplayback?expire=1700000000&x=2","mimeType":"audio/mp4; codecs=\"mp4a.40.2\"","bitrate":128000,"audioSampleRate":"44100"},
{"itag":249,"url":"https://g/videoplayback?expire=1700000000&x=3","mimeType":"audio/webm; codecs=\"opus\"","bitrate":50000,"audioSampleRate":"48000"}
]},
"videoDetails":{"title":"Refractions","author":"Aurora Glow","lengthSeconds":"227"}}""").jsonObject
        val formats = Parsers.rawFormats(root)
        assertEquals(3, formats.size)
        val high = Parsers.pickFormat(formats, "high")!!
        assertEquals(160000, high.bitrate)
        assertEquals("opus", Parsers.codecOf(high.mime))
        assertEquals(1700000000L, Parsers.expireOf(high.url))
        val low = Parsers.pickFormat(formats, "low")!!
        assertEquals(50000, low.bitrate)
        val (title, artist, ms) = Parsers.videoMeta(root)
        assertEquals("Refractions", title)
        assertEquals("Aurora Glow", artist)
        assertEquals(227_000L, ms)
        assertEquals("OK" to "", Parsers.playability(root))
    }

    @Test
    fun `ciphered-only player is detected`() {
        val root = json.parseToJsonElement("""
{"playabilityStatus":{"status":"OK"},
"streamingData":{"adaptiveFormats":[
{"itag":251,"signatureCipher":"s=ABC&url=x","mimeType":"audio/webm; codecs=\"opus\"","bitrate":160000}
]}}""").jsonObject
        assertTrue(Parsers.rawFormats(root).isEmpty())
        assertTrue(Parsers.hasCipheredOnly(root))
        assertNull(Parsers.pickFormat(emptyList(), "high"))
    }

    @Test
    fun `lyrics browseId and text extraction`() {
        val next = buildJsonObject {
            put("contents", buildJsonObject {
                put("singleColumnMusicWatchNextResultsRenderer", buildJsonObject {
                    put("tabs", buildJsonArray {
                        add(buildJsonObject {
                            put("tabRenderer", buildJsonObject {
                                put("title", buildJsonObject {
                                    put("runs", buildJsonArray { add(run("Up next")) })
                                })
                            })
                        })
                        add(buildJsonObject {
                            put("tabRenderer", buildJsonObject {
                                put("title", buildJsonObject {
                                    put("runs", buildJsonArray { add(run("Lyrics")) })
                                })
                                put("endpoint", buildJsonObject {
                                    put("browseEndpoint", buildJsonObject { put("browseId", "MPLYt_vid123") })
                                })
                            })
                        })
                    })
                })
            })
        }
        assertEquals("MPLYt_vid123", Parsers.lyricsBrowseId(next))
        val browse = buildJsonObject {
            put("contents", buildJsonObject {
                put("singleColumnBrowseResultsRenderer", buildJsonObject {
                    put("tabs", buildJsonArray {
                        add(buildJsonObject {
                            put("tabRenderer", buildJsonObject {
                                put("content", buildJsonObject {
                                    put("sectionListRenderer", buildJsonObject {
                                        put("contents", buildJsonArray {
                                            add(buildJsonObject {
                                                put("musicDescriptionShelfRenderer", buildJsonObject {
                                                    put("description", buildJsonObject {
                                                        put("runs", buildJsonArray {
                                                            add(run("line one\n")); add(run("line two"))
                                                        })
                                                    })
                                                })
                                            })
                                        })
                                    })
                                })
                            })
                        })
                    })
                })
            })
        }
        assertEquals("line one\nline two", Parsers.lyricsText(browse))
        assertNotNull(Parsers.lyricsText(browse))
    }

    @Test
    fun `suggestions extraction`() {
        val root = json.parseToJsonElement("""
{"contents":[{"searchSuggestionsSectionRenderer":{"contents":[
{"searchSuggestionRenderer":{"navigationEndpoint":{"searchEndpoint":{"query":"aurora glow"}}}},
{"searchSuggestionRenderer":{"navigationEndpoint":{"searchEndpoint":{"query":"aurora borealis"}}}}]}}]}""").jsonObject
        assertEquals(listOf("aurora glow", "aurora borealis"), Parsers.suggestions(root))
    }
}
