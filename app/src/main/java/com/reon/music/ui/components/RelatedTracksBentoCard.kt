package com.reon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Recommend
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.data.MusicTrack
import com.reon.music.ui.theme.ReonColors
import com.reon.music.ui.theme.ReonSpacing

@Composable
fun RelatedTracksBentoCard(
    tracks: List<MusicTrack>,
    onTrackSelect: (MusicTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 0.dp // we handle internal padding to let horizontal scroll go edge to edge inside card
    ) {
        Column(modifier = Modifier.padding(top = ReonSpacing.lg, start = ReonSpacing.lg, end = ReonSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Recommend,
                        contentDescription = null,
                        tint = ReonColors.ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "RELATED TRACKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ReonColors.TextTertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Scroll to explore",
                    style = MaterialTheme.typography.labelSmall,
                    color = ReonColors.ElectricBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(ReonSpacing.md))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = ReonSpacing.lg,
                end = ReonSpacing.lg,
                bottom = ReonSpacing.lg
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                RelatedTrackItem(track = track, onClick = { onTrackSelect(track) })
            }
        }
    }
}

@Composable
private fun RelatedTrackItem(
    track: MusicTrack,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .width(220.dp)
            .clip(shape)
            .background(ReonColors.SurfaceMuted)
            .border(1.dp, ReonColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrackArtImage(
            url = track.albumArtUrl,
            contentDescription = track.title,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = ReonColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.labelSmall,
                color = ReonColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track.durationLabel,
                style = MaterialTheme.typography.labelSmall,
                color = ReonColors.TextTertiary,
                fontSize = 10.sp
            )
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ReonColors.ElectricBlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play track",
                tint = ReonColors.ElectricBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
