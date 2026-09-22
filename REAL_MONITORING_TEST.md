# Real Monitoring - Complete Testing Guide

## What's Different Now (vs. Vertical Slice)

**Before**: SessionScreen simulated intervention after 10 seconds  
**Now**: Real monitoring in background service that triggers when you actually use an app

---

## Prerequisites

1. **Android Device or Emulator** (API 24+)
2. **Grant PACKAGE_USAGE_STATS Permission** - CRITICAL
3. **TikTok, Instagram, or other social media app installed**

---

## Step-by-Step Testing

### Step 1: Build & Install (2 minutes)

```bash
cd c:\Users\USER\Desktop\grayzone-app
./gradlew :androidApp:assembleDebug
adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### Step 2: Grant Permissions (1 minute)

**CRITICAL**: Without this, nothing will work!

1. Go to **Settings** on your device
2. Navigate to **Apps**
3. Find **Special app access** (or scroll down in Apps)
4. Look for **Usage access** or **Usage and diagnostics**
5. Find **Grayzone** and **Enable it**

### Step 3: Add TikTok to Monitoring (1 minute)

1. Open Grayzone app
2. Tap **Apps** tab
3. Tap **Add monitored app**
4. Tap **Browse installed apps**
5. Select **TikTok** (or Instagram, YouTube, etc.)
6. Set:
   - **Daily limit**: 60 minutes
   - **Intervention threshold**: **1 minute** ← Use 1 for quick testing
7. Tap **Add**

App is now in your monitored apps list.

### Step 4: The Real Test (3+ minutes)

**Important**: The monitoring service is now running in the background.

#### Option A: Quick Test with Simulation
1. Go to Home tab
2. Tap your monitored app (TikTok) in the "Protected apps" section
3. SessionScreen opens with simulated feed
4. After ~10 seconds, friction card appears
5. This confirms the intervention UI works

#### Option B: Real Usage Test (Recommended)
1. **Keep Grayzone in the background** (don't close it)
2. Open the actual **TikTok app** on your device
3. **Use it for at least 1 minute** (scroll, watch videos, etc.)
4. While using TikTok, one of these happens:
   - **Full-screen intervention appears** (InterventionActivity shows)
   - **OR notification appears** (depending on what triggers first)
5. You see: "Hold on. You've been using TikTok for X minutes"
6. Choose: **Continue using TikTok** or **Take a break**

---

## What You Should See

### When Everything Works:

**Timeline:**
```
0:00 - Open Grayzone
0:30 - Add TikTok, set threshold to 1 minute
1:30 - Open real TikTok app
2:30 - **INTERVENTION APPEARS** (full screen or notification)
       "Hold on. You've been using TikTok for 1 minute."
2:35 - You tap "Continue" or "Take a break"
```

### Expected Notifications:

1. **Monitoring Notification** (always visible in status bar)
   - Title: "Grayzone"
   - Text: "Monitoring app usage"
   - Low priority, doesn't interrupt

2. **Intervention Notification** (when threshold reached)
   - Title: "Usage Alert"
   - Text: "You've used TikTok for 1 minute"
   - High priority, vibrates

3. **Intervention Activity** (full screen)
   - Appears on top of the app
   - Non-dismissible (must choose Continue or Break)

---

## Detailed Flow Explanation

### How the Real Monitoring Works

```
Grayzone Starts
    ↓
MainActivity launches
    ↓
UsageTrackingService.start() called
    ↓
Service starts in foreground with monitoring notification
    ↓
MonitoringManager.startMonitoring() loop begins
    ↓
Every 5 seconds:
    - ForegroundAppDetector checks: which app is active?
    - If TikTok is active:
        └─ Get real usage from UsageStatsManager
        └─ Compare: actual usage >= your 1-minute threshold?
        └─ If YES: Trigger intervention
            ├─ Show InterventionActivity (full screen)
            └─ Show notification
    - If not TikTok: Continue checking
    ↓
User sees intervention and makes a choice
    ↓
Service continues monitoring in background
```

### Why the Delay?

- **UsageStatsManager has ~15 second latency** - it takes time to update
- **MonitoringManager checks every 5 seconds** - so detection happens quickly after latency
- **Typical delay: 15-25 seconds after your 1-minute threshold**

Example:
- You use TikTok for exactly 1 minute
- UsageStatsManager doesn't show the update immediately
- After ~15 more seconds, MonitoringManager detects it
- Intervention appears at ~1:15 of usage

This is normal and expected.

---

## Testing Checklist

Use this to verify real monitoring works:

### Permissions & Setup
- [ ] PACKAGE_USAGE_STATS permission is granted
- [ ] Grayzone launches without crashing
- [ ] Service starts (check logcat for "Service created" log)
- [ ] Monitoring notification appears in status bar

### Adding App
- [ ] Can add TikTok from installed apps
- [ ] Threshold is set to 1 minute
- [ ] App appears in Home screen "Protected apps" list

### Real Monitoring
- [ ] Open real TikTok app
- [ ] Use it for ~1-2 minutes
- [ ] Intervention appears (full screen or notification)
- [ ] Intervention shows correct app name
- [ ] Intervention shows usage time
- [ ] Can tap "Continue" or "Take a break"

### Service Continuity
- [ ] Close Grayzone app (but don't kill it)
- [ ] Service keeps running (notification stays visible)
- [ ] Open TikTok again
- [ ] Intervention still triggers

### Data Persistence
- [ ] Close Grayzone completely
- [ ] Reopen Grayzone
- [ ] TikTok is still in monitored apps
- [ ] Configuration is remembered

✅ **All checked?** Real monitoring is working!

---

## Troubleshooting

### "No intervention appears even after 2+ minutes"

**Problem 1**: Permission not granted
- Fix: Check Settings → Apps → Usage access → Grayzone is enabled

**Problem 2**: Threshold set too high
- Fix: Go to Apps tab, verify threshold is 1 minute (not 60)

**Problem 3**: Monitoring notification doesn't appear
- Fix: Check logcat for errors, restart app

**Problem 4**: Service not running
- Fix: Check logcat for "Service created", restart app

### "Intervention appears but shows wrong app"

- This shouldn't happen, but if it does:
- Check that you added the correct app (e.g., TikTok, not Instagram)
- Restart the app and try again

### "Intervention keeps appearing every 5 seconds"

- Throttling might be off
- Fix: Check MonitoringManager line that says "Only intervene once per 5 minutes"
- This is deliberate to avoid spam

### "Grayzone crashes when I open an app"

- Could be a permission issue or database issue
- Check logcat for error messages
- Try: Clear app data and reinstall

---

## Advanced Testing

### Test with Different Thresholds

Try testing with:
- 1 minute threshold (quick test)
- 5 minute threshold (medium test)
- 15 minute threshold (realistic test)

### Test with Multiple Apps

1. Add TikTok (1 min threshold)
2. Add Instagram (1 min threshold)
3. Use TikTok → intervention
4. Use Instagram → different intervention
5. Verify each has its own threshold

### Test Intervention Throttling

1. Use TikTok for 1 minute → intervention appears
2. Tap "Continue using TikTok"
3. Close intervention activity
4. Immediately open TikTok again
5. Intervention should NOT appear again for 5 minutes
6. After 5 minutes, you can trigger it again

---

## How to Check Logs

If something isn't working, check the logs:

```bash
# Real-time logs
adb logcat | grep -i grayzone

# All logs with timestamps
adb logcat -v threadtime | grep -i grayzone

# Just errors
adb logcat | grep -i "error\|exception" | grep grayzone
```

Look for:
- "Service created" - Service started
- "Starting real monitoring loop" - Monitoring began
- "Monitored app in foreground" - App detected
- "INTERVENTION TRIGGERED" - Threshold reached

---

## What Each Message Means

| Log Message | Meaning |
|-------------|---------|
| `Service created` | Service initialized |
| `Starting real monitoring loop` | Monitoring started |
| `Monitored app in foreground: com.tiktok.android` | TikTok detected |
| `TikTok: 1m used, 1m threshold` | Usage matches threshold |
| `🚨 INTERVENTION TRIGGERED` | Intervention showing now |
| `Showing intervention notification` | Notification sent |

---

## Performance & Battery

The real monitoring is designed to be efficient:

- **Checks every 5 seconds** (not every second)
- **Only queries UsageStatsManager** (lightweight system API)
- **Throttles interventions** (prevents spam)
- **Low-priority notification** (doesn't drain battery)

Expected battery impact: Minimal (< 1% per hour)

---

## Next Steps After Testing

If real monitoring works:
1. ✅ Concept is proven
2. ✅ Architecture is sound
3. ✅ Now add friction mechanisms:
   - Grayscale effect
   - Throttling / app slowdown
   - Audio muting
   - Custom intervention messages

---

## Still Have Questions?

Check:
- Logcat for error messages
- Permissions are granted
- Threshold is set correctly
- Service is running (notification visible)
- PACKAGE_USAGE_STATS permission in particular

If stuck, rebuild and reinstall the app completely.

---

## Success Indicators

You know it's working when:

✅ Monitoring notification appears immediately when app starts  
✅ Real TikTok usage triggers intervention within 15-25 seconds  
✅ Intervention activity appears and can't be dismissed with back button  
✅ Service continues running even after closing Grayzone  
✅ Configuration persists across app restarts  

**That's real monitoring working!** 🎉
