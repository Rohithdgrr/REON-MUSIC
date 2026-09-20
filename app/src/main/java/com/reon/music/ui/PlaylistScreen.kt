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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.ui.components.TrackArtImage

/**
 * REON — Playlist Screen
 * Pixel-perfect match to user's uploaded reference UI image:
 * Hero playlist artwork with "HI-RES MQA" badge, "REON ORIGINAL" tag,
 * format pills (96kHz / 24-bit Hi-Res, Dolby Atmos, Lossless),
 * round action buttons, track counter, and track list.
 */
@Composable
fun PlaylistScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onLikeToggle: () -> Unit = {},
    onDownloadToggle: () -> Unit = {},
    onShuffleClick: () -> Unit = {},
    onPlayAllClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FD)) // Smooth background canvas
            .testTag("reon_playlist_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | Title | Share >)
            item(key = "playlist_top_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0B1020),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Audio Settings",
                        style = ReonTokens.HeadlineMedium.copy(
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        )
                    )

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF0B1020),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Centered Large Artwork Hero Card with HI-RES MQA Badge
            item(key = "playlist_artwork_hero") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color(0x18002060),
                                spotColor = Color(0x20002060)
                            )
                            .clip(RoundedCornerShape(28.dp))
                    ) {
                        TrackArtImage(
                            url = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                            contentDescription = state.activePlaylistTitle,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom-Right "HI-RES MQA" Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF181820))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "HI-RES MQA",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // REON ORIGINAL • 2 hr 14 min Subtitle Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE8EEFA))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "REON ORIGINAL",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "• 2 hr 14 min",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF5B6480)
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Playlist Title
                    Text(
                        text = state.activePlaylistTitle,
                        style = ReonTokens.HeadlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    // Playlist Description
                    Text(
                        text = state.activePlaylistDescription,
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 13.5.sp,
                            color = Color(0xFF5B6480),
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Format Tag Chips Row (96kHz / 24-bit Hi-Res, Dolby Atmos, Lossless)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 96kHz / 24-bit Hi-Res
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE8EEFA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "96kHz / 24-bit Hi-Res",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0057FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Dolby Atmos
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE8EEFA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Dolby Atmos",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0057FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Lossless
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFF0F4FA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "❇ Lossless",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF5B6480),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // 3. Row of Action Buttons (Heart, Download Check, More ..., Shuffle, Big Play FAB)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Heart Like Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isPlaylistLiked) Color(0xFFFFECEF) else Color(0xFFF0F4FA))
                                    .clickable { onLikeToggle() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (state.isPlaylistLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (state.isPlaylistLiked) Color(0xFFFF2A55) else Color(0xFF384360),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Download Checkmark Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE6EEFF))
                                    .clickable { onDownloadToggle() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Downloaded",
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // More Options ...
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF0F4FA))
                                    .clickable { onMoreOptionsClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = "More",
                                    tint = Color(0xFF384360),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shuffle Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8EEFA))
                                    .clickable { onShuffleClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Main Play FAB (Vibrant Blue Circle)
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .shadow(6.dp, CircleShape, ambientColor = Color(0x300057FF), spotColor = Color(0x400057FF))
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF))
                                    .clickable { onPlayAllClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play All",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Tracks Section Subheader ("Tracks 28" + "Default ⌄")
            item(key = "tracks_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tracks",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        // Count pill 28
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE8EEFA))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${state.activePlaylistTracks.size.coerceAtLeast(28)}",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Default dropdown button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFF0F4FA))
                            .clickable { onShowToast("Sort: Default") }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Default",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF384360),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF384360),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 5. Playlist Track List
            items(state.activePlaylistTracks, key = { "pl_trk_${it.id}" }) { track ->
                val isPlaying = state.currentTrack.id == track.id || track.id == "pl_1"

                PlaylistTrackRowItem(
                    track = track,
                    isPlaying = isPlaying,
                    onClick = { onTrackSelect(track) },
                    onMoreClick = { onShowToast("Options for ${track.title}") }
                )
            }
        }
    }
}

/**
 * Renders individual track row items matching the reference image.
 */
@Composable
private fun PlaylistTrackRowItem(
    track: TrackItem,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isPlaying) Color(0xFFF2F6FF) else Color.Transparent)
            .border(
                width = if (isPlaying) 1.dp else 0.dp,
                color = if (isPlaying) Color(0xFFD2E0FF) else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Art / Icon
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0057FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Playing",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                ) {
                    TrackArtImage(
                        url = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80",
                        contentDescription = track.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // Title, Artist & Format Tag
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = ReonTokens.TitleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color(0xFF0057FF) else Color(0xFF0B1020)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artist,
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF5B6480)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.width(6.dp))
                    Text(text = "•", style = ReonTokens.BodySmall.copy(color = Color(0xFF8B93AC)))
                    Spacer(Modifier.width(6.dp))

                    if (track.badge.contains("320") || track.badge.contains("Master") || track.badge.contains("Lossless")) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color(0xFF0057FF),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                    }

                    Text(
                        text = track.badge.ifEmpty { "Hi-Res FLAC" },
                        style = ReonTokens.LabelSmall.copy(
                            color = if (isPlaying) Color(0xFF0057FF) else Color(0xFF5B6480),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Duration
            Text(
                text = track.duration,
                style = ReonTokens.DurationText.copy(
                    fontSize = 13.sp,
                    color = if (isPlaying) Color(0xFF0057FF) else Color(0xFF5B6480)
                )
            )

            Spacer(Modifier.width(4.dp))

            // More Options (Three Dots)
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = if (isPlaying) Color(0xFF0057FF) else Color(0xFF8B93AC),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
