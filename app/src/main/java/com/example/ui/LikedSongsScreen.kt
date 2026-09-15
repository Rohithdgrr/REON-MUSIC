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
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.example.ui.components.TrackArtImage

/**
 * REON — Liked Songs / Audiophile Library Screen
 * Pixel-perfect match to user's uploaded reference UI image:
 * Header with "REON HI-RES Audiophile Library", Hero card with "Liked Masterpieces",
 * Search bar with microphone icon, Filter Chips row, and Bitrate Verified Tracklist.
 */
@Composable
fun LikedSongsScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onPlayAllClick: () -> Unit = {},
    onToggleLike: (TrackItem) -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All (428)") }

    val filterChips = listOf("All (428)", "• Hi-Res FLAC", "Electronic", "Ambient", "Modular")

    val likedTracks = remember(searchQuery, selectedFilterChip) {
        sampleLikedSongsList().filter { track ->
            (searchQuery.isEmpty() || track.title.contains(searchQuery, ignoreCase = true) || track.artist.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .testTag("reon_liked_songs_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | REON HI-RES / Audiophile Library | Share, More >)
            item(key = "liked_songs_top_bar") {
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
                            text = "REON HI-RES",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = "Audiophile Library",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
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
                            onClick = { onShowToast("Library Options") },
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

            // 2. Hero Header Card (Soft Blue Card with Liked Masterpieces title & Play All)
            item(key = "liked_songs_hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEEF4FF))
                        .border(1.dp, Color(0xFFD4E2F8), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular blue Heart badge
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = "ARCHIVE · 428 TRACKS",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = "Liked Masterpieces",
                            style = ReonTokens.HeadlineLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "28 hr 14 min • Direct DSD & 96kHz/24-Bit FLAC Master Pipeline",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 12.5.sp,
                                color = Color(0xFF5B6480)
                            )
                        )

                        Spacer(Modifier.height(18.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Play All Pill Button
                            Box(
                                modifier = Modifier
                                    .height(46.dp)
                                    .padding(end = 10.dp)
                                    .shadow(8.dp, CircleShape, spotColor = Color(0xFF0057FF).copy(alpha = 0.35f))
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF))
                                    .clickable {
                                        onPlayAllClick()
                                        if (likedTracks.isNotEmpty()) onTrackSelect(likedTracks.first())
                                        onShowToast("Playing Liked Masterpieces")
                                    }
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Play All",
                                        style = ReonTokens.TitleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                            }

                            // Circular Wave visualizer button
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                    .clickable { onShowToast("Audio Pipeline Calibration") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = "Wave",
                                    tint = Color(0xFF0057FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            // Three Dots Options button
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                    .clickable { onShowToast("Liked Songs Options") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreHoriz,
                                    contentDescription = "Options",
                                    tint = Color(0xFF0B1020),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search Bar Input (Find in 428 Liked Songs...)
            item(key = "liked_songs_search_bar") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Find in 428 Liked Songs...",
                                style = ReonTokens.BodySmall.copy(color = Color(0xFF8A94A6), fontSize = 14.sp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF5B6480),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { onShowToast("Voice search in Liked Songs") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Voice",
                                    tint = Color(0xFF5B6480),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2EAF8), RoundedCornerShape(16.dp))
                    )
                }
            }

            // 4. Filter Chips Row
            item(key = "liked_songs_filter_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterChips.forEach { chip ->
                        val isSelected = chip == selectedFilterChip
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF0057FF) else Color(0xFFEEF4FF))
                                .border(1.dp, if (isSelected) Color(0xFF0057FF) else Color(0xFFD4E2F8), CircleShape)
                                .clickable { selectedFilterChip = chip }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = chip,
                                style = ReonTokens.LabelSmall.copy(
                                    color = if (isSelected) Color.White else Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 5. Bitrate Verified Sub-Header Row
            item(key = "liked_songs_sub_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BITRATE VERIFIED  •  Audio Engine v4.8",
                        style = ReonTokens.LabelSmall.copy(
                            color = Color(0xFF8A94A6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.8.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onShowToast("Sort by Date Added") }
                    ) {
                        Text(
                            text = "Date Added",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0B1020),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF0B1020),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 6. Tracklist Items
            itemsIndexed(likedTracks, key = { _, track -> "liked_tr_${track.id}" }) { index, track ->
                val isCurrentPlaying = index == 0 && state.currentTrack.id == track.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrentPlaying) Color(0xFFEEF4FF) else Color.White)
                        .border(1.dp, if (isCurrentPlaying) Color(0xFFC2D8FF) else Color(0xFFEBF1FA), RoundedCornerShape(16.dp))
                        .clickable { onTrackSelect(track) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album art or active playing visualizer box
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        if (isCurrentPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
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
                            TrackArtImage(
                                url = getArtUrlForSeed(track.artSeed),
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Title & Artist / Duration
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentPlaying) Color(0xFF0057FF) else Color(0xFF0B1020)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${track.artist}  •  ${track.duration}",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 12.5.sp,
                                color = Color(0xFF5B6480)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Solid Blue Heart Icon
                    IconButton(
                        onClick = { onToggleLike(track) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Liked",
                            tint = Color(0xFF0057FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Options menu icon
                    IconButton(
                        onClick = { onShowToast("Options for ${track.title}") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF8A94A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun sampleLikedSongsList(): List<TrackItem> {
    return listOf(
        TrackItem("tr_ref_1", "Refractions", "Aurora Glow", "Refractions", "04:18", isLiked = true, isPlaying = true, artSeed = 1),
        TrackItem("tr_ref_2", "Nightfall Prism", "Aurora Glow", "Refractions", "04:45", isLiked = true, artSeed = 2),
        TrackItem("tr_nightcall", "Nightcall (Neon Re-edit)", "Kavinsky", "OutRun", "04:45", isLiked = true, artSeed = 3),
        TrackItem("tr_usb002", "USB 002 Live Set", "Fred again..", "USB", "1h 12m", isLiked = true, artSeed = 4),
        TrackItem("tr_chroma", "Chroma 004", "Bicep", "Chroma 004", "06:12", isLiked = true, artSeed = 5),
        TrackItem("tr_deep_theta", "Deep Theta Waves", "REON Focus", "Spatial Waves", "45:00", isLiked = true, artSeed = 6),
        TrackItem("tr_subtle_drift", "Subtle Drift", "Aurora Glow", "Refractions", "05:12", isLiked = true, artSeed = 7),
        TrackItem("tr_crystalline", "Crystalline", "Aurora Glow", "Refractions", "04:55", isLiked = true, artSeed = 8)
    )
}

private fun getArtUrlForSeed(seed: Int): String {
    return when (seed % 6) {
        0 -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80"
        1 -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200&q=80"
        2 -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80"
        3 -> "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200&q=80"
        4 -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&q=80"
        else -> "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&q=80"
    }
}
