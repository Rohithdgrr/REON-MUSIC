package com.reon.music.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.ui.components.TrackArtImage

/**
 * REON — Downloads & Offline Library Screen
 * Pixel-perfect match to reference UI design with custom format badges,
 * filter pill counters, "OFFLINE READY" status pill, and track action cards.
 */
@Composable
fun DownloadsScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit,
    onDownloadAll: () -> Unit = {},
    onRemoveDownload: (String) -> Unit = {},
    onToggleOfflineMode: () -> Unit = {},
    onToggleAutoSync: () -> Unit = {},
    onToggleCellular: () -> Unit = {},
    onFilterSelect: (String) -> Unit = {},
    onQualitySelect: (String) -> Unit = {},
    onToggleQualitySelector: (Boolean) -> Unit = {},
    onShuffleAll: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onOpenPlaylist: (PlaylistItem?) -> Unit = {},
    onOpenAlbum: (AlbumItem?) -> Unit = {},
    onOpenArtist: (ArtistItem?) -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var selectedMenuTrack by remember { mutableStateOf<TrackItem?>(null) }
    var showSettingsModal by remember { mutableStateOf(false) }

    // Active filter logic
    val filteredTracks = when (state.selectedDownloadFilter) {
        "Playlists" -> state.downloadedTracks.filter { it.title.contains("Live", ignoreCase = true) || it.title.contains("Set", ignoreCase = true) }
        "Albums" -> state.downloadedTracks.filter { it.album.isNotEmpty() }
        "Tracks" -> state.downloadedTracks.filter { !it.title.contains("Set", ignoreCase = true) }
        else -> state.downloadedTracks
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FD)) // Soft canvas background from image
            .testTag("reon_downloads_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 180.dp)
        ) {
            // 1. Top Bar Header (REON DOWNLOADS + Action Icons)
            item(key = "top_brand_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Brand Emblem
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0057FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ElectricBolt,
                                contentDescription = "REON Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "REON",
                                style = ReonTokens.HeadlineMedium.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = Color(0xFF0B1020)
                                )
                            )
                            Text(
                                text = "DOWNLOADS",
                                style = ReonTokens.LabelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFF0057FF)
                                )
                            )
                        }
                    }

                    // Right Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleQualitySelector(true) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Filter Tuning",
                                tint = Color(0xFF384360),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSettingsModal = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF384360),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Main Title & Offline Ready Status Pill
            item(key = "downloads_title_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloads",
                            style = ReonTokens.HeadlineLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        // OFFLINE READY Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE6EEFF))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0057FF))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "OFFLINE READY",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0057FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Available offline anywhere · 142 tracks (8.4 GB)",
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 13.5.sp,
                            color = Color(0xFF5B6480)
                        )
                    )
                }
            }

            // 3. Filter Chips Row (All 142, Playlists 6, Albums 8, Tracks 128)
            item(key = "filter_chips_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val chipData = listOf(
                        "All" to "142",
                        "Playlists" to "6",
                        "Albums" to "8",
                        "Tracks" to "128"
                    )

                    chipData.forEach { (label, count) ->
                        val isSelected = state.selectedDownloadFilter == label || (state.selectedDownloadFilter == "All" && label == "All")

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF0057FF) else Color(0xFFE8EEFA))
                                .clickable {
                                    onFilterSelect(label)
                                    if (label == "Playlists") {
                                        onOpenPlaylist(null)
                                    } else if (label == "Albums") {
                                        onOpenAlbum(null)
                                    } else if (label == "Artists") {
                                        onOpenArtist(null)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = label,
                                    style = ReonTokens.LabelSmall.copy(
                                        color = if (isSelected) Color.White else Color(0xFF384360),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize = 13.5.sp
                                    )
                                )

                                Spacer(Modifier.width(8.dp))

                                // Badge count container
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF0040D0) else Color(0xFFD6E2F7))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = count,
                                        style = ReonTokens.LabelSmall.copy(
                                            color = if (isSelected) Color.White else Color(0xFF384360),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section Header: "Downloaded Tracks · Recently Added" + "Sort ⇅"
            item(key = "section_subheader") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Downloaded Tracks",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                        Text(
                            text = " · ",
                            style = ReonTokens.BodySmall.copy(color = Color(0xFF8B93AC))
                        )
                        Text(
                            text = "Recently Added",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 13.5.sp,
                                color = Color(0xFF5B6480)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onShowToast("Sorted by Recently Added") }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF384360),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Sort",
                            tint = Color(0xFF384360),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 5. Track List Cards
            items(filteredTracks, key = { "card_${it.id}" }) { track ->
                val isPlaying = state.currentTrack.id == track.id || track.isPlaying

                TrackCardItem(
                    track = track,
                    isPlaying = isPlaying,
                    onClick = { onTrackSelect(track) },
                    onMoreClick = { selectedMenuTrack = track }
                )
            }
        }
    }

    // Contextual Menu Dialog for track
    selectedMenuTrack?.let { track ->
        AlertDialog(
            onDismissRequest = { selectedMenuTrack = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = track.title,
                    style = ReonTokens.HeadlineMedium.copy(fontSize = 18.sp, color = Color(0xFF0B1020))
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Artist: ${track.artist}", style = ReonTokens.BodySmall)
                    Text("Quality: ${track.badge}", style = ReonTokens.BodySmall)
                    Text("Duration: ${track.duration}", style = ReonTokens.BodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveDownload(track.id)
                        selectedMenuTrack = null
                    }
                ) {
                    Text("Remove Download", color = Color(0xFFCF094C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMenuTrack = null }) {
                    Text("Cancel", color = Color(0xFF5B6480))
                }
            }
        )
    }

    // Settings Modal Dialog
    if (showSettingsModal) {
        AlertDialog(
            onDismissRequest = { showSettingsModal = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Downloads & Offline Settings", style = ReonTokens.HeadlineMedium.copy(fontSize = 18.sp))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Smart Auto-Sync (Wi-Fi)", style = ReonTokens.TitleMedium.copy(fontSize = 14.sp))
                        TextButton(onClick = { onToggleAutoSync() }) {
                            Text(if (state.isSmartAutoSyncEnabled) "ON" else "OFF", color = Color(0xFF0057FF), fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cellular Data Usage", style = ReonTokens.TitleMedium.copy(fontSize = 14.sp))
                        TextButton(onClick = { onToggleCellular() }) {
                            Text(if (state.isCellularDownloadAllowed) "Allowed" else "Wi-Fi Only", color = Color(0xFF0057FF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsModal = false }) {
                    Text("Done", color = Color(0xFF0057FF), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * High-precision Track Card Item strictly adhering to reference image design.
 */
@Composable
private fun TrackCardItem(
    track: TrackItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isPlaying) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0C002060),
                spotColor = Color(0x10002060)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(if (isPlaying) Color(0xFFF2F6FF) else Color.White)
            .border(
                width = 1.dp,
                color = if (isPlaying) Color(0xFFD2E0FF) else Color(0xFFEBF0FA),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Art Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                TrackArtImage(
                    url = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=300&q=80",
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxSize()
                )

                // Playing animated wave overlay if selected
                if (isPlaying) {
                    val transition = rememberInfiniteTransition(label = "eq_anim")
                    val bar1 by transition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "b1"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0057FF).copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Playing",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // Title, Artist, Badge & Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = ReonTokens.TitleMedium.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color(0xFF0057FF) else Color(0xFF0B1020)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = track.artist,
                    style = ReonTokens.BodySmall.copy(
                        fontSize = 12.5.sp,
                        color = Color(0xFF5B6480)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(5.dp))

                // Badge & Duration Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QualityBadgePill(badgeText = track.badge)

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "·",
                        style = ReonTokens.BodySmall.copy(color = Color(0xFF8B93AC))
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = track.duration,
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF5B6480)
                        )
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Action Icons: Blue Checkmark Badge + Three Vertical Dots
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Blue filled checkmark badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0057FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Downloaded",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = Color(0xFF5B6480),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders badges matching the reference image styles.
 */
@Composable
private fun QualityBadgePill(badgeText: String) {
    val (bg, textColor) = when {
        badgeText.contains("96kHz", ignoreCase = true) || badgeText.contains("FLAC", ignoreCase = true) ->
            Color(0xFF0057FF) to Color.White

        badgeText.contains("320", ignoreCase = true) || badgeText.contains("KBPS", ignoreCase = true) ->
            Color(0xFFECEAF7) to Color(0xFF494268)

        badgeText.contains("24-BIT", ignoreCase = true) || badgeText.contains("MASTER", ignoreCase = true) ->
            Color(0xFF1B243B) to Color.White

        badgeText.contains("LOSSLESS", ignoreCase = true) ->
            Color(0xFFE3ECFF) to Color(0xFF0043C6)

        badgeText.contains("SPATIAL", ignoreCase = true) || badgeText.contains("3D", ignoreCase = true) ->
            Color(0xFFEFE6FF) to Color(0xFF5C20C2)

        else -> Color(0xFFE8EEFA) to Color(0xFF0057FF)
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = badgeText,
            style = ReonTokens.LabelSmall.copy(
                color = textColor,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        )
    }
}
