package com.rork.grayzone.monitoring

import android.util.Log
import com.rork.grayzone.data.UsageRepository
import com.rork.grayzone.data.entities.MonitoredApp
import kotlin.math.roundToInt

/**
 * Tracks app usage in real-time and detects when intervention thresholds are reached.
 * Maintains state about which apps have already triggered interventions to avoid spam.
 */
class UsageTracker(private val repository: UsageRepository) {
    private val tag = "UsageTracker"
    
    // Track which apps have already triggered interventions (to avoid repeat triggers)
    private val interventionTriggeredFor = mutableSetOf<String>()
    
    // Track currently active app sessions
    private val activeSessions = mutableMapOf<String, Long>() // packageName -> sessionStartTime

    /**
     * Start tracking usage for an app
     */
    suspend fun startTracking(packageName: String) {
        if (!activeSessions.containsKey(packageName)) {
            activeSessions[packageName] = System.currentTimeMillis()
            Log.d(tag, "Started tracking: $packageName")
        }
    }

    /**
     * Stop tracking usage for an app
     */
    suspend fun stopTracking(packageName: String) {
        activeSessions.remove(packageName)
        Log.d(tag, "Stopped tracking: $packageName")
    }

    /**
     * Check if an intervention should be triggered for the currently active app
     * Returns the package name if intervention should trigger, null otherwise
     */
    suspend fun checkForIntervention(packageName: String): String? {
        // Don't re-trigger if we already intervened today
        if (interventionTriggeredFor.contains(packageName)) {
            return null
        }

        // Get the monitored app config
        val monitoredApp = repository.getAllApps()
        // We need a way to get a single app - let's enhance this
        return null
    }

    /**
     * Check intervention threshold based on current real-world usage
     */
    suspend fun checkThresholdReached(
        packageName: String,
        monitoredApp: MonitoredApp
    ): Boolean {
        // Get real daily usage in seconds
        val dailyUsageSeconds = repository.getRealDailyUsage(packageName)
        val dailyUsageMinutes = dailyUsageSeconds / 60
        
        Log.d(
            tag,
            "App: $packageName | Usage: ${dailyUsageMinutes}m | Threshold: ${monitoredApp.interventionThresholdMinutes}m | Limit: ${monitoredApp.dailyLimitMinutes}m"
        )

        // Check if threshold is reached
        if (dailyUsageMinutes >= monitoredApp.interventionThresholdMinutes) {
            return true
        }

        return false
    }

    /**
     * Mark that we've triggered an intervention for this app
     */
    fun markInterventionTriggered(packageName: String) {
        interventionTriggeredFor.add(packageName)
        Log.d(tag, "Marked intervention triggered for: $packageName")
    }

    /**
     * Reset intervention state (typically at midnight or when user resets)
     */
    fun resetInterventionState() {
        interventionTriggeredFor.clear()
        Log.d(tag, "Reset intervention state")
    }

    /**
     * Check if daily limit has been reached
     */
    suspend fun isDailyLimitReached(
        packageName: String,
        monitoredApp: MonitoredApp
    ): Boolean {
        val dailyUsageSeconds = repository.getRealDailyUsage(packageName)
        val dailyUsageMinutes = dailyUsageSeconds / 60
        
        return dailyUsageMinutes >= monitoredApp.dailyLimitMinutes
    }

    /**
     * Get current usage for an app in minutes
     */
    suspend fun getCurrentUsageMinutes(packageName: String): Int {
        return repository.getRealDailyUsage(packageName) / 60
    }
}
