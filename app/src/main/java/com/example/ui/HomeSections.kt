package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

// ==========================================
// 1. Top Bar
// ==========================================
@Composable
fun HomeTopBar(
    onNotificationClick: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: REON bolt logo in Electric Blue circle (28dp) + wordmark + caption
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ReonTokens.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ElectricBolt,
                    contentDescription = "REON Logo",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    text = "REON",
                    style = ReonTokens.HeadlineMedium.copy(
                        fontSize = 18.sp,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "UNIFIED AUDIO EXPERIENCE",
                    style = ReonTokens.LabelSmall.copy(
                        letterSpacing = 2.sp,
                        fontSize = 8.sp,
                        color = ReonTokens.TextTertiary
                    )
                )
            }
        }

        // Right: notification bell icon + analytics icon + settings gear icon (24dp, tint #0B1020, 40dp touch targets)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                        onClick = onNotificationClick
                    )
                    .testTag("top_bell_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = ReonTokens.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                        onClick = onAnalyticsClick
                    )
                    .testTag("top_analytics_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = "Analytics",
                    tint = ReonTokens.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                        onClick = onSettingsClick
                    )
                    .testTag("top_settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = ReonTokens.TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ==========================================
// 2. Greeting Block
// ==========================================
@Composable
fun HomeGreetingBlock(
    name: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        // Space Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ReonTokens.Primary)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "DEEP FOCUS SPACE",
                style = ReonTokens.LabelSmall.copy(
                    color = ReonTokens.Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
        }

        Text(
            text = "Good evening, $name",
            style = ReonTokens.HeadlineLarge
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = ReonTokens.BodySmall
        )
    }
}

// ==========================================
// 3. Search + Filters
// ==========================================
@Composable
fun HomeSearchAndFilters(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: String,
    filters: List<String>,
    onFilterSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        // Search Pill: 48dp, rounded-full, #FFFFFF fill, 1dp #E4E9F5 border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(2.dp, CircleShape, ambientColor = Color(0x0A0B1020), spotColor = Color(0x0A0B1020))
                .clip(CircleShape)
                .background(ReonTokens.Surface)
                .border(1.dp, ReonTokens.Hairline, CircleShape)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = ReonTokens.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = if (searchQuery.isEmpty()) "Search songs, artists, moods…" else searchQuery,
                    style = ReonTokens.BodyMedium.copy(
                        color = if (searchQuery.isEmpty()) ReonTokens.TextTertiary else ReonTokens.TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice Search",
                    tint = ReonTokens.TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Filter chip row: 8dp gap
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.ChipGap)
        ) {
            items(filters) { filter ->
                val isSelected = filter == selectedFilter
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) ReonTokens.SoftContainer else ReonTokens.Surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) ReonTokens.Primary.copy(alpha = 0.20f) else ReonTokens.Hairline,
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.15f)),
                            onClick = { onFilterSelect(filter) }
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .testTag("filter_chip_$filter"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        style = ReonTokens.LabelMedium.copy(
                            color = if (isSelected) ReonTokens.Primary else ReonTokens.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. Continue Listening (Hero Bento Card)
// ==========================================
@Composable
fun HomeContinueListeningHero(
    track: TrackItem,
    progress: Float,
    currentPos: String,
    remainingPos: String,
    queueText: String,
    playingInText: String,
    onCardClick: () -> Unit,
    onPlayPause: () -> Unit,
    onLike: () -> Unit,
    onSeeAll: () -> Unit,
    onMoreOptions: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 4.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue Listening",
                    style = ReonTokens.HeadlineMedium
                )
                Spacer(Modifier.width(10.dp))
                // Right-side small pill badge "NOW PLAYING" (blue dot + labelSmall, blue text)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ReonTokens.SoftContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.Primary)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "NOW PLAYING",
                            style = ReonTokens.LabelSmall.copy(
                                color = ReonTokens.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = "See all →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSeeAll
                    )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Hero Bento Card: 28dp radius, white surface, 1dp hairline-blue border, active blue glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .reonCardShadow(shape = ReonTokens.ShapeHero, isActive = true)
                .clip(ReonTokens.ShapeHero)
                .background(ReonTokens.Surface)
                .border(1.dp, ReonTokens.Primary.copy(alpha = 0.16f), ReonTokens.ShapeHero)
                .reonCardPress(onClick = onCardClick)
                .padding(16.dp)
                .testTag("continue_listening_hero")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 88dp album art (20dp radius) on left with LOSSLESS badge
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(ReonTokens.ShapeArt20)
                            .background(ReonTokens.Muted)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.art_refractions),
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // LOSSLESS quality badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xCC060913))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(ReonTokens.SuccessGreen)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "LOSSLESS",
                                    color = ReonTokens.SuccessGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    // Track details & like button
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tiny "DEEP FOCUS" pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0x140057FF))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEEP FOCUS",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = ReonTokens.Primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Heart button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (track.isLiked) Color(0x1AFF3B6B) else Color.Transparent)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = ReonTokens.Pink.copy(alpha = 0.3f)),
                                            onClick = onLike
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = "Like",
                                        tint = if (track.isLiked) ReonTokens.Pink else ReonTokens.TextTertiary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(Modifier.width(4.dp))

                                // Three dots options button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                                            onClick = onMoreOptions
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "Options",
                                        tint = ReonTokens.TextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = track.title,
                            style = ReonTokens.TitleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${track.artist} · ${track.album}",
                            style = ReonTokens.BodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        // Linear progress bar (#0057FF fill, #E4E9F5 track, 3dp, rounded)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.Hairline)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(3.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        // Row underneath: "01:24" left, "-02:23" right in labelSmall #8B93AC, tabular nums
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = currentPos, style = ReonTokens.DurationText)
                            Text(text = remainingPos, style = ReonTokens.DurationText)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Bottom action row: Queue pill, Playing in pill, previous, 48dp blue circular play button, next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark Queue pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0C1326))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = queueText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Light "Playing in ~ 14" pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ReonTokens.SoftContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = playingInText,
                                color = ReonTokens.Primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Transport controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipPrevious,
                                contentDescription = "Previous",
                                tint = ReonTokens.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 48dp Electric Blue circular play button with white play icon and glow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(10.dp, CircleShape, ambientColor = ReonTokens.Primary, spotColor = ReonTokens.Primary)
                                .clip(CircleShape)
                                .background(ReonTokens.Primary)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.3f)),
                                    onClick = onPlayPause
                                )
                                .testTag("continue_listening_play_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (track.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (track.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "Next",
                                tint = ReonTokens.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. Quick Access — 2×2 Bento
// ==========================================
@Composable
fun HomeQuickAccessBento(
    likedCount: String,
    downloadsCount: String,
    historyText: String,
    flowRadioText: String,
    onItemClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Text(
            text = "Quick Access",
            style = ReonTokens.HeadlineMedium
        )

        Spacer(Modifier.height(12.dp))

        // Row 1: Liked Songs & Downloads
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            QuickAccessTile(
                title = "Liked Songs",
                subtitle = likedCount,
                icon = Icons.Rounded.Favorite,
                iconTint = ReonTokens.Pink,
                housingBg = Color(0xFFFFE8EE),
                modifier = Modifier.weight(1f),
                onClick = { onItemClick("liked_songs") }
            )

            QuickAccessTile(
                title = "Downloads",
                subtitle = downloadsCount,
                icon = Icons.Rounded.GraphicEq,
                iconTint = ReonTokens.SuccessGreen,
                housingBg = Color(0xFFE6FAF3),
                modifier = Modifier.weight(1f),
                onClick = { onItemClick("downloads") }
            )
        }

        Spacer(Modifier.height(ReonTokens.CardGap))

        // Row 2: History & FlowRadio
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            QuickAccessTile(
                title = "History",
                subtitle = historyText,
                icon = Icons.Rounded.History,
                iconTint = ReonTokens.Primary,
                housingBg = ReonTokens.SoftContainer,
                modifier = Modifier.weight(1f),
                onClick = { onItemClick("history") }
            )

            QuickAccessTile(
                title = "FlowRadio",
                subtitle = flowRadioText,
                icon = Icons.Rounded.Radio,
                iconTint = ReonTokens.Primary,
                housingBg = ReonTokens.SoftContainer,
                modifier = Modifier.weight(1f),
                onClick = { onItemClick("flow_radio") }
            )
        }
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    housingBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .reonCardShadow(shape = ReonTokens.ShapeBento)
            .clip(ReonTokens.ShapeBento)
            .background(ReonTokens.Surface)
            .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeBento)
            .reonCardPress(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 40dp tinted rounded-square icon housing (12dp radius)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(housingBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = ReonTokens.TitleMedium.copy(fontSize = 14.sp)
                )
                Text(
                    text = subtitle,
                    style = ReonTokens.LabelSmall
                )
            }
        }
    }
}

// ==========================================
// 6. Trending Now
// ==========================================
@Composable
fun HomeTrendingNowSection(
    tracks: List<TrackItem>,
    onTrackClick: (TrackItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Trending Now", style = ReonTokens.HeadlineMedium)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFE8EE))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "🔥 HOT", color = Color(0xFFCF094C), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "See all →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            items(tracks, key = { it.id }) { track ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .reonCardPress(onClick = { onTrackClick(track) })
                ) {
                    // Square art (24dp radius) with rank badge in top-left
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(ReonTokens.ShapeArt24)
                            .background(ReonTokens.Muted)
                    ) {
                        ProceduralAlbumArt(
                            seed = track.artSeed,
                            title = track.title,
                            modifier = Modifier.fillMaxSize()
                        )

                        // 32dp dark translucent circle (rgba(0,0,0,0.55)) with white bold rank number
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${track.rank}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = track.title,
                        style = ReonTokens.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} · ${track.plays}",
                        style = ReonTokens.BodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 7. Top Charts
// ==========================================
@Composable
fun HomeTopChartsSection(
    tracks: List<TrackItem>,
    onTrackClick: (TrackItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Top Charts", style = ReonTokens.HeadlineMedium)
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ReonTokens.SoftContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "Weekly", color = ReonTokens.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                text = "See all →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            items(tracks, key = { it.id }) { track ->
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .reonCardShadow(shape = ReonTokens.ShapeBento)
                        .clip(ReonTokens.ShapeBento)
                        .background(ReonTokens.Surface)
                        .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeBento)
                        .reonCardPress(onClick = { onTrackClick(track) })
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = track.rank,
                            style = ReonTokens.HeadlineMedium.copy(
                                color = ReonTokens.Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ReonTokens.Muted)
                        ) {
                            ProceduralAlbumArt(seed = track.artSeed, title = track.title)
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = ReonTokens.TitleMedium.copy(fontSize = 13.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.SoftContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = ReonTokens.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. Featured Artists
// ==========================================
@Composable
fun HomeFeaturedArtistsSection(
    artists: List<ArtistItem>,
    onArtistClick: (ArtistItem) -> Unit,
    onFollowToggle: (String) -> Unit,
    onDiscover: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Featured Artists", style = ReonTokens.HeadlineMedium)
            Text(
                text = "Discover →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onDiscover)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            items(artists, key = { it.id }) { artist ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(88.dp)
                        .reonCardPress(onClick = { onArtistClick(artist) })
                ) {
                    // 72dp avatar (full radius, 2dp white ring)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .background(ReonTokens.Muted)
                    ) {
                        ProceduralArtistAvatar(seed = artist.artSeed, name = artist.name)
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = artist.name,
                        style = ReonTokens.LabelMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = artist.genre,
                        style = ReonTokens.LabelSmall.copy(fontSize = 10.sp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    // Follow pill button
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (artist.isFollowing) ReonTokens.Primary else Color.Transparent)
                            .border(
                                1.dp,
                                if (artist.isFollowing) ReonTokens.Primary else ReonTokens.Hairline,
                                CircleShape
                            )
                            .clickable { onFollowToggle(artist.id) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (artist.isFollowing) "Following" else "+ Follow",
                            color = if (artist.isFollowing) Color.White else ReonTokens.Primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. Artist of the Week (Editorial Bento)
// ==========================================
@Composable
fun HomeArtistOfTheWeekEditorial(
    headline: String,
    description: String,
    tag: String,
    onReadStory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
            .shadow(12.dp, ReonTokens.ShapeHero, ambientColor = Color(0x330057FF), spotColor = Color(0x400057FF))
            .clip(ReonTokens.ShapeHero)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0057FF), Color(0xFF0038B8))
                )
            )
            .border(1.dp, Color(0x40FFFFFF), ReonTokens.ShapeHero)
            .padding(20.dp)
            .testTag("artist_of_week_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x2EFFFFFF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "EXCLUSIVE INTERVIEW",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "5 min read",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                }

                Text(
                    text = tag,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headline,
                        style = ReonTokens.HeadlineMedium.copy(color = Color.White, fontSize = 18.sp)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = description,
                        style = ReonTokens.BodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(14.dp))

                // 72dp artist image thumbnail with 20dp radius
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(ReonTokens.ShapeArt20)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), ReonTokens.ShapeArt20)
                ) {
                    ProceduralAlbumArt(seed = 999, title = "ISOxo Knock2")
                }
            }

            Spacer(Modifier.height(14.dp))

            // "Read Story →" white pill button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                        onClick = onReadStory
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("read_story_button")
            ) {
                Text(
                    text = "Read story →",
                    color = ReonTokens.Primary,
                    style = ReonTokens.LabelLarge.copy(fontSize = 12.sp)
                )
            }
        }
    }
}

// ==========================================
// 10. New Releases
// ==========================================
@Composable
fun HomeNewReleasesSection(
    releases: List<TrackItem>,
    onReleaseClick: (TrackItem) -> Unit,
    onFreshDrops: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "New Releases", style = ReonTokens.HeadlineMedium)
            Text(
                text = "Fresh Drops →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onFreshDrops)
            )
        }

        Spacer(Modifier.height(12.dp))

        // 2-column grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            releases.take(2).forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .reonCardPress(onClick = { onReleaseClick(item) })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ReonTokens.ShapeArt24)
                            .background(ReonTokens.Muted)
                    ) {
                        ProceduralAlbumArt(seed = item.artSeed, title = item.title, modifier = Modifier.fillMaxSize())

                        // Tiny "New" badge in top-left
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "New",
                                color = ReonTokens.Primary,
                                style = ReonTokens.LabelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Play button
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        style = ReonTokens.TitleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.artist,
                        style = ReonTokens.BodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 11. Moods & Genres
// ==========================================
@Composable
fun HomeMoodsAndGenresSection(
    moods: List<MoodGenreItem>,
    selectedMood: String,
    onMoodSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Moods & Genres", style = ReonTokens.HeadlineMedium)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ReonTokens.SoftContainer)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(text = selectedMood, color = ReonTokens.Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ReonTokens.ScreenMargin),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.ChipGap)
        ) {
            items(moods, key = { it.id }) { mood ->
                val isSelected = mood.isSelected || mood.label == selectedMood
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) ReonTokens.Primary else Color(mood.backgroundColorHex)
                        )
                        .clickable { onMoodSelect(mood.label) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mood.emoji}  ${mood.label}",
                        color = if (isSelected) Color.White else Color(mood.textColorHex),
                        style = ReonTokens.LabelLarge
                    )
                }
            }
        }
    }
}

// ==========================================
// 12. Featured Playlists
// ==========================================
@Composable
fun HomeFeaturedPlaylistsSection(
    playlists: List<PlaylistItem>,
    onPlaylistClick: (PlaylistItem) -> Unit,
    onExplore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Featured Playlists", style = ReonTokens.HeadlineMedium)
            Text(
                text = "Explore →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onExplore)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            playlists.take(2).forEach { pl ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .reonCardShadow(shape = ReonTokens.ShapeBento)
                        .clip(ReonTokens.ShapeBento)
                        .background(ReonTokens.Surface)
                        .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeBento)
                        .reonCardPress(onClick = { onPlaylistClick(pl) })
                        .padding(8.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .clip(ReonTokens.ShapeInnerArt)
                                .background(ReonTokens.Muted)
                        ) {
                            ProceduralAlbumArt(seed = pl.artSeed, title = pl.title, modifier = Modifier.fillMaxSize())

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = pl.title,
                            style = ReonTokens.TitleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pl.trackCount} · ${pl.duration}",
                            style = ReonTokens.BodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 13. Made For You
// ==========================================
@Composable
fun HomeMadeForYouSection(
    mixes: List<DailyMixItem>,
    onMixClick: (DailyMixItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Made For You", style = ReonTokens.HeadlineMedium)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ReonTokens.SoftContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ReonTokens.Primary))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "DAILY UPDATED", color = ReonTokens.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            mixes.take(2).forEach { mix ->
                val gradient = if (mix.isPrimaryGradient) ReonTokens.DailyMixGradient1 else ReonTokens.DailyMixGradient2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp)
                        .shadow(8.dp, ReonTokens.ShapeBento, ambientColor = ReonTokens.Primary, spotColor = ReonTokens.Primary)
                        .clip(ReonTokens.ShapeBento)
                        .background(gradient)
                        .border(1.dp, Color(0x33FFFFFF), ReonTokens.ShapeBento)
                        .reonCardPress(onClick = { onMixClick(mix) })
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = mix.mixNumber,
                                color = Color.White.copy(alpha = 0.8f),
                                style = ReonTokens.LabelSmall
                            )
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = mix.title,
                                style = ReonTokens.HeadlineMedium.copy(color = Color.White, fontSize = 18.sp)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = mix.subtitle,
                                style = ReonTokens.BodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 14. Because You Played
// ==========================================
@Composable
fun HomeBecauseYouPlayedSection(
    artistName: String,
    matchBadge: String,
    tracks: List<TrackItem>,
    onTrackClick: (TrackItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Because You Played", style = ReonTokens.HeadlineMedium)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE6FAF3))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ReonTokens.SuccessGreen))
                    Spacer(Modifier.width(4.dp))
                    Text(text = matchBadge, color = ReonTokens.SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "Similar to $artistName",
            style = ReonTokens.BodySmall
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            tracks.take(2).forEach { track ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .reonCardPress(onClick = { onTrackClick(track) })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ReonTokens.ShapeArt24)
                            .background(ReonTokens.Muted)
                    ) {
                        ProceduralAlbumArt(seed = track.artSeed, title = track.title, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = track.title,
                        style = ReonTokens.TitleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = ReonTokens.BodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 15. Editor's Picks
// ==========================================
@Composable
fun HomeEditorsPicksSection(
    headline: String,
    description: String,
    onReadStory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Editor's Picks", style = ReonTokens.HeadlineMedium)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ReonTokens.SoftContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ReonTokens.Primary))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "CURATED", color = ReonTokens.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Full-width editorial bento card: 28dp radius, white fill, hairline border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .reonCardShadow(shape = ReonTokens.ShapeHero)
                .clip(ReonTokens.ShapeHero)
                .background(ReonTokens.Surface)
                .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeHero)
                .padding(14.dp)
        ) {
            Column {
                // Large 200dp hero image (20dp radius, top)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(ReonTokens.ShapeArt20)
                        .background(Color(0xFF0C1326))
                ) {
                    ProceduralTokyoSkylineArt(modifier = Modifier.fillMaxSize())

                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "SPOTLIGHT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Tiny "DEEP FOCUS" blue pill below the image
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ReonTokens.SoftContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = "DEEP FOCUS", color = ReonTokens.Primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = headline,
                    style = ReonTokens.HeadlineMedium.copy(fontSize = 18.sp)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = description,
                    style = ReonTokens.BodySmall
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Read Story →",
                    style = ReonTokens.LabelLarge,
                    modifier = Modifier.clickable(onClick = onReadStory)
                )
            }
        }
    }
}

// ==========================================
// 16. Recently Played
// ==========================================
@Composable
fun HomeRecentlyPlayedSection(
    tracks: List<TrackItem>,
    onTrackClick: (TrackItem) -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recently Played", style = ReonTokens.HeadlineMedium)
            Text(
                text = "History →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onHistoryClick)
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tracks.take(3).forEach { track ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .reonCardShadow(shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(ReonTokens.Surface)
                        .border(1.dp, ReonTokens.Hairline, RoundedCornerShape(16.dp))
                        .reonCardPress(onClick = { onTrackClick(track) })
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 44dp album art (10dp radius)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ReonTokens.Muted)
                        ) {
                            if (track.id == "rp1") {
                                Image(
                                    painter = painterResource(R.drawable.art_refractions),
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ProceduralAlbumArt(seed = track.artSeed, title = track.title)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = ReonTokens.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = ReonTokens.BodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (track.isPlaying) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Playing",
                                    color = ReonTokens.Primary,
                                    style = ReonTokens.LabelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.width(6.dp))
                                AnimatedEqualizerBars()
                            }
                        } else {
                            Text(
                                text = track.duration,
                                style = ReonTokens.DurationText
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = ReonTokens.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 17. Rediscover
// ==========================================
@Composable
fun HomeRediscoverSection(
    tracks: List<TrackItem>,
    onTrackClick: (TrackItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Rediscover", style = ReonTokens.HeadlineMedium)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ReonTokens.Muted)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(text = "3 forgotten tracks", color = ReonTokens.TextSecondary, fontSize = 9.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            tracks.take(2).forEach { track ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .reonCardPress(onClick = { onTrackClick(track) })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ReonTokens.ShapeArt24)
                            .background(ReonTokens.Muted)
                    ) {
                        ProceduralAlbumArt(seed = track.artSeed, title = track.title, modifier = Modifier.fillMaxSize())

                        // Subtle grayscale overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.25f))
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Played 3mo ago", color = Color.White, fontSize = 8.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = track.title,
                        style = ReonTokens.TitleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = ReonTokens.BodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// 18. Live & Upcoming
// ==========================================
@Composable
fun HomeLiveUpcomingSection(
    events: List<LiveEventItem>,
    onEventClick: (LiveEventItem) -> Unit,
    onNearYou: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Live & Upcoming", style = ReonTokens.HeadlineMedium)
            Text(
                text = "Near You →",
                style = ReonTokens.LabelLarge,
                modifier = Modifier.clickable(onClick = onNearYou)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ReonTokens.CardGap)
        ) {
            events.take(2).forEach { event ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .reonCardShadow(shape = ReonTokens.ShapeBento)
                        .clip(ReonTokens.ShapeBento)
                        .background(ReonTokens.Surface)
                        .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeBento)
                        .reonCardPress(onClick = { onEventClick(event) })
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Top-left tiny LIVE or UPCOMING pink pill
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFE8EE))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = event.statusBadge,
                                    color = ReonTokens.Pink,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Artist thumbnail as small circle
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary)
                            ) {
                                ProceduralArtistAvatar(seed = event.artSeed, name = event.artist)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = event.artist,
                            style = ReonTokens.TitleMedium.copy(fontSize = 13.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${event.venue} · ${event.city}",
                            style = ReonTokens.BodySmall.copy(fontSize = 10.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(10.dp))

                        // Date chip bottom-right in blue
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${event.dateMonth} ${event.dateDay}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Graphics, Equalizer & Procedural Artwork Helpers
// ==========================================

@Composable
fun AnimatedEqualizerBars() {
    val transition = rememberInfiniteTransition(label = "eq_anim")
    val bar1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(14.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * bar1).dp)
                .clip(CircleShape)
                .background(ReonTokens.Primary)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * bar2).dp)
                .clip(CircleShape)
                .background(ReonTokens.Primary)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height((14 * bar3).dp)
                .clip(CircleShape)
                .background(ReonTokens.Primary)
        )
    }
}

@Composable
fun ProceduralAlbumArt(
    seed: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    val palette = remember(seed) {
        when (seed % 5) {
            0 -> listOf(Color(0xFF0057FF), Color(0xFF0038B8), Color(0xFF0A0F24))
            1 -> listOf(Color(0xFF00C48C), Color(0xFF0057FF), Color(0xFF071B26))
            2 -> listOf(Color(0xFFFF3B6B), Color(0xFF7A3FE0), Color(0xFF1E0A24))
            3 -> listOf(Color(0xFF22D3EE), Color(0xFF0057FF), Color(0xFF061426))
            else -> listOf(Color(0xFF6366F1), Color(0xFF2563EB), Color(0xFF0B1020))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.radialGradient(
                colors = palette,
                center = Offset(w * 0.4f, h * 0.3f),
                radius = w * 0.85f
            )
        )

        // Geometric cybernetic rings
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = w * 0.35f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.20f),
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 1.dp.toPx())
        )

        // Dynamic light line
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(0f, h * 0.7f),
            end = Offset(w, h * 0.3f),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun ProceduralArtistAvatar(
    seed: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    val colors = remember(seed) {
        when (seed % 4) {
            0 -> listOf(Color(0xFF0057FF), Color(0xFF0038B8))
            1 -> listOf(Color(0xFF7A3FE0), Color(0xFF0057FF))
            2 -> listOf(Color(0xFFFF3B6B), Color(0xFF0038B8))
            else -> listOf(Color(0xFF00C48C), Color(0xFF0057FF))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ProceduralTokyoSkylineArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark atmospheric Tokyo gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF060913), Color(0xFF0E1A38), Color(0xFF0038B8))
            )
        )

        // Neon ambient glow in center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x990057FF), Color(0x3322D3EE), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.45f),
                radius = w * 0.6f
            )
        )

        // Silhouette futuristic grid
        for (i in 0..8) {
            val x = (w / 8) * i
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(x, h * 0.5f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
