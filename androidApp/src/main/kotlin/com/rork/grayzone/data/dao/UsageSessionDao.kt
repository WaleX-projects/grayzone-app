package com.rork.grayzone.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rork.grayzone.data.entities.UsageSession
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSessionDao {
    /**
     * Insert a new usage session
     */
    @Insert
    suspend fun insertSession(session: UsageSession): Long

    /**
     * Update an existing session
     */
    @Update
    suspend fun updateSession(session: UsageSession)

    /**
     * Get all sessions for a specific app on a given date
     */
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName AND date = :date ORDER BY startedAt DESC")
    suspend fun getSessionsByDateAndPackage(packageName: String, date: String): List<UsageSession>

    /**
     * Get all sessions for a package on a given date as a Flow
     */
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName AND date = :date ORDER BY startedAt DESC")
    fun getSessionsByDateAndPackageFlow(packageName: String, date: String): Flow<List<UsageSession>>

    /**
     * Calculate total duration in seconds for a specific app on a given date
     */
    @Query("SELECT COALESCE(SUM(durationSeconds), 0) FROM usage_sessions WHERE packageName = :packageName AND date = :date")
    suspend fun getTotalDurationForDate(packageName: String, date: String): Int

    /**
     * Get the total duration for multiple apps on a given date
     */
    @Query("SELECT packageName, COALESCE(SUM(durationSeconds), 0) as total FROM usage_sessions WHERE date = :date GROUP BY packageName")
    suspend fun getTotalUsageByPackageForDate(date: String): List<UsageByPackage>

    /**
     * Delete old sessions (older than a certain date)
     */
    @Query("DELETE FROM usage_sessions WHERE date < :date")
    suspend fun deleteSessionsBeforeDate(date: String)

    /**
     * Get the most recent session for a package (ongoing or not)
     */
    @Query("SELECT * FROM usage_sessions WHERE packageName = :packageName ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLastSessionForPackage(packageName: String): UsageSession?
}

/**
 * Helper data class for aggregating usage by package
 */
data class UsageByPackage(
    val packageName: String,
    val total: Int  // total seconds
)
