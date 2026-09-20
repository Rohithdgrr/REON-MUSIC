package com.reon.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.reon.music.ui.theme.ReonColors
import com.reon.music.ui.theme.ReonSpacing

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    background: Color = ReonColors.Surface,
    contentPadding: Dp = ReonSpacing.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(ReonSpacing.cardRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .border(1.dp, ReonColors.Border, shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(contentPadding),
        content = content,
    )
}
