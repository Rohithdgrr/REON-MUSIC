package com.reon.music.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.R

/**
 * 19. Floating Mini Player
 * Sticky at the bottom, above the bottom nav.
 * 64dp height, 28dp radius, white rgba(255,255,255,0.96) fill, 1dp hairline-blue border, 12dp blue ambient glow.
 * Contents: 48dp album art (12dp radius), track title (bodyMedium), artist (bodySmall),
 * previous icon, 40dp blue play button, next icon, like icon.
 */
@Composable
fun MiniPlayerDock(
    track: TrackItem,
    isPlaying: Boolean,
    isLiked: Boolean,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin)
            .shadow(
                elevation = 14.dp,
                shape = ReonTokens.ShapeHero,
                ambientColor = Color(0x330057FF),
                spotColor = Color(0x400057FF)
            )
            .clip(ReonTokens.ShapeHero)
            .background(Color.White.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = ReonTokens.Primary.copy(alpha = 0.18f),
                shape = ReonTokens.ShapeHero
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.12f)),
                onClick = onExpand
            )
            .height(64.dp)
            .padding(horizontal = 10.dp)
            .testTag("floating_mini_player")
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: 48dp album art (12dp radius) + title & artist (tap to expand)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.08f)),
                        onClick = onExpand
                    )
                    .testTag("mini_track_info")
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReonTokens.SoftContainer)
                        .border(1.dp, ReonTokens.Hairline, RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.art_refractions),
                        contentDescription = "Mini Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.Primary)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = track.title,
                            style = ReonTokens.BodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${track.artist} · Swipe up to expand",
                        style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Like, Previous, 40dp Blue Play Button, Next
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Like Button (Pink when liked)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ReonTokens.Pink.copy(alpha = 0.2f)),
                            onClick = onLike
                        )
                        .testTag("mini_like_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isLiked) "Favorited" else "Favorite",
                        tint = if (isLiked) ReonTokens.Pink else ReonTokens.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.15f)),
                            onClick = onPrevious
                        )
                        .testTag("mini_prev_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = ReonTokens.TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // 40dp Electric Blue circular play button with white play icon and glow
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(8.dp, CircleShape, ambientColor = ReonTokens.Primary, spotColor = ReonTokens.Primary)
                        .clip(CircleShape)
                        .background(ReonTokens.Primary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.3f)),
                            onClick = onPlayPause
                        )
                        .testTag("mini_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Next Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.15f)),
                            onClick = onNext
                        )
                        .testTag("mini_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = ReonTokens.TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
