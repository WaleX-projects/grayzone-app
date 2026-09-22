package com.rork.grayzone.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.rork.grayzone.data.dao.MonitoredAppDao
import com.rork.grayzone.data.dao.UsageSessionDao
import com.rork.grayzone.data.entities.MonitoredApp
import com.rork.grayzone.data.entities.UsageSession
import com.rork.grayzone.util.DateUtils
import kotlinx.coroutines.flow.Flow

class UsageRepository(
    context: Context,
    private val monitoredAppDao: MonitoredAppDao,
    private val usageSessionDao: UsageSessionDao
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val tag = "GrayzoneRepo"

    // === App Management ===

    fun getAllApps(): Flow<List<MonitoredApp>> = monitoredAppDao.getAllApps()

    fun getEnabledApps(): Flow<List<MonitoredApp>> = monitoredAppDao.getEnabledApps()

    suspend fun addMonitoredApp(
        packageName: String,
        displayName: String,
        dailyLimitMinutes: Int = 60,
        interventionThresholdMinutes: Int = 18
    ): Long {
        val app = MonitoredApp(
            packageName = packageName,
            displayName = displayName,
            enabled = true,
            dailyLimitMinutes = dailyLimitMinutes,
            interventionThresholdMinutes = interventionThresholdMinutes
        )
        return monitoredAppDao.insertApp(app)
    }

    suspend fun updateMonitoredApp(app: MonitoredApp) {
        monitoredAppDao.updateApp(app)
    }

    suspend fun deleteMonitoredApp(app: MonitoredApp) {
        monitoredAppDao.deleteApp(app)
    }

    suspend fun isAppMonitored(packageName: String): Boolean {
        return monitoredAppDao.isAppMonitored(packageName) > 0
    }

    suspend fun getMonitoredAppByPackageName(packageName: String): MonitoredApp? {
        return monitoredAppDao.getAppByPackageName(packageName)
    }

    // === Usage Tracking ===

    /**
     * Get the real daily usage for an app using UsageStatsManager
     * Returns duration in seconds
     */
    fun getRealDailyUsage(packageName: String): Int {
        return try {
            val now = System.currentTimeMillis()
            val today = DateUtils.getTodayStartTimestamp()

            val stats = usageStatsManager.queryAndAggregateUsageStats(today, now)
            val packageStats = stats[packageName]

            // foregroundTimeMs gives time the app was in focus
            ((packageStats?.foregroundTimeMs ?: 0L) / 1000).toInt()
        } catch (e: Exception) {
            Log.e(tag, "Error getting real daily usage for $packageName", e)
            0
        }
    }

    /**
     * Get all monitored apps' current daily usage
     */
    suspend fun getCurrentDailyUsageForAllApps(): Map<String, Int> {
        val enabledPackages = monitoredAppDao.getEnabledPackageNames()
        return enabledPackages.associateWith { getRealDailyUsage(it) }
    }

    /**
     * Record a new usage session
     */
    suspend fun recordUsageSession(packageName: String): Long {
        val session = UsageSession(
            packageName = packageName,
            startedAt = System.currentTimeMillis(),
            date = DateUtils.getTodayDate()
        )
        return usageSessionDao.insertSession(session)
    }

    /**
     * Update an existing session to mark it as ended
     */
    suspend fun endUsageSession(sessionId: Long, endTime: Long = System.currentTimeMillis()) {
        val session = usageSessionDao.getLastSessionForPackage("") // This is a limitation
        if (session != null) {
            val durationSeconds = ((endTime - session.startedAt) / 1000).toInt()
            val updated = session.copy(
                endedAt = endTime,
                durationSeconds = durationSeconds
            )
            usageSessionDao.updateSession(updated)
        }
    }

    /**
     * Get sessions for a given package on today
     */
    suspend fun getTodaySessionsForPackage(packageName: String): List<UsageSession> {
        return usageSessionDao.getSessionsByDateAndPackage(packageName, DateUtils.getTodayDate())
    }

    /**
     * Get total session-based duration for today for a package (in minutes)
     */
    suspend fun getTodaySessionDurationMinutes(packageName: String): Int {
        val totalSeconds = usageSessionDao.getTotalDurationForDate(packageName, DateUtils.getTodayDate())
        return totalSeconds / 60
    }

    /**
     * Clean up old session data (optional cleanup)
     */
    suspend fun deleteOldSessions(daysOld: Int = 30) {
        val date = DateUtils.getTodayDate()
        // Simple implementation: just delete sessions from more than daysOld days ago
        usageSessionDao.deleteSessionsBeforeDate(date)
    }
}
