package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FileDownloadDone
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MusicTrack
import kotlinx.coroutines.delay

/**
 * Custom tactile press-bounce animation modifier.
 * Provides responsive scale-down, bouncy spring release, and Material ripple feedback on click.
 */
@Composable
fun Modifier.bounceClick(
    scaleDown: Float = 0.90f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "button_bounce_scale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.35f)),
            onClick = onClick
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    state: NowPlayingState,
    onBack: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
    onShuffle: () -> Unit = {},
    onRepeat: () -> Unit = {},
    onLike: () -> Unit = {},
    onMenuToggle: () -> Unit = {},
    onMenuClose: () -> Unit = {},
    onDownload: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onAddToPlaylist: (String) -> Unit = {},
    onPlayNextMenu: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onViewAlbum: () -> Unit = {},
    onShareTrack: () -> Unit = {},
    onSleepTimerClick: () -> Unit = {},
    onTrackSelect: (MusicTrack) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
    onDismissToast: () -> Unit = {}
) {
    // Auto dismiss toast after 2.5 seconds
    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(2500L)
            onDismissToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .systemBarsPadding()
            .testTag("now_playing_screen")
    ) {
        // Ambient Radial Glow Layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.32f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0066FF).copy(alpha = 0.22f),
                        Color(0xFF00F2FE).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.85f
                ),
                center = center,
                radius = size.width * 0.85f
            )
        }

        if (state.isMinimized) {
            // Minimized View: Floating Interactive Mini Player docked above navigation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                MiniPlayerBar(
                    state = state,
                    onExpand = onExpandPlayer,
                    onPlayPause = onPlayPause,
                    onNext = onNext
                )
            }
        } else {
            // Full Scrollable Viewport Container
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .widthIn(max = 420.dp)
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Viewport (790dp target view height)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Bar Navigation
                    ZenHeader(
                        category = state.category,
                        isMenuOpen = state.isMenuOpen,
                        onBack = onBack,
                        onMenuToggle = onMenuToggle
                    )

                    Spacer(Modifier.height(16.dp))

                    // Central Holographic Artwork & Synced Lyric Card
                    ArtworkAndLyricsSection(
                        track = state.currentTrack,
                        isPlaying = state.isPlaying
                    )

                    Spacer(Modifier.height(20.dp))

                    // Track Identity & Action Controls
                    TrackIdentityRow(
                        title = state.title,
                        artist = state.artist,
                        isLiked = state.isLiked,
                        onLike = onLike,
                        onPlaylistClick = onPlaylistClick
                    )

                    Spacer(Modifier.height(14.dp))

                    // Scrubbing Progress Bar Component
                    ScrubberProgressBar(
                        progress = state.progress,
                        positionLabel = state.positionLabel,
                        remainingLabel = state.remainingLabel,
                        onSeek = onSeek
                    )

                    Spacer(Modifier.height(16.dp))

                    // Master Audio Transport Controls
                    MasterTransportRow(
                        isPlaying = state.isPlaying,
                        repeatOn = state.repeatOn,
                        onDownload = onDownload,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onRepeat = onRepeat
                    )

                    Spacer(Modifier.height(16.dp))

                    // Bottom Quick Dock Navigation Pills
                    QuickDockNavigationRow(
                        sleepTimer = state.sleepTimer,
                        onQueueClick = onQueueClick,
                        onSleepTimerClick = onSleepTimerClick
                    )

                    Spacer(Modifier.height(14.dp))

                    // Scroll Down Cue Indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "SCROLL FOR DETAILS & ARTIST",
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.4.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Scroll down",
                            tint = Color(0xFF38BDF8).copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Extended Vertical Content Sections (Up Next, Artist, Song Details, Audio Specs)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // 1. Up Next / Queue Preview Card
                    UpNextSectionCard(
                        nextTrack = state.nextTrack,
                        onQueueClick = onQueueClick
                    )

                    // 2. About the Artist Card
                    AboutArtistCard(
                        artistName = state.artist,
                        listeners = state.currentTrack.monthlyListeners,
                        onFollow = { onShareTrack() }
                    )

                    // 3. Song Details & Credits Card
                    SongDetailsCard(
                        album = state.album,
                        isrc = "US-RE8-24-00142"
                    )

                    // 4. Audio & Stream Quality Specs Card
                    AudioQualitySpecsCard(
                        codec = state.codec,
                        sampleRate = state.sampleRate,
                        source = state.source
                    )
                }
            }
        }

        // Clean Dim Backdrop Overlay for Active Options Bottom Sheet
        AnimatedVisibility(
            visible = state.isMenuOpen,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF02040A).copy(alpha = 0.70f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onMenuClose
                    )
            )
        }

        // Three Dots Modal Options Bottom Sheet
        AnimatedVisibility(
            visible = state.isMenuOpen,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                initialOffsetY = { it }
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(180),
                targetOffsetY = { it }
            ) + fadeOut(tween(140)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ZenOptionsBottomSheet(
                state = state,
                onAddToPlaylist = onPlaylistClick,
                onViewAlbum = onViewAlbum,
                onGoToArtist = onViewAlbum,
                onSleepTimerClick = onSleepTimerClick,
                onShare = onShareTrack,
                onClose = onMenuClose
            )
        }

        // Interactive Toast Notification
        AnimatedVisibility(
            visible = state.toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
        ) {
            state.toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF0C1326).copy(alpha = 0.96f))
                        .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.45f), RoundedCornerShape(50))
                        .shadow(12.dp, RoundedCornerShape(50), ambientColor = Color.Black, spotColor = Color(0x6600C8FF))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = Color.White),
                            onClick = onDismissToast
                        )
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Queue Modal Bottom Sheet
        if (state.isQueueExpanded) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onQueueClick,
                sheetState = sheetState,
                containerColor = Color(0xFF090D22)
            ) {
                QueueModalContent(
                    state = state,
                    onTrackSelect = { track ->
                        onTrackSelect(track)
                        onQueueClick()
                    },
                    onClose = onQueueClick
                )
            }
        }

        // Playlist Selection Modal Bottom Sheet
        if (state.isPlaylistDialogOpen) {
            val playlistSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = onPlaylistClick,
                sheetState = playlistSheetState,
                containerColor = Color(0xFF090D22)
            ) {
                PlaylistModalContent(
                    trackTitle = state.title,
                    onSelectPlaylist = { playlistName ->
                        onAddToPlaylist(playlistName)
                    },
                    onClose = onPlaylistClick
                )
            }
        }
    }
}

@Composable
private fun ZenHeader(
    category: String,
    isMenuOpen: Boolean = false,
    onBack: () -> Unit,
    onMenuToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dismiss Modal Chevron Icon (w-10 h-10 rounded-full bg-white/[0.05] border border-white/10)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                .bounceClick(scaleDown = 0.90f, onClick = onBack)
                .testTag("back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Dismiss view",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(22.dp)
            )
        }

        // Center Title & Category Context
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = category.uppercase(),
                color = Color.White.copy(alpha = 0.50f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.4.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Now Playing",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
        }

        // Context Menu Button (w-10 h-10 rounded-full bg-white/[0.05] border border-white/10)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isMenuOpen) Color(0xFF0066FF).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                .border(1.dp, if (isMenuOpen) Color(0xFF00F2FE).copy(alpha = 0.40f) else Color.White.copy(alpha = 0.10f), CircleShape)
                .bounceClick(scaleDown = 0.90f, onClick = onMenuToggle)
                .testTag("menu_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "More options",
                tint = if (isMenuOpen) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ArtworkAndLyricsSection(
    track: MusicTrack,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_anim")
    val haloPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Concentric Holographic Orbital Disc (w-64 h-64 equivalent)
        Box(
            modifier = Modifier
                .size(256.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer soft boundary ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF0066FF).copy(alpha = if (isPlaying) haloPulseAlpha * 0.4f else 0.2f), CircleShape)
            )
            // Inner subtle accent ring
            Box(
                modifier = Modifier
                    .size(236.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.20f), CircleShape)
            )

            // Central Holographic Artwork Disk (w-56 h-56 / 224.dp)
            Box(
                modifier = Modifier
                    .size(224.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF008CFF),
                        spotColor = Color(0xFF38BDF8)
                    )
                    .clip(CircleShape)
                    .border(1.2.dp, Color(0xFF00F2FE).copy(alpha = 0.35f), CircleShape)
            ) {
                Image(
                    painter = painterResource(R.drawable.art_refractions),
                    contentDescription = "Aurora Glow - Refractions Crystalline Sculpture Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Translucent glass reflection sheen overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF00F2FE).copy(alpha = 0.10f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                )

                // Bottom subtle artist vignette watermark overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.40f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .padding(bottom = 14.dp, top = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "AURORA GLOW",
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.0.sp
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = "REFRACTIONS",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.5.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Synced Lyrics Mood Callout Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0B101D).copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Synced Lyrics Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF082F49).copy(alpha = 0.9f))
                        .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "SYNCED LYRICS",
                            color = Color(0xFF7DD3FC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                // Lyrics Lines
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Floating through shards of electric light",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "“Caught in the frequency of the night”",
                        color = Color(0xFFCFFAFE),
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.shadow(8.dp, spotColor = Color(0xFF38BDF8))
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackIdentityRow(
    title: String,
    artist: String,
    isLiked: Boolean,
    onLike: () -> Unit,
    onPlaylistClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Track Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = artist,
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Buttons: Add + Favorite
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add to Collection Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0B101D).copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .bounceClick(scaleDown = 0.90f, onClick = onPlaylistClick)
                    .testTag("add_playlist_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add to collection",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Favorite Button (Heart)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isLiked) Color(0xFFF43F5E).copy(alpha = 0.20f) else Color(0xFF0B101D).copy(alpha = 0.85f))
                    .border(1.dp, if (isLiked) Color(0xFFF43F5E).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f), CircleShape)
                    .shadow(
                        elevation = if (isLiked) 12.dp else 0.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFF43F5E)
                    )
                    .bounceClick(scaleDown = 0.88f, onClick = onLike)
                    .testTag("fav_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Mark as favorite",
                    tint = if (isLiked) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ScrubberProgressBar(
    progress: Float,
    positionLabel: String,
    remainingLabel: String,
    onSeek: (Float) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progress) }
    val activeFraction = if (isDragging) dragFraction else progress

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val frac = (offset.x / size.width).coerceIn(0f, 1f)
                            dragFraction = frac
                            onSeek(frac)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek(dragFraction)
                        },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                            dragFraction = frac
                            onSeek(frac)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val frac = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(frac)
                        }
                    )
                }
                .testTag("progress_container"),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
                val railHeight = 4.dp.toPx()
                val centerY = size.height / 2f

                // Inactive Rail
                drawRoundRect(
                    color = Color(0xFF1E293B).copy(alpha = 0.9f),
                    topLeft = Offset(0f, centerY - railHeight / 2f),
                    size = androidx.compose.ui.geometry.Size(size.width, railHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(railHeight / 2f, railHeight / 2f)
                )

                // Active Gradient Rail
                val activeWidth = size.width * activeFraction
                if (activeWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2563EB),
                                Color(0xFF00F2FE),
                                Color(0xFF38BDF8)
                            )
                        ),
                        topLeft = Offset(0f, centerY - railHeight / 2f),
                        size = androidx.compose.ui.geometry.Size(activeWidth, railHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(railHeight / 2f, railHeight / 2f)
                    )
                }

                // Scrubber Thumb Glow + Circle
                val thumbX = activeWidth.coerceIn(0f, size.width)
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.40f),
                    radius = 8.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.5.dp.toPx(),
                    center = Offset(thumbX, centerY)
                )
            }
        }

        // Timestamp Readouts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = positionLabel,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = remainingLabel,
                color = Color(0xFF38BDF8).copy(alpha = 0.90f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MasterTransportRow(
    isPlaying: Boolean,
    repeatOn: Boolean,
    onDownload: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Download Toggle Button
        IconButton(
            onClick = onDownload,
            modifier = Modifier.testTag("download_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "Download for offline",
                tint = Color.White.copy(alpha = 0.60f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Previous Track
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.testTag("previous_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous track",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Glowing Circular Master Play/Pause Button (w-16 h-16 / 64.dp)
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF0066FF),
                    spotColor = Color(0xFF00F2FE)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                .bounceClick(scaleDown = 0.92f, onClick = onPlayPause)
                .testTag("zen_play_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause playback" else "Play playback",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }

        // Next Track
        IconButton(
            onClick = onNext,
            modifier = Modifier.testTag("next_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next track",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Repeat / Shuffle Toggle
        IconButton(
            onClick = onRepeat,
            modifier = Modifier.testTag("repeat_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Repeat,
                contentDescription = "Repeat track",
                tint = if (repeatOn) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.60f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QuickDockNavigationRow(
    sleepTimer: String,
    onQueueClick: () -> Unit,
    onSleepTimerClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Pill: Queue + Badge 24
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0B101D).copy(alpha = 0.90f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                .bounceClick(scaleDown = 0.94f, onClick = onQueueClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("queue_pill")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.QueueMusic,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Queue",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                // Badge 24
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF082F49))
                        .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.40f), RoundedCornerShape(50))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "24",
                        color = Color(0xFF7DD3FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right Pill: Sleep Timer
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF0B101D).copy(alpha = 0.90f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                .bounceClick(scaleDown = 0.94f, onClick = onSleepTimerClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("sleep_timer_pill")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = sleepTimer,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UpNextSectionCard(
    nextTrack: MusicTrack,
    onQueueClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0B101D).copy(alpha = 0.80f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UP NEXT",
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.4.sp
                )
                Text(
                    text = "View Queue (24)",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onQueueClick() }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQueueClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.art_refractions),
                            contentDescription = "Prism Dreams artwork preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0B101D).copy(alpha = 0.4f))
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = nextTrack.title.ifEmpty { "Prism Dreams" },
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${nextTrack.artist.ifEmpty { "Aurora Glow" }} • Refractions EP",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "03:42",
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun AboutArtistCard(
    artistName: String,
    listeners: String,
    onFollow: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A).copy(alpha = 0.90f),
                        Color(0xFF0B101D)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Artist Portrait Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.art_refractions),
                    contentDescription = "Aurora Glow Portrait",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF0B101D).copy(alpha = 0.5f),
                                    Color(0xFF0B101D).copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = artistName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified artist",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "1.4M Monthly Listeners",
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF0066FF))
                            .clickable { onFollow() }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Follow",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bio
            Text(
                text = "Pioneering electronic duo blending ambient modular synthesizers with crystalline acoustic textures and deep spatial soundscapes.",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 18.sp
            )

            // Sub row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Based in Berlin, Germany",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "View Profile",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SongDetailsCard(
    album: String,
    isrc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0B101D).copy(alpha = 0.80f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SONG DETAILS & CREDITS",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.0.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF082F49))
                        .border(1.dp, Color(0xFF00F2FE).copy(alpha = 0.30f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = isrc,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            val credits = listOf(
                "Album" to "$album EP",
                "Release Date" to "October 24, 2024",
                "Written by" to "Aurora Glow, Elena Vance",
                "Produced by" to "Aurora Glow",
                "Mixed & Mastered" to "Cortex Spatial Labs, Berlin",
                "Record Label" to "Horizon Electric Recordings"
            )

            credits.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = value,
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioQualitySpecsCard(
    codec: String,
    sampleRate: String,
    source: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0B101D).copy(alpha = 0.80f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Text(
                    text = "AUDIO & STREAM INFO",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.0.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Format Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "FORMAT",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = "Lossless ($codec)",
                            color = Color(0xFF7DD3FC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sample Rate Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SAMPLE RATE",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            letterSpacing = 1.0.sp
                        )
                        Text(
                            text = sampleRate.ifEmpty { "96 kHz / 24-bit" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Streaming via",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Bit-Perfect Direct DAC",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ZenOptionsBottomSheet(
    state: NowPlayingState,
    onAddToPlaylist: () -> Unit,
    onViewAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF070B16))
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .testTag("three_dots_bottom_sheet")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Spacer(Modifier.height(18.dp))

            // Track Artwork + Title + Artist Header Row & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF131B2E))
                    ) {
                        Image(
                            painter = painterResource(R.drawable.art_refractions),
                            contentDescription = state.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = state.title.ifEmpty { "Refractions" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = state.artist.ifEmpty { "Aurora Glow" },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.60f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Close X Circle Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF162036))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Spacer(Modifier.height(10.dp))

            // Menu Items List matching reference UI
            // 1. Add to Playlist
            BottomSheetOptionItem(
                icon = Icons.Rounded.Add,
                title = "Add to Playlist",
                onClick = {
                    onClose()
                    onAddToPlaylist()
                }
            )

            // 2. View Album
            BottomSheetOptionItem(
                icon = Icons.Rounded.Album,
                title = "View Album",
                onClick = {
                    onClose()
                    onViewAlbum()
                }
            )

            // 3. Go to Artist
            BottomSheetOptionItem(
                icon = Icons.Rounded.Person,
                title = "Go to Artist",
                onClick = {
                    onClose()
                    onGoToArtist()
                }
            )

            // 4. Sleep Timer
            BottomSheetOptionItem(
                icon = Icons.Rounded.Bedtime,
                title = "Sleep Timer",
                badgeContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF13283E))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.sleepTimer.replace("m", "").trim().ifEmpty { "45" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22D3EE)
                        )
                        Text(
                            text = "m",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF22D3EE)
                        )
                    }
                },
                onClick = onSleepTimerClick
            )

            // 5. Share Track
            BottomSheetOptionItem(
                icon = Icons.Rounded.Share,
                title = "Share Track",
                onClick = {
                    onClose()
                    onShare()
                }
            )

            Spacer(Modifier.height(18.dp))

            // Done Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111827))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BottomSheetOptionItem(
    icon: ImageVector,
    title: String,
    badgeContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bg by animateColorAsState(
        targetValue = if (isPressed) Color.White.copy(alpha = 0.08f) else Color.Transparent,
        label = "option_press_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131B2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            badgeContent?.invoke()

            if (badgeContent != null) {
                Spacer(Modifier.width(10.dp))
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun QueueModalContent(
    state: NowPlayingState,
    onTrackSelect: (MusicTrack) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Playback Queue",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "24 ambient tracks loaded",
                    color = Color(0xFF67E8F9),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.70f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.queueTracks, key = { it.id }) { track ->
                val isCurrent = track.id == state.currentTrack.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isCurrent) Color(0xFF22D3EE).copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.04f)
                        )
                        .bounceClick(scaleDown = 0.98f) { onTrackSelect(track) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C1428)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.art_refractions),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isCurrent) Color(0xFF67E8F9) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isCurrent) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Playing",
                            tint = Color(0xFF22D3EE),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text(
                            text = track.durationLabel,
                            color = Color.White.copy(alpha = 0.40f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistModalContent(
    trackTitle: String,
    onSelectPlaylist: (String) -> Unit = {},
    onClose: () -> Unit
) {
    val playlists = listOf(
        "Deep Focus Sanctuary",
        "Cybernetic Ambient Waves",
        "Night Chill & Synths",
        "Lossless Hi-Res Favorites"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Add to Playlist",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose playlist for '$trackTitle'",
                    color = Color.White.copy(alpha = 0.60f),
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.70f)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        playlists.forEach { pl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .bounceClick(scaleDown = 0.98f) {
                        onSelectPlaylist(pl)
                        onClose()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistAdd,
                    contentDescription = null,
                    tint = Color(0xFF67E8F9),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = pl,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun MiniPlayerBar(
    state: NowPlayingState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_disc_rotation")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mini_disc_angle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = Color(0x6600C8FF))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF131D38),
                        Color(0xFF090E1F)
                    )
                )
            )
            .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.2f)),
                onClick = onExpand
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("mini_player_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF050814))
                        .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.art_refractions),
                        contentDescription = "Mini Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = if (state.isPlaying) discRotation else 0f
                            }
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${state.artist} · 96kHz FLAC",
                        color = Color(0xFF67E8F9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22D3EE).copy(alpha = 0.18f))
                        .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.40f), CircleShape)
                        .bounceClick(scaleDown = 0.88f, onClick = onPlayPause)
                        .testTag("mini_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.88f, onClick = onNext)
                        .testTag("mini_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .bounceClick(scaleDown = 0.88f, onClick = onExpand)
                        .testTag("mini_expand_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Expand Player",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
