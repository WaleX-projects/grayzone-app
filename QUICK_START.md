# Grayzone Quick Start

## 30-Second Overview

Grayzone is now a **working Android app** that:
1. Discovers your installed apps
2. Lets you select apps to monitor
3. Tracks app usage via UsageStatsManager
4. Shows intervention UI when thresholds are reached
5. Stores everything locally in Room database

**Core concept**: Measure → Detect → Interrupt → User decides

---

## Build & Run (5 minutes)

```bash
# From workspace root
./gradlew :androidApp:assembleDebug

# Install on connected device/emulator
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Or install and run directly
./gradlew :androidApp:installDebug
```

---

## Enable Required Permission (Important!)

Before testing, grant the usage stats permission:

**On Android Device:**
1. Settings → Apps
2. Find "Special app access" or scroll down
3. Look for "Usage access" or "Usage and diagnostics"
4. Find "Grayzone" and enable it

**This is required for usage detection to work!**

---

## Test the Vertical Slice (3 minutes)

### Step 1: Launch App
Tap Grayzone icon

### Step 2: Add an App
- Tap **Apps** tab (bottom)
- Tap **Add monitored app**
- Tap **Browse installed apps**
- Select **TikTok** (or Instagram, YouTube, etc.)
- Set: Daily limit = 60 min, Intervention = **1 min**
- Tap **Add**

### Step 3: See It Work
- Tap **Home** tab
- Tap your monitored app in "Protected apps" list
- SessionScreen opens with simulated feed
- Wait ~10 seconds
- See friction UI: "Still want to keep going?"
- Tap buttons to interact

**That's it!** You've tested the vertical slice.

---

## What Each Screen Does

| Screen | Purpose |
|--------|---------|
| **Home** | Shows all monitored apps + daily usage breakdown |
| **Apps** | Add new apps, manage configuration, set thresholds |
| **Session** | Simulates app usage, shows intervention UI |
| **Insights** | (Placeholder) Future analytics |
| **Settings** | (Placeholder) Future user settings |

---

## Key Thresholds

For testing, use:
- **Daily limit**: 60 minutes (or any value)
- **Intervention threshold**: **1 minute** ← Quick testing
- In production: 18 minutes is typical

---

## What's Saved

Everything is stored in the Room database:
- ✅ Monitored apps list
- ✅ App configuration (daily limit, threshold)
- ✅ Usage sessions (when implemented)
- ✅ Survives app restart

---

## Troubleshooting

**Q: No apps in installed apps list**
- A: The app filters for social media apps. Make sure you have TikTok, Instagram, YouTube, etc. installed, or scroll to see all apps.

**Q: SessionScreen doesn't show friction**
- A: Wait 10+ seconds (grace period in code). Check if your threshold is set correctly.

**Q: Usage not calculating**
- A: Make sure you granted PACKAGE_USAGE_STATS permission in Settings.

**Q: App won't install**
- A: Clear previous: `adb uninstall com.rork.grayzone` then reinstall

---

## Next Development Steps

1. **Connect the background service** - UsageTrackingService runs monitoring continuously
2. **Real intervention triggers** - Based on actual app usage, not simulation
3. **Grayscale effect** - Darken the monitored app when approaching limit
4. **Throttling** - Slow down app responsiveness
5. **Emotion-based suggestions** - Help users understand their usage patterns

---

## Code Structure

```
androidApp/src/main/kotlin/com/rork/grayzone/

├── data/                    # Room database + DAOs
│   ├── entities/           # MonitoredApp, UsageSession
│   ├── dao/                # Database access objects
│   ├── GrayzoneDatabase.kt
│   └── UsageRepository.kt
│
├── monitoring/             # Monitoring logic
│   ├── MonitoringManager.kt
│   ├── InterventionDetector.kt
│   └── UsageTracker.kt
│
├── ui/
│   ├── screens/            # Compose UI screens
│   ├── components/         # Reusable Compose components
│   ├── models/             # UI state models
│   ├── theme/              # Design system
│   └── GrayzoneViewModel.kt
│
├── util/                   # Utilities
│   ├── AppUtils.kt        # App discovery
│   ├── PermissionHelper.kt
│   └── DateUtils.kt
│
└── UsageTrackingService.kt # Foreground service
```

---

## Key Classes to Know

- **GrayzoneViewModel** - Main app state, connected to database
- **UsageRepository** - Queries usage & manages app config
- **InterventionDetector** - Checks if thresholds reached
- **AppUtils** - Discovers installed apps
- **SessionScreen** - Shows intervention UI demo

---

## Permission Requirements

In `AndroidManifest.xml`:
- ✅ `PACKAGE_USAGE_STATS` - Read app usage (requires manual grant)
- ✅ `FOREGROUND_SERVICE` - Background monitoring
- ✅ `FOREGROUND_SERVICE_SPECIAL_USE` - Monitoring service type

---

## Database Schema

### MonitoredApp
```kotlin
id                              Long
packageName                     String
displayName                     String
enabled                         Boolean
dailyLimitMinutes              Int
interventionThresholdMinutes   Int
createdAt                       Long
```

### UsageSession
```kotlin
id                      Long
packageName             String
startedAt               Long
endedAt                 Long?
durationSeconds         Int
date                    String (YYYY-MM-DD)
```

---

## For More Details

- See **BUILD_SUMMARY.md** for what was built
- See **VERTICAL_SLICE_GUIDE.md** for detailed testing
- See **Architecture artifact** for system design

---

## Questions?

Each file is well-commented. Start with:
1. `GrayzoneViewModel.kt` - Entry point for app state
2. `AppsScreen.kt` - How to browse apps
3. `InterventionDetector.kt` - How intervention works
4. `UsageRepository.kt` - How usage is queried

Enjoy! 🎯
