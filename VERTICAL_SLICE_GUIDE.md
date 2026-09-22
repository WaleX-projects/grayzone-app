# Grayzone Vertical Slice - Complete Testing Guide

This guide walks through the complete vertical slice: monitoring an app and triggering an intervention.

## What This Vertical Slice Demonstrates

```
Install Grayzone
      ↓
Grant usage access permission
      ↓
Select TikTok (or any app)
      ↓
Set intervention threshold = 1 minute
      ↓
Open the app in SessionScreen
      ↓
Grayzone detects usage and shows intervention
      ↓
User chooses Continue or Take a Break
```

## Step-by-Step Setup

### 1. Build and Install

```bash
cd c:\Users\USER\Desktop\grayzone-app
./gradlew :androidApp:assembleDebug
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### 2. Grant Required Permissions

**Critical**: Grayzone needs `PACKAGE_USAGE_STATS` permission to read app usage.

On your Android device:
1. Open **Settings**
2. Navigate to **Apps** → **Special app access** (or **Advanced**)
3. Find **Usage access** or **Usage and diagnostics**
4. Enable it for **Grayzone**

Without this permission, the app cannot detect usage.

### 3. Launch Grayzone

Tap the Grayzone app icon to start.

### 4. Add a Monitored App

1. Tap the **Apps** tab at the bottom
2. Tap **Add monitored app**
3. Choose **Browse installed apps**
4. Select **TikTok** (or Instagram, YouTube, etc.)
5. Configure:
   - Daily limit: **60 minutes**
   - Intervention threshold: **1 minute** ← KEY: Use 1 minute for quick testing
6. Tap **Add**

The app now appears in your monitored apps list.

### 5. Test the Session Screen

1. Go to **Home** tab
2. In "Protected apps" section, tap on your monitored app
3. The **SessionScreen** opens showing a simulated feed

### 6. See the Intervention UI

In SessionScreen:
- The app shows a simulated social media feed
- After ~10 seconds, a friction card appears: "Still want to keep going?"
- This demonstrates the intervention UI
- Tap **Continue** or **I'm done** to interact

## Real Usage Detection (Advanced)

For real usage detection:

1. **Open your monitored app** (TikTok, Instagram, etc.) on your device
2. Use it for at least 1 minute
3. Return to Grayzone and open SessionScreen for that app
4. The real usage time will be calculated based on UsageStatsManager
5. If >= 1 minute threshold, intervention will trigger

**Note**: UsageStatsManager updates every ~15 seconds, so real detection has a slight delay.

## Key Components of the Vertical Slice

### Database Layer (Room)
- **MonitoredApp**: Stores app configuration (package name, thresholds)
- **UsageSession**: Stores usage session data

### Monitoring
- **ForegroundAppDetector**: Detects which app is currently active
- **InterventionDetector**: Checks if usage thresholds are reached
- **UsageRepository**: Queries real usage via UsageStatsManager

### UI
- **AppsScreen**: Browse and add monitored apps
- **SessionScreen**: Simulates app usage and shows intervention UI
- **HomeScreen**: Shows monitored apps and current usage

## Testing Checklist

- [ ] Permission granted for PACKAGE_USAGE_STATS
- [ ] Can add an app from installed apps list
- [ ] App appears in Apps list with correct configuration
- [ ] Can tap app to open SessionScreen
- [ ] SessionScreen shows simulated feed
- [ ] Friction UI appears after time passes
- [ ] Can interact with friction buttons

## Troubleshooting

**Issue**: "No apps are protected yet"
- Solution: Go to Apps tab and add an app

**Issue**: SessionScreen doesn't show friction
- Solution: Wait 10+ seconds or check if threshold is set correctly

**Issue**: Usage not detected
- Solution: Ensure PACKAGE_USAGE_STATS permission is granted in Settings

**Issue**: App doesn't appear in installed apps list
- Solution: Some pre-installed or system apps may be filtered. Try another app.

## What's Next

After the vertical slice works:
1. Connect the background monitoring service (UsageTrackingService)
2. Implement real-time intervention triggers
3. Add grayscale and throttling friction
4. Build the intervention modal with emotion-based suggestions
5. Implement earn-back mechanisms
