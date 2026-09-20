# REON — Android Music Client (Compose M3) + Thin Backend Proxy

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" height="120" alt="REON Music Core Logo" />
</p>

<p align="center">
  <strong>Jetpack Compose Material Design 3 • Thin Ktor Backend Proxy • Media3 Playback (in progress)</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Audio-24--bit%20%2F%20192kHz-0057FF?style=for-the-badge" alt="Audio Quality" />
  <img src="https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge" alt="License" />
</p>

---

## 🎧 Overview — honest MVP status (Phase 0)

**REON** is currently a **Compose UI prototype with a Room-backed mock catalog**, being converted into a working client. Package is `com.reon.music`. There is **no real playback, no network use, and no `MusicRepository` yet** — see `phasewiseplan.md`, `todolist.md`, `tasks.md` for the salvage path.

Direction (locked): thin anonymous Ktor proxy in `backend/` (in-memory cache, no Redis/Postgres in v1), app talks backend-only with Room offline fallback, data-layer first, `LOCAL_HIRES` vs `YT_STREAM` split labeling (YouTube streams are Opus/AAC 128–160kbps and are never labeled FLAC/Hi-Res), NewPipeExtractor deferred to fallback only. Private/self-host use only.

Hi-Res/bit-perfect/USB-DAC/DSD/Atmos goals remain **future work** and must be measured before re-claiming. Do not market YT streams as lossless.

---

## ✨ Key Features & Capabilities

### 🎛️ 1. Audio Engine & DSP — current vs planned
- **Now (prototype):** mock `NowPlayingViewModel` timer, no ExoPlayer/Media3, no DSP, no gapless/crossfade.
- **Next (Phase 3):** Media3 `MediaSessionService` + ExoPlayer background playback, audio focus, re-resolve on URL expiry. Hi-Res local files (`LOCAL_HIRES`) tracked separately from `YT_STREAM` (Opus/AAC).

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

### 📱 3. Screen Ecosystem — prototype status
- **Now Playing / Home / Search / Downloads / Analytics / Notifications / Settings / Library screens exist as Compose UI** with mock data (`MusicTrack.sampleTracks`, Room seeds). Search queries local mocks, downloads are boolean flags, lyrics/voice are hardcoded/simulated.
- **Next:** wire to `backend/` via `ReonBackendApi` + `MusicRepository` (missing — to be created in Phase 2).

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
app/src/main/java/com/reon/music/
├── MainActivity.kt                 # Single Activity entry point & theme orchestrator
├── ReonApplication.kt
├── data/
│   ├── MusicTrack.kt               # UI model + sampleTracks mock (to be replaced)
│   ├── ReonDatabase.kt             # Room entities/DAO (v1; v2 adds videoId/sourceKind/stream fields)
│   └── remote/                     # TO BE CREATED Phase 2: ReonBackendApi.kt, BackendDto.kt, MusicRepository.kt
├── playback/                       # TO BE CREATED Phase 3: PlaybackService.kt (MediaSessionService + ExoPlayer)
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
- Minimum SDK: Android 7.0 (API 24, matches `app/build.gradle.kts`)

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
