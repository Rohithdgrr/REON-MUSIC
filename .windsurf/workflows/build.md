---
description: Build REON Music Android App
---

# Build REON Music Android App

## Prerequisites
- Android Studio or IntelliJ IDEA with Android plugin
- JDK 17 or higher
- Android SDK installed

## Build Steps

1. Clean the project
// turbo
```powershell
.\gradlew.bat clean
```

2. Build the debug APK
// turbo
```powershell
.\gradlew.bat assembleDebug
```

3. Or build the release APK (requires signing configuration)
```powershell
.\gradlew.bat assembleRelease
```

4. Install debug APK to connected device
// turbo
```powershell
.\gradlew.bat installDebug
```

5. Run the app on connected device
// turbo
```powershell
.\gradlew.bat run
```

## Quick Deploy Script

For faster iteration, use the provided deploy script:
```powershell
.\quick-deploy.bat
```

Or use PowerShell:
```powershell
.\deploy.ps1
```

## Verify Build

After building, the APK will be located at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Troubleshooting

- If build fails with memory issues, increase heap size in `gradle.properties`
- For sync issues, try: `.\gradlew.bat --stop` then rebuild
- Clear caches: `.\gradlew.bat cleanBuildCache`
