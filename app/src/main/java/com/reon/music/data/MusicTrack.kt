package com.reon.music.data

import com.reon.music.R

data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * Split quality story: LOCAL_HIRES = real FLAC/ALAC/WAV (96kHz/192kHz), YT_STREAM = Opus/AAC 128-160kbps never labeled Hi-Res.
 * See SourceKind in ReonDatabase.kt
 */
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val category: String = "DEEP FOCUS",
    val durationMs: Long,
    val albumArtUrl: String = "",
    val albumArtResId: Int? = null,
    val artistImageUrl: String = "",
    val source: String = "FLAC Master Direct",
    val quality: String = "96KHZ · FLAC",
    val spatialMode: String = "Binaural Spatial",
    val codec: String = "FLAC",
    val sampleRate: String = "24-bit / 96kHz",
    val monthlyListeners: String = "48.6M monthly",
    val lyricsQuote: String = "“Floating through shards of electric light\nCaught in the frequency of the night”",
    val lyrics: List<LyricLine> = emptyList(),
    val relatedTrackIds: List<String> = emptyList(),
    val isLiked: Boolean = false,
    val videoId: String = "",
    val sourceKind: String = SourceKind.LOCAL_HIRES,
    val streamUrl: String? = null,
    val streamExpiresAt: Long = 0L,
) {
    /** Enforce labeling contract: YT_STREAM never exposes FLAC/Hi-Res descriptors. */
    fun normalized(): MusicTrack {
        if (sourceKind != SourceKind.YT_STREAM) return this
        val isHiResCodec = codec.contains("FLAC", true) || codec.contains("DSD", true) || codec.contains("ALAC", true) || codec.contains("WAV", true)
        val isHiResQuality = quality.contains("FLAC", true) || quality.contains("96", true) || quality.contains("192", true) || quality.contains("Hi-Res", true) || quality.contains("192kHz", true)
        val isHiResRate = sampleRate.contains("96kHz", true) || sampleRate.contains("192kHz", true) || sampleRate.contains("24-bit", true) && codec.contains("FLAC", true)
        if (!isHiResCodec && !isHiResQuality && !isHiResRate) return this
        // Force Opus/AAC for YT
        val forcedCodec = when {
            codec.contains("opus", true) -> "Opus"
            codec.contains("aac", true) || codec.contains("mp4a", true) -> "AAC"
            else -> "Opus"
        }
        return copy(
            codec = forcedCodec,
            quality = if (forcedCodec == "Opus") "Opus · 160kbps" else "AAC · 128kbps",
            sampleRate = "48kHz",
            spatialMode = "Stereo",
            source = "YT Stream",
            category = "YT Stream",
        )
    }

    val durationLabel: String
        get() = formatTime(durationMs)

    companion object {
        fun formatTime(ms: Long): String {
            val totalSeconds = (ms / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        fun formatNegativeRemaining(positionMs: Long, totalMs: Long): String {
            val remainingSeconds = ((totalMs - positionMs).coerceAtLeast(0) / 1000)
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "-%02d:%02d".format(minutes, seconds)
        }

        val sampleTracks = listOf(
            MusicTrack(
                id = "track_refractions",
                title = "Refractions",
                artist = "Aurora Glow",
                album = "REFRACTIONS",
                category = "DEEP FOCUS",
                durationMs = 227000L, // 01:24 (84s) + 02:23 (143s) = 227s
                albumArtResId = R.drawable.art_refractions,
                albumArtUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&auto=format&fit=crop&q=80",
                artistImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                source = "Direct FLAC Studio",
                quality = "96KHZ · FLAC",
                spatialMode = "Binaural Spatial",
                codec = "FLAC",
                sampleRate = "24-bit / 96kHz",
                monthlyListeners = "62.4M monthly",
                lyricsQuote = "“Floating through shards of electric light\nCaught in the frequency of the night”",
                lyrics = listOf(
                    LyricLine(0L, "Floating through shards of electric light"),
                    LyricLine(84000L, "Caught in the frequency of the night"),
                    LyricLine(120000L, "Resonating across the crystalline void"),
                    LyricLine(160000L, "Submerged in frequencies of peace")
                ),
                isLiked = false
            ),
            MusicTrack(
                id = "track_2",
                title = "Cybernetic Dreams",
                artist = "Neon Mirage",
                album = "Synthetic Odyssey",
                category = "SYNTH AMBIENCE",
                durationMs = 195000L,
                albumArtResId = R.drawable.art_refractions,
                albumArtUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
                artistImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                source = "Qobuz Studio Hi-Res",
                quality = "192KHZ · DSD",
                spatialMode = "Dolby Atmos",
                codec = "DSD / FLAC",
                sampleRate = "32-bit / 192kHz",
                monthlyListeners = "18.2M monthly",
                lyricsQuote = "“Digital waves across the neural sky\nInfinite echoes passing by”",
                lyrics = listOf(
                    LyricLine(0L, "Digital waves across the neural sky"),
                    LyricLine(55000L, "Drifting in quantum melodies")
                ),
                isLiked = true
            )
        )
    }
}
