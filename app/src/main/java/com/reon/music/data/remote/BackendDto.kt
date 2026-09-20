package com.reon.music.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Mirrors backend innertube.Dto.kt — field names are frozen. Moshi codegen must match kotlinx.serialization names. */
@JsonClass(generateAdapter = true)
data class TrackDto(
    val id: String,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val duration: String = "",
    val durationMs: Long = 0L,
    val artUrl: String = "",
    val videoId: String = "",
    val badge: String = "",
)

@JsonClass(generateAdapter = true)
data class ArtistDto(
    val id: String,
    val name: String,
    val genre: String = "",
    val artUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class AlbumDto(
    val id: String,
    val title: String,
    val artist: String = "",
    val year: String = "",
    val trackCount: String = "",
    val artUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class PlaylistDto(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val trackCount: String = "",
    val artUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class SearchResponse(
    val tracks: List<TrackDto> = emptyList(),
    val artists: List<ArtistDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
    val playlists: List<PlaylistDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class SuggestionsResponse(val suggestions: List<String> = emptyList())

@JsonClass(generateAdapter = true)
data class HomeSection(
    val title: String,
    val items: List<TrackDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class HomeResponse(val sections: List<HomeSection> = emptyList())

@JsonClass(generateAdapter = true)
data class AlbumDetails(val album: AlbumDto, val tracks: List<TrackDto> = emptyList())

@JsonClass(generateAdapter = true)
data class ArtistDetails(
    val artist: ArtistDto,
    val topTracks: List<TrackDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class PlaylistDetails(val playlist: PlaylistDto, val tracks: List<TrackDto> = emptyList())

@JsonClass(generateAdapter = true)
data class RadioResponse(val seedTrackId: String, val tracks: List<TrackDto> = emptyList())

@JsonClass(generateAdapter = true)
data class StreamResponse(
    val url: String,
    @Json(name = "codec") val codec: String,
    @Json(name = "bitrate") val bitrate: Int,
    @Json(name = "expires_at") val expiresAt: Long,
    @Json(name = "quality") val quality: String,
)

@JsonClass(generateAdapter = true)
data class LyricsResponse(val trackId: String, val text: String, val synced: Boolean = false)

@JsonClass(generateAdapter = true)
data class PlayerResponse(
    val trackId: String,
    val title: String = "",
    val artist: String = "",
    val durationMs: Long = 0L,
    val codec: String = "",
    val bitrate: Int = 0,
    val artUrl: String = "",
)

@JsonClass(generateAdapter = true)
data class ErrorBody(val error: String, val message: String = "")
