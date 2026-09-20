package com.reon.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * REON Notification Screen
 * Modern Electric Horizon layout with category tabs, unread count badge,
 * audio engine alerts, master releases, and actionable card modules.
 */
@Composable
fun NotificationScreen(
    state: HomeState,
    onBackClick: () -> Unit,
    onTrackSelect: (TrackItem) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDownloads: () -> Unit,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onClearAll: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Releases", "Audio Engine", "Downloads", "Live", "System")

    val filteredNotifications = remember(state.notifications, selectedFilter) {
        when (selectedFilter) {
            "Releases" -> state.notifications.filter { it.category.equals("RELEASE", ignoreCase = true) }
            "Audio Engine" -> state.notifications.filter { it.category.equals("AUDIO ENGINE", ignoreCase = true) }
            "Downloads" -> state.notifications.filter { it.category.equals("DOWNLOADS", ignoreCase = true) }
            "Live" -> state.notifications.filter { it.category.equals("LIVE", ignoreCase = true) }
            "System" -> state.notifications.filter { it.category.equals("SYSTEM", ignoreCase = true) }
            else -> state.notifications
        }
    }

    val unreadCount = state.notifications.count { !it.isRead }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReonTokens.Canvas)
            .testTag("reon_notification_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar
            item(key = "notification_top_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ReonTokens.Muted)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = ReonTokens.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Activity & Alerts",
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (unreadCount > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ReonTokens.Primary)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$unreadCount NEW",
                                            style = ReonTokens.LabelSmall.copy(
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "REON UNIFIED ENGINE UPDATES",
                                style = ReonTokens.LabelSmall.copy(
                                    fontSize = 8.sp,
                                    letterSpacing = 1.5.sp,
                                    color = ReonTokens.TextTertiary
                                )
                            )
                        }
                    }

                    // Action Icons: Mark all as read & Clear all
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (unreadCount > 0) {
                            IconButton(
                                onClick = onMarkAllAsRead,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.SoftContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = "Mark all as read",
                                    tint = ReonTokens.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (state.notifications.isNotEmpty()) {
                            IconButton(
                                onClick = onClearAll,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Muted)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Clear all notifications",
                                    tint = ReonTokens.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Filter Tabs
            item(key = "notification_filter_tabs") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = ReonTokens.ScreenMargin),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterTabs) { tab ->
                        val isSelected = selectedFilter == tab
                        val count = when (tab) {
                            "All" -> state.notifications.size
                            "Releases" -> state.notifications.count { it.category.equals("RELEASE", ignoreCase = true) }
                            "Audio Engine" -> state.notifications.count { it.category.equals("AUDIO ENGINE", ignoreCase = true) }
                            "Downloads" -> state.notifications.count { it.category.equals("DOWNLOADS", ignoreCase = true) }
                            "Live" -> state.notifications.count { it.category.equals("LIVE", ignoreCase = true) }
                            "System" -> state.notifications.count { it.category.equals("SYSTEM", ignoreCase = true) }
                            else -> 0
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ReonTokens.TextPrimary else Color.White)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else ReonTokens.Hairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedFilter = tab }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = tab,
                                    style = ReonTokens.LabelSmall.copy(
                                        color = if (isSelected) Color.White else ReonTokens.TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                )
                                if (count > 0) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) ReonTokens.Primary else ReonTokens.Muted
                                            )
                                            .padding(horizontal = 6.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = ReonTokens.LabelSmall.copy(
                                                color = if (isSelected) Color.White else ReonTokens.TextSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "spacer_feed") {
                Spacer(Modifier.height(8.dp))
            }

            // 3. Notification Feed Items or Empty State
            if (filteredNotifications.isEmpty()) {
                item(key = "notification_empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.SoftContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsOff,
                                    contentDescription = null,
                                    tint = ReonTokens.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "All Caught Up",
                                style = ReonTokens.TitleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = if (selectedFilter == "All")
                                    "No unread notifications or sound engine updates."
                                else
                                    "No notifications in the $selectedFilter category.",
                                style = ReonTokens.BodySmall.copy(
                                    color = ReonTokens.TextSecondary,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredNotifications,
                    key = { it.id }
                ) { item ->
                    NotificationCard(
                        item = item,
                        onClick = {
                            onMarkAsRead(item.id)
                            when {
                                item.trackId != null -> {
                                    val track = state.trendingList.find { it.id == item.trackId }
                                        ?: state.currentTrack
                                    onTrackSelect(track)
                                }
                                item.category == "AUDIO ENGINE" || item.category == "SYSTEM" -> {
                                    onOpenSettings()
                                }
                                item.category == "DOWNLOADS" -> {
                                    onOpenDownloads()
                                }
                            }
                        },
                        onActionClick = {
                            onMarkAsRead(item.id)
                            when {
                                item.trackId != null -> {
                                    val track = state.trendingList.find { it.id == item.trackId }
                                        ?: state.currentTrack
                                    onTrackSelect(track)
                                }
                                item.category == "AUDIO ENGINE" || item.category == "SYSTEM" -> {
                                    onOpenSettings()
                                }
                                item.category == "DOWNLOADS" -> {
                                    onOpenDownloads()
                                }
                            }
                        },
                        onDismiss = { onDeleteNotification(item.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: ReonNotificationItem,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val iconVector = when (item.iconType) {
        "bolt" -> Icons.Rounded.ElectricBolt
        "download" -> Icons.Rounded.DownloadDone
        "event" -> Icons.Rounded.Event
        "settings" -> Icons.Rounded.Settings
        else -> Icons.Rounded.GraphicEq
    }

    val iconBgColor = when (item.category) {
        "AUDIO ENGINE" -> ReonTokens.SoftContainer
        "RELEASE" -> Color(0xFFEFF6FF)
        "DOWNLOADS" -> Color(0xFFECFDF5)
        "LIVE" -> Color(0xFFFFF7ED)
        else -> ReonTokens.Muted
    }

    val iconTint = when (item.category) {
        "AUDIO ENGINE" -> ReonTokens.Primary
        "RELEASE" -> Color(0xFF2563EB)
        "DOWNLOADS" -> Color(0xFF059669)
        "LIVE" -> Color(0xFFEA580C)
        else -> ReonTokens.TextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin)
            .shadow(
                elevation = if (!item.isRead) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (!item.isRead) ReonTokens.Primary.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(if (!item.isRead) Color.White else Color(0xFFFCFDFE))
            .border(
                width = if (!item.isRead) 1.5.dp else 1.dp,
                color = if (!item.isRead) ReonTokens.Primary.copy(alpha = 0.3f) else ReonTokens.Hairline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Left Icon in styled squircle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Top Meta Row: Category Badge + Timestamp + Unread Dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(iconBgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.badge,
                                style = ReonTokens.LabelSmall.copy(
                                    color = iconTint,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        Text(
                            text = item.timestamp,
                            style = ReonTokens.BodySmall.copy(
                                color = ReonTokens.TextTertiary,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!item.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.Primary)
                            )
                            Spacer(Modifier.width(6.dp))
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = ReonTokens.TextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Title
                Text(
                    text = item.title,
                    style = ReonTokens.TitleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ReonTokens.TextPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                // Message description
                Text(
                    text = item.message,
                    style = ReonTokens.BodySmall.copy(
                        color = ReonTokens.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )

                Spacer(Modifier.height(10.dp))

                // Action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!item.isRead) ReonTokens.Primary else ReonTokens.Muted)
                            .clickable(onClick = onActionClick)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = item.actionText,
                            style = ReonTokens.LabelSmall.copy(
                                color = if (!item.isRead) Color.White else ReonTokens.TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
