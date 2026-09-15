package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrackArtImage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    state: HomeState,
    onSearchChange: (String) -> Unit,
    onFilterSelect: (String) -> Unit,
    onSelectRecentSearch: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onStartVoiceSearch: () -> Unit,
    onCancelVoiceSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onTrackSelect: (TrackItem) -> Unit,
    onArtistFollowToggle: (String) -> Unit,
    onPlaylistSelect: (PlaylistItem) -> Unit = {},
    onAlbumSelect: (AlbumItem) -> Unit = {},
    onArtistSelect: (ArtistItem) -> Unit = {},
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ReonTokens.Canvas)
            .statusBarsPadding()
            .testTag("reon_search_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 180.dp)
        ) {
            // 1. Search Header Bar & Title
            item(key = "search_header_bar") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Search",
                                style = ReonTokens.HeadlineLarge
                            )
                            Text(
                                text = "Find songs, albums, playlists & artists",
                                style = ReonTokens.BodySmall
                            )
                        }

                        // Lossless Audio Engine Badge
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ReonTokens.SoftContainer)
                                .border(1.dp, ReonTokens.Primary.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(ReonTokens.SuccessGreen)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = "96kHz Master",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = ReonTokens.Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Search Input Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(4.dp, CircleShape, ambientColor = Color(0x100B1020), spotColor = Color(0x140B1020))
                            .clip(CircleShape)
                            .background(ReonTokens.Surface)
                            .border(
                                width = if (state.searchQuery.isNotEmpty()) 1.5.dp else 1.dp,
                                color = if (state.searchQuery.isNotEmpty()) ReonTokens.Primary else ReonTokens.Hairline,
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = if (state.searchQuery.isNotEmpty()) ReonTokens.Primary else ReonTokens.TextTertiary,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(Modifier.width(10.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (state.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search songs, albums, playlists, artists…",
                                        style = ReonTokens.BodyMedium.copy(color = ReonTokens.TextTertiary)
                                    )
                                }
                                BasicTextField(
                                    value = state.searchQuery,
                                    onValueChange = onSearchChange,
                                    textStyle = ReonTokens.BodyMedium.copy(
                                        color = ReonTokens.TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    singleLine = true,
                                    cursorBrush = SolidColor(ReonTokens.Primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        keyboardController?.hide()
                                    }),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester)
                                        .testTag("search_text_input")
                                )
                            }

                            if (state.searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(ReonTokens.Muted)
                                        .clickable { onClearSearch() }
                                        .testTag("clear_search_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = ReonTokens.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.SoftContainer)
                                    .clickable { onStartVoiceSearch() }
                                    .testTag("voice_search_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Voice Search",
                                    tint = ReonTokens.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Filter Chips Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(ReonTokens.ChipGap)
                    ) {
                        items(state.filterChips) { filter ->
                            val isSelected = filter == state.selectedFilter
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) ReonTokens.Primary else ReonTokens.Surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ReonTokens.Primary else ReonTokens.Hairline,
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                                        onClick = { onFilterSelect(filter) }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 7.dp)
                                    .testTag("search_filter_$filter"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    style = ReonTokens.LabelMedium.copy(
                                        color = if (isSelected) Color.White else ReonTokens.TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // If searchQuery is EMPTY: Show Recent Searches, Trending Tags, and Browse Categories Grid
            if (state.searchQuery.isEmpty()) {
                // Recent Searches
                if (state.recentSearches.isNotEmpty()) {
                    item(key = "search_recent_section") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ReonTokens.ScreenMargin, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.History,
                                        contentDescription = null,
                                        tint = ReonTokens.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Recent Searches",
                                        style = ReonTokens.TitleMedium
                                    )
                                }

                                Text(
                                    text = "Clear all",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = ReonTokens.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier
                                        .clickable { onClearRecentSearches() }
                                        .padding(4.dp)
                                        .testTag("clear_recent_searches")
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.recentSearches.forEach { query ->
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(ReonTokens.Surface)
                                            .border(1.dp, ReonTokens.Hairline, CircleShape)
                                            .clickable { onSelectRecentSearch(query) }
                                            .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = query,
                                                style = ReonTokens.LabelMedium.copy(color = ReonTokens.TextPrimary)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Remove $query",
                                                tint = ReonTokens.TextTertiary,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { onRemoveRecentSearch(query) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Trending Searches
                item(key = "search_trending_section") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Whatshot,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Trending Searches",
                                style = ReonTokens.TitleMedium
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.trendingSearches) { term ->
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF0F3))
                                        .border(1.dp, Color(0xFFFFCCD5), CircleShape)
                                        .clickable { onSelectRecentSearch(term) }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🔥 $term",
                                            style = ReonTokens.LabelMedium.copy(
                                                color = Color(0xFFD61F4E),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Browse Categories Title
                item(key = "search_browse_title") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ReonTokens.ScreenMargin)
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = ReonTokens.Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Browse All Categories",
                                style = ReonTokens.HeadlineMedium.copy(fontSize = 18.sp)
                            )
                        }
                        Text(
                            text = "Curated spatial and lossless listening spaces",
                            style = ReonTokens.BodySmall
                        )
                    }
                }

                // 2-Column Bento Categories Grid
                val categories = state.browseCategories
                val rows = categories.chunked(2)
                items(rows.size, key = { "cat_row_$it" }) { index ->
                    val pair = rows[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { category ->
                            SearchCategoryCard(
                                category = category,
                                onClick = { onSelectRecentSearch(category.queryTarget) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // If searchQuery is ACTIVE: Show Filtered Results
                val hasResults = state.searchResultsTracks.isNotEmpty() ||
                        state.searchResultsArtists.isNotEmpty() ||
                        state.searchResultsAlbums.isNotEmpty() ||
                        state.searchResultsPlaylists.isNotEmpty() ||
                        state.searchResultsMoods.isNotEmpty() ||
                        state.topMatch != null

                if (!hasResults) {
                    // Empty Search State
                    item(key = "search_no_results") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(ReonTokens.SoftContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = ReonTokens.Primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "No results found for \"${state.searchQuery}\"",
                                style = ReonTokens.HeadlineMedium.copy(
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Check for typos or try searching by artist name, genre (e.g. 'Electronic', 'Synthwave', 'Focus') or 96kHz Lossless tracks.",
                                style = ReonTokens.BodySmall.copy(textAlign = TextAlign.Center)
                            )

                            Spacer(Modifier.height(20.dp))

                            Box(
                                modifier = Modifier
                                    .clip(ReonTokens.ShapePill)
                                    .background(ReonTokens.Primary)
                                    .clickable { onClearSearch() }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Explore All Tracks",
                                    style = ReonTokens.LabelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // 1. Top Match (Hero Bento Card)
                    state.topMatch?.let { match ->
                        item(key = "search_top_match") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "TOP MATCH",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = ReonTokens.Primary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    )
                                )
                                Spacer(Modifier.height(6.dp))

                                TopMatchCard(
                                    match = match,
                                    onPlayClick = {
                                        val targetTrack = state.searchResultsTracks.firstOrNull {
                                            it.id == match.id || it.album.equals(match.title, ignoreCase = true)
                                        } ?: state.currentTrack
                                        onTrackSelect(targetTrack)
                                        onShowToast("Playing ${match.title}")
                                    },
                                    onFollowToggle = { onArtistFollowToggle(match.id) }
                                )
                            }
                        }
                    }

                    // 2. Songs Results Section
                    if (state.searchResultsTracks.isNotEmpty()) {
                        item(key = "search_songs_header") {
                            Text(
                                text = "Songs (${state.searchResultsTracks.size})",
                                style = ReonTokens.HeadlineMedium.copy(fontSize = 17.sp),
                                modifier = Modifier
                                    .padding(horizontal = ReonTokens.ScreenMargin)
                                    .padding(top = 14.dp, bottom = 6.dp)
                            )
                        }

                        items(state.searchResultsTracks, key = { "search_trk_${it.id}" }) { track ->
                            SearchTrackRow(
                                track = track,
                                onTrackSelect = onTrackSelect,
                                isCurrentPlaying = state.currentTrack.id == track.id,
                                onMoreClick = { onShowToast("Options for ${track.title}") }
                            )
                        }
                    }

                    // 3. Artists Results Section
                    if (state.searchResultsArtists.isNotEmpty()) {
                        item(key = "search_artists_header") {
                            Text(
                                text = "Artists (${state.searchResultsArtists.size})",
                                style = ReonTokens.HeadlineMedium.copy(fontSize = 17.sp),
                                modifier = Modifier
                                    .padding(horizontal = ReonTokens.ScreenMargin)
                                    .padding(top = 18.dp, bottom = 6.dp)
                            )
                        }

                        items(state.searchResultsArtists, key = { "search_art_${it.id}" }) { artist ->
                            SearchArtistRow(
                                artist = artist,
                                onFollowToggle = { onArtistFollowToggle(artist.id) },
                                onArtistClick = { onArtistSelect(artist) }
                            )
                        }
                    }

                    // 4. Albums Results Section
                    if (state.searchResultsAlbums.isNotEmpty()) {
                        item(key = "search_albums_header") {
                            Text(
                                text = "Albums & EPs (${state.searchResultsAlbums.size})",
                                style = ReonTokens.HeadlineMedium.copy(fontSize = 17.sp),
                                modifier = Modifier
                                    .padding(horizontal = ReonTokens.ScreenMargin)
                                    .padding(top = 18.dp, bottom = 6.dp)
                            )
                        }

                        items(state.searchResultsAlbums, key = { "search_alb_${it.id}" }) { album ->
                            SearchAlbumRow(
                                album = album,
                                onAlbumClick = { onAlbumSelect(album) }
                            )
                        }
                    }

                    // 5. Playlists Results Section
                    if (state.searchResultsPlaylists.isNotEmpty()) {
                        item(key = "search_playlists_header") {
                            Text(
                                text = "Playlists (${state.searchResultsPlaylists.size})",
                                style = ReonTokens.HeadlineMedium.copy(fontSize = 17.sp),
                                modifier = Modifier
                                    .padding(horizontal = ReonTokens.ScreenMargin)
                                    .padding(top = 18.dp, bottom = 6.dp)
                            )
                        }

                        items(state.searchResultsPlaylists, key = { "search_pl_${it.id}" }) { playlist ->
                            SearchPlaylistRow(
                                playlist = playlist,
                                onPlaylistClick = { onPlaylistSelect(playlist) }
                            )
                        }
                    }
                }
            }
        }

        // Voice Search Modal Overlay
        AnimatedVisibility(
            visible = state.isVoiceSearching,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.fillMaxSize()
        ) {
            VoiceSearchOverlay(
                transcript = state.voiceTranscript,
                onCancel = onCancelVoiceSearch
            )
        }
    }
}

@Composable
private fun SearchCategoryCard(
    category: CategoryBrowseItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = category.gradientColors.map { Color(it) }
    )

    Box(
        modifier = modifier
            .height(112.dp)
            .clip(ReonTokens.ShapeBento)
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = category.emoji,
                    fontSize = 24.sp
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column {
                Text(
                    text = category.title,
                    style = ReonTokens.TitleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = category.subtitle,
                    style = ReonTokens.LabelSmall.copy(
                        color = Color.White.copy(alpha = 0.80f),
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TopMatchCard(
    match: TopMatchResult,
    onPlayClick: () -> Unit,
    onFollowToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, ReonTokens.ShapeBento, ambientColor = Color(0x140B1020), spotColor = Color(0x140B1020))
            .clip(ReonTokens.ShapeBento)
            .background(ReonTokens.Surface)
            .border(1.dp, ReonTokens.Hairline, ReonTokens.ShapeBento)
            .padding(16.dp)
            .testTag("search_top_match_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork / Avatar
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(if (match.type == "ARTIST") CircleShape else RoundedCornerShape(16.dp))
                    .background(ReonTokens.SoftContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (match.type) {
                        "ARTIST" -> Icons.Rounded.Person
                        "ALBUM" -> Icons.Rounded.Album
                        "PLAYLIST" -> Icons.Rounded.QueueMusic
                        else -> Icons.Rounded.MusicNote
                    },
                    contentDescription = null,
                    tint = ReonTokens.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(ReonTokens.SoftContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = match.badge,
                        style = ReonTokens.LabelSmall.copy(
                            color = ReonTokens.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = match.title,
                    style = ReonTokens.HeadlineMedium.copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = match.subtitle,
                    style = ReonTokens.BodySmall.copy(color = ReonTokens.TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = match.extraInfo,
                    style = ReonTokens.LabelSmall.copy(color = ReonTokens.TextTertiary, fontSize = 10.sp),
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(10.dp))

            if (match.type == "ARTIST") {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (match.isFollowing) ReonTokens.SoftContainer else ReonTokens.Primary)
                        .clickable(onClick = onFollowToggle)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (match.isFollowing) "Following" else "Follow",
                        style = ReonTokens.LabelSmall.copy(
                            color = if (match.isFollowing) ReonTokens.Primary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0x330057FF), spotColor = Color(0x400057FF))
                        .clip(CircleShape)
                        .background(ReonTokens.Primary)
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTrackRow(
    track: TrackItem,
    onTrackSelect: (TrackItem) -> Unit,
    isCurrentPlaying: Boolean,
    onMoreClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrackSelect(track) }
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track art / icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrentPlaying) ReonTokens.SoftContainer else ReonTokens.Muted),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCurrentPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = if (isCurrentPlaying) ReonTokens.Primary else ReonTokens.TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = ReonTokens.TitleMedium.copy(
                    fontSize = 14.sp,
                    color = if (isCurrentPlaying) ReonTokens.Primary else ReonTokens.TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (track.badge.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ReonTokens.SoftContainer)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = track.badge,
                            style = ReonTokens.LabelSmall.copy(
                                color = ReonTokens.Primary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }

                Text(
                    text = "${track.artist} · ${track.album}",
                    style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = track.duration,
            style = ReonTokens.DurationText
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isCurrentPlaying) ReonTokens.Primary else ReonTokens.SoftContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = if (isCurrentPlaying) Color.White else ReonTokens.Primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Options",
                tint = ReonTokens.TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchArtistRow(
    artist: ArtistItem,
    onFollowToggle: () -> Unit,
    onArtistClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onArtistClick() }
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ReonTokens.SoftContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = ReonTokens.Primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = ReonTokens.TitleMedium.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Artist · ${artist.genre}",
                style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (artist.isFollowing) ReonTokens.SoftContainer else ReonTokens.Primary)
                .clickable(onClick = onFollowToggle)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (artist.isFollowing) "Following" else "Follow",
                style = ReonTokens.LabelSmall.copy(
                    color = if (artist.isFollowing) ReonTokens.Primary else Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun SearchAlbumRow(
    album: AlbumItem,
    onAlbumClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAlbumClick)
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE8EEF8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Album,
                contentDescription = null,
                tint = ReonTokens.Primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = album.title,
                style = ReonTokens.TitleMedium.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Album · ${album.artist} · ${album.year}",
                style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = album.trackCount,
            style = ReonTokens.LabelSmall.copy(color = ReonTokens.TextTertiary)
        )
    }
}

@Composable
private fun SearchPlaylistRow(
    playlist: PlaylistItem,
    onPlaylistClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlaylistClick)
            .padding(horizontal = ReonTokens.ScreenMargin, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ReonTokens.ElectricGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.QueueMusic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                style = ReonTokens.TitleMedium.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.subtitle,
                style = ReonTokens.BodySmall.copy(fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = playlist.duration,
            style = ReonTokens.DurationText
        )
    }
}

@Composable
private fun VoiceSearchOverlay(
    transcript: String,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "voice_wave_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60B1020))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing outer halo
                Box(
                    modifier = Modifier
                        .size((100 * waveScale).dp)
                        .clip(CircleShape)
                        .background(ReonTokens.Primary.copy(alpha = 0.25f))
                )

                // Inner Mic Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(16.dp, CircleShape, ambientColor = ReonTokens.Primary, spotColor = ReonTokens.Primary)
                        .clip(CircleShape)
                        .background(ReonTokens.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = transcript,
                style = ReonTokens.HeadlineMedium.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Say an artist, song title, or genre...",
                style = ReonTokens.BodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onCancel)
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = ReonTokens.LabelMedium.copy(color = Color.White)
                )
            }
        }
    }
}
