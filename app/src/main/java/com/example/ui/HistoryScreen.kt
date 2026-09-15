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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrackArtImage

data class HistorySection(
    val title: String,
    val isToday: Boolean,
    val sessionCount: String,
    val tracks: List<HistoryTrackItem>
)

data class HistoryTrackItem(
    val id: String,
    val title: String,
    val artist: String,
    val timeAgo: String,
    val duration: String,
    val isLiked: Boolean,
    val artSeed: Int
)

/**
 * REON — Listening History / Audiophile Library Timeline Screen
 * Pixel-perfect match to user's uploaded reference UI image:
 * Header with "REON HI-RES Audiophile Library", Timeline Active status bar,
 * Filter Chips row (All Streams, Hi-Res Only, Spatial),
 * and grouped date sections ("Today — March 30", "Yesterday — March 29").
 */
@Composable
fun HistoryScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onClearHistoryClick: () -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("All Streams") }

    val historySections = sampleHistorySections()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .testTag("reon_history_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | REON HI-RES / Audiophile Library | Share, More >)
            item(key = "history_top_bar") {
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
                            onClick = { onShowToast("History options") },
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

            // 2. Sub-Header Control Bar (Timeline Active | Filter (Hi-Res) | Trash icon)
            item(key = "history_control_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timeline Active Pill
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEEF4FF))
                            .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "TIMELINE ACTIVE",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.8.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Filter (Hi-Res) Pill Button
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFEEF4FF))
                                .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                .clickable { onShowToast("Filtering Hi-Res Audio History") }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.FilterList,
                                    contentDescription = "Filter",
                                    tint = Color(0xFF0B1020),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Filter (Hi-Res)",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0B1020),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        // Trash / Clear History Icon Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF4FF))
                                .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                .clickable { onClearHistoryClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = Color(0xFF5B6480),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Filter Chips Row (All Streams, Hi-Res Only (96k+), Spatial)
            item(key = "history_filter_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Triple("All Streams", Icons.Rounded.AllInclusive, "∞"),
                        Triple("Hi-Res Only (96k+)", Icons.Rounded.Hd, "HQ"),
                        Triple("Spatial", Icons.Rounded.SurroundSound, "3D")
                    )

                    filters.forEach { (label, icon, badge) ->
                        val isSelected = label == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF0057FF) else Color(0xFFEEF4FF))
                                .border(1.dp, if (isSelected) Color(0xFF0057FF) else Color(0xFFD4E2F8), CircleShape)
                                .clickable { selectedFilter = label }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF0057FF),
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = label,
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
            }

            // 4. Timeline Grouped Sections ("Today — March 30", "Yesterday — March 29")
            historySections.forEach { section ->
                item(key = "history_section_header_${section.title}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (section.isToday) Color(0xFF0057FF) else Color(0xFF94A3B8))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = section.title,
                                style = ReonTokens.HeadlineMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B1020)
                                )
                            )
                        }

                        Text(
                            text = section.sessionCount,
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF8A94A6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                items(section.tracks, key = { "history_item_${it.id}" }) { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEBF1FA), RoundedCornerShape(16.dp))
                            .clickable {
                                onTrackSelect(
                                    TrackItem(
                                        id = track.id,
                                        title = track.title,
                                        artist = track.artist,
                                        album = "History Stream",
                                        duration = track.duration,
                                        isLiked = track.isLiked,
                                        artSeed = track.artSeed
                                    )
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork Thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            TrackArtImage(
                                url = getHistoryArtUrl(track.artSeed),
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        // Title & Artist / Time Ago
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = ReonTokens.TitleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B1020)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${track.artist}  •  ${track.timeAgo}",
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.5.sp,
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
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        Spacer(Modifier.width(4.dp))

                        // Heart Button (filled pink/red if liked, outlined if not)
                        IconButton(
                            onClick = { onToggleLike(track.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (track.isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (track.isLiked) Color(0xFFE11D48) else Color(0xFF8A94A6),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // More options
                        IconButton(
                            onClick = { onShowToast("Options for ${track.title}") },
                            modifier = Modifier.size(34.dp)
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
}

private fun sampleHistorySections(): List<HistorySection> {
    return listOf(
        HistorySection(
            title = "Today — March 30",
            isToday = true,
            sessionCount = "4 SESSIONS",
            tracks = listOf(
                HistoryTrackItem("hist_ref", "Refractions", "Aurora Glow", "12m ago", "04:18", isLiked = true, artSeed = 1),
                HistoryTrackItem("hist_aeth", "Aether Resonance", "Aurora Glow", "34m ago", "05:40", isLiked = false, artSeed = 2),
                HistoryTrackItem("hist_chrom", "Chroma 004", "Bicep", "1h ago", "06:12", isLiked = true, artSeed = 5),
                HistoryTrackItem("hist_usb", "USB 002 Live Set", "Fred again..", "2h ago", "1h 12m", isLiked = false, artSeed = 4)
            )
        ),
        HistorySection(
            title = "Yesterday — March 29",
            isToday = false,
            sessionCount = "4 SESSIONS",
            tracks = listOf(
                HistoryTrackItem("hist_night", "Nightcall (Neon Re-edit)", "Kavinsky", "Yesterday 23:15", "04:45", isLiked = false, artSeed = 3),
                HistoryTrackItem("hist_deep", "Deep Theta Waves", "REON Focus", "Yesterday 21:00", "45:00", isLiked = true, artSeed = 6),
                HistoryTrackItem("hist_subtle", "Subtle Drift", "Aurora Glow", "Yesterday 18:40", "05:12", isLiked = false, artSeed = 7),
                HistoryTrackItem("hist_elec", "Electric Horizon", "Aurora Glow", "Yesterday 17:22", "03:58", isLiked = true, artSeed = 8)
            )
        )
    )
}

private fun getHistoryArtUrl(seed: Int): String {
    return when (seed % 6) {
        0 -> "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200&q=80"
        1 -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=200&q=80"
        2 -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200&q=80"
        3 -> "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=200&q=80"
        4 -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200&q=80"
        else -> "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=200&q=80"
    }
}
