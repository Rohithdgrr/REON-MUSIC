package com.reon.music.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.reon.music.ui.theme.ReonColors

@Composable
fun TrackArtImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var isSuccess by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(targetValue = if (isSuccess) 1.0f else 0.0f, label = "image_fade")

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(ReonColors.SurfaceMuted, ReonColors.ElectricBlueSoft)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .crossfade(250)
                .allowHardware(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                // Downsample to appropriate thumbnail dimensions to avoid heavy memory allocation
                .size(128, 128)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            onSuccess = { isSuccess = true },
            onError = { isSuccess = false },
            modifier = Modifier
                .fillMaxSize()
                .alpha(fadeAlpha)
        )
        
        // Show subtle emblem in center as fallback if image isn't loaded yet/fails
        if (!isSuccess) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = ReonColors.ElectricBlue.copy(alpha = 0.25f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

