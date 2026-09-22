package com.rork.grayzone.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rork.grayzone.data.entities.MonitoredApp
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredAppDao {
    /**
     * Get all monitored apps as a Flow for reactive updates
     */
    @Query("SELECT * FROM monitored_apps ORDER BY createdAt DESC")
    fun getAllApps(): Flow<List<MonitoredApp>>

    /**
     * Get all enabled monitored apps
     */
    @Query("SELECT * FROM monitored_apps WHERE enabled = 1 ORDER BY createdAt DESC")
    fun getEnabledApps(): Flow<List<MonitoredApp>>

    /**
     * Get a specific app by package name
     */
    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): MonitoredApp?

    /**
     * Get a specific app by ID
     */
    @Query("SELECT * FROM monitored_apps WHERE id = :id")
    suspend fun getAppById(id: Long): MonitoredApp?

    /**
     * Insert a new monitored app
     */
    @Insert
    suspend fun insertApp(app: MonitoredApp): Long

    /**
     * Update an existing monitored app
     */
    @Update
    suspend fun updateApp(app: MonitoredApp)

    /**
     * Delete a monitored app
     */
    @Delete
    suspend fun deleteApp(app: MonitoredApp)

    /**
     * Check if an app is already being monitored
     */
    @Query("SELECT COUNT(*) FROM monitored_apps WHERE packageName = :packageName")
    suspend fun isAppMonitored(packageName: String): Int

    /**
     * Get all package names of monitored apps
     */
    @Query("SELECT packageName FROM monitored_apps WHERE enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>
}
