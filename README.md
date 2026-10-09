# Teleprompter Pro

A professional, 100% offline studio teleprompter app for Android built with Jetpack Compose (Material 3), Room SQLite, and CameraX.

## Key Features
- **Zero Cloud / 100% Private & Offline**: No API keys, no internet connection required, zero tracking.
- **Direct Gallery Video Recording**: 1080p camera recording saved instantly to your phone's local storage (`Movies/TeleprompterPro`) and accessible in your phone's Gallery app.
- **Physical Beam-Splitter Mirror Mode**: Hardware-accelerated horizontal flip for physical glass teleprompter rigs without inverting the camera background.
- **High-Performance Smooth Auto-Scroll**: Frame-synced 60fps/120fps scrolling with zero stutter and tactile touch scrubbing.
- **Eye-Level Contact Guide**: Cyan gradient marker keeping your gaze anchored near your front camera lens.
- **Calm, Modern UI with Tactile Animations**: OLED pure dark theme, subtle spring-bounce touch feedback, and breathing recording indicator.
- **Local Script Studio**: Word count, reading time calculator at calibrated WPM, search, duplicate, and persistent autosave.

---

## How to Build the APK & Install on Your Phone (Without Play Store)

You can build the app and install it directly to your mobile phone in minutes.

### Step 1: Build the APK
1. Open **[Android Studio](https://developer.android.com/studio)**.
2. Click **Open** and select this `teleprompter-pro` folder.
3. In the top menu, click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. When the build finishes (takes ~1 minute), click the **locate** link in the bottom-right pop-up.
5. You will see your installable file: **`app-debug.apk`** (located at `app/build/outputs/apk/debug/app-debug.apk`).

### Step 2: Transfer to Your Phone via WhatsApp Web
1. Open [web.whatsapp.com](https://web.whatsapp.com) on your computer browser.
2. Send `app-debug.apk` as a document to your own chat or a trusted group.
3. On your Android phone, open WhatsApp, tap `app-debug.apk`, and tap **Install**.
   *(If prompted, toggle "Allow from this source" in Settings).*

Your app is now installed and ready to use anytime on your phone!
