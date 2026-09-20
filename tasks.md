# REON-GOVINDA — Tasks (granular, with acceptance criteria)

> IDs are stable: `P0-xxx`, `P1-xxx`, `P2-xxx`, `P4-xxx`, `P5-xxx`.
> Each task lists files, steps, and done-criteria. Effort: S <0.5d, M 0.5–2d, L 2–5d.

## P0 — Hygiene + scaffold

### P0-01 Rename package — M
- Files: `app/build.gradle.kts:17`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/example/**`, tests.
- Steps: rename to `com.reon.music` via Android Studio refactor → fix imports → clean build.
- Done: zero `com.example` / `aistudio` matches; `:app:assembleDebug` green.

### P0-02 Honest README + config — S
- Files: `README.md`, `.env.example`, `app/build.gradle.kts:66-72`.
- Steps: replace bit-perfect/DSD/Atmos/streaming-FLAC claims with MVP status + split quality story + private-use note; add `REON_API_BASE_URL`, `REON_API_KEY`; verify `BuildConfig` fields exist.
- Done: README matches code; emulator debug reads base URL `http://10.0.2.2:8080`.

### P0-03 Backend scaffold — M
- Files: `backend/settings.gradle.kts`, `backend/build.gradle.kts`, `backend/src/main/kotlin/com/reon/Application.kt`, `backend/src/main/kotlin/com/reon/routes/SystemRoutes.kt`.
- Steps: Ktor Netty + kotlinx.serialization + CORS + CallLogging + status pages; `GET /api/v1/health`, `/api/v1/config`; IP bucket ~10rps; optional `X-Reon-Key`.
- Done: `backend` builds independently; curl both endpoints 200; root build unaffected (backend not in `settings.gradle.kts:27`).

## P1 — Backend thin proxy (anonymous)

### P1-01 InnerTube client core — L
- Files: `backend/.../innertube/InnerTubeClient.kt`, `Models.kt`, `Requests.kt`.
- Steps: implement `search`, `browse`, `player`, `next` POSTs; Web Remix + Android configs; 10s timeout, 1 retry on 429/5xx with backoff; tag errors by client-config.
- Done: unit tests with canned JSON pass; unknown fields ignored, no crash on schema drift.

### P1-02 Catalog routes — M
- Files: `backend/.../routes/SearchRoutes.kt`, `CatalogRoutes.kt`, `MusicService.kt`.
- Steps: map InnerTube → stable DTOs (id/title/artist/album/duration/art); `home`, `albums/{id}`, `artists/{id}`, `playlists/{id}`, `radio/{trackId}`.
- Done: curl each returns schema-valid JSON; empty upstream → `200 []`, not 500.

### P1-03 Stream + lyrics + player — M
- Files: `backend/.../routes/StreamRoutes.kt`.
- Steps: `player` → pick audio-only renditions for `high|medium|low`; return `{url,codec,bitrate,expires_at,quality}`; `lyrics/{id}`, `player/{id}` metadata.
- Done: returned URL plays in `ffplay`/ExoPlayer; `expires_at` present; ciphered-only stream returns `502 STREAM_CIPHERED` (Phase 4 hook), not stack trace.

### P1-04 Memory cache — S
- Files: `backend/.../cache/MemoryCache.kt`.
- Steps: Caffeine `maximumSize=10k`, `expireAfterWrite` per TTL (search 1h, home 30m, album/artist 24h, playlist 6h, stream 5m, lyrics 7d); keys `v1:{type}:{id}`.
- Done: second identical call is cache hit in logs; stream never served past `expires_at`.

## P2 — Android data layer

### P2-01 Backend API + DTOs — S
- Files: `app/.../data/remote/ReonBackendApi.kt`, `BackendDto.kt`.
- Steps: Retrofit `GET` defs matching §3; Moshi codegen; OkHttp logging + timeouts; base URL from `BuildConfig`.
- Done: MockWebServer test proves paths + query params + error mapping.

### P2-02 Repository — M
- Files: `app/.../data/MusicRepository.kt` (new), `ReonDatabase.kt`.
- Steps: `search/query/home/catalog/stream/lyrics` with Room write-through; `try backend catch IOException → Room fallback`; expose `Flow`.
- Done: airplane-mode test returns seeds; stream re-resolve when `expiresAt-60s` passed.

### P2-03 Room v1→v2 — S
- Files: `app/.../data/ReonDatabase.kt:8-30,137-147`.
- Steps: add `videoId, sourceKind, streamUrl, streamExpiresAt`; `@Query getTrackByVideoId`, `updateStream`; bump `version=2`.
- Done: migration test (or destructive-dev) passes; old installs don't crash.

### P2-04 ViewModel wiring + labeling — M
- Files: `app/.../ui/HomeViewModel.kt`, `SearchScreen.kt`, `MusicTrack.kt:10-29`.
- Steps: 300ms debounce search → repo; home/catalog → repo; mapper forces `YT_STREAM→Opus/AAC`, `LOCAL_HIRES→FLAC` only; update pills/badges.
- Done: no YT item shows `FLAC/96kHz/192kHz`; UI tests green.

## P3 — Playback (P1 priority after data)

### P3-01 PlaybackService — L
- Files: `app/.../playback/PlaybackService.kt`, `AndroidManifest.xml`, `app/build.gradle.kts` (+ `media3-exoplayer`, `media3-session`, `media3-datasource-okhttp` 1.10.1).
- Steps: `MediaSessionService` + ExoPlayer + OkHttp DS + audio focus/ducking/noisy; notification channel; `FOREGROUND_SERVICE*` permissions.
- Done: background + lock-screen controls work; `adb` kill-UI keeps audio.

### P3-02 Controller wiring — M
- Files: `MainActivity.kt`, `ui/NowPlayingViewModel.kt`, `MiniPlayerDock.kt`, `NowPlayingScreen.kt`.
- Steps: delete delay timer; `MediaController` session lifecycle; shared position/queue state; 403/410 → repo re-resolve + retry once.
- Done: play/pause/next/prev/seek/queue pass manual script; no dual-source-of-truth timer.

### P3-03 Offline — M
- Files: `DownloadsScreen.kt`, `ReonDao`, Media3 `DownloadManager`.
- Steps: YT offline via DownloadManager (AAC/Opus); local Hi-Res via existing Room flag; separate storage accounting.
- Done: downloaded YT track plays offline and is labeled correctly.

## P4 — Hardening (deferred, flagged)

### P4-01 NewPipe fallback — M
- Files: `app/.../data/remote/NewPipeStreamResolver.kt`, `gradle/libs.versions.toml`, ProGuard.
- Steps: behind `FLAG_NEWPIPE_FALLBACK=false` default; `Dispatchers.IO`; JitPack `v0.26.5` + `desugar_jdk_libs_nio`.
- Done: flag ON + forced primary failure still resolves; flag OFF path untouched; APK size delta noted.

### P4-02 SponsorBlock — S
- Files: `playback/SponsorBlockClient.kt`, `SettingsScreen.kt`.
- Steps: `GET sponsor.ajay.app/api/skipSegments`; skip `sponsor/intro/outro` in `Player.Listener`; Settings default ON; per-track opt-out.
- Done: known-segment video auto-skips; boundary loop-free; toggle respected.

### P4-03 Quality + lyrics fallback — S
- Steps: high/medium/low selector → `stream?quality=`; Lrclib second source.
- Done: switching quality re-resolves and continues playback.

## P5 — Scale (later)

### P5-01 Redis swap — M
- Replace `MemoryCache` with Redis impl behind interface; add `docker-compose.yml` (backend+redis); keep single-node default.
- Done: same route tests pass with `CACHE=redis`.

### P5-02 User sync — L
- Only if cross-device needed: Postgres schema from backend spec, JWT, `/user/*`.
- Done: two devices share likes/playlists; anonymous mode still works.

### P5-03 Ops — M
- Nginx TLS/rate-limit, Prometheus/Grafana, Sentry; `/health` enriched.
- Done: dashboards show p95 + hit ratio + upstream errors.

## Cross-cutting test commands

```bash
# backend (in backend/)
./gradlew test
./gradlew run  # curl localhost:8080/api/v1/health

# android (repo root)
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
