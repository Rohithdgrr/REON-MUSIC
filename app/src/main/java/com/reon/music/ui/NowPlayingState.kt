package com.reon.music.ui

import com.reon.music.data.MusicTrack

data class NowPlayingState(
    val currentTrack: MusicTrack,
    val nextTrack: MusicTrack,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val positionMs: Long = 0L,
    val shuffleOn: Boolean = false,
    val repeatOn: Boolean = false,
    val isLiked: Boolean = false,
    val selectedEqPreset: String = "Binaural Spatial",
    val audioOutputDevice: String = "REON Player",
    val sleepTimer: String = "45m",
    val isLyricsExpanded: Boolean = false,
    val isQueueExpanded: Boolean = false,
    val isMenuOpen: Boolean = false,
    val isPlaylistDialogOpen: Boolean = false,
    val isDownloaded: Boolean = false,
    val isMinimized: Boolean = false,
    val toastMessage: String? = null,
    val relatedTracks: List<MusicTrack> = emptyList(),
    val queueTracks: List<MusicTrack> = emptyList(),
    val isLoading: Boolean = false,
    val streamUrl: String? = null,
) {
    val title: String get() = currentTrack.title
    val artist: String get() = currentTrack.artist
    val album: String get() = currentTrack.album
    val category: String get() = currentTrack.category
    val albumArtUrl: String get() = currentTrack.albumArtUrl
    val albumArtResId: Int? get() = currentTrack.albumArtResId
    val artistImageUrl: String get() = currentTrack.artistImageUrl
    val source: String get() = currentTrack.source
    val quality: String get() = currentTrack.quality
    val spatialMode: String get() = currentTrack.spatialMode
    val codec: String get() = currentTrack.codec
    val sampleRate: String get() = currentTrack.sampleRate
    val lyricsQuote: String get() = currentTrack.lyricsQuote
    val lyricsPreview: String get() = currentTrack.lyrics.joinToString("\n") { it.text }.take(180)

    val nextTrackTitle: String get() = nextTrack.title
    val nextTrackArtist: String get() = nextTrack.artist
    val nextTrackArtUrl: String get() = nextTrack.albumArtUrl

    val positionLabel: String get() = MusicTrack.formatTime(positionMs)
    val remainingLabel: String get() = MusicTrack.formatNegativeRemaining(positionMs, currentTrack.durationMs)
    val durationLabel: String get() = currentTrack.durationLabel
}
