package com.reon.music.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * REON — Design System "Electric Horizon" Tokens
 * Strictly obeying the requested palette, radii, typography, and card shadows.
 */
object ReonTokens {
    // Canvas & Surfaces
    val Canvas = Color(0xFFF6F8FD)
    val Surface = Color(0xFFFFFFFF)
    val Muted = Color(0xFFF0F3FA)
    val Hairline = Color(0xFFE4E9F5)

    // Primary & Electric Accents
    val Primary = Color(0xFF0057FF)          // Electric Blue
    val SoftContainer = Color(0xFFE8EFFF)    // Soft Blue Container
    val DeepCobalt = Color(0xFF0038B8)       // Deep Cobalt

    // Semantic Highlights
    val Pink = Color(0xFFFF3B6B)             // Likes / Hearts only
    val SuccessGreen = Color(0xFF00C48C)     // Quality / Lossless status

    // Text Hierarchy
    val TextPrimary = Color(0xFF0B1020)
    val TextSecondary = Color(0xFF5B6480)
    val TextTertiary = Color(0xFF8B93AC)

    // Radii
    val RadiusHero = 28.dp
    val RadiusBento = 24.dp
    val RadiusInnerArt = 16.dp
    val RadiusArt20 = 20.dp
    val RadiusArt24 = 24.dp
    val RadiusPill = 999.dp

    // Corner Shapes
    val ShapeHero = RoundedCornerShape(RadiusHero)
    val ShapeBento = RoundedCornerShape(RadiusBento)
    val ShapeInnerArt = RoundedCornerShape(RadiusInnerArt)
    val ShapeArt20 = RoundedCornerShape(RadiusArt20)
    val ShapeArt24 = RoundedCornerShape(RadiusArt24)
    val ShapePill = RoundedCornerShape(50)

    // Spacing Grid
    val ScreenMargin = 16.dp
    val SectionSpacing = 24.dp
    val CardGap = 16.dp
    val ChipGap = 8.dp

    // Typography (Plus Jakarta Sans for headings/labels, Inter for body/metadata)
    val HeadlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = TextPrimary
    )

    val HeadlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextPrimary
    )

    val TitleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    )

    val BodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    )

    val BodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    )

    val LabelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Primary
    )

    val LabelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextPrimary
    )

    val LabelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = TextTertiary
    )

    // Tabular numerals for durations
    val DurationText = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = "tnum",
        color = TextTertiary
    )

    // Gradients
    val ElectricGradient = Brush.linearGradient(
        colors = listOf(Primary, DeepCobalt)
    )

    val DailyMixGradient1 = Brush.linearGradient(
        colors = listOf(Color(0xFF0057FF), Color(0xFF0038B8))
    )

    val DailyMixGradient2 = Brush.linearGradient(
        colors = listOf(Color(0xFF4338CA), Color(0xFF1E1B4B))
    )
}

/**
 * Card press animation: scale 0.97 + spring return.
 */
fun Modifier.reonCardPress(
    scaleDown: Float = 0.97f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "reon_card_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.15f)),
            onClick = onClick
        )
}

/**
 * Feather ambient shadow + active blue glow modifier for cards.
 */
fun Modifier.reonCardShadow(
    shape: RoundedCornerShape = ReonTokens.ShapeBento,
    isActive: Boolean = false,
    elevation: Dp = if (isActive) 12.dp else 4.dp
): Modifier {
    return this.shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = if (isActive) Color(0x330057FF) else Color(0x080B1020),
        spotColor = if (isActive) Color(0x400057FF) else Color(0x0D0B1020)
    )
}
