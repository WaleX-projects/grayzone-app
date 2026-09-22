package com.rork.grayzone.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Get today's date in YYYY-MM-DD format
     */
    fun getTodayDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Convert timestamp to YYYY-MM-DD format
     */
    fun timestampToDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Check if two timestamps are on the same calendar date
     */
    fun areSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        return timestampToDate(timestamp1) == timestampToDate(timestamp2)
    }

    /**
     * Get the start of today (midnight) as a timestamp
     */
    fun getTodayStartTimestamp(): Long {
        val today = getTodayDate()
        return dateFormat.parse(today)?.time ?: System.currentTimeMillis()
    }

    /**
     * Get the end of today (11:59:59 PM) as a timestamp
     */
    fun getTodayEndTimestamp(): Long {
        return getTodayStartTimestamp() + (24 * 60 * 60 * 1000) - 1
    }
}
