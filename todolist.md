# REON-GOVINDA — Todo List

> Companion to `phasewiseplan.md` (strategy) and `tasks.md` (execution detail).
> Check off `[ ] → [x]` as you go. Owner = `dev` unless noted.

## Phase 0 — Hygiene + scaffold — P0

- [x] Rename package `com.example` → `com.reon.music` (manifest, Room, screens, tests)
- [x] Fix `applicationId` in `app/build.gradle.kts:17` + app label/icon sanity
- [x] Rewrite README claims (remove bit-perfect/DSD/Atmos until measured)
- [x] Add `REON_API_BASE_URL`, `REON_API_KEY` to `.env.example`, wire Secrets → `BuildConfig`
- [x] Scaffold `backend/` isolated Ktor project (own `settings.gradle.kts`)
- [x] Backend `/health` + `/config` return 200 locally (verified: `backend test` green + live `run` smoke test 2026-09-15)

## Phase 1 — Backend thin proxy — P0

- [ ] `InnerTubeClient` search/browse/player/next with 2 client configs
- [ ] Canned-JSON parser tests (search, browse, player)
- [ ] Routes: search, home, albums, artists, playlists, radio
- [ ] Routes: stream-resolve (high/medium/low, audio-only), lyrics, player
- [ ] Caffeine cache with TTLs + `v1:` key prefix
- [ ] IP token-bucket + `X-Reon-Key` optional check + JSON logs
- [ ] `PoTokenProvider` stub + `CipherResolver=null` stub + selector interface
- [ ] Manual curl suite passes; README documents local run

## Phase 2 — Android data layer (data-first) — P0

- [x] `ReonBackendApi.kt` + `BackendDto.kt` (Moshi, names match backend)
- [x] `MusicRepository.kt` (backend → Room → UI, offline fallback)
- [x] Room v1→v2 (`videoId`, `sourceKind`, `streamUrl`, `streamExpiresAt`) + DAO methods
- [x] `HomeViewModel` search (debounce 300ms) + home/catalog on repo
- [x] Split labeling enforced (`YT_STREAM` = Opus/AAC; Hi-Res only for local)
- [x] Search/*/stream unit tests (MockWebServer + Room in-memory) — 14/14 green
- [ ] Emulator: online works, airplane mode shows seeds

## Phase 3 — Real playback — P1

- [x] `PlaybackService` (`MediaSessionService` + ExoPlayer + OkHttp DS)
- [x] Manifest permissions + service declaration + notification channel (FOREGROUND_SERVICE, POST_NOTIFICATIONS, WAKE_LOCK, AUDIO_FOCUS)
- [x] `NowPlayingViewModel` timer deleted, `MediaController` wired, 500ms position poll via `Player.Listener`
- [x] Mini-player + NowPlaying share controller state
- [x] Expiry re-resolve (60s margin, 403/410 retry) in `PlaybackService.onPlayerError` + `MusicRepository.stream`
- [x] Media3 `DownloadManager` placeholder (tracked in Phase 4)
- [x] Background / lock-screen / headset / queue manual pass
- [x] Build: `:app:compileDebugKotlin` green, `:app:testDebugUnitTest` passes (17/19, 2 pre-existing Robolectric Java 21 issues)

## Phase 4 — Hardening (deferred) — P1

- [ ] NewPipe fallback behind flag (JitPack + desugaring + ProGuard)
- [ ] SponsorBlock client + skip logic + Settings toggle + opt-out
- [ ] Quality selector (high/medium/low) + Lrclib fallback

## Phase 5 — Scale (later) — P2/P3

- [ ] Redis swap + Compose file
- [ ] Postgres user sync + JWT (only if cross-device needed)
- [ ] Nginx TLS/rate-limit, Grafana/Sentry
- [ ] Optional: RYD / DeArrow / ListenBrainz

## Global gates

- [x] No `com.example` / `aistudio` strings remain (in `app/` source; plan docs retain history)
- [ ] No YT track labeled FLAC/Hi-Res
- [x] `./gradlew :app:assembleDebug` green (verified 2026-09-15: 25MB APK, installed + launched on Pixel_API34, UI renders, no crashes)
- [ ] MVP DoD in `phasewiseplan.md §7` all checked
