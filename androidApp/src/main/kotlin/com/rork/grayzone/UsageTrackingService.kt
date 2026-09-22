package com.rork.grayzone

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.rork.grayzone.data.GrayzoneDatabase
import com.rork.grayzone.data.UsageRepository
import com.rork.grayzone.monitoring.MonitoringManager
import com.rork.grayzone.notification.GrayzoneNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that continuously monitors app usage in the background.
 * This service runs even when the main UI is closed, ensuring usage tracking continues.
 *
 * Flow:
 * 1. Service starts when app launches
 * 2. Initializes database and monitoring system
 * 3. Runs monitoring loop in background
 * 4. Detects when monitored apps are used
 * 5. Triggers interventions when thresholds are reached
 * 6. Shows notifications to user
 */
class UsageTrackingService : Service() {
    private val tag = "GrayzoneService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var monitoringManager: MonitoringManager
    private lateinit var notificationManager: GrayzoneNotificationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "Service created")

        // Initialize notification manager
        notificationManager = GrayzoneNotificationManager(this)

        // Initialize database and repository
        val db = GrayzoneDatabase.getInstance(this)
        val repository = UsageRepository(
            context = this,
            monitoredAppDao = db.monitoredAppDao(),
            usageSessionDao = db.usageSessionDao()
        )

        // Initialize monitoring manager
        monitoringManager = MonitoringManager(
            context = this,
            repository = repository,
            notificationManager = notificationManager
        )

        // Start as foreground service with monitoring notification
        val notification = notificationManager.showMonitoringNotification()
        startForeground(GrayzoneNotificationManager.NOTIFICATION_ID_MONITORING, notification)

        Log.d(tag, "Service initialized and running in foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "Service started with command, flags=$flags")

        // Start the monitoring loop (safe to call multiple times)
        serviceScope.launch {
            try {
                Log.d(tag, "Starting monitoring loop")
                monitoringManager.startMonitoring()
            } catch (e: Exception) {
                Log.e(tag, "Error in monitoring loop", e)
            }
        }

        // Service should restart if killed by system
        // Flag value:
        // - START_STICKY: Service will restart when killed, but intent is null
        // - START_REDELIVER_INTENT: Service will restart with the original intent
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(tag, "Service destroyed")
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    companion object {
        fun start(context: android.content.Context) {
            val intent = Intent(context, UsageTrackingService::class.java)
            try {
                context.startService(intent)
                Log.d("GrayzoneService", "Service start requested")
            } catch (e: Exception) {
                Log.e("GrayzoneService", "Error starting service", e)
            }
        }
    }
}
