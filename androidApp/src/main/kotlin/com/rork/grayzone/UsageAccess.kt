package com.rork.grayzone

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.rork.grayzone.util.PermissionHelper

/**
 * Convenience wrapper for checking usage stats permission
 */
object UsageAccess {
    fun hasPermission(context: Context): Boolean {
        return PermissionHelper.hasUsageStatsPermission(context)
    }
}
