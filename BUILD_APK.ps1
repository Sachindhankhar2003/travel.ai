# ═══════════════════════════════════════════════════════════════════════════
# 📱 Travel.ai - Build APK Script
# ═══════════════════════════════════════════════════════════════════════════
Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║         Travel.ai - APK Builder for Your Phone          ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if Android SDK is available
$env:ANDROID_HOME = $env:ANDROID_HOME ?? $env:ANDROID_SDK_ROOT
if (-not $env:ANDROID_HOME) {
    Write-Host "❌ Android SDK not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUTION: Please use Android Studio to build APK instead:" -ForegroundColor Yellow
    Write-Host "  1. Open Android Studio" -ForegroundColor White
    Write-Host "  2. Open this project folder" -ForegroundColor White
    Write-Host "  3. Build → Build Bundle(s) / APK(s) → Build APK(s)" -ForegroundColor White
    Write-Host "  4. APK will be in: app\build\outputs\apk\debug\" -ForegroundColor Green
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✅ Android SDK found at: $env:ANDROID_HOME" -ForegroundColor Green
Write-Host ""

# Navigate to project directory
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

Write-Host "📁 Project directory: $ProjectRoot" -ForegroundColor Cyan
Write-Host ""

# Check if gradlew exists, if not download wrapper jar
if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
    Write-Host "⚠️  Gradle wrapper JAR missing. Downloading..." -ForegroundColor Yellow
    Write-Host ""
    
    # Download gradle-wrapper.jar
    $wrapperUrl = "https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar"
    $wrapperPath = "gradle\wrapper\gradle-wrapper.jar"
    
    try {
        Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperPath -ErrorAction Stop
        Write-Host "✅ Gradle wrapper downloaded successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to download Gradle wrapper" -ForegroundColor Red
        Write-Host "   Error: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "SOLUTION: Use Android Studio instead (see BUILD_APK_INSTRUCTIONS.md)" -ForegroundColor Yellow
        Read-Host "Press Enter to exit"
        exit 1
    }
}

Write-Host "🔨 Starting APK build..." -ForegroundColor Cyan
Write-Host "   This may take 2-5 minutes on first run..." -ForegroundColor Gray
Write-Host ""

# Build the debug APK
try {
    & ".\gradlew.bat" assembleDebug --no-daemon --warning-mode all
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "╔═══════════════════════════════════════════════════════════╗" -ForegroundColor Green
        Write-Host "║              ✅ APK BUILD SUCCESSFUL! ✅                ║" -ForegroundColor Green
        Write-Host "╚═══════════════════════════════════════════════════════════╝" -ForegroundColor Green
        Write-Host ""
        
        $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
        if (Test-Path $apkPath) {
            $apkSize = (Get-Item $apkPath).Length / 1MB
            Write-Host "📱 APK Location:" -ForegroundColor Cyan
            Write-Host "   $ProjectRoot\$apkPath" -ForegroundColor White
            Write-Host ""
            Write-Host "📊 APK Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "📲 Next Steps:" -ForegroundColor Yellow
            Write-Host "   1. Copy app-debug.apk to your phone (USB / Google Drive)" -ForegroundColor White
            Write-Host "   2. Open APK on phone → Install" -ForegroundColor White
            Write-Host "   3. Allow 'Install from unknown sources' if asked" -ForegroundColor White
            Write-Host "   4. Launch Travel.ai app!" -ForegroundColor White
            Write-Host ""
            
            # Offer to open folder
            $openFolder = Read-Host "Open APK folder now? (Y/N)"
            if ($openFolder -eq "Y" -or $openFolder -eq "y") {
                explorer.exe "app\build\outputs\apk\debug"
            }
        } else {
            Write-Host "⚠️  APK file not found at expected location" -ForegroundColor Yellow
            Write-Host "   Try building again or check app\build\outputs\apk\ manually" -ForegroundColor Gray
        }
    } else {
        Write-Host ""
        Write-Host "❌ Build failed with errors" -ForegroundColor Red
        Write-Host "   Check error messages above" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Common fixes:" -ForegroundColor Yellow
        Write-Host "  - Make sure Android SDK is properly installed" -ForegroundColor White
        Write-Host "  - Try: File → Invalidate Caches in Android Studio" -ForegroundColor White
        Write-Host "  - Or use Android Studio: Build → Build APK(s)" -ForegroundColor White
    }
} catch {
    Write-Host ""
    Write-Host "❌ Build command failed" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "SOLUTION: Please use Android Studio to build APK" -ForegroundColor Yellow
    Write-Host "   (See BUILD_APK_INSTRUCTIONS.md for detailed steps)" -ForegroundColor Gray
}

Write-Host ""
Read-Host "Press Enter to exit"
