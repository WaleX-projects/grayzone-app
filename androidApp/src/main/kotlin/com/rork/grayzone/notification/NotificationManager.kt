package com.rork.grayzone.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rork.grayzone.InterventionActivity
import com.rork.grayzone.MainActivity
import com.rork.grayzone.R

/**
 * Manages all notifications for Grayzone
 */
class GrayzoneNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val tag = "GrayzoneNotifications"

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Monitoring channel - low priority, for foreground service
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING,
                "Grayzone Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Grayzone is monitoring your app usage"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(monitoringChannel)

            // Intervention channel - high priority, for alerts
            val interventionChannel = NotificationChannel(
                CHANNEL_INTERVENTION,
                "Usage Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "You've reached your usage limit"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(interventionChannel)
        }
    }

    /**
     * Show the monitoring notification for the foreground service
     */
    fun showMonitoringNotification(): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_MONITORING)
            .setContentTitle("Grayzone")
            .setContentText("Monitoring app usage")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Show intervention notification when threshold is reached
     */
    fun showInterventionNotification(appName: String, usedMinutes: Int, thresholdMinutes: Int) {
        Log.d(tag, "Showing intervention notification for $appName: ${usedMinutes}m/${thresholdMinutes}m")

        // Launch the full-screen intervention activity
        val intent = Intent(context, InterventionActivity::class.java).apply {
            putExtra("intervention_app", appName)
            putExtra("intervention_used", usedMinutes)
            putExtra("intervention_threshold", thresholdMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)

        // Also show a notification
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appName.hashCode(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_INTERVENTION)
            .setContentTitle("Usage Alert")
            .setContentText("You've used $appName for $usedMinutes minutes")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500))
            .build()

        notificationManager.notify(
            appName.hashCode(),
            notification
        )
    }

    companion object {
        const val CHANNEL_MONITORING = "grayzone_monitoring"
        const val CHANNEL_INTERVENTION = "grayzone_intervention"
        const val NOTIFICATION_ID_MONITORING = 1337
    }
}
