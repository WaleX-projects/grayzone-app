package com.rork.grayzone

import android.app.ActivityManager
import android.content.Context
import android.util.Log

/**
 * Manages the UsageTrackingService lifecycle
 * - Ensures service starts on app launch
 * - Keeps service running even if app is closed
 * - Handles restarts and recovery
 */
object ServiceManager {
    private const val tag = "ServiceManager"

    /**
     * Start the monitoring service
     * Safe to call multiple times - only starts once
     */
    fun startMonitoringService(context: Context) {
        Log.d(tag, "Starting monitoring service")

        if (isServiceRunning(context, UsageTrackingService::class.java)) {
            Log.d(tag, "Service already running")
            return
        }

        UsageTrackingService.start(context)
        Log.d(tag, "Service start initiated")
    }

    /**
     * Check if the monitoring service is currently running
     */
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                Log.d(tag, "Service is running: ${service.service.className}")
                return true
            }
        }

        Log.d(tag, "Service not running: ${serviceClass.name}")
        return false
    }

    /**
     * Stop the monitoring service (not normally called)
     * Use only for testing or explicit user action
     */
    fun stopMonitoringService(context: Context) {
        Log.d(tag, "Stopping monitoring service")
        val intent = android.content.Intent(context, UsageTrackingService::class.java)
        context.stopService(intent)
    }
}
