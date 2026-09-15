package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * REON — System Share Intent & External URL Helper
 * Uses Android Intent.ACTION_SEND with standard Chooser for true system integration.
 */
object ShareHelper {

    fun shareTrack(context: Context, title: String, artist: String, album: String? = null) {
        val slug = title.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val link = "https://reon.audio/track/$slug"
        val extraText = buildString {
            append("🎵 Stream \"$title\" by $artist")
            if (!album.isNullOrBlank()) append(" from \"$album\"")
            append(" on REON Hi-Res Music Core (24-bit/192kHz Lossless FLAC):\n$link")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$title - $artist | REON Hi-Res")
            putExtra(Intent.EXTRA_TEXT, extraText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Track via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareArtist(context: Context, artistName: String) {
        val slug = artistName.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val link = "https://reon.audio/artist/$slug"
        val text = "🎧 Check out $artistName on REON Hi-Res Lossless Music:\n$link"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$artistName on REON")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Artist Profile via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareAlbum(context: Context, albumTitle: String, artist: String) {
        val slug = albumTitle.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val link = "https://reon.audio/album/$slug"
        val text = "💿 Listen to \"$albumTitle\" by $artist in Studio Master FLAC on REON:\n$link"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$albumTitle - $artist | REON")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Album via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePlaylist(context: Context, playlistTitle: String) {
        val slug = playlistTitle.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        val link = "https://reon.audio/playlist/$slug"
        val text = "✨ Discover \"$playlistTitle\" curated playlist on REON Hi-Res:\n$link"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$playlistTitle | REON Playlist")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share Playlist via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp(context: Context) {
        val link = "https://github.com/reon-audio/reon-android"
        val text = "⚡ REON — Ultra High-Fidelity Lossless Music Engine for Android with Bit-Perfect Audio Pipeline:\n$link"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "REON — Hi-Res Music Engine")
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Share REON via").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch share sheet", Toast.LENGTH_SHORT).show()
        }
    }

    fun openBrowserUrl(context: Context, url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Opening $url", Toast.LENGTH_SHORT).show()
        }
    }
}
