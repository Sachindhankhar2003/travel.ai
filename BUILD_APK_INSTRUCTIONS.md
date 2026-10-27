# 📱 Build APK for Your Phone - Step by Step

## ⚠️ Important: Your laptop doesn't have Gradle wrapper set up

Since building on your laptop is slow/hanging, I recommend **2 options**:

---

## 🚀 **Option 1: Use Android Studio (EASIEST - 5 minutes)**

### Steps:
1. **Open Android Studio**
2. **Open this project**: `C:\Users\Public\Desktop\Travel.ai`
3. Wait for Gradle sync to finish (may take 2-5 minutes first time)
4. **Build APK**:
   - Go to: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Or press: `Ctrl + Shift + F9`
5. **Wait for build** (2-3 minutes)
6. When done, you'll see: **"APK(s) generated successfully"**
7. Click **"locate"** button
8. APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

### Transfer to Phone:
- Copy `app-debug.apk` to your phone via USB cable
- Or upload to Google Drive and download on phone
- Open APK on phone → Install → Allow "Install from unknown sources"

---

## 🔧 **Option 2: Use Online Build Service (If Android Studio hangs)**

### A. **Appetize.io** (Free trial - builds in cloud):
1. Zip your entire project folder
2. Go to: https://appetize.io/
3. Upload zip → Build APK
4. Download APK

### B. **Use a Friend's PC/Laptop**:
Copy this entire folder to a better PC and use Option 1

---

## 🛠️ **Option 3: Manual Gradle Setup (Advanced - 10-15 min)**

If you want to build via command line:

### Step 1: Download & Install Gradle Wrapper
```powershell
# Navigate to project
cd "C:\Users\Public\Desktop\Travel.ai"

# Download gradle wrapper (if you have internet)
# You need Gradle 8.6+ installed first
```

**OR download Gradle manually:**
1. Go to: https://gradle.org/releases/
2. Download: `gradle-8.6-bin.zip`
3. Extract to: `C:\Gradle\gradle-8.6`
4. Add to PATH: `C:\Gradle\gradle-8.6\bin`

### Step 2: Create Gradle Wrapper
```powershell
cd "C:\Users\Public\Desktop\Travel.ai"
gradle wrapper --gradle-version 8.6
```

### Step 3: Build APK
```powershell
.\gradlew.bat assembleDebug
```

APK location: `app\build\outputs\apk\debug\app-debug.apk`

---

## ❓ **Which Features Work Without Backend?**

Since the FastAPI backend isn't deployed yet, here's what works:

### ✅ **FULLY WORKING** (No backend needed):
- ✅ **Home Screen**: Weather (Open-Meteo API)
- ✅ **Map Screen**: Search places (Photon) + Routing (OSRM)
- ✅ **Explore Screen**: Find restaurants, hotels, ATMs, etc. (Overpass API)
- ✅ **Settings**: Change city, temp unit, language
- ✅ **Budget Calculator**: Distance (OSRM) + room prices (fallback defaults)

### ⚠️ **PARTIALLY WORKING** (Uses cached/fallback data):
- ⚠️ **Metro Routes**: Uses local DB (112 stations seeded)
- ⚠️ **Bus Search**: Will show "No connection" error (needs backend)
- ⚠️ **Budget Room Prices**: Uses default prices (₹800/₹2000/₹5000)

---

## 📲 **After Installing APK on Phone**

### First Launch Checklist:
1. ✅ Grant **Location Permission** (for weather & nearby places)
2. ✅ Grant **Internet Permission** (automatic)
3. ✅ Complete onboarding (2 screens)
4. ✅ Select your city
5. ✅ Test each screen!

### Expected Behavior:
- **Home**: Should load weather for your location
- **Explore**: Should find nearby restaurants/hotels
- **Map**: Should allow route planning
- **Metro**: Should show Delhi metro stations (works offline!)
- **Bus**: Will show error (backend not deployed)
- **Budget**: Works with default room prices

---

## 🐛 **Troubleshooting**

### Build Fails with "SDK not found":
- Install Android SDK via Android Studio
- OR set `ANDROID_HOME` environment variable

### "Out of memory" during build:
Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m
```

### Phone says "App not installed":
- Enable: Settings → Security → **Install from unknown sources**
- OR: Settings → Apps → **Special access** → Install unknown apps

### App crashes on launch:
- Check logcat: `adb logcat | findstr "AndroidRuntime"`
- Share error with me

---

## 💡 **Recommended: Use Android Studio Option 1**

Since your laptop hangs with emulator, just:
1. Build APK in Android Studio (NO emulator needed!)
2. Transfer APK file to phone
3. Test everything on real device

**Building APK doesn't need emulator to run!** 🎉

---

## 🆘 Need Help?

If you get stuck:
1. Take screenshot of error
2. Tell me which option you tried
3. I'll help debug!
