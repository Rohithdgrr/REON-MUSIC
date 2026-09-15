package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * REON — Audio Settings & Engine Configuration Screen
 * Pixel-perfect match to user's uploaded reference UI image (screen.png):
 * Search bar, REON Hyper-Fidelity Pro Tier hero card, Audio Quality settings,
 * Playback Dynamics (Crossfade slider, Gapless, Automix BETA),
 * Storage & Cache visual breakdown, App & Hardware (USB DAC Exclusive),
 * and Engine Build footer links.
 */
@Composable
fun SettingsScreen(
    state: HomeState,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    // Toggle states matching the UI
    var downloadOverWifiOnly by remember { mutableStateOf(!state.isCellularDownloadAllowed) }
    var gaplessPlayback by remember { mutableStateOf(true) }
    var crossfadeDuration by remember { mutableStateOf(4.0f) }
    var automixTransitions by remember { mutableStateOf(true) }
    var normalizeVolume by remember { mutableStateOf(false) }
    var usbDacExclusive by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .testTag("reon_settings_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | Audio Settings | Share >)
            item(key = "settings_top_bar") {
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

                    Text(
                        text = "Audio Settings",
                        style = ReonTokens.HeadlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        )
                    )

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
                }
            }

            // 2. Search Bar Input (Search settings, audio codecs, hardware)
            item(key = "settings_search_bar") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search settings, audio codecs, hardware",
                                style = ReonTokens.BodySmall.copy(color = Color(0xFF8A94A6), fontSize = 13.5.sp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF8A94A6),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Tune",
                                tint = Color(0xFF5B6480),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2EAF8), RoundedCornerShape(16.dp))
                    )
                }
            }

            // 3. REON Hyper-Fidelity Pro Tier Hero Card
            item(key = "settings_hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFEEF4FF))
                        .border(1.dp, Color(0xFFD4E2F8), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0057FF).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF0057FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(Modifier.width(10.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "PRO TIER",
                                            style = ReonTokens.LabelSmall.copy(
                                                color = Color(0xFF0057FF),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0057FF))
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Active Engine",
                                            style = ReonTokens.LabelSmall.copy(
                                                color = Color(0xFF5B6480),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF0057FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "REON Hyper-Fidelity",
                            style = ReonTokens.HeadlineMedium.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF0057FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Bit-Perfect Pipeline",
                                        style = ReonTokens.LabelSmall.copy(
                                            color = Color(0xFF0B1020),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "DSD / FLAC READY",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Section: Audio Quality (96kHz / 24-Bit)
            item(key = "settings_audio_quality_header") {
                SectionHeaderRow(title = "Audio Quality", rightLabel = "96kHz / 24-Bit")
            }

            item(key = "settings_audio_quality_card") {
                SettingsContainerBox {
                    // Streaming Quality
                    SettingsRowItem(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Streaming Quality",
                        subtitle = "Lossless FLAC (24-bit / 96kHz)",
                        badgeText = "Hi-Res",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = Color(0xFF0057FF),
                        onClick = { onShowToast("Streaming Quality set to Hi-Res 96kHz FLAC") }
                    )

                    SettingsDivider()

                    // Download Quality
                    SettingsRowItem(
                        icon = Icons.Rounded.Download,
                        title = "Download Quality",
                        subtitle = "Studio Master (FLAC)",
                        valueText = "~45 MB/track",
                        onClick = { onShowToast("Download Quality: Studio Master FLAC") }
                    )

                    SettingsDivider()

                    // Download over Wi-Fi only
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.Wifi,
                        title = "Download over Wi-Fi only",
                        subtitle = "Prevent high cellular data usage",
                        checked = downloadOverWifiOnly,
                        onCheckedChange = {
                            downloadOverWifiOnly = it
                            onShowToast(if (it) "Downloads restricted to Wi-Fi" else "Cellular downloads enabled")
                        }
                    )

                    SettingsDivider()

                    // Equalizer & Spatial
                    SettingsRowItem(
                        icon = Icons.Rounded.Equalizer,
                        title = "Equalizer & Spatial Audio",
                        subtitle = "Dolby Atmos · REON 3D Sound Engine",
                        badgeText = "Custom EQ",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = Color(0xFF0057FF),
                        onClick = { onShowToast("Opening REON Spatial Equalizer") }
                    )
                }
            }

            // 5. Section: Playback Dynamics (Continuous)
            item(key = "settings_playback_dynamics_header") {
                SectionHeaderRow(title = "Playback Dynamics", rightLabel = "Continuous")
            }

            item(key = "settings_playback_dynamics_card") {
                SettingsContainerBox {
                    // Gapless Playback
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.Repeat,
                        title = "Gapless Playback",
                        subtitle = "Zero latency between track transitions",
                        checked = gaplessPlayback,
                        onCheckedChange = { gaplessPlayback = it }
                    )

                    SettingsDivider()

                    // Crossfade Transition with Slider
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFEEF4FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = null,
                                        tint = Color(0xFF0057FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Crossfade Transition",
                                        style = ReonTokens.TitleMedium.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0B1020)
                                        )
                                    )
                                    Text(
                                        text = "Blend outgoing & incoming waves",
                                        style = ReonTokens.BodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF5B6480)
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF4FF))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${crossfadeDuration.toInt()}.0s",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0057FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Slider(
                            value = crossfadeDuration,
                            onValueChange = { crossfadeDuration = it },
                            valueRange = 0f..12f,
                            steps = 11,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF0057FF),
                                activeTrackColor = Color(0xFF0057FF),
                                inactiveTrackColor = Color(0xFFE2EAF8)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("OFF", style = ReonTokens.LabelSmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
                            Text("2s", style = ReonTokens.LabelSmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
                            Text("4s", style = ReonTokens.LabelSmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
                            Text("8s", style = ReonTokens.LabelSmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
                            Text("12s", style = ReonTokens.LabelSmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
                        }
                    }

                    SettingsDivider()

                    // Automix Transitions BETA
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Automix Transitions",
                        badgeText = "BETA",
                        subtitle = "Seamless AI-driven tempo matching between tracks",
                        checked = automixTransitions,
                        onCheckedChange = { automixTransitions = it }
                    )

                    SettingsDivider()

                    // Normalize Volume
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Normalize Volume",
                        subtitle = "Consistent loudness across all master tracks",
                        checked = normalizeVolume,
                        onCheckedChange = { normalizeVolume = it }
                    )
                }
            }

            // 6. Section: Storage & Cache (64.0 GB Total)
            item(key = "settings_storage_header") {
                SectionHeaderRow(title = "Storage & Cache", rightLabel = "64.0 GB Total")
            }

            item(key = "settings_storage_card") {
                SettingsContainerBox {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Storage Progress Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2EAF8))
                        ) {
                            // Downloads (8.4 GB -> ~13%)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.13f)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0057FF))
                            )
                            Spacer(Modifier.width(2.dp))
                            // Cache (1.2 GB -> ~3%)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.04f)
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Storage legend row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0057FF))
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("8.4 GB", style = ReonTokens.TitleMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                                    Text("DOWNLOADS", style = ReonTokens.LabelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF8A94A6)))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6))
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("1.2 GB", style = ReonTokens.TitleMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                                    Text("CACHE", style = ReonTokens.LabelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF8A94A6)))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE2EAF8))
                                )
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("54.4 GB", style = ReonTokens.TitleMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                                    Text("FREE", style = ReonTokens.LabelSmall.copy(fontSize = 9.5.sp, color = Color(0xFF8A94A6)))
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Clear Streaming Cache Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Streaming Cache",
                                    style = ReonTokens.TitleMedium.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Clear cached tracks to reclaim memory",
                                    style = ReonTokens.BodySmall.copy(fontSize = 12.sp, color = Color(0xFF5B6480))
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF4FF))
                                    .border(1.dp, Color(0xFFD4E2F8), CircleShape)
                                    .clickable { onShowToast("Streaming cache cleared (1.2 GB freed)") }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Clear Cache",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color(0xFF0B1020),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 7. Section: App & Hardware
            item(key = "settings_hardware_header") {
                SectionHeaderRow(title = "App & Hardware", rightLabel = "")
            }

            item(key = "settings_hardware_card") {
                SettingsContainerBox {
                    // USB DAC Exclusive AUDIOPHILE
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.Usb,
                        title = "USB DAC Exclusive",
                        badgeText = "AUDIOPHILE",
                        badgeBg = Color(0xFF0057FF),
                        badgeTextTint = Color.White,
                        subtitle = "Bypass OS audio mixer for direct external stream",
                        checked = usbDacExclusive,
                        onCheckedChange = { usbDacExclusive = it }
                    )

                    SettingsDivider()

                    // Connected Devices
                    SettingsRowItem(
                        icon = Icons.Rounded.Cast,
                        title = "Connected Devices",
                        subtitle = "AirPlay 2 · Chromecast · TIDAL Connect",
                        onClick = { onShowToast("Searching for Audio Devices...") }
                    )

                    SettingsDivider()

                    // Visual Horizon Theme
                    SettingsRowItem(
                        icon = Icons.Rounded.Palette,
                        title = "Visual Horizon Theme",
                        subtitle = "System (Electric Horizon Light)",
                        valueDotColor = Color(0xFF0057FF),
                        onClick = { onShowToast("Theme set to Electric Horizon Light") }
                    )
                }
            }

            // 8. Engine Build Footer (Infinity Icon + Version Info + Links)
            item(key = "settings_footer") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AllInclusive,
                            contentDescription = "REON Engine",
                            tint = Color(0xFF0057FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "REON Engine v2.4.0",
                        style = ReonTokens.TitleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        )
                    )

                    Text(
                        text = "Build 892 (High-Fidelity Audio Core)",
                        style = ReonTokens.BodySmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF8A94A6)
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Diagnostics",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("Running REON Audio Diagnostics...") }
                        )

                        Text(
                            text = "•",
                            style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6))
                        )

                        Text(
                            text = "Release Notes",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFF0057FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("REON Engine v2.4.0 Release Notes") }
                        )

                        Text(
                            text = "•",
                            style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6))
                        )

                        Text(
                            text = "Sign Out",
                            style = ReonTokens.LabelSmall.copy(
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("Signed out of REON Hi-Res") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String, rightLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ReonTokens.HeadlineMedium.copy(
                fontSize = 17.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B1020)
            )
        )

        if (rightLabel.isNotEmpty()) {
            Text(
                text = rightLabel,
                style = ReonTokens.LabelSmall.copy(
                    color = Color(0xFF0057FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun SettingsContainerBox(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFEBF1FA), RoundedCornerShape(20.dp))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badgeText: String = "",
    badgeBg: Color = Color(0xFFEEF4FF),
    badgeTextTint: Color = Color(0xFF0057FF),
    valueText: String = "",
    valueDotColor: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEF4FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0057FF),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = ReonTokens.TitleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B1020)
                    )
                )

                if (badgeText.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = ReonTokens.LabelSmall.copy(
                                color = badgeTextTint,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Text(
                text = subtitle,
                style = ReonTokens.BodySmall.copy(
                    fontSize = 12.sp,
                    color = Color(0xFF5B6480)
                )
            )
        }

        if (valueText.isNotEmpty()) {
            Text(
                text = valueText,
                style = ReonTokens.BodySmall.copy(
                    color = Color(0xFF8A94A6),
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(end = 6.dp)
            )
        }

        if (valueDotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(valueDotColor)
            )
            Spacer(Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF8A94A6),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badgeText: String = "",
    badgeBg: Color = Color(0xFF0057FF),
    badgeTextTint: Color = Color.White,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEF4FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0057FF),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = ReonTokens.TitleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B1020)
                    )
                )

                if (badgeText.isNotEmpty()) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = ReonTokens.LabelSmall.copy(
                                color = badgeTextTint,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Text(
                text = subtitle,
                style = ReonTokens.BodySmall.copy(
                    fontSize = 12.sp,
                    color = Color(0xFF5B6480)
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF0057FF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(Color(0xFFEBF1FA))
    )
}
