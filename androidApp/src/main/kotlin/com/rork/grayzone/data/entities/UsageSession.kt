package com.rork.grayzone.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a usage session for a monitored app.
 * Tracks when the app was used on a given day.
 *
 * @param id Unique identifier for this session
 * @param packageName The package name of the app that was used
 * @param startedAt Timestamp when the session began
 * @param endedAt Timestamp when the session ended (null if ongoing)
 * @param durationSeconds Total duration of the session in seconds
 * @param date The calendar date (YYYY-MM-DD) for aggregating daily usage
 */
@Entity(
    tableName = "usage_sessions",
    indices = [
        Index("packageName"),
        Index("date"),
        Index("packageName", "date")
    ]
)
data class UsageSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val durationSeconds: Int = 0,
    val date: String = "" // YYYY-MM-DD format
)
