# REON Music App - Deployment Script
# Local build helper for generating release artifacts.
# Note: Signing configs and credentials must be provided via Gradle
# configuration (e.g. gradle.properties / environment variables) and
# should never be hardcoded in this script or committed to version control.

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "REON Music App - Deployment Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Gradle is available
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "Error: gradlew.bat not found!" -ForegroundColor Red
    exit 1
}

# Clean previous builds
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
.\gradlew.bat clean

# Build Release APK (FOSS version)
Write-Host ""
Write-Host "Building FOSS Release APK..." -ForegroundColor Yellow
.\gradlew.bat assembleFossRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ FOSS Release APK built successfully!" -ForegroundColor Green
    $fossApk = Get-ChildItem -Path ".\app\build\outputs\apk\foss\release" -Filter "*.apk" | Select-Object -First 1
    if ($fossApk) {
        Write-Host "  Location: $($fossApk.FullName)" -ForegroundColor Green
    }
} else {
    Write-Host "✗ FOSS Release build failed!" -ForegroundColor Red
}

# Build Release APK (Full version)
Write-Host ""
Write-Host "Building Full Release APK..." -ForegroundColor Yellow
.\gradlew.bat assembleFullRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Full Release APK built successfully!" -ForegroundColor Green
    $fullApk = Get-ChildItem -Path ".\app\build\outputs\apk\full\release" -Filter "*.apk" | Select-Object -First 1
    if ($fullApk) {
        Write-Host "  Location: $($fullApk.FullName)" -ForegroundColor Green
    }
} else {
    Write-Host "✗ Full Release build failed!" -ForegroundColor Red
}

# Build Release AAB (for Play Store)
Write-Host ""
Write-Host "Building Release AAB (for Play Store)..." -ForegroundColor Yellow
.\gradlew.bat bundleFullRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Release AAB built successfully!" -ForegroundColor Green
    $aab = Get-ChildItem -Path ".\app\build\outputs\bundle\fullRelease" -Filter "*.aab" | Select-Object -First 1
    if ($aab) {
        Write-Host "  Location: $($aab.FullName)" -ForegroundColor Green
    }
} else {
    Write-Host "✗ AAB build failed!" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deployment Build Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Test the APK on a device" -ForegroundColor White
Write-Host "2. For Play Store: Upload the AAB file" -ForegroundColor White
Write-Host "3. For direct distribution: Share the APK file" -ForegroundColor White
Write-Host ""

