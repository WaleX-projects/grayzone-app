package com.rork.grayzone.monitoring

import android.content.Context
import android.util.Log
import com.rork.grayzone.ForegroundAppDetector
import com.rork.grayzone.data.UsageRepository
import com.rork.grayzone.notification.GrayzoneNotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Manages the core monitoring loop:
 * - Detects which app is in the foreground
 * - Calculates usage duration for today
 * - Triggers interventions when thresholds are reached
 * - Shows notifications
 *
 * This is the heart of Grayzone's real-time monitoring.
 */
class MonitoringManager(
    private val context: Context,
    private val repository: UsageRepository,
    private val notificationManager: GrayzoneNotificationManager
) {
    private val tag = "MonitoringManager"
    private val appDetector = ForegroundAppDetector(context)
    private var previousForegroundApp: String? = null
    private val interventionTracker = mutableMapOf<String, Long>() // app -> last intervention time
    private val interventionDetector = InterventionDetector(context, repository)

    /**
     * Start the monitoring loop - runs continuously in background
     */
    suspend fun startMonitoring() {
        Log.d(tag, "Starting real monitoring loop")

        while (isActive) {
            try {
                val currentApp = appDetector.getForegroundApp()

                if (currentApp != null && currentApp != "com.android.systemui") {
                    // Check if this app is monitored
                    val isMonitored = repository.isAppMonitored(currentApp)
                    if (isMonitored) {
                        handleMonitoredApp(currentApp)
                    } else if (currentApp != previousForegroundApp) {
                        Log.d(tag, "Non-monitored app: $currentApp")
                        previousForegroundApp = currentApp
                    }
                } else if (currentApp != previousForegroundApp) {
                    Log.d(tag, "System UI or null app")
                    previousForegroundApp = currentApp
                }

                // Check every few seconds (balance battery vs responsiveness)
                delay(MONITOR_CHECK_INTERVAL_MS)

            } catch (e: Exception) {
                Log.e(tag, "Error in monitoring loop", e)
                delay(MONITOR_CHECK_INTERVAL_MS)
            }
        }

        Log.d(tag, "Monitoring loop ended")
    }

    /**
     * Handle a monitored app that's in the foreground
     */
    private suspend fun handleMonitoredApp(packageName: String) {
        // Log app change
        if (packageName != previousForegroundApp) {
            Log.d(tag, "Monitored app in foreground: $packageName")
            previousForegroundApp = packageName
        }

        // Check if intervention should trigger
        val monitoredApp = repository.getMonitoredAppByPackageName(packageName)
        if (monitoredApp != null && monitoredApp.enabled) {
            val dailyUsageSeconds = repository.getRealDailyUsage(packageName)
            val dailyUsageMinutes = dailyUsageSeconds / 60

            Log.d(
                tag,
                "$packageName: ${dailyUsageMinutes}m used, ${monitoredApp.interventionThresholdMinutes}m threshold"
            )

            // Check if threshold reached and we haven't intervened recently
            if (dailyUsageMinutes >= monitoredApp.interventionThresholdMinutes) {
                val lastIntervention = interventionTracker[packageName] ?: 0L
                val timeSinceLastIntervention = System.currentTimeMillis() - lastIntervention

                // Only intervene once per 5 minutes to avoid spam
                if (timeSinceLastIntervention > 5 * 60 * 1000) {
                    Log.d(tag, "🚨 INTERVENTION TRIGGERED for $packageName")
                    triggerIntervention(packageName, monitoredApp.displayName, dailyUsageMinutes, monitoredApp.interventionThresholdMinutes)
                    interventionTracker[packageName] = System.currentTimeMillis()
                }
            }
        }
    }

    /**
     * Trigger an intervention - show notification to user
     */
    private fun triggerIntervention(
        packageName: String,
        appName: String,
        usedMinutes: Int,
        thresholdMinutes: Int
    ) {
        Log.d(tag, "Triggering intervention for $appName")

        // Show notification to user
        notificationManager.showInterventionNotification(
            appName = appName,
            usedMinutes = usedMinutes,
            thresholdMinutes = thresholdMinutes
        )
    }

    companion object {
        // Check every 5 seconds for balance between responsiveness and battery
        // (UsageStatsManager has ~15 second latency anyway)
        const val MONITOR_CHECK_INTERVAL_MS = 5000L
    }
}
