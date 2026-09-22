package com.rork.grayzone.monitoring

import android.content.Context
import android.util.Log
import com.rork.grayzone.data.UsageRepository
import com.rork.grayzone.data.entities.MonitoredApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Detects when an intervention should be triggered based on real app usage.
 * This is the core system for the vertical slice.
 */
class InterventionDetector(
    private val context: Context,
    private val repository: UsageRepository
) {
    private val tag = "InterventionDetector"

    private val _interventionTriggered = MutableStateFlow<InterventionEvent?>(null)
    val interventionTriggered: StateFlow<InterventionEvent?> = _interventionTriggered.asStateFlow()

    // Track which apps have already triggered interventions today
    private val triggeredAppsToday = mutableSetOf<String>()

    /**
     * Check if intervention should trigger for a monitored app
     * Returns true if intervention was triggered
     */
    suspend fun checkAndTriggerIntervention(packageName: String): Boolean {
        // Don't re-trigger same app multiple times
        if (triggeredAppsToday.contains(packageName)) {
            return false
        }

        val monitoredApp = repository.getMonitoredAppByPackageName(packageName) ?: return false
        if (!monitoredApp.enabled) return false

        // Get real daily usage in seconds
        val dailyUsageSeconds = repository.getRealDailyUsage(packageName)
        val dailyUsageMinutes = dailyUsageSeconds / 60

        Log.d(
            tag,
            "Checking intervention for $packageName: ${dailyUsageMinutes}m used, ${monitoredApp.interventionThresholdMinutes}m threshold"
        )

        // Check if threshold reached
        if (dailyUsageMinutes >= monitoredApp.interventionThresholdMinutes) {
            Log.d(tag, "Intervention triggered for $packageName at $dailyUsageMinutes minutes")
            triggeredAppsToday.add(packageName)

            val event = InterventionEvent(
                packageName = packageName,
                appName = monitoredApp.displayName,
                usedMinutes = dailyUsageMinutes,
                thresholdMinutes = monitoredApp.interventionThresholdMinutes,
                dailyLimitMinutes = monitoredApp.dailyLimitMinutes,
                isDailyLimitReached = dailyUsageMinutes >= monitoredApp.dailyLimitMinutes
            )

            _interventionTriggered.emit(event)
            return true
        }

        return false
    }

    /**
     * Clear the intervention event after user responds
     */
    fun clearIntervention() {
        _interventionTriggered.tryEmit(null)
    }

    /**
     * Reset intervention state (typically at midnight)
     */
    fun resetDailyState() {
        triggeredAppsToday.clear()
        _interventionTriggered.tryEmit(null)
    }
}

/**
 * Data class representing an intervention event
 */
data class InterventionEvent(
    val packageName: String,
    val appName: String,
    val usedMinutes: Int,
    val thresholdMinutes: Int,
    val dailyLimitMinutes: Int,
    val isDailyLimitReached: Boolean
)
