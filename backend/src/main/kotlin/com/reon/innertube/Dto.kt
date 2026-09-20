package com.reon.innertube

import kotlinx.serialization.Serializable

/** Stable public contract. Field names are frozen — the Android app mirrors them. */
@Serializable
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

@Serializable
data class ArtistDto(
    val id: String,
    val name: String,
    val genre: String = "",
    val artUrl: String = "",
)

@Serializable
data class AlbumDto(
    val id: String,
    val title: String,
    val artist: String = "",
    val year: String = "",
    val trackCount: String = "",
    val artUrl: String = "",
)

@Serializable
data class PlaylistDto(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val trackCount: String = "",
    val artUrl: String = "",
)

@Serializable
data class SearchResponse(
    val tracks: List<TrackDto> = emptyList(),
    val artists: List<ArtistDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
    val playlists: List<PlaylistDto> = emptyList(),
)

@Serializable
data class SuggestionsResponse(val suggestions: List<String> = emptyList())

@Serializable
data class HomeSection(
    val title: String,
    val items: List<TrackDto> = emptyList(),
)

@Serializable
data class HomeResponse(val sections: List<HomeSection> = emptyList())

@Serializable
data class AlbumDetails(val album: AlbumDto, val tracks: List<TrackDto> = emptyList())

@Serializable
data class ArtistDetails(
    val artist: ArtistDto,
    val topTracks: List<TrackDto> = emptyList(),
    val albums: List<AlbumDto> = emptyList(),
)

@Serializable
data class PlaylistDetails(val playlist: PlaylistDto, val tracks: List<TrackDto> = emptyList())

@Serializable
data class RadioResponse(val seedTrackId: String, val tracks: List<TrackDto> = emptyList())

@Serializable
data class StreamResponse(
    val url: String,
    val codec: String,
    val bitrate: Int,
    val expires_at: Long,
    val quality: String,
)

@Serializable
data class LyricsResponse(val trackId: String, val text: String, val synced: Boolean = false)

@Serializable
data class PlayerResponse(
    val trackId: String,
    val title: String = "",
    val artist: String = "",
    val durationMs: Long = 0L,
    val codec: String = "",
    val bitrate: Int = 0,
    val artUrl: String = "",
)

@Serializable
data class ErrorBody(val error: String, val message: String = "")
