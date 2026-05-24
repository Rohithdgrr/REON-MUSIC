/*
 * REON Music App - Download Manager
 * Copyright (c) 2024 REON
 * Enhanced download system with animations and status tracking
 */

package com.reon.music.ui.download

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

/**
 * Download status for UI
 */
data class DownloadInfo(
    val id: String,
    val title: String,
    val artist: String,
    val progress: Float,
    val status: DownloadStatus,
    val size: String,
    val downloadedSize: String
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Download Manager Composable
 */
@Composable
fun DownloadManager(
    downloads: List<DownloadInfo>,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onCancel: (String) -> Unit,
    onRetry: (String) -> Unit
) {
    var expandedDownloads by remember { mutableStateOf(setOf<String>()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause All Button
                FilledTonalButton(
                    onClick = {
                        downloads.filter { it.status == DownloadStatus.DOWNLOADING }.forEach { 
                            onPause(it.id) 
                        }
                    },
                    enabled = downloads.any { it.status == DownloadStatus.DOWNLOADING },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause All",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pause All", color = Color.White)
                }
                
                // Clear Completed Button
                OutlinedButton(
                    onClick = {
                        downloads.filter { it.status == DownloadStatus.COMPLETED }.forEach { 
                            onCancel(it.id) 
                        }
                    },
                    enabled = downloads.any { it.status == DownloadStatus.COMPLETED }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Completed",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Completed", color = Color(0xFFE53935))
                }
            }
        }
        
        // Downloads List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(downloads, key = { it.id }) { download ->
                DownloadItem(
                    download = download,
                    isExpanded = download.id in expandedDownloads,
                    onExpand = { 
                        expandedDownloads = if (download.id in expandedDownloads) {
                            expandedDownloads - download.id
                        } else {
                            expandedDownloads + download.id
                        }
                    },
                    onPause = { onPause(download.id) },
                    onResume = { onResume(download.id) },
                    onCancel = { onCancel(download.id) },
                    onRetry = { onRetry(download.id) }
                )
            }
        }
    }
}

@Composable
private fun DownloadItem(
    download: DownloadInfo,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = download.progress,
        animationSpec = tween(300, easing = LinearEasing),
        label = "progress"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = tween(300, easing = LinearEasing)
                )
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        maxLines = 2
                    )
                    Text(
                        text = download.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        maxLines = 1
                    )
                    Text(
                        text = "${download.size} • ${download.downloadedSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
                
                // Status Icon
                StatusIcon(
                    status = download.status,
                    progress = download.progress,
                    modifier = Modifier.size(24.dp)
                )
                
                // Expand/Collapse Button
                IconButton(
                    onClick = onExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Show less" else "Show more",
                        tint = Color(0xFFE53935)
                    )
                }
            }
            
            // Progress Bar (always visible when downloading)
            if (download.status == DownloadStatus.DOWNLOADING || download.status == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(12.dp))
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFE53935),
                    trackColor = Color(0xFFE0E0E0),
                    strokeCap = StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress Text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(download.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${download.downloadedSize} / ${download.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666)
                    )
                }
            }
            
            // Action Buttons (when expanded or specific status)
            if (isExpanded || download.status == DownloadStatus.PAUSED || download.status == DownloadStatus.FAILED || download.status == DownloadStatus.CANCELLED) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (download.status) {
                        DownloadStatus.DOWNLOADING -> {
                            FilledTonalButton(
                                onClick = onPause,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFFF9800)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pause", color = Color.White)
                            }
                        }
                        
                        DownloadStatus.PAUSED -> {
                            FilledTonalButton(
                                onClick = onResume,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF43A047)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resume", color = Color.White)
                            }
                        }
                        
                        DownloadStatus.FAILED, DownloadStatus.CANCELLED -> {
                            FilledTonalButton(
                                onClick = onRetry,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFE53935)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry", color = Color.White)
                            }
                            
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove", color = Color(0xFFE53935))
                            }
                        }
                        
                        else -> {}
                    }
                }
            }
            
            // Success Animation
            if (download.status == DownloadStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                
                SuccessAnimation(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Download Complete!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF43A047),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(
    status: DownloadStatus,
    progress: Float,
    modifier: Modifier = Modifier.size(24.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status")
    
    when (status) {
        DownloadStatus.PENDING -> {
            CircularProgressIndicator(
                modifier = modifier,
                color = Color(0xFF666666),
                strokeWidth = 2.dp
            )
        }
        
        DownloadStatus.DOWNLOADING -> {
            CircularProgressIndicator(
                modifier = modifier,
                color = Color(0xFFE53935),
                strokeWidth = 2.dp
            )
        }
        
        DownloadStatus.PAUSED -> {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = "Paused",
                tint = Color(0xFFFF9800),
                modifier = modifier
            )
        }
        
        DownloadStatus.COMPLETED -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = Color(0xFF43A047),
                modifier = modifier
            )
        }
        
        DownloadStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Failed",
                tint = Color(0xFFE53935),
                modifier = modifier
            )
        }
        
        DownloadStatus.CANCELLED -> {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancelled",
                tint = Color(0xFF666666),
                modifier = modifier
            )
        }
    }
}

@Composable
private fun SuccessAnimation(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF43A047),
            modifier = Modifier
                .scale(scale)
                .size(48.dp)
        )
    }
}