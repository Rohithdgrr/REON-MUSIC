package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrackArtImage

/**
 * REON — Artist Profile Screen
 * Pixel-perfect match to user's uploaded reference UI image (screen.png):
 * Hero card with REON Featured badge, Follow/Radio/Play action bar,
 * REON Master Acoustic Profile bento card, Popular Tracks list,
 * Discography & Master Vault horizontal row, and Editorial Note & Sound Lab card.
 */
@Composable
fun ArtistScreen(
    state: HomeState,
    onTrackSelect: (TrackItem) -> Unit,
    onAlbumSelect: (AlbumItem) -> Unit = {},
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onFollowToggle: () -> Unit = {},
    onRadioClick: () -> Unit = {},
    onPlayClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val artist = state.selectedArtist ?: ArtistItem(
        id = "art_aurora",
        name = state.activeArtistName,
        genre = "Electronic",
        isFollowing = state.isArtistFollowed,
        artSeed = 1
    )

    val popularTracks = if (state.activeArtistTracks.isNotEmpty()) state.activeArtistTracks else sampleArtistPopularTracks()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("reon_artist_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | ARTIST PROFILE / Artist Name | Share, More >)
            item(key = "artist_top_bar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0B1020),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ARTIST PROFILE",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF8A94A6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = artist.name,
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share",
                                tint = Color(0xFF0B1020),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onMoreOptionsClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "More",
                                tint = Color(0xFF0B1020),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // 2. Hero Header Card (Dark Studio Artwork with Badges & Text Overlay)
            item(key = "artist_hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(260.dp)
                        .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF0057FF).copy(alpha = 0.25f))
                        .clip(RoundedCornerShape(26.dp))
                ) {
                    // Studio background image
                    TrackArtImage(
                        url = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&q=80",
                        contentDescription = artist.name,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.1f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Top Pills inside Hero
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // REON Featured Artist Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "REON Featured Artist",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Format Pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF0057FF).copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "96K / 24-BIT",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    // Bottom Content Overlay inside Hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = artist.name,
                                style = ReonTokens.HeadlineLarge.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "${state.activeArtistListeners} Monthly Listeners · Tokyo / Berlin",
                            style = ReonTokens.BodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Electronic   •   Modular Synth   •   Deep Ambient",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF70C5FF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 3. Action Buttons Row (Follow | Radio | Big Blue Play)
            item(key = "artist_action_buttons") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Follow Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(if (state.isArtistFollowed) Color(0xFF0057FF) else Color(0xFF0B1020))
                            .clickable { onFollowToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.isArtistFollowed) Icons.Rounded.Check else Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (state.isArtistFollowed) "Following" else "Follow",
                                style = ReonTokens.TitleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Radio Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F4FC))
                            .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                            .clickable { onRadioClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Radio,
                                contentDescription = null,
                                tint = Color(0xFF0B1020),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Radio",
                                style = ReonTokens.TitleMedium.copy(
                                    color = Color(0xFF0B1020),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }

                    // Big Circular Blue Play Button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFF0057FF).copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(Color(0xFF0057FF))
                            .clickable {
                                onPlayClick()
                                if (popularTracks.isNotEmpty()) onTrackSelect(popularTracks.first())
                                onShowToast("Playing ${artist.name}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play Artist",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // 4. REON Master Acoustic Profile Bento Card
            item(key = "artist_acoustic_profile_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2EAF8), RoundedCornerShape(20.dp))
                        .clickable { onShowToast("REON Acoustic Calibration Details") }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Soft blue icon square
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEEF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "REON Master Acoustic Profile",
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B1020)
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFDCFCE7))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "BIT-PERFECT",
                                        style = ReonTokens.LabelSmall.copy(
                                            color = Color(0xFF15803D),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = "Native 96kHz / 24-Bit FLAC · Dynamic Range 14.2 dB",
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5B6480)
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF8A94A6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 5. Popular Tracks Section Header
            item(key = "artist_popular_tracks_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Tracks",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        Text(
                            text = "See All",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("All tracks by ${artist.name}") }
                        )
                    }

                    Text(
                        text = "Master releases streamed in bit-perfect lossless",
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF5B6480)
                        )
                    )
                }
            }

            // 6. Popular Tracks List Items
            itemsIndexed(popularTracks, key = { _, track -> "artist_pop_${track.id}" }) { index, track ->
                val isCurrentPlaying = index == 0 && state.currentTrack.id == track.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrentPlaying) Color(0xFFEEF4FF) else Color.Transparent)
                        .border(1.dp, if (isCurrentPlaying) Color(0xFFC2D8FF) else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { onTrackSelect(track) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank number or active equalizing icon
                    if (isCurrentPlaying) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0057FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = "Playing",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF8A94A6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.width(28.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Square icon or track art
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF0F4FC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = if (isCurrentPlaying) Color(0xFF0057FF) else Color(0xFF5B6480),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Track Title & Plays
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.title,
                                style = ReonTokens.TitleMedium.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentPlaying) Color(0xFF0057FF) else Color(0xFF0B1020)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (track.badge.isNotEmpty()) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isCurrentPlaying) Color(0xFF0057FF) else Color(0xFFEEF2FA))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = track.badge,
                                        style = ReonTokens.LabelSmall.copy(
                                            color = if (isCurrentPlaying) Color.White else Color(0xFF424D6B),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = track.plays.ifEmpty { "5,412,890 plays" },
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF5B6480)
                            )
                        )
                    }

                    Text(
                        text = track.duration,
                        style = ReonTokens.BodySmall.copy(
                            color = Color(0xFF5B6480),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    IconButton(
                        onClick = { onShowToast("Options for ${track.title}") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF8A94A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 7. Discography & Master Vault Section
            item(key = "artist_discography_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 22.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discography & Master Vault",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        Text(
                            text = "Albums",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("Viewing Discography") }
                        )
                    }
                }
            }

            // Horizontal Albums Row
            item(key = "artist_discography_row") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val sampleAlbums = listOf(
                        AlbumItem("alb_sonic_geom", "Sonic Geometry", "Aurora Glow", "2024", "11 Tracks", "FLAC", artSeed = 1),
                        AlbumItem("alb_aether_res", "Aether Resonance", "Aurora Glow", "2023", "9 Tracks", "24-Bit", artSeed = 2)
                    )

                    sampleAlbums.forEach { album ->
                        Column(
                            modifier = Modifier
                                .width(165.dp)
                                .clickable { onAlbumSelect(album) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(165.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shadow(8.dp, RoundedCornerShape(20.dp))
                            ) {
                                TrackArtImage(
                                    url = if (album.id == "alb_sonic_geom") "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80" else "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&q=80",
                                    contentDescription = album.title,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Top format badge inside card
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (album.id == "alb_sonic_geom") "Master 96k" else "Dolby Atmos",
                                        style = ReonTokens.LabelSmall.copy(
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                // Bottom tag inside card
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0057FF))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (album.id == "alb_sonic_geom") "Latest LP" else "STUDIO ARCHIVE",
                                        style = ReonTokens.LabelSmall.copy(
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = album.title,
                                style = ReonTokens.TitleMedium.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B1020)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${album.year} · ${album.trackCount} · ${album.genre}",
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5B6480)
                                )
                            )
                        }
                    }
                }
            }

            // 8. Editorial Note & Sound Lab Bento Box (Bottom Card)
            item(key = "artist_editorial_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2EAF8), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EDITORIAL NOTE & SOUND LAB",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF0057FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            )

                            Text(
                                text = "REON Sonic ID #892",
                                style = ReonTokens.LabelSmall.copy(
                                    color = Color(0xFF8A94A6),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "Formed in Berlin and refined through Tokyo's avant-garde analog clubs, Aurora Glow pair vintage Buchla and Eurorack modular synthesizers with pristine 96kHz acoustic spatialization. Their recordings feature unfiltered transient responses and custom-engineered harmonic overtones.",
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF384360),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mastering Vault Status",
                                style = ReonTokens.BodySmall.copy(
                                    color = Color(0xFF0B1020),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Verified Bit-Perfect",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sampleArtistPopularTracks(): List<TrackItem> {
    return listOf(
        TrackItem("tr_ref_1", "Refractions", "Aurora Glow", "Refractions", "04:18", badge = "96k FLAC", plays = "8,924,103 plays", isPlaying = true, artSeed = 1),
        TrackItem("tr_ref_2", "Nightfall Prism", "Aurora Glow", "Refractions", "04:45", badge = "LOSSLESS", plays = "6,412,890 plays", artSeed = 1),
        TrackItem("tr_ref_4", "Electric Horizon", "Aurora Glow", "Refractions", "03:58", badge = "24-BIT", plays = "4,891,012 plays", artSeed = 1),
        TrackItem("tr_ref_3", "Subtle Drift", "Aurora Glow", "Refractions", "05:12", badge = "ATMOS", plays = "3,170,440 plays", artSeed = 1),
        TrackItem("tr_ref_8", "Crystalline", "Aurora Glow", "Refractions", "04:55", badge = "FLAC", plays = "2,852,990 plays", artSeed = 1)
    )
}
