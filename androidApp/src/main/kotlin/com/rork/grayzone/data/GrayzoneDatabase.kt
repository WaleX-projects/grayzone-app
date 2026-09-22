package com.rork.grayzone.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rork.grayzone.data.dao.MonitoredAppDao
import com.rork.grayzone.data.dao.UsageSessionDao
import com.rork.grayzone.data.entities.MonitoredApp
import com.rork.grayzone.data.entities.UsageSession

@Database(
    entities = [MonitoredApp::class, UsageSession::class],
    version = 1,
    exportSchema = true
)
abstract class GrayzoneDatabase : RoomDatabase() {
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun usageSessionDao(): UsageSessionDao

    companion object {
        @Volatile
        private var INSTANCE: GrayzoneDatabase? = null

        fun getInstance(context: Context): GrayzoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrayzoneDatabase::class.java,
                    "grayzone.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
