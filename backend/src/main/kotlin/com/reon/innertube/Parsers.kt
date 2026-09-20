package com.reon.innertube

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/** Pure parsers over InnerTube JSON. All defensive: bad shapes are skipped, never thrown. */
object Parsers {

    // ---------- shared item shapes ----------

    /** musicResponsiveListItemRenderer → track-ish row. `defaultAlbum` fills album for album listings. */
    fun listItem(item: JsonObject, defaultAlbum: String = "", defaultArtist: String = ""): TrackDto? {
        val r = item.obj("musicResponsiveListItemRenderer") ?: return null
        return try {
            val flex = r.arr("flexColumns")?.mapNotNull {
                (it as? JsonObject)?.obj("musicResponsiveListItemFlexColumnRenderer")
            } ?: emptyList()
            val titleRuns = flex.getOrNull(0)?.runs() ?: emptyList()
            val title = titleRuns.joinedText().trim()
            if (title.isEmpty()) return null

            var videoId = titleRuns.firstOrNull()?.watchVideoId()
            var browseId = titleRuns.firstOrNull()?.browseId()
            if (videoId == null) {
                val overlay = r.obj("overlay")?.obj("musicItemThumbnailOverlayRenderer")
                    ?.obj("content")?.obj("musicPlayButtonRenderer")
                    ?.obj("playNavigationEndpoint")
                videoId = overlay?.obj("watchEndpoint")?.get("videoId").text()
                if (browseId == null) browseId = overlay?.obj("browseEndpoint")?.get("browseId").text()
            }

            val subRuns = flex.getOrNull(1)?.runs() ?: emptyList()
            val texts = subRuns.filter { (it["text"].text() ?: "") != " • " }
            var artist = defaultArtist
            var album = defaultAlbum
            var duration = ""
            var durationMs = 0L
            if (texts.isNotEmpty()) {
                val last = texts.last()["text"].text() ?: ""
                var body = texts
                if (DURATION_RE.matches(last)) {
                    duration = last
                    durationMs = last.toDurationMs()
                    body = texts.dropLast(1)
                }
                if (body.isNotEmpty() && artist.isEmpty()) {
                    artist = body.first()["text"].text() ?: ""
                    if (body.size >= 2 && album.isEmpty()) album = body[1]["text"].text() ?: ""
                }
            }

            val badges = r.arr("badges")?.mapNotNull {
                (it as? JsonObject)?.obj("musicInlineBadgeRenderer")
                    ?.obj("icon")?.get("iconType").text()
            } ?: emptyList()
            val badge = when {
                badges.any { it.contains("EXPLICIT") } -> "Explicit"
                else -> ""
            }

            val id = videoId ?: browseId ?: return null
            TrackDto(
                id = id,
                title = title,
                artist = artist,
                album = album,
                duration = duration.ifEmpty { durationMs.toDurationLabel().takeIf { durationMs > 0 } ?: "" },
                durationMs = durationMs,
                artUrl = r.obj("thumbnail")?.thumbUrl() ?: "",
                videoId = videoId ?: "",
                badge = badge,
            )
        } catch (_: Exception) {
            null
        }
    }

    /** musicResponsiveListItemRenderer → artist row (title + fans subtitle). */
    fun artistItem(item: JsonObject): ArtistDto? {
        val r = item.obj("musicResponsiveListItemRenderer") ?: return null
        return try {
            val flex = r.arr("flexColumns")?.mapNotNull {
                (it as? JsonObject)?.obj("musicResponsiveListItemFlexColumnRenderer")
            } ?: emptyList()
            val titleRuns = flex.getOrNull(0)?.runs() ?: emptyList()
            val name = titleRuns.joinedText().trim().ifEmpty { return null }
            var id = titleRuns.firstOrNull()?.browseId()
            if (id == null) {
                id = r.obj("overlay")?.obj("musicItemThumbnailOverlayRenderer")
                    ?.obj("content")?.obj("musicPlayButtonRenderer")
                    ?.obj("playNavigationEndpoint")?.browseId()
            }
            val genre = flex.getOrNull(1)?.runs()?.joinedText()?.trim() ?: ""
            ArtistDto(
                id = id ?: name,
                name = name,
                genre = genre,
                artUrl = r.obj("thumbnail")?.thumbUrl() ?: "",
            )
        } catch (_: Exception) {
            null
        }
    }

    /** musicResponsiveListItemRenderer → album or playlist row. */
    fun albumItem(item: JsonObject): AlbumDto? {
        val r = item.obj("musicResponsiveListItemRenderer") ?: return null
        return try {
            val flex = r.arr("flexColumns")?.mapNotNull {
                (it as? JsonObject)?.obj("musicResponsiveListItemFlexColumnRenderer")
            } ?: emptyList()
            val titleRuns = flex.getOrNull(0)?.runs() ?: emptyList()
            val title = titleRuns.joinedText().trim().ifEmpty { return null }
            val id = titleRuns.firstOrNull()?.browseId() ?: return null
            val sub = flex.getOrNull(1)?.runs()?.joinedText()?.trim() ?: ""
            val parts = sub.split(" • ").map { it.trim() }
            AlbumDto(
                id = id,
                title = title,
                artist = parts.getOrNull(1) ?: "",
                year = parts.firstOrNull { it.matches(Regex("""\d{4}""")) } ?: "",
                trackCount = parts.firstOrNull { it.contains("song", true) || it.contains("track", true) } ?: "",
                artUrl = r.obj("thumbnail")?.thumbUrl() ?: "",
            )
        } catch (_: Exception) {
            null
        }
    }

    fun playlistItem(item: JsonObject): PlaylistDto? {
        val a = albumItem(item) ?: return null
        return PlaylistDto(id = a.id, title = a.title, subtitle = a.artist, trackCount = a.trackCount, artUrl = a.artUrl)
    }

    /** musicTwoRowItemRenderer (carousels) → track. */
    fun twoRowItem(item: JsonObject, artistFallback: String = ""): TrackDto? {
        val r = item.obj("musicTwoRowItemRenderer") ?: return null
        return try {
            val titleRuns = r.obj("title")?.runsAny() ?: emptyList()
            val title = titleRuns.joinedText().trim().ifEmpty { return null }
            val videoId = titleRuns.firstOrNull()?.watchVideoId()
            val browseId = titleRuns.firstOrNull()?.browseId()
            val sub = r.obj("subtitle")?.runsAny()?.joinedText()?.trim() ?: ""
            val id = videoId ?: browseId ?: return null
            TrackDto(
                id = id,
                title = title,
                artist = sub.substringBefore(" • ").trim().takeIf { it.isNotEmpty() } ?: artistFallback,
                album = "",
                duration = "",
                durationMs = 0L,
                artUrl = r.obj("thumbnailRenderer")?.thumbUrl() ?: "",
                videoId = videoId ?: "",
            )
        } catch (_: Exception) {
            null
        }
    }

    fun twoRowAlbum(item: JsonObject, artistFallback: String = ""): AlbumDto? {
        val r = item.obj("musicTwoRowItemRenderer") ?: return null
        return try {
            val titleRuns = r.obj("title")?.runsAny() ?: emptyList()
            val title = titleRuns.joinedText().trim().ifEmpty { return null }
            val id = titleRuns.firstOrNull()?.browseId() ?: return null
            val sub = r.obj("subtitle")?.runsAny()?.joinedText()?.trim() ?: ""
            AlbumDto(id = id, title = title, artist = sub.substringBefore(" • ").trim().ifEmpty { artistFallback }, artUrl = r.obj("thumbnailRenderer")?.thumbUrl() ?: "")
        } catch (_: Exception) {
            null
        }
    }

    // ---------- section helpers ----------

    /** sectionListRenderer.contents[] passthrough. */
    fun sections(root: JsonObject): List<JsonObject> {
        return try {
            root.obj("contents")?.obj("tabbedSearchResultsRenderer")?.arr("tabs")
                ?.firstOrNull()?.let { (it as? JsonObject)?.obj("tabRenderer") }
                ?.obj("content")?.obj("sectionListRenderer")?.arr("contents")
                ?.mapNotNull { it as? JsonObject }
                ?: root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")
                    ?.arr("tabs")?.firstOrNull()?.let { (it as? JsonObject)?.obj("tabRenderer") }
                    ?.obj("content")?.obj("sectionListRenderer")?.arr("contents")
                    ?.mapNotNull { it as? JsonObject }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun shelfItems(shelf: JsonObject): List<JsonObject> {
        val s = shelf.obj("musicShelfRenderer") ?: shelf.obj("musicPlaylistShelfRenderer") ?: return emptyList()
        return s.arr("contents")?.mapNotNull { (it as? JsonObject) } ?: emptyList()
    }

    fun carouselParts(carousel: JsonObject): Pair<String, List<JsonObject>> {
        val c = carousel.obj("musicCarouselShelfRenderer") ?: return "" to emptyList()
        val title = c.obj("header")?.obj("musicCarouselShelfBasicHeaderRenderer")
            ?.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
        val items = c.arr("contents")?.mapNotNull { it as? JsonObject } ?: emptyList()
        return title to items
    }

    // ---------- search ----------

    fun search(root: JsonObject): SearchResponse {
        val tracks = mutableListOf<TrackDto>()
        val artists = mutableListOf<ArtistDto>()
        val albums = mutableListOf<AlbumDto>()
        val playlists = mutableListOf<PlaylistDto>()
        for (section in sections(root)) {
            section.obj("musicShelfRenderer")?.let { shelf ->
                val kind = shelf.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
                val items = shelfItems(section)
                when {
                    kind.contains("ong", true) || kind.contains("ideo", true) ->
                        items.mapNotNullTo(tracks) { listItem(it) }
                    kind.contains("rtist", true) ->
                        items.mapNotNullTo(artists) { artistItem(it) }
                    kind.contains("lbum", true) ->
                        items.mapNotNullTo(albums) { albumItem(it) }
                    kind.contains("laylist", true) || kind.contains("ommunity", true) ->
                        items.mapNotNullTo(playlists) { playlistItem(it) }
                }
            }
            section.obj("musicCardShelfRenderer")?.let { card ->
                try {
                    val titleRuns = card.obj("title")?.runsAny() ?: emptyList()
                    val title = titleRuns.joinedText().trim()
                    val sub = card.obj("subtitle")?.runsAny()?.joinedText()?.trim() ?: ""
                    val art = card.obj("thumbnail")?.thumbUrl() ?: ""
                    val videoId = titleRuns.firstOrNull()?.watchVideoId()
                    val browseId = titleRuns.firstOrNull()?.browseId()
                    if (title.isNotEmpty()) {
                        if (videoId != null) {
                            tracks.add(0, TrackDto(id = videoId, title = title, artist = sub.substringBefore(" • ").trim(), artUrl = art, videoId = videoId))
                        } else if (browseId != null) {
                            when {
                                browseId.startsWith("UC") -> artists.add(0, ArtistDto(id = browseId, name = title, genre = sub, artUrl = art))
                                browseId.startsWith("MPRE") -> albums.add(0, AlbumDto(id = browseId, title = title, artist = sub.substringBefore(" • ").trim(), artUrl = art))
                                browseId.startsWith("VL") -> playlists.add(0, PlaylistDto(id = browseId, title = title, subtitle = sub, artUrl = art))
                            }
                        }
                    }
                } catch (_: Exception) { }
            }
        }
        return SearchResponse(tracks, artists, albums, playlists)
    }

    // ---------- home ----------

    fun home(root: JsonObject): HomeResponse {
        val out = mutableListOf<HomeSection>()
        for (section in sections(root)) {
            val (title, items) = carouselParts(section)
            if (title.isEmpty() || items.isEmpty()) continue
            val tracks = items.mapNotNull { listItem(it) ?: twoRowItem(it) }
            if (tracks.isNotEmpty()) out.add(HomeSection(title = title, items = tracks))
        }
        return HomeResponse(out)
    }

    // ---------- album ----------

    fun album(id: String, root: JsonObject): AlbumDetails {
        val header = root.obj("header")?.obj("musicDetailHeaderRenderer")
        val title = header?.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
        val sub = header?.obj("subtitle")?.runsAny()?.joinedText()?.trim() ?: ""
        val parts = sub.split(" • ").map { it.trim() }
        val artist = parts.getOrNull(0) ?: ""
        val year = parts.firstOrNull { it.matches(Regex("""\d{4}""")) } ?: ""
        val count = header?.obj("secondSubtitle")?.runsAny()?.joinedText()?.trim() ?: ""
        val album = AlbumDto(id = id, title = title, artist = artist, year = year, trackCount = count, artUrl = header?.obj("thumbnail")?.thumbUrl() ?: "")
        val tracks = sections(root).flatMap { shelfItems(it) }.mapNotNull { listItem(it, defaultAlbum = title, defaultArtist = artist) }
        return AlbumDetails(album, tracks)
    }

    // ---------- artist ----------

    fun artist(id: String, root: JsonObject): ArtistDetails {
        val header = root.obj("header")?.obj("musicImmersiveHeaderRenderer")
            ?: root.obj("header")?.obj("musicVisualHeaderRenderer")
        val name = header?.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
        val desc = header?.obj("description")?.runsAny()?.joinedText()?.trim() ?: ""
        val art = header?.obj("thumbnail")?.thumbUrl() ?: ""
        val top = mutableListOf<TrackDto>()
        val albums = mutableListOf<AlbumDto>()
        for (section in sections(root)) {
            section.obj("musicShelfRenderer")?.let { shelf ->
                val kind = shelf.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
                if (kind.contains("ong", true) && top.isEmpty()) {
                    shelfItems(section).mapNotNullTo(top) { listItem(it, defaultArtist = name) }
                }
            }
            val (title, items) = carouselParts(section)
            if (title.contains("lbum", true) || title.contains("ingle", true) || title.contains("elease", true)) {
                items.mapNotNullTo(albums) { twoRowAlbum(it, name) }
            }
        }
        return ArtistDetails(ArtistDto(id = id, name = name, genre = desc, artUrl = art), top, albums)
    }

    // ---------- playlist ----------

    fun playlist(id: String, root: JsonObject): PlaylistDetails {
        val header = root.obj("header")?.obj("musicDetailHeaderRenderer")
            ?: root.obj("header")?.obj("musicEditablePlaylistDetailHeaderRenderer")
        val title = header?.obj("title")?.runsAny()?.joinedText()?.trim() ?: ""
        val sub = (header?.obj("subtitle") ?: header?.obj("description"))?.runs()?.joinedText()?.trim() ?: ""
        val count = header?.obj("secondSubtitle")?.runsAny()?.joinedText()?.trim() ?: ""
        val pl = PlaylistDto(id = id, title = title, subtitle = sub, trackCount = count, artUrl = header?.obj("thumbnail")?.thumbUrl() ?: "")
        val tracks = sections(root).flatMap { shelfItems(it) }.mapNotNull { listItem(it) }
        return PlaylistDetails(pl, tracks)
    }

    // ---------- radio / next ----------

    fun radio(seedTrackId: String, root: JsonObject): RadioResponse {
        return try {
            val panel = root.obj("contents")?.obj("singleColumnMusicWatchNextResultsRenderer")
                ?.obj("playlist")?.obj("playlistPanelRenderer")
            val items = panel?.arr("contents")?.mapNotNull { it as? JsonObject } ?: emptyList()
            val tracks = items.mapNotNull { w ->
                val v = w.obj("playlistPanelVideoRenderer") ?: return@mapNotNull null
                val title = v.obj("title")?.runsAny()?.joinedText()?.trim() ?: return@mapNotNull null
                val videoId = v.get("videoId").text() ?: return@mapNotNull null
                val artist = v.obj("shortBylineText")?.runsAny()?.joinedText()?.trim()
                    ?: v.obj("longBylineText")?.runsAny()?.joinedText()?.trim() ?: ""
                val len = v.obj("lengthText")?.runsAny()?.joinedText()?.trim() ?: ""
                TrackDto(id = videoId, title = title, artist = artist, duration = len, durationMs = len.toDurationMs(), artUrl = v.obj("thumbnail")?.thumbUrl() ?: "", videoId = videoId)
            }
            RadioResponse(seedTrackId, tracks)
        } catch (_: Exception) {
            RadioResponse(seedTrackId)
        }
    }

    /** Lyrics tab browseId from a `next` response, or null. */
    fun lyricsBrowseId(nextRoot: JsonObject): String? {
        return try {
            nextRoot.obj("contents")?.obj("singleColumnMusicWatchNextResultsRenderer")
                ?.arr("tabs")?.mapNotNull { (it as? JsonObject)?.obj("tabRenderer") }
                ?.firstOrNull { (it.obj("title")?.runsAny()?.joinedText() ?: "").contains("yric", true) }
                ?.let { tab ->
                    tab.obj("endpoint")?.obj("browseEndpoint")?.get("browseId").text()
                        ?: tab.browseId()
                }
        } catch (_: Exception) {
            null
        }
    }

    fun lyricsText(browseRoot: JsonObject): String? {
        return try {
            val desc = sections(browseRoot).firstOrNull()
                ?.obj("musicDescriptionShelfRenderer")?.obj("description")
                ?.arr("runs")?.mapNotNull { (it as? JsonObject)?.get("text").text() }
                ?.joinToString("")?.trim()
            desc?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    fun suggestions(root: JsonObject): List<String> {
        return try {
            root.arr("contents")?.firstOrNull()?.let { (it as? JsonObject)?.obj("searchSuggestionsSectionRenderer") }
                ?.arr("contents")?.mapNotNull { (it as? JsonObject)?.obj("searchSuggestionRenderer") }
                ?.mapNotNull { it.obj("navigationEndpoint")?.obj("searchEndpoint")?.get("query").text() }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ---------- player ----------

    data class RawFormat(
        val url: String,
        val mime: String,
        val bitrate: Int,
        val sampleRate: Int,
    )

    fun rawFormats(playerRoot: JsonObject): List<RawFormat> {
        return try {
            playerRoot.obj("streamingData")?.arr("adaptiveFormats")
                ?.mapNotNull { (it as? JsonObject) }
                ?.mapNotNull { f ->
                    val mime = f.get("mimeType").text() ?: return@mapNotNull null
                    if (!mime.startsWith("audio/")) return@mapNotNull null
                    val url = f.get("url").text() ?: return@mapNotNull null // ciphered → skipped in P1
                    RawFormat(
                        url = url,
                        mime = mime,
                        bitrate = f.get("bitrate").text()?.toIntOrNull() ?: 0,
                        sampleRate = f.get("audioSampleRate").text()?.toIntOrNull() ?: 0,
                    )
                } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun hasCipheredOnly(playerRoot: JsonObject): Boolean {
        return try {
            val fmts = playerRoot.obj("streamingData")?.arr("adaptiveFormats")
                ?.mapNotNull { it as? JsonObject } ?: emptyList()
            val audio = fmts.filter { (it.get("mimeType").text() ?: "").startsWith("audio/") }
            audio.isNotEmpty() && audio.none { (it.get("url").text() ?: "").isNotEmpty() }
        } catch (_: Exception) {
            false
        }
    }

    fun pickFormat(formats: List<RawFormat>, quality: String): RawFormat? {
        if (formats.isEmpty()) return null
        val opus = formats.filter { it.mime.contains("opus") }.sortedByDescending { it.bitrate }
        val aac = formats.filter { it.mime.contains("mp4a") }.sortedByDescending { it.bitrate }
        val all = formats.sortedByDescending { it.bitrate }
        return when (quality) {
            "low" -> all.lastOrNull()
            "medium" -> (opus.firstOrNull { it.bitrate <= 160_000 } ?: opus.lastOrNull())
                ?: (aac.firstOrNull { it.bitrate <= 160_000 } ?: aac.lastOrNull())
                ?: all.lastOrNull()
            else -> opus.firstOrNull() ?: aac.firstOrNull() ?: all.firstOrNull()
        }
    }

    fun codecOf(mime: String): String = when {
        mime.contains("opus") -> "opus"
        mime.contains("mp4a") -> "aac"
        mime.contains("vorbis") -> "vorbis"
        else -> "audio"
    }

    fun expireOf(url: String): Long {
        return try {
            Regex("[?&]expire=(\\d+)").find(url)?.groupValues?.get(1)?.toLong() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun videoMeta(playerRoot: JsonObject): Triple<String, String, Long> {
        return try {
            val d = playerRoot.obj("videoDetails")
            Triple(
                d?.get("title").text() ?: "",
                d?.get("author").text() ?: "",
                d?.get("lengthSeconds").text()?.toLongOrNull()?.times(1000) ?: 0L,
            )
        } catch (_: Exception) {
            Triple("", "", 0L)
        }
    }

    fun playability(playerRoot: JsonObject): Pair<String, String> {
        return try {
            val s = playerRoot.obj("playabilityStatus")
            (s?.get("status").text() ?: "UNKNOWN") to (s?.get("reason").text() ?: "")
        } catch (_: Exception) {
            "UNKNOWN" to ""
        }
    }
}
