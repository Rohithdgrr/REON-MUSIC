# REON-GOVINDA — Phase-wise Build Plan (Salvage Prototype Path)

> Status: approved direction = **salvage UI prototype**, thin backend proxy, backend-only app.
> Source repo is an AI Studio UI prototype (no playback, no network use). This plan turns it into a working app without forking.

## 0. Locked decisions (do not revisit without explicit override)

| # | Decision | Value |
|---|---|---|
| 1 | Build path | Salvage prototype (keep Compose UI + Room shape) |
| 2 | Auth | Anonymous-only, no OAuth/login in v1 |
| 3 | Build order | Data layer first, playback second |
| 4 | Quality story | Split paths: `LOCAL_HIRES` (real FLAC/ALAC/WAV) vs `YT_STREAM` (Opus/AAC 128–160kbps, never labeled Hi-Res) |
| 5 | Extractor | Defer `NewPipeExtractor` to Phase 4 (fallback only) |
| 6 | Backend scope | Thin anonymous proxy MVP, no Postgres/auth/user-sync; Room stays for likes/history/downloads |
| 7 | Backend location | `backend/` folder in monorepo, isolated Gradle build (not in root `settings.gradle.kts`) |
| 8 | Cache v1 | In-memory Caffeine first, no Redis/Docker required |
| 9 | App wiring | Backend-only; Room seeds = offline fallback; no on-device InnerTube in v1 |

## 1. Verified starting point (evidence, Sep 2026)

- Playback: grep `ExoPlayer|MediaSession|AudioTrack` → 0 hits. No `media3` dependency in `app/build.gradle.kts`.
- Network: grep `@GET|OkHttpClient|Retrofit.Builder|InnerTube` → 0 hits. Retrofit/OkHttp/Moshi declared in `app/build.gradle.kts:117-121` but never used.
- Missing layer: `**/MusicRepository*` → none, despite README claiming it.
- Identity: `app/build.gradle.kts:17` = `com.aistudio.reonmusic.pxrtzq`, all sources `package com.example`, `.env.example:1-7` references AI Studio Gemini inject.
- Data: `app/src/main/java/com/example/data/MusicTrack.kt:49-98` = hardcoded `sampleTracks` with Unsplash art; `data/ReonDatabase.kt:8-30` = 5 entities, `version = 1`, `fallbackToDestructiveMigration` at `ReonDatabase.kt:162`.
- Keep: ~20 Compose screens, `ui/ReonTokens.kt` + theme, Room schema shape. Fix/remove: mock seeds, README bit-perfect/DSD/Atmos claims, `com.example` package.

## 2. Target architecture

```
Compose UI (existing screens, ReonTokens)
  → HomeViewModel / NowPlayingViewModel (thin orchestrators)
    → data/remote/ReonBackendApi.kt (Retrofit, NEW)
    → data/MusicRepository.kt (NEW, Single Source of Truth)
      → Room (offline fallback + likes/history) + Backend (search/catalog/stream/lyrics)
backend/ (NEW, Ktor Netty, isolated build)
  → routes: system/search/catalog/stream
  → innertube/InnerTubeClient.kt + parsers
  → cache/MemoryCache.kt (Caffeine, v-prefixed keys)
  → UpstreamClientSelector + PoTokenProvider(stub) + CipherResolver(null stub)
PlaybackService: MediaSessionService + ExoPlayer (Phase 3)
```

## 3. MVP API contract (backend ↔ app)

```
GET /api/v1/health
GET /api/v1/config
GET /api/v1/search?q=&filter=          # songs|albums|artists|playlists
GET /api/v1/home
GET /api/v1/albums/{id}
GET /api/v1/artists/{id}
GET /api/v1/playlists/{id}
GET /api/v1/radio/{trackId}
GET /api/v1/stream/{trackId}?quality=  # high|medium|low
GET /api/v1/lyrics/{trackId}
GET /api/v1/player/{trackId}
```

Stream JSON (Kotlinx on server, Moshi on app — identical names):
```json
{"url":"https://...googlevideo.com/...","codec":"opus","bitrate":160000,"expires_at":0,"quality":"high"}
```

Out of v1: `/user/*`, `/charts`, `/trending`, `/stream/.../proxy`, Postgres, Redis, Nginx, Prometheus.

## 4. Phases

### Phase 0 — Hygiene + scaffold (0.5–1d) — P0
- Rename `com.example` → `com.reon.music`, fix `applicationId`, update manifest/room package refs.
- Rewrite README aspirational claims → honest MVP status; document split quality story + private-use ToS note.
- `.env.example`: add `REON_API_BASE_URL=http://10.0.2.2:8080`, optional `REON_API_KEY=`; remove Gemini key if unused. Wire Secrets plugin → `BuildConfig`.
- Scaffold `backend/` as standalone Ktor project (`backend/settings.gradle.kts`, `backend/build.gradle.kts`, `Application.kt`, `/health`, `/config`, JSON + CORS + logging + IP token-bucket).
- Gate: `./gradlew` in backend runs; app still builds; no behavior change.

### Phase 1 — Backend thin proxy (1–2w) — P0
- `innertube/InnerTubeClient.kt`: POST `/youtubei/v1/search|browse|player|next`, 2 client configs (Web Remix + Android), timeouts/retries, canned-JSON parser tests.
- Routes: search/home/catalog/radio/lyrics/player/stream-resolve (audio-only pick high/medium/low). Never store audio.
- `cache/MemoryCache.kt`: Caffeine `maximumSize + expireAfterWrite`; TTLs search 1h, home 30m, album/artist 24h, playlist 6h, stream `min(expiry,5m)`, lyrics 7d; keys `v1:...`; bump prefix on parser break.
- Stubs: `PoTokenProvider` (log + metrics on `LOGIN_REQUIRED`), `CipherResolver? = null`, `UpstreamClientSelector` interface for later rotation.
- Gate: `curl /search?q=aurora`, `/albums/{id}`, `/stream/{id}` returns playable expiring URL; restart clears cache (expected).

### Phase 2 — Android data layer (1–2w) — P0, data-first
- New `data/remote/ReonBackendApi.kt` + `BackendDto.kt` (Moshi) + `data/MusicRepository.kt`; `OkHttp` logging + 10s timeouts.
- Room v1→v2: `TrackEntity` += `videoId, sourceKind, streamUrl, streamExpiresAt`; DAO `getTrackByVideoId`, `updateStream`.
- `HomeViewModel` search (300ms debounce) + home/catalog → repo; `IOException` → Room seeds fallback; recent-search logic preserved.
- Enforce labeling: `YT_STREAM` forced `Opus/AAC`; `FLAC/24-bit/192kHz` only for `LOCAL_HIRES`; update pills/badges/filters.
- Gate: emulator search works, airplane mode shows seeds, YT tracks never show Hi-Res badge.

### Phase 3 — Real playback (1–2w) — P1
- `playback/PlaybackService.kt` (`MediaSessionService` + ExoPlayer + OkHttp datasource), manifest `FOREGROUND_SERVICE* + POST_NOTIFICATIONS`, audio-focus/ducking/noisy handling.
- `NowPlayingViewModel`: delete delay-timer, connect `MediaController`, 500ms position poll + `Player.Listener`; `MainActivity` session lifecycle; mini-player + NowPlaying share controller state.
- Media3 `DownloadManager` for YT offline (never labeled FLAC); Room `isDownloaded` becomes real state.
- Gate: background/lock-screen/headset/queue next-prev pass; expiry triggers re-resolve not 403 stall.

### Phase 4 — Hardening (deferred) — P1
- NewPipe fallback only when `player` yields no URL (`data/remote/NewPipeStreamResolver.kt`, `Dispatchers.IO`); JitPack + `desugar_jdk_libs_nio` (minSdk 24) + ProGuard Rhino keeps. Feature-flagged.
- SponsorBlock: `GET sponsor.ajay.app/api/skipSegments`, skip in `Player.Listener`, Settings default ON (sponsor/intro/outro), per-track opt-out.
- Lrclib lyrics fallback; stream quality selector (high/medium/low).

### Phase 5 — Scale (later) — P2/P3
- Swap Caffeine → Redis (Compose), add Postgres user sync + JWT, Nginx TLS/rate-limit/cache, Grafana/Sentry, multi-source (Saavn/SoundCloud via NewPipe), RYD/DeArrow/ListenBrainz optional.

## 5. Cache, rate-limit, expiry rules

- Server IP bucket ~10 rps; client retries with backoff on 429/5xx; log upstream failures with client-config tag.
- Stream cache cap 5m even though Google URLs live ~6h — prevents stale 403s.
- Android re-resolves when `now > streamExpiresAt - 60s` or ExoPlayer reports 403/410.

## 6. Risks

| Risk | Mitigation |
|---|---|
| InnerTube breaks | Pinned upstream version, parser unit tests on canned JSON, `v1→v2` cache bump, `UpstreamClientSelector` seam |
| `LOGIN_REQUIRED` / PO token | Stub metrics + log; full refresh in Phase 4 |
| ToS | Private/self-host only, no public API, no YT-to-FLAC download claims |
| AGP↔Ktor clash | Backend isolated build, not in root settings |
| Scope creep (auth/sync/monitoring) | Explicitly P5; Room covers v1 user data |

## 7. Definition of done (MVP)

- [ ] Backend runs locally, all §3 endpoints return real data
- [ ] App search/browse/stream/lyrics work against backend; offline shows Room
- [ ] Background playback + queue + re-resolve on expiry work
- [ ] No false Hi-Res labels; README honest; package renamed; secrets via `.env`
