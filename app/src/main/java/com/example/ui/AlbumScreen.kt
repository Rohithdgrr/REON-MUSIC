package com.example.ui

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.TrackArtImage

/**
 * REON — Album Details Screen
 * Pixel-perfect match to user's uploaded reference UI image (screen.png):
 * Master Edition badge, artist verified checkmark, audio format pills,
 * play/shuffle action buttons, tracklist with active playing track card,
 * and Mastering Verification card at the bottom.
 */
@Composable
fun AlbumScreen(
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
    val album = state.selectedAlbum ?: AlbumItem(
        id = "alb_refractions",
        title = state.activeAlbumTitle,
        artist = state.activeAlbumArtist,
        year = state.activeAlbumYear,
        trackCount = "9 songs",
        genre = "Electronic",
        artSeed = 1
    )

    val tracks = if (state.activeAlbumTracks.isNotEmpty()) state.activeAlbumTracks else sampleRefractionsAlbumTracks()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .testTag("reon_album_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | Album Details / REON AUDIO | Share, More >)
            item(key = "album_top_bar") {
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

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Album Details",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                        Text(
                            text = "REON AUDIO",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                        IconButton(
                            onClick = onMoreOptionsClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFF0B1020),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Hero Album Artwork & Metadata
            item(key = "album_hero_section") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 12.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Album Cover Image
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(26.dp),
                                spotColor = Color(0xFF0057FF).copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(26.dp))
                    ) {
                        TrackArtImage(
                            url = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                            contentDescription = album.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // MASTER EDITION Badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEBF2FF))
                            .border(1.dp, Color(0xFFC2D8FF), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "MASTER EDITION",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Album Title
                    Text(
                        text = album.title,
                        style = ReonTokens.HeadlineLarge.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    // Artist Row with Verified Checkmark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = album.artist,
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 16.sp,
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified Artist",
                            tint = Color(0xFF0057FF),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Metadata line
                    Text(
                        text = "Album • ${album.year} • ${state.activeAlbumTrackCount}",
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 13.sp,
                            color = Color(0xFF5B6480)
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Audio Format Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active Chip
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFE3EDFF))
                                .border(1.dp, Color(0xFFB8D3FF), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "24-Bit / 96kHz FLAC",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0057FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Neutral Chip 1
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Dolby Atmos",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF424D6B),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Neutral Chip 2
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Hi-Res Audio",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF424D6B),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }

            // 3. Action Buttons Row (Play | Shuffle | Like | Download)
            item(key = "album_action_buttons") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0057FF))
                            .clickable {
                                onPlayAllClick()
                                if (tracks.isNotEmpty()) onTrackSelect(tracks.first())
                                onShowToast("Playing ${album.title}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Play",
                                style = ReonTokens.TitleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Shuffle Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEBF1FD))
                            .clickable {
                                onShuffleClick()
                                if (tracks.isNotEmpty()) onTrackSelect(tracks.shuffled().first())
                                onShowToast("Shuffling ${album.title}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Shuffle",
                                style = ReonTokens.TitleMedium.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Heart Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFD0DCF0), CircleShape)
                            .clickable {
                                onLikeToggle()
                                onShowToast(if (state.isAlbumLiked) "Removed from Library" else "Saved to Library")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isAlbumLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (state.isAlbumLiked) Color(0xFFFF3B6B) else Color(0xFF384360),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Download Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFD0DCF0), CircleShape)
                            .clickable {
                                onDownloadToggle()
                                onShowToast(if (state.isAlbumDownloaded) "Album downloaded" else "Downloading album...")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (state.isAlbumDownloaded) Icons.Rounded.Check else Icons.Rounded.Download,
                            contentDescription = "Download",
                            tint = Color(0xFF0057FF),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // More Options (Three Dots) Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFFD0DCF0), CircleShape)
                            .clickable {
                                onMoreOptionsClick()
                                onShowToast("Options for ${album.title}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = "More",
                            tint = Color(0xFF384360),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 4. Tracklist Header Section
            item(key = "album_tracklist_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 18.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRACKLIST • ${tracks.size} SONGS",
                        style = ReonTokens.LabelSmall.copy(
                            color = Color(0xFF5B6480),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Text(
                        text = "Spatial Audio Active",
                        style = ReonTokens.LabelSmall.copy(
                            color = Color(0xFF0057FF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.clickable {
                            onShowToast("Spatial Audio Engine Active")
                        }
                    )
                }
            }

            // 5. Tracklist Items
            itemsIndexed(tracks, key = { _, track -> "album_tr_${track.id}" }) { index, track ->
                val isCurrentPlaying = track.isPlaying || (index == 0 && state.currentTrack.id == track.id)

                if (isCurrentPlaying) {
                    // Active Track Highlighting Container Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEEF4FF))
                            .border(1.dp, Color(0xFFC2D8FF), RoundedCornerShape(16.dp))
                            .clickable { onTrackSelect(track) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Now Playing",
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = track.title,
                                        style = ReonTokens.TitleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0057FF)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (track.badge.isNotEmpty()) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF0057FF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = track.badge,
                                                style = ReonTokens.LabelSmall.copy(
                                                    color = Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = track.artist,
                                    style = ReonTokens.BodySmall.copy(
                                        fontSize = 12.sp,
                                        color = Color(0xFF4863A0)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = track.duration,
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )

                            IconButton(
                                onClick = { onShowToast("Options for ${track.title}") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More",
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Regular Track Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackSelect(track) }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF8A94A6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.width(26.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = track.title,
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0B1020)
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (track.badge.isNotEmpty()) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEEF2FA))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = track.badge,
                                            style = ReonTokens.LabelSmall.copy(
                                                color = Color(0xFF424D6B),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Text(
                                text = track.artist,
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5B6480)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = track.duration,
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF5B6480),
                                fontSize = 12.5.sp
                            )
                        )

                        IconButton(
                            onClick = { onShowToast("Options for ${track.title}") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFF8A94A6),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 6. Mastering Verification Card (Bottom Bento Box)
            item(key = "album_mastering_verification_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF0F5FF))
                        .border(1.dp, Color(0xFFD0E0FF), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = null,
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "MASTERING VERIFICATION",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Delivered bit-perfect directly from the REON Master Vault at 24-Bit / 96kHz. Acoustic calibration preserved with zero dynamic range compression.",
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF3A4766),
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REON Records © 2024",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF808DA0),
                                    fontSize = 11.sp
                                )
                            )

                            Text(
                                text = "Lossless Audio Lab",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.clickable {
                                    onShowToast("REON Lossless Audio Lab Certified")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Sample tracks matching reference image `screen.png`:
 * 1. Refractions (96kHz FLAC) - 04:18
 * 2. Nightfall Prism (Lossless) - 04:45
 * 3. Subtle Drift (24-Bit) - 05:12
 * 4. Electric Horizon (FLAC) - 03:58
 * 5. Static Bloom (Lossless) - 04:30
 * 6. Chamber Float (96kHz) - 06:14
 * 7. Atomic Velocity (Dolby Atmos) - 03:22
 * 8. Crystalline (FLAC) - 04:55
 * 9. Signal Return (Master) - 04:46
 */
private fun sampleRefractionsAlbumTracks(): List<TrackItem> {
    return listOf(
        TrackItem("tr_ref_1", "Refractions", "Aurora Glow", "Refractions", "04:18", badge = "96kHz FLAC", isPlaying = true, artSeed = 1),
        TrackItem("tr_ref_2", "Nightfall Prism", "Aurora Glow", "Refractions", "04:45", badge = "Lossless", artSeed = 1),
        TrackItem("tr_ref_3", "Subtle Drift", "Aurora Glow", "Refractions", "05:12", badge = "24-Bit", artSeed = 1),
        TrackItem("tr_ref_4", "Electric Horizon", "Aurora Glow", "Refractions", "03:58", badge = "FLAC", artSeed = 1),
        TrackItem("tr_ref_5", "Static Bloom", "Aurora Glow", "Refractions", "04:30", badge = "Lossless", artSeed = 1),
        TrackItem("tr_ref_6", "Chamber Float", "Aurora Glow", "Refractions", "06:14", badge = "96kHz", artSeed = 1),
        TrackItem("tr_ref_7", "Atomic Velocity", "Aurora Glow", "Refractions", "03:22", badge = "Dolby Atmos", artSeed = 1),
        TrackItem("tr_ref_8", "Crystalline", "Aurora Glow", "Refractions", "04:55", badge = "FLAC", artSeed = 1),
        TrackItem("tr_ref_9", "Signal Return", "Aurora Glow", "Refractions", "04:46", badge = "Master", artSeed = 1)
    )
}
