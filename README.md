# REON — Ultra High-Fidelity Lossless Music Engine for Android

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" height="120" alt="REON Music Core Logo" />
</p>

<p align="center">
  <strong>Bit-Perfect Audio Pipeline • 24-bit / 192kHz Lossless FLAC • 3D HRTF Spatial DSP • Jetpack Compose Material Design 3</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Audio-24--bit%20%2F%20192kHz-0057FF?style=for-the-badge" alt="Audio Quality" />
  <img src="https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge" alt="License" />
</p>

---

## 🎧 Overview

**REON** is a state-of-the-art Android music player and audio engine crafted for true audiophiles, music collectors, and high-fidelity sound engineers. Built from the ground up using **Kotlin**, **Jetpack Compose (Material Design 3)**, and a **64-bit Floating-Point DSP bus**, REON delivers an uncompromising acoustic listening experience.

Whether streaming studio-master FLAC tracks, driving external high-end USB DACs with bit-perfect bypass, customizing visual canvas tones, or analyzing acoustic listening spectrums, REON provides professional-grade audio fidelity with fluid modern aesthetics.

---

## ✨ Key Features & Capabilities

### 🎛️ 1. Hyper-Fidelity Audio Engine & DSP
- **Bit-Perfect Pipeline**: Bypasses Android's default resampling layer for direct, bit-accurate digital-to-analog conversion over USB DACs.
- **Master Audio Codec Support**: Native decoding for 24-bit/192kHz FLAC, ALAC, WAV, and DSD256.
- **3D HRTF Spatial Audio**: Binaural spatial simulation and Dolby Atmos preset integration for multidimensional acoustic stage placement.
- **Gapless & Crossfade**: Zero-latency transitions between consecutive tracks with configurable 0–12s crossfade duration and smart automixing.

### 🎨 2. Comprehensive UI/UX Customization
- **Theme Modes**: Seamless switching between **System Default**, **Light**, **Obsidian Dark (Deep Focus UI)**, and pure **OLED Pitch Black**.
- **6 Electric Accent Colors**:
  - 🔵 **Electric Blue** (`#0057FF`)
  - 💎 **Neon Cyan** (`#00D2FF`)
  - 🔮 **Cyber Purple** (`#8B5CF6`)
  - 🌿 **Emerald Green** (`#10B981`)
  - 🌅 **Sunset Coral** (`#FF4757`)
  - 🌸 **Rose Magenta** (`#FF3377`)
- **Background Canvas Palettes**: Customizable canvas undertones in both light (Modern Ice Blue, Minimal White, Soft Slate, Warm Sand) and dark variations.
- **Custom Typography**: Select from **Plus Jakarta Sans** (Modern Geometric), **Inter** (Clean), **Technical Monospace**, or **Editorial Serif**.
- **Text Size Scaling**: Adjustable global UI scale slider from 90% (Compact) to 120% (Extra Large).

### 📱 3. Feature-Packed Screen Ecosystem
- **Now Playing Studio**: Full-screen interactive player with real-time waveform progress visualizer, synchronized karaoke lyrics viewer, queue manager, sleep timer, and spatial sound switch.
- **Interactive Home Feed**: Dynamic daily greeting, Continue Listening hero banner, High-Res Daily Highlights, Curated Moods, Audio Quality telemetry badges, and mini-player dock.
- **Smart Search & Filters**: Search catalog with instant codec pills (Lossless, Hi-Res 24-bit, Studio Master, Spatial Atmos, DSD), recent query chips, and voice search simulation.
- **Offline Downloads Core**: Multi-track download manager, offline storage breakdown visualizer, Wi-Fi-only safety toggles, and auto-sync queue.
- **Acoustic Analytics**: Radar listening radar, weekly audio hours graph, top artist affinity breakdown, and average bitrate telemetry.
- **Notification Feed**: Categorized notification center (Releases, Audio Engine, Downloads, Live Streams, System) with unread badges, mark-all-as-read, and actionable cards.
- **User Profile & Settings**: Edit user name and audiophile bio, examine audio engine specs, toggle hardware DAC mode, launch GitHub repository, and browse Open Source licenses.

### 🔗 4. Native System Sharing
- Instant Android `Intent.ACTION_SEND` integration across tracks, artists, albums, playlists, and engine configurations with rich formatted metadata.

---

## 🏛️ Architecture & Tech Stack

REON is built following modern Android Architecture best practices:

- **Language**: 100% Kotlin with Coroutines & Flow
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) + Single Source of Truth `StateFlow`
- **State Management**: `collectAsStateWithLifecycle` for lifecycle-aware reactive UI updates
- **Navigation & Animation**: Fluid `AnimatedContent` cross-fades and vertical slide transitions
- **Image Loading**: Async image rendering and placeholders
- **Design System**: Modular `ReonTokens` (Colors, Spacing, Typography, Radius, Shadows)

```
app/src/main/java/com/example/
├── MainActivity.kt                 # Single Activity entry point & theme orchestrator
├── data/
│   └── MusicRepository.kt          # Hi-Res catalog, tracks, playlists, artist repositories
└── ui/
    ├── HomeScreen.kt               # Central dashboard, tab host, & sub-screen coordinator
    ├── HomeViewModel.kt            # Global application state, filter, customization & notification VM
    ├── HomeState.kt                # Immutable UI state models
    ├── NowPlayingScreen.kt         # Immersive playback studio & lyrics sheet
    ├── NowPlayingViewModel.kt      # Playback engine, sleep timer, DSP presets VM
    ├── SettingsScreen.kt           # Customization controls, profile editor, about & licenses
    ├── NotificationScreen.kt       # Activity & engine notifications feed
    ├── AnalyticsScreen.kt          # Acoustic telemetry & listening statistics
    ├── SearchScreen.kt             # Search engine & lossless format filters
    ├── DownloadsScreen.kt          # Offline storage manager & download queue
    ├── LikedSongsScreen.kt         # User favorites library
    ├── HistoryScreen.kt            # Listening history timeline
    ├── ArtistScreen.kt             # Artist profile, discography & radio
    ├── AlbumScreen.kt              # Album tracks, cover art & lossless metadata
    ├── PlaylistScreen.kt           # Curated playlist tracklist & playback
    ├── ReonTokens.kt               # Design system typography, tokens & palettes
    ├── ShareHelper.kt              # Android system share intent helper
    └── theme/
        ├── ReonTheme.kt            # Dynamic M3 theme, accent color & font injector
        ├── Color.kt
        ├── Type.kt
        └── Shape.kt
```

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or JDK 21
- Android SDK 35 (Android 15)
- Minimum SDK: Android 8.0 (API 26)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/reon-audio/reon-android.git
   cd reon-android
   ```
2. Open the project in **Android Studio**.
3. Build the debug APK:
   ```bash
   gradle :app:assembleDebug
   ```
4. Run on an Android device or emulator with API 26+.

---

## 📄 Open Source License

```
MIT License

Copyright (c) 2024-2026 REON Audio Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
