/*
 * REON Music App - Unified Song Options Sheet
 * Copyright (c) 2024 REON
 * Reusable bottom sheet for song options across all screens
 */

package com.reon.music.ui.components

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.reon.music.core.model.Song

// Color constants
private val BackgroundWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1C1C1C)
private val TextSecondary = Color(0xFF757575)
private val AccentRed = Color(0xFFE53935)
private val SurfaceLight = Color(0xFFFAFAFA)
private val AccentGreen = Color(0xFF43A047)

/**
 * Unified Song Options Bottom Sheet
 * Use this component across all screens for consistent menu behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    isDownloaded: Boolean = false,
    isLiked: Boolean = false,
    isDownloading: Boolean = false,
    downloadProgress: Int = 0,
    showDownloadOption: Boolean = true,
    showRemoveFromLibrary: Boolean = false,
    showRemoveFromPlaylist: Boolean = false,
    showGoToArtist: Boolean = true,
    showGoToAlbum: Boolean = true,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit = {},
    onRemoveDownload: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    onGoToArtist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onRemoveFromLibrary: () -> Unit = {},
    onRemoveFromPlaylist: () -> Unit = {},
    onShare: () -> Unit
) {
    val context = LocalContext.current
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Divider(
                    modifier = Modifier.fillMaxSize(),
                    color = TextSecondary.copy(alpha = 0.3f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Song Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.getHighQualityArtwork(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimary
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (song.album.isNotBlank()) {
                        Text(
                            text = song.album,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            HorizontalDivider(color = SurfaceLight)
            
            // Playback Options
            OptionItem(
                icon = Icons.Filled.PlayArrow,
                title = "Play Now",
                onClick = {
                    onPlay()
                    onDismiss()
                }
            )
            OptionItem(
                icon = Icons.Outlined.PlaylistPlay,
                title = "Play Next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )
            OptionItem(
                icon = Icons.Outlined.QueueMusic,
                title = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = SurfaceLight
            )
            
            // Library Options
            OptionItem(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                title = if (isLiked) "Remove from Favorites" else "Add to Favorites",
                iconTint = if (isLiked) AccentRed else TextPrimary,
                onClick = {
                    onToggleLike()
                    onDismiss()
                }
            )
            OptionItem(
                icon = Icons.Outlined.PlaylistAdd,
                title = "Add to Playlist",
                onClick = {
                    onAddToPlaylist()
                }
            )
            
            // Download Option
            if (showDownloadOption) {
                if (isDownloading) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = (downloadProgress.coerceIn(0, 100) / 100f),
                        animationSpec = tween(durationMillis = 300),
                        label = "download_progress"
                    )
                    OptionItem(
                        icon = Icons.Outlined.Download,
                        title = "Downloading...",
                        subtitle = if (downloadProgress > 0) "$downloadProgress%" else "Preparing",
                        iconTint = AccentRed,
                        trailingContent = {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AccentRed
                            )
                        },
                        onClick = { }
                    )
                } else if (isDownloaded) {
                    OptionItem(
                        icon = Icons.Filled.DownloadDone,
                        title = "Downloaded",
                        subtitle = "Tap to remove",
                        iconTint = AccentGreen,
                        onClick = {
                            onRemoveDownload()
                            onDismiss()
                        }
                    )
                } else {
                    OptionItem(
                        icon = Icons.Outlined.Download,
                        title = "Download",
                        onClick = {
                            onDownload()
                            onDismiss()
                        }
                    )
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = SurfaceLight
            )
            
            // Navigation Options
            if (showGoToArtist && song.artist.isNotBlank()) {
                OptionItem(
                    icon = Icons.Outlined.Person,
                    title = "Go to Artist",
                    subtitle = song.artist,
                    onClick = {
                        onGoToArtist()
                        onDismiss()
                    }
                )
            }
            if (showGoToAlbum && song.album.isNotBlank()) {
                OptionItem(
                    icon = Icons.Outlined.Album,
                    title = "Go to Album",
                    subtitle = song.album,
                    onClick = {
                        onGoToAlbum()
                        onDismiss()
                    }
                )
            }
            
            // Remove Options
            if (showRemoveFromPlaylist) {
                OptionItem(
                    icon = Icons.Outlined.RemoveCircleOutline,
                    title = "Remove from Playlist",
                    iconTint = AccentRed,
                    textColor = AccentRed,
                    onClick = {
                        onRemoveFromPlaylist()
                        onDismiss()
                    }
                )
            }
            if (showRemoveFromLibrary) {
                OptionItem(
                    icon = Icons.Outlined.Delete,
                    title = "Remove from Library",
                    iconTint = AccentRed,
                    textColor = AccentRed,
                    onClick = {
                        onRemoveFromLibrary()
                        onDismiss()
                    }
                )
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                color = SurfaceLight
            )
            
            // Share
            OptionItem(
                icon = Icons.Outlined.Share,
                title = "Share",
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Listen to ${song.title} by ${song.artist}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Song"))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = TextPrimary,
    trailingContent: (@Composable (() -> Unit))? = null,
    textColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}
