# 📱 How to Get APK for Your Phone - QUICK START

## 🎯 **EASIEST METHOD - Use Android Studio (5 minutes)**

### Step-by-Step:

1. **Open Android Studio** (the app with green Android robot icon)

2. **Open Project**
   - Click: `File` → `Open`
   - Navigate to: `C:\Users\Public\Desktop\Travel.ai`
   - Click: `OK`

3. **Wait for Sync** (2-3 minutes first time)
   - You'll see "Gradle build running..." at bottom
   - Wait until it says "Build Successful" or just completes

4. **Build APK** 
   - Click: `Build` menu (top of screen)
   - Then: `Build Bundle(s) / APK(s)`
   - Then: `Build APK(s)`
   
   **OR** just press: `Ctrl + Shift + F9`

5. **Wait 2-3 minutes**
   - You'll see progress at bottom: "Building APK..."
   - When done, you'll see green notification: **"APK(s) generated successfully"**

6. **Find Your APK**
   - Click the **"locate"** link in the notification
   - **OR** manually go to:
     ```
     C:\Users\Public\Desktop\Travel.ai\app\build\outputs\apk\debug\app-debug.apk
     ```

7. **Transfer to Phone**
   - **USB Cable**: Copy `app-debug.apk` to phone
   - **Google Drive**: Upload APK, download on phone
   - **Bluetooth**: Send file to phone
   - **Email**: Email to yourself, download on phone

8. **Install on Phone**
   - Open `app-debug.apk` on your phone
   - Allow "Install from unknown sources" (Settings will pop up)
   - Click Install
   - Click Open
   - 🎉 **Done!**

---

## 🔧 **ALTERNATIVE - Use PowerShell Script**

If you prefer command line:

1. **Open PowerShell** (Right-click Windows Start → Windows PowerShell)

2. **Navigate to project**:
   ```powershell
   cd "C:\Users\Public\Desktop\Travel.ai"
   ```

3. **Run build script**:
   ```powershell
   .\BUILD_APK.ps1
   ```

4. **Wait 3-5 minutes** for build to complete

5. APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

---

## ⚠️ **If Your Laptop is Too Slow**

### Option A: Use a Friend's PC
1. Copy entire `Travel.ai` folder to USB drive
2. Build on friend's PC using Android Studio method above
3. Copy APK back to your phone

### Option B: Use Online IDE
1. Zip your project folder
2. Upload to: https://replit.com/ or similar
3. Build APK online
4. Download APK

---

## ✅ **What Works in the App (Without Backend)**

After installing APK on your phone, these features work:

- ✅ **Home Screen**: Live weather for your city
- ✅ **Map Screen**: Search places, get directions
- ✅ **Explore Screen**: Find nearby restaurants, hotels, ATMs, parks, etc.
- ✅ **Metro Tab**: View Delhi metro stations and routes (works offline!)
- ✅ **Budget Planner**: Calculate trip costs with real distances
- ✅ **Settings**: Change city, temperature unit (°C/°F), language

**Note**: Bus search needs backend server (not deployed yet), so it will show "No connection" error. Everything else works perfectly!

---

## 🆘 **Problems?**

### "Android Studio not installed"
- Download from: https://developer.android.com/studio
- Install and try again

### "Gradle build failed"
- In Android Studio: `File` → `Sync Project with Gradle Files`
- Wait and try building again

### "Phone won't install APK"
- Go to: `Settings` → `Security` → Enable `Unknown sources`
- Or: `Settings` → `Apps` → `Special access` → `Install unknown apps` → Enable for your file manager

### "Build is too slow"
- Close other programs
- Or use friend's faster PC
- APK only needs to be built ONCE!

---

## 📱 **Expected APK Size**: 15-25 MB

## ⏱️ **Build Time**: 
- First time: 3-5 minutes
- Later builds: 1-2 minutes

---

## 🎉 **Quick Summary**

```
1. Open Android Studio
2. Open Travel.ai project
3. Build → Build APK(s)
4. Wait 3 minutes
5. Copy app-debug.apk to phone
6. Install & enjoy!
```

That's it! 🚀
