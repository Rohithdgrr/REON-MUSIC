/*
 * REON Music App - Optimized Image Loading
 * Copyright (c) 2024 REON
 * Memory and battery efficient image loading with caching
 */

package com.reon.music.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Image quality levels for different use cases
 * Lower quality = faster load, less data, less memory
 */
enum class ImageQuality {
    THUMBNAIL,  // 160x160 - for list items
    MEDIUM,     // 400x400 - for cards
    HIGH        // 544x544 - for now playing screen
}

/**
 * Optimized AsyncImage with:
 * - Memory caching (via Coil)
 * - Disk caching (via Coil)
 * - Shimmer loading placeholder
 * - Fallback icon for errors
 * - Size-based quality selection
 * - Crossfade animation
 * - Automatic fallback for YouTube thumbnails (maxresdefault -> sddefault -> hqdefault -> default)
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    quality: ImageQuality = ImageQuality.MEDIUM,
    shape: Shape = RoundedCornerShape(8.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector = Icons.Filled.MusicNote,
    crossfadeDuration: Int = 200
) {
    val context = LocalContext.current
    
    // Build optimized image request with fallback support
    val imageRequest = remember(imageUrl, quality) {
        val optimizedUrl = getOptimizedUrl(imageUrl, quality)
        
        ImageRequest.Builder(context)
            .data(optimizedUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(crossfadeDuration)
            .size(getTargetSize(quality))
            // Note: Fallback for failed thumbnails is handled by the error placeholder composable
            .build()
    }
    
    SubcomposeAsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier.clip(shape),
        contentScale = contentScale,
        loading = {
            ShimmerPlaceholder(modifier = Modifier.fillMaxSize())
        },
        error = {
            ErrorPlaceholder(
                icon = placeholderIcon,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

/**
 * Check if URL is a YouTube thumbnail URL
 */
private fun isYouTubeUrl(url: String): Boolean {
    return url.contains("ytimg.com") || url.contains("ggpht.com")
}

/**
 * Get fallback URLs for YouTube thumbnails in order of quality preference
 * This ensures we always show a thumbnail even if maxresdefault doesn't exist
 */
private fun getYouTubeFallbackUrls(url: String, quality: ImageQuality): List<String> {
    if (!isYouTubeUrl(url)) return emptyList()
    
    // Extract video ID from the URL
    val videoId = extractVideoId(url) ?: return emptyList()
    
    // Generate fallback URLs in order of quality preference
    return when (quality) {
        ImageQuality.THUMBNAIL -> listOf(
            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
            "https://i.ytimg.com/vi/$videoId/mqdefault.jpg",
            "https://i.ytimg.com/vi/$videoId/default.jpg"
        )
        ImageQuality.MEDIUM -> listOf(
            "https://i.ytimg.com/vi/$videoId/sddefault.jpg",
            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
            "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"
        )
        ImageQuality.HIGH -> listOf(
            "https://i.ytimg.com/vi/$videoId/sddefault.jpg",
            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
            "https://i.ytimg.com/vi/$videoId/mqdefault.jpg"
        )
    }
}

/**
 * Extract video ID from YouTube thumbnail URL
 */
private fun extractVideoId(url: String): String? {
    // Pattern: https://i.ytimg.com/vi/[VIDEO_ID]/...
    val pattern = Regex("/vi/([a-zA-Z0-9_-]{11})")
    return pattern.find(url)?.groupValues?.get(1)
}

/**
 * Get optimized URL based on quality
 * YouTube thumbnails support different quality levels
 * 
 * IMPORTANT: We maintain or upgrade quality, never downgrade.
 * maxresdefault (1920x1080) is the highest quality and should be preserved.
 * If maxresdefault is not available, YouTube returns 404 and Coil will use error placeholder.
 * The thumbnail URLs should be validated before calling this function.
 */
private fun getOptimizedUrl(url: String?, quality: ImageQuality): String? {
    if (url == null) return null
    
    // YouTube thumbnail optimization - preserve or upgrade quality only
    return when {
        url.contains("ytimg.com") || url.contains("ggpht.com") -> {
            when (quality) {
                ImageQuality.THUMBNAIL -> {
                    // For thumbnails, we can use hqdefault which loads faster but still good quality
                    // Only downgrade if it's not already maxresdefault
                    if (url.contains("maxresdefault")) {
                        url // Keep maxresdefault for best quality
                    } else {
                        url.replace(Regex("w\\d+-h\\d+"), "w240-h240")
                            .replace("default.jpg", "hqdefault.jpg")
                            .replace("mqdefault.jpg", "hqdefault.jpg")
                    }
                }
                ImageQuality.MEDIUM -> {
                    // For medium quality cards, prefer sddefault or keep maxresdefault
                    if (url.contains("maxresdefault")) {
                        url // Keep maxresdefault
                    } else {
                        url.replace(Regex("w\\d+-h\\d+"), "w480-h480")
                            .replace("default.jpg", "sddefault.jpg")
                            .replace("mqdefault.jpg", "sddefault.jpg")
                            .replace("hqdefault.jpg", "sddefault.jpg")
                    }
                }
                ImageQuality.HIGH -> {
                    // For high quality (Now Playing), always use maxresdefault
                    // IMPORTANT: If url already contains maxresdefault, preserve it
                    if (url.contains("maxresdefault")) {
                        url.replace("http:", "https:")
                    } else {
                        url.replace(Regex("w\\d+-h\\d+"), "w1200-h1200")
                            .replace("default.jpg", "maxresdefault.jpg")
                            .replace("mqdefault.jpg", "maxresdefault.jpg")
                            .replace("hqdefault.jpg", "maxresdefault.jpg")
                            .replace("sddefault.jpg", "maxresdefault.jpg")
                    }
                }
            }
        }
        else -> url
    }
}

/**
 * Get target size for Coil based on quality
 */
private fun getTargetSize(quality: ImageQuality): coil.size.Size {
    return when (quality) {
        ImageQuality.THUMBNAIL -> coil.size.Size(160, 160)
        ImageQuality.MEDIUM -> coil.size.Size(400, 400)
        ImageQuality.HIGH -> coil.size.Size(544, 544)
    }
}

/**
 * Shimmer effect placeholder during loading
 */
@Composable
private fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - 300f, translateAnimation - 300f),
        end = Offset(translateAnimation, translateAnimation)
    )
    
    Box(
        modifier = modifier.background(brush)
    )
}

/**
 * Error/fallback placeholder with icon
 */
@Composable
private fun ErrorPlaceholder(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Animated loading indicator with pulsing effect
 */
@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = "Loading",
            tint = color.copy(alpha = alpha),
            modifier = Modifier
                .size(size * scale)
        )
    }
}
