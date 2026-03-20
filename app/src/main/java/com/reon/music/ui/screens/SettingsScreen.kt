/*
 * REON Music App - Settings Screen
 * Copyright (c) 2024 REON
 * Light Purple Theme Design
 */

package com.reon.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.reon.music.core.preferences.AudioQuality
import com.reon.music.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

// Light Purple Theme Colors (matching other screens)
private val BackgroundPurple = Color(0xFFF5F0FF)
private val SurfacePurple = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A2E)
private val TextSecondary = Color(0xFF6B6B7B)
private val AccentPurple = Color(0xFF8B5CF6)
private val LightPurple = Color(0xFFE9D5FF)
private val IconPink = Color(0xFFFF6B9D)
private val IconBlue = Color(0xFF4A90D9)
private val IconGreen = Color(0xFF50C878)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController? = null,
    onBackClick: () -> Unit = {},
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    
    var showAboutDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentPurple
                        )
                    }
                },
                actions = {
                    // No actions needed - time/battery removed
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPurple
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = BackgroundPurple
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(BackgroundPurple),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Playback Section
            item {
                SettingsSection(
                    title = "Playback",
                    icon = Icons.Outlined.PlayCircle,
                    iconColor = IconPink
                ) {
                    SettingsCard {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.GraphicEq,
                            iconColor = IconBlue,
                            title = "Equalizer",
                            subtitle = "Customize audio output",
                            checked = uiState.normalizeAudio,
                            onCheckedChange = { settingsViewModel.setNormalizeAudio(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.SkipNext,
                            iconColor = IconGreen,
                            title = "Gapless Playback",
                            subtitle = "Seamless transitions",
                            checked = uiState.gaplessPlayback,
                            onCheckedChange = { settingsViewModel.setGaplessPlayback(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.VolumeUp,
                            iconColor = IconPink,
                            title = "Normalize Volume",
                            subtitle = "Equalize volume levels",
                            checked = uiState.normalizeAudio,
                            onCheckedChange = { settingsViewModel.setNormalizeAudio(it) }
                        )
                    }
                }
            }
            
            // Audio Quality Section
            item {
                SettingsSection(
                    title = "Audio Quality",
                    icon = Icons.Outlined.HighQuality,
                    iconColor = IconBlue
                ) {
                    SettingsCard {
                        SettingsItem(
                            icon = Icons.Outlined.HighQuality,
                            iconColor = AccentPurple,
                            title = "Streaming Quality",
                            subtitle = when (uiState.audioQuality.name) {
                                "LOW" -> "Low (96 kbps)"
                                "MEDIUM" -> "Medium (160 kbps)"
                                "HIGH" -> "High (320 kbps)"
                                else -> "High (320 kbps)"
                            },
                            onClick = { showQualityDialog = true }
                        )
                        
                        DividerLine()
                        
                        SettingsItem(
                            icon = Icons.Outlined.Download,
                            iconColor = IconGreen,
                            title = "Download Quality",
                            subtitle = "320 kbps - Best quality",
                            onClick = { showQualityDialog = true }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Wifi,
                            iconColor = IconBlue,
                            title = "Download on Wi-Fi Only",
                            subtitle = "Save mobile data",
                            checked = uiState.downloadWifiOnly,
                            onCheckedChange = { settingsViewModel.setDownloadWifiOnly(it) }
                        )
                    }
                }
            }
            
            // Appearance Section
            item {
                SettingsSection(
                    title = "Appearance",
                    icon = Icons.Outlined.Palette,
                    iconColor = IconPink
                ) {
                    SettingsCard {
                        SettingsItem(
                            icon = Icons.Outlined.DarkMode,
                            iconColor = TextPrimary,
                            title = "Theme",
                            subtitle = when (uiState.theme) {
                                com.reon.music.core.preferences.AppTheme.LIGHT -> "Light"
                                com.reon.music.core.preferences.AppTheme.DARK -> "Dark"
                                com.reon.music.core.preferences.AppTheme.SYSTEM -> "System Default"
                                com.reon.music.core.preferences.AppTheme.AMOLED -> "AMOLED Black"
                            },
                            onClick = { showThemeDialog = true }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.AutoAwesome,
                            iconColor = AccentPurple,
                            title = "Dynamic Colors",
                            subtitle = "Adapt to album art",
                            checked = uiState.dynamicColors,
                            onCheckedChange = { settingsViewModel.setDynamicColors(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Lyrics,
                            iconColor = IconPink,
                            title = "Show Lyrics",
                            subtitle = "Display lyrics when available",
                            checked = uiState.showLyricsDefault,
                            onCheckedChange = { settingsViewModel.setShowLyricsDefault(it) }
                        )
                    }
                }
            }
            
            // Data & Storage Section
            item {
                SettingsSection(
                    title = "Data & Storage",
                    icon = Icons.Outlined.Storage,
                    iconColor = IconGreen
                ) {
                    SettingsCard {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.DataSaverOn,
                            iconColor = IconGreen,
                            title = "Data Saver",
                            subtitle = "Reduce data usage",
                            checked = uiState.dataSaverEnabled,
                            onCheckedChange = { settingsViewModel.setDataSaverEnabled(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.CloudDownload,
                            iconColor = IconBlue,
                            title = "Auto-Cache Songs",
                            subtitle = "Cache while streaming",
                            checked = uiState.autoCacheEnabled,
                            onCheckedChange = { settingsViewModel.setAutoCacheEnabled(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsItem(
                            icon = Icons.Outlined.Delete,
                            iconColor = IconPink,
                            title = "Clear Cache",
                            subtitle = "Free up storage space",
                            onClick = { settingsViewModel.clearCache() }
                        )
                    }
                }
            }
            
            // Notifications Section
            item {
                SettingsSection(
                    title = "Notifications",
                    icon = Icons.Outlined.Notifications,
                    iconColor = IconBlue
                ) {
                    SettingsCard {
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Notifications,
                            iconColor = AccentPurple,
                            title = "New Releases",
                            subtitle = "Artist new releases",
                            checked = uiState.artistNotifications,
                            onCheckedChange = { settingsViewModel.setArtistNotifications(it) }
                        )
                        
                        DividerLine()
                        
                        SettingsSwitchItem(
                            icon = Icons.Outlined.Recommend,
                            iconColor = IconPink,
                            title = "Recommendations",
                            subtitle = "Personalized suggestions",
                            checked = uiState.aiRecommendations,
                            onCheckedChange = { settingsViewModel.setAIRecommendations(it) }
                        )
                    }
                }
            }
            
            // About Section
            item {
                SettingsSection(
                    title = "About",
                    icon = Icons.Outlined.Info,
                    iconColor = TextSecondary
                ) {
                    SettingsCard {
                        SettingsItem(
                            icon = Icons.Outlined.Info,
                            iconColor = AccentPurple,
                            title = "About REON",
                            subtitle = "Version 1.0.0",
                            onClick = { showAboutDialog = true }
                        )
                        
                        DividerLine()
                        
                        SettingsItem(
                            icon = Icons.Outlined.Star,
                            iconColor = IconPink,
                            title = "Rate App",
                            subtitle = "Give us 5 stars",
                            onClick = { /* Rate app */ }
                        )
                        
                        DividerLine()
                        
                        SettingsItem(
                            icon = Icons.Outlined.Share,
                            iconColor = IconBlue,
                            title = "Share App",
                            subtitle = "Share with friends",
                            onClick = { /* Share app */ }
                        )
                    }
                }
            }
            
            // Bottom spacing
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    
    // Audio Quality Dialog
    if (showQualityDialog) {
        QualityDialog(
            currentQuality = uiState.audioQuality,
            onQualitySelected = { quality ->
                settingsViewModel.setAudioQuality(quality)
                showQualityDialog = false
            },
            onDismiss = { showQualityDialog = false }
        )
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = uiState.theme,
            onThemeSelected = { theme ->
                settingsViewModel.setTheme(theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        content()
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AccentPurple,
                checkedTrackColor = LightPurple
            )
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = LightPurple.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}

@Composable
private fun QualityDialog(
    currentQuality: AudioQuality,
    onQualitySelected: (AudioQuality) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Audio Quality",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                QualityOption(
                    title = "Low (96 kbps)",
                    subtitle = "Save data, lower quality",
                    selected = currentQuality == AudioQuality.LOW,
                    onClick = { onQualitySelected(AudioQuality.LOW) }
                )
                QualityOption(
                    title = "Medium (160 kbps)",
                    subtitle = "Balanced quality and data",
                    selected = currentQuality == AudioQuality.MEDIUM,
                    onClick = { onQualitySelected(AudioQuality.MEDIUM) }
                )
                QualityOption(
                    title = "High (320 kbps)",
                    subtitle = "Best quality, uses more data",
                    selected = currentQuality == AudioQuality.HIGH,
                    onClick = { onQualitySelected(AudioQuality.HIGH) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        containerColor = SurfacePurple
    )
}

@Composable
private fun QualityOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentPurple
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ThemeDialog(
    currentTheme: com.reon.music.core.preferences.AppTheme,
    onThemeSelected: (com.reon.music.core.preferences.AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Theme",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                ThemeOption(
                    title = "Light",
                    subtitle = "Always use light theme",
                    selected = currentTheme == com.reon.music.core.preferences.AppTheme.LIGHT,
                    onClick = { onThemeSelected(com.reon.music.core.preferences.AppTheme.LIGHT) }
                )
                ThemeOption(
                    title = "Dark",
                    subtitle = "Always use dark theme",
                    selected = currentTheme == com.reon.music.core.preferences.AppTheme.DARK,
                    onClick = { onThemeSelected(com.reon.music.core.preferences.AppTheme.DARK) }
                )
                ThemeOption(
                    title = "System Default",
                    subtitle = "Follow system setting",
                    selected = currentTheme == com.reon.music.core.preferences.AppTheme.SYSTEM,
                    onClick = { onThemeSelected(com.reon.music.core.preferences.AppTheme.SYSTEM) }
                )
                ThemeOption(
                    title = "AMOLED Black",
                    subtitle = "Pure black for OLED screens",
                    selected = currentTheme == com.reon.music.core.preferences.AppTheme.AMOLED,
                    onClick = { onThemeSelected(com.reon.music.core.preferences.AppTheme.AMOLED) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AccentPurple)
            }
        },
        containerColor = SurfacePurple
    )
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AccentPurple
            )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "About REON",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(AccentPurple, IconPink)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "REON Music",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "A modern music streaming app with support for Indian music channels. Discover, stream, and enjoy your favorite songs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Created with ❤️ by Rohith",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentPurple
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = AccentPurple)
            }
        },
        containerColor = SurfacePurple
    )
}
