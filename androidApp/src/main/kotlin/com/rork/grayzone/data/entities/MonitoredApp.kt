package com.rork.grayzone.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app that the user has selected for monitoring.
 *
 * @param id Unique identifier for this monitored app
 * @param packageName The Android package name (e.g., "com.tiktok.android")
 * @param displayName The user-friendly app name (e.g., "TikTok")
 * @param enabled Whether monitoring is currently enabled for this app
 * @param dailyLimitMinutes The user's daily limit in minutes (e.g., 60)
 * @param interventionThresholdMinutes When to trigger intervention (e.g., 18)
 * @param createdAt Timestamp when this app was added to monitoring
 */
@Entity(tableName = "monitored_apps")
data class MonitoredApp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val displayName: String,
    val enabled: Boolean = true,
    val dailyLimitMinutes: Int = 60,
    val interventionThresholdMinutes: Int = 18,
    val createdAt: Long = System.currentTimeMillis()
)
