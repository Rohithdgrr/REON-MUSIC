package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ReonColors {
    // Electric Blue scale
    val ElectricBlue       = Color(0xFF0057FF)   // primary action
    val ElectricBlueDeep   = Color(0xFF0038B8)   // pressed state
    val ElectricBlueSoft   = Color(0xFFE8EFFF)   // container fill
    val ElectricBlueGlow   = Color(0x330057FF)   // shadow tint

    // Obsidian Dark theme (Deep Focus UI)
    val ObsidianBg         = Color(0xFF060913)
    val ObsidianSurface    = Color(0xFF0F1626)
    val ObsidianSurfaceBtn = Color(0xFF131A2B)
    val ObsidianBorder     = Color(0xFF1A263B)
    val NeonCyan           = Color(0xFF00E5FF)
    val NeonCyanSoft       = Color(0xFF00D2FF)
    val NeonBlue           = Color(0xFF0066FF)
    val DarkTextPrimary    = Color(0xFFFFFFFF)
    val DarkTextSecondary  = Color(0xFF7E8EA2)
    val DarkTextTertiary   = Color(0xFF5C6C82)
    val DarkLyrics         = Color(0xFFC0CEE0)
    val HeartMagenta       = Color(0xFFFF3377)

    // Surfaces (light theme)
    val Background         = Color(0xFFF6F8FD)
    val Surface            = Color(0xFFFFFFFF)
    val SurfaceMuted       = Color(0xFFF0F3FA)
    val Border             = Color(0xFFE4E9F5)

    // Text
    val TextPrimary        = Color(0xFF0B1020)
    val TextSecondary      = Color(0xFF5B6480)
    val TextTertiary       = Color(0xFF8B93AC)

    // Semantic
    val Like               = Color(0xFFFF3B6B)
    val Success            = Color(0xFF00C48C)
}

object ReonSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val cardRadius = 24.dp
    val heroRadius = 28.dp
}

private val ReonDarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = ReonColors.NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = ReonColors.ObsidianSurface,
    onPrimaryContainer = ReonColors.NeonCyan,
    background = ReonColors.ObsidianBg,
    onBackground = ReonColors.DarkTextPrimary,
    surface = ReonColors.ObsidianSurface,
    onSurface = ReonColors.DarkTextPrimary,
    surfaceVariant = ReonColors.ObsidianSurfaceBtn,
    onSurfaceVariant = ReonColors.DarkTextSecondary,
    outline = ReonColors.ObsidianBorder,
)

private val ReonLightColorScheme = lightColorScheme(
    primary = ReonColors.ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ReonColors.ElectricBlueSoft,
    onPrimaryContainer = ReonColors.ElectricBlueDeep,
    background = ReonColors.Background,
    onBackground = ReonColors.TextPrimary,
    surface = ReonColors.Surface,
    onSurface = ReonColors.TextPrimary,
    surfaceVariant = ReonColors.SurfaceMuted,
    onSurfaceVariant = ReonColors.TextSecondary,
    outline = ReonColors.Border,
)

@Composable
fun ReonTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ReonDarkColorScheme else ReonLightColorScheme,
        typography = Typography,
        content = content
    )
}
