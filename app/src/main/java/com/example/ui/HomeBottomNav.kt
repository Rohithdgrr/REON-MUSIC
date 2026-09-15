package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CompassCalibration
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * REON Floating Bottom Navigation Dock
 * Three tabs only: Home, Search, Downloads.
 * Floating dock style, rounded-full, white fill, 1dp hairline border,
 * 12dp ambient shadow, 16dp horizontal margin from screen edges, 12dp above bottom.
 */
@Composable
fun HomeBottomNav(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = ReonTokens.ShapePill,
                    ambientColor = Color(0x140B1020),
                    spotColor = Color(0x1A0B1020)
                )
                .clip(ReonTokens.ShapePill)
                .background(ReonTokens.Surface)
                .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapePill)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .testTag("floating_bottom_nav")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTabItem(
                    title = "Home",
                    icon = Icons.Rounded.Explore,
                    isSelected = currentTab == HomeTab.Home,
                    onClick = { onTabSelected(HomeTab.Home) },
                    testTag = "nav_tab_home"
                )

                NavTabItem(
                    title = "Search",
                    icon = Icons.Rounded.Search,
                    isSelected = currentTab == HomeTab.Search,
                    onClick = { onTabSelected(HomeTab.Search) },
                    testTag = "nav_tab_search"
                )

                NavTabItem(
                    title = "Downloads",
                    icon = Icons.Rounded.CloudDownload,
                    isSelected = currentTab == HomeTab.Downloads,
                    onClick = { onTabSelected(HomeTab.Downloads) },
                    testTag = "nav_tab_downloads"
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "nav_tab_scale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) ReonTokens.SoftContainer else Color.Transparent,
        label = "nav_tab_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) ReonTokens.Primary else ReonTokens.TextTertiary,
        label = "nav_tab_content_color"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(ReonTokens.ShapePill)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = ReonTokens.Primary.copy(alpha = 0.2f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )

            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = ReonTokens.LabelMedium.copy(color = contentColor)
                )
            }
        }
    }
}
