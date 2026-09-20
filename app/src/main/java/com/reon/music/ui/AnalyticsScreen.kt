package com.reon.music.ui

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.R

/**
 * REON Analytics Screen — Built precisely matching the reference UI.
 */
@Composable
fun AnalyticsScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit = {},
    onBackClick: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onLike: () -> Unit = {},
    onOpenNowPlaying: () -> Unit = {},
    onShowToast: (String) -> Unit = {}
) {
    var selectedPeriod by remember { mutableStateOf("This Month") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FE))
            .testTag("analytics_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("analytics_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Analytics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // Main Scrollable Analytics Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 18.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Period Selector Tabs
                item(key = "period_selector") {
                    PeriodSelectorTabs(
                        selectedPeriod = selectedPeriod,
                        onSelectPeriod = {
                            selectedPeriod = it
                            onShowToast("Analytics updated for $it")
                        }
                    )
                }

                // 2. Total Resonance Card
                item(key = "total_resonance_card") {
                    TotalResonanceCard(period = selectedPeriod)
                }

                // 3. Listening Streak Card
                item(key = "streak_card") {
                    StreakCard()
                }

                // 4. Sonic Spectrum Card
                item(key = "sonic_spectrum") {
                    SonicSpectrumCard()
                }

                // 5. Heavy Rotation Section
                item(key = "heavy_rotation") {
                    HeavyRotationSection(
                        onTrackSelect = onTrackSelect,
                        onViewAll = { onShowToast("Viewing all top listened artists") }
                    )
                }

                // 6. Acoustic Insights Section
                item(key = "acoustic_insights") {
                    AcousticInsightsSection()
                }

                // 7. Fidelity Breakdown Card
                item(key = "fidelity_breakdown") {
                    FidelityBreakdownCard()
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }

        // Mini Player Bar at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            AnalyticsMiniPlayerBar(
                track = state.currentTrack,
                isPlaying = state.currentTrack.isPlaying,
                isLiked = state.currentTrack.isLiked,
                onPlayPause = onPlayPause,
                onLike = onLike,
                onExpand = onOpenNowPlaying
            )
        }
    }
}

@Composable
private fun PeriodSelectorTabs(
    selectedPeriod: String,
    onSelectPeriod: (String) -> Unit
) {
    val options = listOf("This Week", "This Month", "All Time")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFEEF2FF))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option == selectedPeriod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) Color(0xFF0055FF) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectPeriod(option) }
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF475569)
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalResonanceCard(period: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL RESONANCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    color = Color(0xFF64748B)
                )

                // Growth Badge +14%
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0EDFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF0055FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "+14%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0055FF)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Big Metric: 42.8 hrs
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (period == "This Week") "11.2" else if (period == "All Time") "482.0" else "42.8",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "hrs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055FF),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // Subheader Row for Bar Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Activity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )

                Text(
                    text = "Thu: 3.4 hrs peak",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055FF)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Daily Activity Bar Chart Component
            DailyActivityBarChart()
        }
    }
}

@Composable
private fun DailyActivityBarChart() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val heights = listOf(0.40f, 0.25f, 0.35f, 1.0f, 0.65f, 0.50f, 0.30f)
    val highlightIndex = 3 // Thursday

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            val isHighlight = index == highlightIndex
            val barHeightFraction = heights[index]

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Tooltip for Peak Day (Thursday)
                if (isHighlight) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0055FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "3.4h",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                } else {
                    Spacer(Modifier.height(23.dp))
                }

                // Vertical Bar
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(90.dp * barHeightFraction)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isHighlight) Color(0xFF0055FF) else Color(0xFFDCE6FF)
                        )
                        .then(
                            if (isHighlight) {
                                Modifier.shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0xFF0055FF))
                            } else Modifier
                        )
                )

                Spacer(Modifier.height(8.dp))

                // Day Label
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHighlight) Color(0xFF0055FF) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun StreakCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEEF2FF))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Waveform Icon in Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCE6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Color(0xFF0055FF),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "18 Days Streak",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Top 4% consistent listener",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Streak Pill
            Text(
                text = "18d",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        }
    }
}

@Composable
private fun SonicSpectrumCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sonic Spectrum",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "4 Dominant Vibrations",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                Box(modifier = Modifier.weight(0.42f).fillMaxHeight().background(Color(0xFF0055FF)))
                Box(modifier = Modifier.weight(0.28f).fillMaxHeight().background(Color(0xFF2563EB)))
                Box(modifier = Modifier.weight(0.18f).fillMaxHeight().background(Color(0xFF1D4ED8)))
                Box(modifier = Modifier.weight(0.12f).fillMaxHeight().background(Color(0xFFDBEAFE)))
            }

            Spacer(Modifier.height(18.dp))

            // 2x2 Legend Grid
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SpectrumLegendItem(
                        color = Color(0xFF0055FF),
                        title = "Ambient & Drone",
                        stat = "42% • 18.0 hrs",
                        modifier = Modifier.weight(1f)
                    )
                    SpectrumLegendItem(
                        color = Color(0xFF2563EB),
                        title = "Synthwave / Cyber",
                        stat = "28% • 12.0 hrs",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    SpectrumLegendItem(
                        color = Color(0xFF1D4ED8),
                        title = "Deep Electronic",
                        stat = "18% • 7.7 hrs",
                        modifier = Modifier.weight(1f)
                    )
                    SpectrumLegendItem(
                        color = Color(0xFFDBEAFE),
                        title = "Lo-Fi Downtempo",
                        stat = "12% • 5.1 hrs",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpectrumLegendItem(
    color: Color,
    title: String,
    stat: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A)
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = stat,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun HeavyRotationSection(
    onTrackSelect: (TrackItem) -> Unit,
    onViewAll: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Heavy Rotation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0055FF),
                    modifier = Modifier.clickable { onViewAll() }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Item 1 (Aurora Glow - Featured)
            HeavyRotationItem1()

            Spacer(Modifier.height(14.dp))

            // Item 2
            HeavyRotationItem(
                rank = "2",
                title = "Komorebi Pulse",
                stat = "38 plays • 8.4 hrs",
                label = "Solitude",
                artSeed = 2
            )

            Spacer(Modifier.height(12.dp))

            // Item 3
            HeavyRotationItem(
                rank = "3",
                title = "Hyperion 04",
                stat = "29 plays • 6.1 hrs",
                label = "Voxel Dusk",
                artSeed = 3
            )

            Spacer(Modifier.height(12.dp))

            // Item 4
            HeavyRotationItem(
                rank = "4",
                title = "Null Vector",
                stat = "21 plays • 4.8 hrs",
                label = "Stasis",
                artSeed = 4
            )
        }
    }
}

@Composable
private fun HeavyRotationItem1() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rank Badge overlapping Album Art
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                ) {
                    Image(
                        painter = painterResource(R.drawable.art_refractions),
                        contentDescription = "Aurora Glow",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Rank 1 pill badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0055FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = "Aurora Glow",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Top track: ",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Refractions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0055FF)
                    )
                }
                Text(
                    text = "14.2 hrs listened",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Equalizer Animation Circle Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.BarChart,
                contentDescription = null,
                tint = Color(0xFF0055FF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeavyRotationItem(
    rank: String,
    title: String,
    stat: String,
    label: String,
    artSeed: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = rank,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                modifier = Modifier.width(18.dp)
            )

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            ) {
                Image(
                    painter = painterResource(R.drawable.art_refractions),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stat,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
    }
}

@Composable
private fun AcousticInsightsSection() {
    Column {
        Text(
            text = "Acoustic Insights",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bento Card 1: Peak Window
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = Color(0xFF0055FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "PEAK WINDOW",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF0055FF)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "11 PM – 2 AM",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "Late Night Deep Focus",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "62% of streams",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0055FF)
                    )
                }
            }

            // Bento Card 2: Exploration
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = Color(0xFF0055FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "EXPLORATION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color(0xFF0055FF)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "184",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "New tracks unlocked",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "+32 this week",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0055FF)
                    )
                }
            }
        }
    }
}

@Composable
private fun FidelityBreakdownCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .padding(18.dp)
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
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0055FF))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HQ",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = "FIDELITY BREAKDOWN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF0055FF)
                    )
                }

                Text(
                    text = "96kHz / 24-bit DAC Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pill 1: Lossless FLAC 74%
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEEF2FF))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Lossless FLAC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "74%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Pill 2: Hi-Res Studio 26%
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE0EDFF))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Hi-Res Studio",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "26%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0055FF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMiniPlayerBar(
    track: TrackItem,
    isPlaying: Boolean,
    isLiked: Boolean,
    onPlayPause: () -> Unit,
    onLike: () -> Unit,
    onExpand: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .clickable { onExpand() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Image(
                        painter = painterResource(R.drawable.art_refractions),
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = track.title.ifEmpty { "Refractions" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${track.artist.ifEmpty { "Aurora Glow" }} • FLAC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) Color(0xFFF43F5E) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0055FF))
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
