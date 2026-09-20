package com.reon.music.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.ui.NowPlayingState
import com.reon.music.ui.theme.ReonColors
import com.reon.music.ui.theme.ReonSpacing
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun MiniWaveform(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    activeColor: Color = ReonColors.ElectricBlue,
    mutedColor: Color = ReonColors.SurfaceMuted,
    progress: Float = 0.37f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalSpacing = (barCount - 1) * 3.dp.toPx()
        val barWidth = ((width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())
        val spacing = 3.dp.toPx()

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            val baseScale = (sin(normalizedIndex * Math.PI).toFloat() * 0.75f + 0.25f)

            val animatedHeight = if (isPlaying) {
                val wave1 = abs(sin(phase + i * 0.45f))
                val wave2 = abs(sin(phase * 0.7f + i * 0.25f))
                val combined = (wave1 * 0.6f + wave2 * 0.4f) * baseScale
                val calculated = height * (0.18f + combined * 0.78f)
                calculated.coerceIn(6.dp.toPx(), height)
            } else {
                val staticCurve = (sin(normalizedIndex * Math.PI).toFloat() * 0.6f + 0.2f) * height
                staticCurve.coerceIn(5.dp.toPx(), height * 0.8f)
            }

            val x = i * (barWidth + spacing)
            val y = (height - animatedHeight) / 2f

            val isPlayed = normalizedIndex <= progress
            val barColor = if (isPlayed) activeColor else activeColor.copy(alpha = 0.25f)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, animatedHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun WaveformBentoCard(
    state: NowPlayingState,
    onPresetSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf("Hi-Fi Pure", "Spatial 3D", "Bass Boost")

    BentoCard(
        modifier = modifier.fillMaxWidth(),
        background = ReonColors.Surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = ReonColors.ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "AUDIO SPECTRUM",
                    style = MaterialTheme.typography.labelSmall,
                    color = ReonColors.TextTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(ReonColors.ElectricBlueSoft)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = state.codec,
                    style = MaterialTheme.typography.labelSmall,
                    color = ReonColors.ElectricBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.height(ReonSpacing.md))

        // Mini Waveform Display
        MiniWaveform(
            isPlaying = state.isPlaying,
            progress = state.progress
        )

        Spacer(Modifier.height(ReonSpacing.md))

        // Bottom audio presets row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            presets.forEach { preset ->
                val isSelected = state.selectedEqPreset == preset
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) ReonColors.ElectricBlue else ReonColors.SurfaceMuted
                        )
                        .clickable { onPresetSelect(preset) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else ReonColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
