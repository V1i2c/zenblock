# ZenFocus (`com.zenfocus.app`)

A modern, 100% offline-first, privacy-respecting Android application blocker and digital well-being companion targeting Android 12+ (`minSdk = 28`, `targetSdk = 35`, `compileSdk = 35`).

---

## 🌟 Key Architectural Pillars

1. **100% Offline-First (Zero Network Tracking):**
   - Completely omits `<uses-permission android:name="android.permission.INTERNET" />`.
   - Guaranteed zero telemetry, zero analytics, zero external network connections, zero login requirements.
   - All profile data, schedules, adult domain blacklists, and statistics are stored locally in Room SQLite database (`zenfocus.db`) in WAL mode.

2. **All 37 Features Permanently Unlocked (Zero Paywalls):**
   - Unlimited profiles, unlimited apps, unlimited websites, unlimited intervals, sub-apps, and full strict mode.
   - Entitlements and quotas configured to `Int.MAX_VALUE`.

3. **Multi-Process Architecture:**
   - **Main UI Process (`com.zenfocus.app`):** Compose Material 3 UI, ViewModels, and local Room master instance.
   - **Lock Service Process (`:LockService`):** High-reliability `specialUse` Foreground Service polling `UsageStatsManager` and coordinating `OverlayService`.
   - **Accessibility Process (`:AccessibilityService`):** Isolated thread processing real-time node events with a decoupled `LinkedBlockingQueue` worker.

4. **Multi-Browser Address Bar Interception:**
   - Real-time address bar view inspection across 25+ major Android web browsers (Chrome, Edge, Samsung Internet, Brave, Opera, Firefox Compose test tag `ADDRESSBAR_URL_BOX`).
   - Automated redirection to `about:blank` on matching blocked domains or adult content.

5. **Granular Sub-App Interception:**
   - YouTube Shorts: Intercepts `:id/reel_player_page_container`
   - Instagram Reels: Intercepts `:id/comment_count`
   - Instagram Stories: Intercepts `:id/reel_viewer_timestamp`
   - Snapchat Spotlight & Stories
   - WhatsApp Status & Channels
   - Injects `GLOBAL_ACTION_BACK` immediately to kick user out of addictive algorithmic feeds.

6. **Anti-Tampering Strict Mode:**
   - **Block Android Settings:** Prevents force stopping, clearing data, or revoking permissions.
   - **Block Uninstallation:** Android `DevicePolicyManager` (Device Administrator API).
   - **Android 13+ Task Manager Guard:** Monitors `com.android.systemui` to prevent terminating foreground services.
   - **Physical Charger Unlock:** Listens for `ACTION_POWER_CONNECTED` to unlock only when plugged into a power source.
   - **Emergency Reflection Gate:** 10-step psychological questionnaire with `NoOpTextToolbar` disabling copy/paste, forcing manual typing.

7. **Dynamic Launcher Icon Disguise:**
   - 19 declared `<activity-alias>` elements enabling dynamic runtime disguise (Calculator, Clock, Notes, Weather, System Tools) without killing the app.

---

## 🛠️ Automated CI/CD & Build Instructions

This repository is ready to compile directly into a signed, installable `.apk` on GitHub.

### Automated GitHub Actions Build:
1. Push this repository to GitHub (`main` or `master` branch).
2. The GitHub Actions workflow (`.github/workflows/build-apk.yml`) automatically triggers:
   - Sets up Temurin JDK 17.
   - Executes `./gradlew assembleRelease --stacktrace`.
   - Signs the APK using the embedded `app/release.keystore`.
   - Uploads the signed APK artifact `ZenFocus-Release-APK`.
3. Download the APK artifact and install directly on any Android 12+ device!

### Local Compilation (Optional):
```powershell
.\gradlew.bat assembleRelease
```
The compiled signed release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`
