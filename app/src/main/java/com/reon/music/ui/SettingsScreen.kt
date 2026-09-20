package com.reon.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.PhotoSizeSelectActual
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reon.music.ui.theme.AccentColors
import com.reon.music.ui.theme.DarkBackgrounds
import com.reon.music.ui.theme.LightBackgrounds

/**
 * REON — Audio Settings & Engine Configuration Screen
 * Includes: User Profile Editor, Appearance/Theme/Colors/Fonts/Size customization,
 * Audio Quality settings, Playback Dynamics, Hardware DAC controls,
 * and About / Open Source License modal viewers.
 */
@Composable
fun SettingsScreen(
    state: HomeState,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onUpdateProfile: (name: String, bio: String) -> Unit = { _, _ -> },
    onOpenEditProfile: () -> Unit = {},
    onCloseEditProfile: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onCloseAbout: () -> Unit = {},
    onOpenLicenses: () -> Unit = {},
    onCloseLicenses: () -> Unit = {},
    onSetThemeMode: (String) -> Unit = {},
    onSetAccentColor: (Int) -> Unit = {},
    onSetBackgroundTheme: (Int) -> Unit = {},
    onSetFontFamily: (String) -> Unit = {},
    onSetFontSizeScale: (Float) -> Unit = {},
    onClearCache: () -> Unit = {},
    onOptimizeThumbnails: () -> Unit = {},
    onShowToast: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }

    // Audio Engine Toggle states
    var downloadOverWifiOnly by remember { mutableStateOf(!state.isCellularDownloadAllowed) }
    var gaplessPlayback by remember { mutableStateOf(true) }
    var crossfadeDuration by remember { mutableFloatStateOf(4.0f) }
    var automixTransitions by remember { mutableStateOf(true) }
    var normalizeVolume by remember { mutableStateOf(false) }
    var usbDacExclusive by remember { mutableStateOf(true) }

    val activeAccent = AccentColors.getOrElse(state.accentColorIndex) { AccentColors[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("reon_settings_screen")
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar (< Back | Audio & App Settings | Share >)
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
                        text = "Settings & Audio Engine",
                        style = ReonTokens.HeadlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B1020)
                        )
                    )

                    IconButton(
                        onClick = {
                            ShareHelper.shareApp(context)
                            onShareClick()
                        },
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

            // 2. User Profile Card with Edit Button
            item(key = "settings_user_profile_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2EAF8), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(activeAccent, Color(0xFF0038B8))
                                    )
                                )
                                .border(2.dp, activeAccent.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.greetingName.take(1).uppercase().ifEmpty { "U" },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.greetingName,
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B1020)
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(activeAccent.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "HI-RES PRO",
                                        style = ReonTokens.LabelSmall.copy(
                                            color = activeAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                }
                            }

                            Spacer(Modifier.height(3.dp))

                            Text(
                                text = state.userProfileBio,
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF5B6480)
                                ),
                                maxLines = 1
                            )

                            Text(
                                text = state.userProfileEmail,
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color(0xFF8A94A6)
                                )
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Edit Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF4FF))
                                .clickable { onOpenEditProfile() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = activeAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = activeAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. Search Bar Input
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
                                text = "Search settings, themes, audio codecs, hardware",
                                style = ReonTokens.BodySmall.copy(color = Color(0xFF8A94A6), fontSize = 13.sp)
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

            // 4. Customization & Visual Themes Section (Theme Mode, Accent Colors, Background, Fonts, Font Size)
            item(key = "settings_appearance_header") {
                SectionHeaderRow(title = "Appearance & Customization", rightLabel = "Live Preview")
            }

            item(key = "settings_appearance_card") {
                SettingsContainerBox {
                    // Theme Mode Selector
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEEF4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Theme Mode",
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B1020)
                                    )
                                )
                                Text(
                                    text = "Select system, light, obsidian, or OLED black",
                                    style = ReonTokens.BodySmall.copy(
                                        fontSize = 12.sp,
                                        color = Color(0xFF5B6480)
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val modes = listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Obsidian", "OLED" to "OLED Black")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            modes.forEach { (modeKey, label) ->
                                val isSelected = state.themeMode == modeKey
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) activeAccent else Color(0xFFF1F5F9))
                                        .clickable { onSetThemeMode(modeKey) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = ReonTokens.LabelSmall.copy(
                                            color = if (isSelected) Color.White else Color(0xFF334155),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    SettingsDivider()

                    // Accent Colors Palette (6 colors)
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Electric Accent Color",
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                        Text(
                            text = "Color accents applied to active players, buttons, and badges",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF5B6480)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        val colorNames = listOf("Electric Blue", "Neon Cyan", "Cyber Purple", "Emerald", "Sunset Coral", "Rose Magenta")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AccentColors.forEachIndexed { index, color ->
                                val isSelected = state.accentColorIndex == index
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF0B1020) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onSetAccentColor(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SettingsDivider()

                    // Background Canvas Style
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Canvas & Background Tone",
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                        Text(
                            text = "Subtle background canvas tinting",
                            style = ReonTokens.BodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF5B6480)
                            )
                        )

                        Spacer(Modifier.height(12.dp))

                        val bgLabels = listOf("Ice Blue", "Minimal White", "Soft Slate", "Warm Sand", "Cool Studio")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LightBackgrounds.forEachIndexed { index, color ->
                                val isSelected = state.backgroundThemeIndex == index
                                val label = bgLabels.getOrElse(index) { "Theme $index" }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) activeAccent else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSetBackgroundTheme(index) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(activeAccent)
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = label,
                                            style = ReonTokens.LabelSmall.copy(
                                                color = Color(0xFF0B1020),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SettingsDivider()

                    // Typography & Font Family
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEEF4FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TextFields,
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Typography & Font Family",
                                    style = ReonTokens.TitleMedium.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B1020)
                                    )
                                )
                                Text(
                                    text = "Tailor readability with customized typefaces",
                                    style = ReonTokens.BodySmall.copy(
                                        fontSize = 12.sp,
                                        color = Color(0xFF5B6480)
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val fontChoices = listOf(
                            "SANS_SERIF" to "Plus Jakarta Sans",
                            "INTER" to "Inter Clean",
                            "MONOSPACE" to "Tech Mono",
                            "SERIF" to "Serif Editorial"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fontChoices.forEach { (key, name) ->
                                val isSelected = state.fontFamilyChoice == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) activeAccent.copy(alpha = 0.12f) else Color(0xFFF1F5F9))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) activeAccent else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSetFontFamily(key) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = name,
                                        style = ReonTokens.LabelSmall.copy(
                                            color = if (isSelected) activeAccent else Color(0xFF334155),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    SettingsDivider()

                    // Font Size Scale Slider
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
                                        imageVector = Icons.Rounded.FormatSize,
                                        contentDescription = null,
                                        tint = activeAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Text Size Scaling",
                                        style = ReonTokens.TitleMedium.copy(
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0B1020)
                                        )
                                    )
                                    Text(
                                        text = "Adjust global interface text density",
                                        style = ReonTokens.BodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF5B6480)
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(state.fontSizeScale * 100).toInt()}%",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Slider(
                            value = state.fontSizeScale,
                            onValueChange = { onSetFontSizeScale(it) },
                            valueRange = 0.9f..1.2f,
                            steps = 2,
                            colors = SliderDefaults.colors(
                                thumbColor = activeAccent,
                                activeTrackColor = activeAccent,
                                inactiveTrackColor = Color(0xFFE2EAF8)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Compact (90%)", style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6)))
                            Text("Standard (100%)", style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6)))
                            Text("Large (120%)", style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6)))
                        }
                    }
                }
            }

            // 5. REON Hyper-Fidelity Pro Tier Hero Card
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
                                        .background(activeAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = activeAccent,
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
                                                color = activeAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(activeAccent)
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
                                tint = activeAccent,
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
                                        tint = activeAccent,
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
                                    .background(activeAccent)
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

            // 6. Section: Audio Quality (96kHz / 24-Bit)
            item(key = "settings_audio_quality_header") {
                SectionHeaderRow(title = "Audio Quality", rightLabel = "96kHz / 24-Bit")
            }

            item(key = "settings_audio_quality_card") {
                SettingsContainerBox {
                    SettingsRowItem(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Streaming Quality",
                        subtitle = "Lossless FLAC (24-bit / 96kHz)",
                        badgeText = "Hi-Res",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = activeAccent,
                        onClick = { onShowToast("Streaming Quality set to Hi-Res 96kHz FLAC") }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Rounded.Download,
                        title = "Download Quality",
                        subtitle = "Studio Master (FLAC)",
                        valueText = "~45 MB/track",
                        onClick = { onShowToast("Download Quality: Studio Master FLAC") }
                    )

                    SettingsDivider()

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

                    SettingsRowItem(
                        icon = Icons.Rounded.Equalizer,
                        title = "Equalizer & Spatial Audio",
                        subtitle = "Dolby Atmos · REON 3D Sound Engine",
                        badgeText = "Custom EQ",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = activeAccent,
                        onClick = { onShowToast("Opening REON Spatial Equalizer") }
                    )
                }
            }

            // 7. Section: Playback Dynamics (Continuous)
            item(key = "settings_playback_dynamics_header") {
                SectionHeaderRow(title = "Playback Dynamics", rightLabel = "Continuous")
            }

            item(key = "settings_playback_dynamics_card") {
                SettingsContainerBox {
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.Repeat,
                        title = "Gapless Playback",
                        subtitle = "Zero latency between track transitions",
                        checked = gaplessPlayback,
                        onCheckedChange = { gaplessPlayback = it }
                    )

                    SettingsDivider()

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
                                        tint = activeAccent,
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
                                        text = "Smooth blend between consecutive tracks",
                                        style = ReonTokens.BodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF5B6480)
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(activeAccent)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${crossfadeDuration.toInt()}s",
                                    style = ReonTokens.LabelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
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
                                thumbColor = activeAccent,
                                activeTrackColor = activeAccent,
                                inactiveTrackColor = Color(0xFFE2EAF8)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsDivider()

                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Automix (Smart Transitions)",
                        subtitle = "AI key & beatmatched seamless mixing",
                        badgeText = "BETA",
                        badgeBg = activeAccent,
                        badgeTextTint = Color.White,
                        checked = automixTransitions,
                        onCheckedChange = { automixTransitions = it }
                    )

                    SettingsDivider()

                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.VolumeUp,
                        title = "Normalize Volume",
                        subtitle = "Maintain consistent audio level across songs",
                        checked = normalizeVolume,
                        onCheckedChange = { normalizeVolume = it }
                    )
                }
            }

            // 8. Section: App & Hardware Integration
            item(key = "settings_hardware_header") {
                SectionHeaderRow(title = "App & Hardware", rightLabel = "Bit-Perfect")
            }

            item(key = "settings_hardware_card") {
                SettingsContainerBox {
                    SettingsSwitchRowItem(
                        icon = Icons.Rounded.Usb,
                        title = "USB DAC Exclusive Mode",
                        subtitle = "Bypass Android audio resampler for true Bit-Perfect output",
                        badgeText = "HI-RES CORE",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = activeAccent,
                        checked = usbDacExclusive,
                        onCheckedChange = {
                            usbDacExclusive = it
                            onShowToast(if (it) "Bit-Perfect USB DAC Mode Active" else "Standard Audio Driver Active")
                        }
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Rounded.Cast,
                        title = "Cast & AirPlay Direct",
                        subtitle = "Stream Lossless 24-bit to Wi-Fi speakers & DACs",
                        valueText = "Ready",
                        valueDotColor = Color(0xFF00C48C),
                        onClick = { onShowToast("Searching for Hi-Fi Network Streamers...") }
                    )
                }
            }

            // Section: Storage & Cache Optimization
            item(key = "settings_cache_header") {
                SectionHeaderRow(title = "Storage & Cache Optimizer", rightLabel = "Active")
            }

            item(key = "settings_cache_card") {
                SettingsContainerBox {
                    val storageUsedText = String.format("%.1f GB Used", state.storageUsedMb / 1000f)
                    SettingsRowItem(
                        icon = Icons.Rounded.Storage,
                        title = "Lossless Audio Cache",
                        subtitle = "Clear temporary high-resolution streaming buffers",
                        valueText = storageUsedText,
                        badgeText = "CLEAN",
                        badgeBg = Color(0xFFFFEAEA),
                        badgeTextTint = Color(0xFFD32F2F),
                        onClick = onClearCache
                    )

                    SettingsDivider()

                    SettingsRowItem(
                        icon = Icons.Rounded.PhotoSizeSelectActual,
                        title = "Artwork & Thumbnail Footprint",
                        subtitle = "Optimize cached album imagery downsampling",
                        valueText = "Optimized",
                        valueDotColor = Color(0xFF00C48C),
                        onClick = onOptimizeThumbnails
                    )
                }
            }

            // 9. Section: About REON & Open Source Licenses
            item(key = "settings_about_header") {
                SectionHeaderRow(title = "About & Open Source", rightLabel = "MIT Licensed")
            }

            item(key = "settings_about_card") {
                SettingsContainerBox {
                    // About Dialog trigger
                    SettingsRowItem(
                        icon = Icons.Rounded.Info,
                        title = "About REON Hi-Res Music Engine",
                        subtitle = "v2.4.0 (Build 892) · Pure Kotlin & Jetpack Compose M3",
                        badgeText = "PRO CORE",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = activeAccent,
                        onClick = { onOpenAbout() }
                    )

                    SettingsDivider()

                    // Open Source Repo Link
                    SettingsRowItem(
                        icon = Icons.Rounded.Code,
                        title = "Open Source Repository",
                        subtitle = "github.com/reon-audio/reon-android",
                        badgeText = "GitHub",
                        badgeBg = Color(0xFF0B1020),
                        badgeTextTint = Color.White,
                        onClick = {
                            ShareHelper.openBrowserUrl(context, "https://github.com/reon-audio/reon-android")
                            onShowToast("Opening GitHub repository")
                        }
                    )

                    SettingsDivider()

                    // Open Source Licenses Viewer
                    SettingsRowItem(
                        icon = Icons.Rounded.Verified,
                        title = "Open Source Software Licenses",
                        subtitle = "MIT, Apache 2.0, Kotlin Coroutines, Jetpack, Coil",
                        badgeText = "Legal",
                        badgeBg = Color(0xFFEEF4FF),
                        badgeTextTint = activeAccent,
                        onClick = { onOpenLicenses() }
                    )
                }
            }

            // 10. Engine Build Footer (Infinity Icon + Version Info + Links)
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
                            tint = activeAccent,
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
                                color = activeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onShowToast("All audio pipelines healthy: 192kHz/24-bit 0 errors") }
                        )

                        Text(
                            text = "•",
                            style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6))
                        )

                        Text(
                            text = "Licenses",
                            style = ReonTokens.LabelSmall.copy(
                                color = activeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onOpenLicenses() }
                        )

                        Text(
                            text = "•",
                            style = ReonTokens.LabelSmall.copy(color = Color(0xFF8A94A6))
                        )

                        Text(
                            text = "About",
                            style = ReonTokens.LabelSmall.copy(
                                color = activeAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onOpenAbout() }
                        )
                    }
                }
            }
        }

        // Dialog 1: Edit User Profile Dialog
        if (state.isEditProfileDialogOpen) {
            var tempName by remember { mutableStateOf(state.greetingName) }
            var tempBio by remember { mutableStateOf(state.userProfileBio) }

            AlertDialog(
                onDismissRequest = onCloseEditProfile,
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Edit Profile Name",
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Customize your display name and bio shown across REON recommendations and daily greeting.",
                            style = ReonTokens.BodySmall.copy(color = Color(0xFF5B6480))
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeAccent,
                                focusedLabelColor = activeAccent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = tempBio,
                            onValueChange = { tempBio = it },
                            label = { Text("Audiophile Bio / Tagline") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeAccent,
                                focusedLabelColor = activeAccent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateProfile(tempName, tempBio)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCloseEditProfile) {
                        Text("Cancel", color = Color(0xFF5B6480))
                    }
                }
            )
        }

        // Dialog 2: About REON Dialog
        if (state.isAboutDialogOpen) {
            AlertDialog(
                onDismissRequest = onCloseAbout,
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(activeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AllInclusive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "REON Music Core",
                                style = ReonTokens.TitleMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0B1020)
                                )
                            )
                            Text(
                                text = "Hyper-Fidelity Audio Engine v2.4.0",
                                style = ReonTokens.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A94A6)
                                )
                            )
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "REON is a modern open-source Android music player engineered for true audiophiles and music lovers. It features a bit-perfect audio processing pipeline, 24-bit / 192kHz master FLAC streaming, HRTF 3D spatial simulation, and comprehensive theme customization.",
                            style = ReonTokens.BodySmall.copy(
                                color = Color(0xFF334155),
                                lineHeight = 18.sp
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEEF4FF))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "• Lossless Codecs: FLAC, ALAC, WAV, DSD256",
                                    style = ReonTokens.LabelSmall.copy(color = Color(0xFF0B1020), fontSize = 11.5.sp)
                                )
                                Text(
                                    text = "• Internal Bus: 64-bit Floating-Point DSP",
                                    style = ReonTokens.LabelSmall.copy(color = Color(0xFF0B1020), fontSize = 11.5.sp)
                                )
                                Text(
                                    text = "• Output Mode: USB DAC Direct & Resampler Bypass",
                                    style = ReonTokens.LabelSmall.copy(color = Color(0xFF0B1020), fontSize = 11.5.sp)
                                )
                                Text(
                                    text = "• UI: Jetpack Compose Material Design 3",
                                    style = ReonTokens.LabelSmall.copy(color = Color(0xFF0B1020), fontSize = 11.5.sp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCloseAbout()
                            ShareHelper.openBrowserUrl(context, "https://github.com/reon-audio/reon-android")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Visit GitHub Repo", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCloseAbout) {
                        Text("Close", color = Color(0xFF5B6480))
                    }
                }
            )
        }

        // Dialog 3: Open Source Licenses Modal
        if (state.isOpenSourceLicensesDialogOpen) {
            AlertDialog(
                onDismissRequest = onCloseLicenses,
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = activeAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Open Source Licenses",
                            style = ReonTokens.TitleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B1020)
                            )
                        )
                    }
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            LicenseItem(
                                name = "REON Android Music Engine",
                                license = "MIT License",
                                copyright = "Copyright (c) 2024-2026 REON Audio Contributors",
                                text = "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files..."
                            )
                        }
                        item {
                            LicenseItem(
                                name = "Jetpack Compose & AndroidX",
                                license = "Apache License 2.0",
                                copyright = "Copyright (c) The Android Open Source Project",
                                text = "Licensed under the Apache License, Version 2.0."
                            )
                        }
                        item {
                            LicenseItem(
                                name = "Kotlin & Coroutines",
                                license = "Apache License 2.0",
                                copyright = "Copyright 2010-2025 JetBrains s.r.o.",
                                text = "Licensed under the Apache License, Version 2.0."
                            )
                        }
                        item {
                            LicenseItem(
                                name = "Coil Image Loading",
                                license = "Apache License 2.0",
                                copyright = "Copyright 2023 Coil Contributors",
                                text = "Licensed under the Apache License, Version 2.0."
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onCloseLicenses,
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun LicenseItem(name: String, license: String, copyright: String, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = ReonTokens.TitleMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B1020))
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFEEF4FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = license,
                        style = ReonTokens.LabelSmall.copy(color = Color(0xFF0057FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = copyright, style = ReonTokens.BodySmall.copy(fontSize = 11.sp, color = Color(0xFF5B6480)))
            Spacer(Modifier.height(4.dp))
            Text(text = text, style = ReonTokens.BodySmall.copy(fontSize = 10.sp, color = Color(0xFF8A94A6)))
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
