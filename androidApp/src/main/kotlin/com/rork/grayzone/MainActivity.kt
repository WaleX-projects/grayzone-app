package com.rork.grayzone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rork.grayzone.ui.navigations.AppNavigation
import com.rork.grayzone.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "Activity created")
        enableEdgeToEdge()

        // Start the background monitoring service
        // This ensures monitoring continues even if the app is closed
        ServiceManager.startMonitoringService(this)

        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "Activity resumed")

        // Ensure service is still running when we return to the app
        if (!ServiceManager.isServiceRunning(this, UsageTrackingService::class.java)) {
            Log.d(tag, "Service not running, restarting")
            ServiceManager.startMonitoringService(this)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "Activity paused")
        // Service continues running in background
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "Activity destroyed")
        // Service continues running in background
    }
}


//./gradlew :androidApp:assembleDebug
