package com.reon.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AccentColors = listOf(
    Color(0xFF0057FF), // 0: Electric Blue
    Color(0xFF00D2FF), // 1: Neon Cyan
    Color(0xFF8B5CF6), // 2: Cyber Purple
    Color(0xFF10B981), // 3: Emerald Green
    Color(0xFFFF4757), // 4: Sunset Crimson
    Color(0xFFFF3377)  // 5: Rose Magenta
)

val LightBackgrounds = listOf(
    Color(0xFFF6F8FD), // 0: Modern Ice Blue
    Color(0xFFFFFFFF), // 1: Pure Minimal White
    Color(0xFFF1F5F9), // 2: Soft Slate
    Color(0xFFFAF9F5), // 3: Warm Sand
    Color(0xFFF3F4F6)  // 4: Cool Studio
)

val DarkBackgrounds = listOf(
    Color(0xFF060913), // 0: Deep Obsidian
    Color(0xFF0B1020), // 1: Midnight Navy
    Color(0xFF000000), // 2: Pure AMOLED Black
    Color(0xFF121824), // 3: Gunmetal Slate
    Color(0xFF16161E)  // 4: Deep Charcoal
)

data class ReonCustomization(
    val accentColor: Color = AccentColors[0],
    val backgroundColor: Color = LightBackgrounds[0],
    val surfaceColor: Color = Color.White,
    val isDark: Boolean = false,
    val fontFamily: FontFamily = FontFamily.SansSerif,
    val fontScale: Float = 1.0f
)

val LocalReonCustomization = staticCompositionLocalOf { ReonCustomization() }

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

@Composable
fun ReonTheme(
    themeMode: String = "LIGHT",
    accentColorIndex: Int = 0,
    backgroundThemeIndex: Int = 0,
    fontFamilyChoice: String = "SANS_SERIF",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "DARK", "OLED" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    val primaryAccent = AccentColors.getOrElse(accentColorIndex) { AccentColors[0] }
    val isOled = themeMode == "OLED"
    
    val canvasBg = if (isOled) {
        Color(0xFF000000)
    } else if (isDark) {
        DarkBackgrounds.getOrElse(backgroundThemeIndex) { DarkBackgrounds[0] }
    } else {
        LightBackgrounds.getOrElse(backgroundThemeIndex) { LightBackgrounds[0] }
    }

    val surfaceBg = if (isDark) {
        if (isOled) Color(0xFF0A0A0A) else Color(0xFF0F1626)
    } else {
        Color(0xFFFFFFFF)
    }

    val textPrimaryColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0B1020)
    val textSecondaryColor = if (isDark) Color(0xFF8E9BAE) else Color(0xFF5B6480)
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    val selectedFontFamily = when (fontFamilyChoice) {
        "INTER" -> FontFamily.Default
        "MONOSPACE" -> FontFamily.Monospace
        "SERIF" -> FontFamily.Serif
        else -> FontFamily.SansSerif
    }

    val dynamicTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = 0.5.sp,
            color = textPrimaryColor
        ),
        titleLarge = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * fontScale).sp,
            lineHeight = (28 * fontScale).sp,
            color = textPrimaryColor
        ),
        labelMedium = TextStyle(
            fontFamily = selectedFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            color = textSecondaryColor
        )
    )

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color.Black,
            primaryContainer = if (isOled) Color(0xFF111111) else Color(0xFF162035),
            onPrimaryContainer = primaryAccent,
            background = canvasBg,
            onBackground = textPrimaryColor,
            surface = surfaceBg,
            onSurface = textPrimaryColor,
            surfaceVariant = if (isOled) Color(0xFF161616) else Color(0xFF131A2B),
            onSurfaceVariant = textSecondaryColor,
            outline = borderColor
        )
    } else {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.12f),
            onPrimaryContainer = primaryAccent,
            background = canvasBg,
            onBackground = textPrimaryColor,
            surface = surfaceBg,
            onSurface = textPrimaryColor,
            surfaceVariant = Color(0xFFF0F3FA),
            onSurfaceVariant = textSecondaryColor,
            outline = borderColor
        )
    }

    val customization = ReonCustomization(
        accentColor = primaryAccent,
        backgroundColor = canvasBg,
        surfaceColor = surfaceBg,
        isDark = isDark,
        fontFamily = selectedFontFamily,
        fontScale = fontScale
    )

    CompositionLocalProvider(LocalReonCustomization provides customization) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = dynamicTypography,
            content = content
        )
    }
}

@Composable
fun ReonTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    ReonTheme(
        themeMode = if (darkTheme) "DARK" else "LIGHT",
        content = content
    )
}

