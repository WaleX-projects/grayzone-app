package com.rork.grayzone.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log

object PermissionHelper {
    private const val tag = "PermissionHelper"

    /**
     * Check if the app has PACKAGE_USAGE_STATS permission
     * This permission requires user to grant it in system settings
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } else {
            true // Assume allowed on older versions
        }
    }

    /**
     * Open the system settings page where users can grant PACKAGE_USAGE_STATS permission
     */
    fun openUsageStatsSettings(context: Context) {
        Log.d(tag, "Opening usage stats settings")
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(tag, "Error opening usage stats settings", e)
        }
    }

    /**
     * Check if foreground service permissions are granted (Android 12+)
     */
    fun hasForegroundServicePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
