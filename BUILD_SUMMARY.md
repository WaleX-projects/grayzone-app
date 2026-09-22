# Grayzone MVP - Build Summary

## What Was Built

### 1. Database Layer (Room)
- `MonitoredApp` entity - stores app configuration
- `UsageSession` entity - stores usage history
- `MonitoredAppDao` - CRUD operations for monitored apps
- `UsageSessionDao` - query operations for usage data
- `GrayzoneDatabase` - database singleton

### 2. Data Access Layer
- `UsageRepository` - business logic for app management and usage queries
- Real usage calculation using `UsageStatsManager`
- Support for adding, updating, and querying monitored apps

### 3. Monitoring System
- `ForegroundAppDetector` - detects currently active app
- `MonitoringManager` - core monitoring loop (will run in service)
- `UsageTracker` - tracks sessions and detects intervention thresholds
- `InterventionDetector` - determines when to trigger interventions

### 4. User Interface
- **AppsScreen**: Browse installed apps, add to monitoring, configure thresholds
- **SessionScreen**: Simulated app session with friction UI
- **HomeScreen**: Shows monitored apps and current usage
- **OnboardingScreen**: Initial setup (skipped for testing)

### 5. Utility & Helper Classes
- `AppUtils` - discover and filter installed apps
- `PermissionHelper` - check/request PACKAGE_USAGE_STATS permission
- `DateUtils` - date/time utilities for usage tracking
- `UsageAccess` - wrapper for permission checks

### 6. Services & Background
- `UsageTrackingService` - foreground service for background monitoring
- Proper manifest configuration with permissions and service definition

### 7. ViewModel Integration
- `GrayzoneViewModel` now connected to Room database
- Loads monitored apps on initialization
- Supports adding apps to monitoring
- Reactive state management with StateFlow

## Key Features Implemented

✅ **App Discovery** - Browse all installed apps and select ones to monitor
✅ **Configurable Thresholds** - Set daily limit and intervention threshold per app
✅ **Real Usage Detection** - Query actual app usage via UsageStatsManager
✅ **Local Persistence** - All data saved in Room database
✅ **Permission Handling** - Check and guide users to grant PACKAGE_USAGE_STATS
✅ **Session Simulation** - SessionScreen demonstrates intervention UI
✅ **Responsive UI** - Material 3 design with Jetpack Compose

## Vertical Slice Working Flow

1. User adds an app (e.g., TikTok) from installed apps
2. User sets 1-minute intervention threshold
3. User opens SessionScreen to view simulated session
4. After ~10 seconds, friction card appears showing intervention
5. User can choose to continue or take a break

## How to Test

See `VERTICAL_SLICE_GUIDE.md` for complete testing instructions.

Quick start:
```bash
./gradlew :androidApp:assembleDebug
# Grant PACKAGE_USAGE_STATS permission in Settings
# Open app → Apps tab → Add monitored app → Select TikTok
# Set threshold to 1 minute
# Go to Home → Tap your app to see SessionScreen
```

## Next Steps (Not Implemented Yet)

- [ ] Connect UsageTrackingService to actually run in background
- [ ] Real-time intervention triggers based on actual usage
- [ ] Grayscale effect when approaching limits
- [ ] Throttling (slow down app responsiveness)
- [ ] Intervention modal with emotion-based suggestions
- [ ] Refill/earn-back mechanisms
- [ ] Settings screen for global configuration
- [ ] Insights/analytics view

## Files Created

### Core Data
- `data/entities/MonitoredApp.kt`
- `data/entities/UsageSession.kt`
- `data/dao/MonitoredAppDao.kt`
- `data/dao/UsageSessionDao.kt`
- `data/GrayzoneDatabase.kt`
- `data/UsageRepository.kt`

### Monitoring
- `monitoring/MonitoringManager.kt`
- `monitoring/UsageTracker.kt`
- `monitoring/InterventionDetector.kt`

### Services
- `UsageTrackingService.kt`
- `UsageAccess.kt`

### Utilities
- `util/AppUtils.kt`
- `util/PermissionHelper.kt`
- `util/DateUtils.kt`

### UI Updates
- Updated `ui/GrayzoneViewModel.kt`
- Updated `ui/screens/AppsScreen.kt`
- Updated `ui/navigations/AppNavigation.kt`

### Config
- Updated `AndroidManifest.xml`
- Updated `androidApp/build.gradle.kts`
- Updated `gradle/libs.versions.toml`

## Dependencies Added

- Room database: `androidx.room:room-runtime`
- Room KTX: `androidx.room:room-ktx`
- Room compiler: `androidx.room:room-compiler`
- KSP plugin for Room annotation processing

## Build & Run

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Install on device
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Or run directly
./gradlew :androidApp:installDebug
```
