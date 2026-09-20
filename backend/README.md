# REON Backend — thin anonymous proxy (Phase 0 scaffold)

Isolated Ktor project. **Not** part of the root Android build (`settings.gradle.kts` includes only `:app`).

## Run

```bash
cd backend
../gradlew run          # Windows: ..\gradlew run
# server on http://0.0.0.0:8080
```

Emulator app points at `http://10.0.2.2:8080` via `REON_API_BASE_URL` (see root `.env.example`).

## Endpoints (v1 scaffold)

- `GET /api/v1/health` → `{"status":"ok","version":"0.1.0"}`
- `GET /api/v1/config` → feature flags + quality options (static for now)

Phase 1 adds: search/home/catalog/radio/stream/lyrics/player + Caffeine cache + InnerTube client.
Phase 4 adds: NewPipe cipher fallback, PO-token refresh, proxy rotation.

## Config (env vars)

| Var | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | listen port |
| `REON_API_KEY` | `` (disabled) | if set, require `X-Reon-Key` header |
| `CACHE_PREFIX` | `v1` | bump to invalidate all cached entries |
| `LOG_LEVEL` | `INFO` | logback level |

Private/self-host use only. Never expose as a public API.
