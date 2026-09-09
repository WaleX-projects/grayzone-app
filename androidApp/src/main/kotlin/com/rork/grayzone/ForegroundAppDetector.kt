package com.rork.grayzone

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class ForegroundAppDetector(
    context: Context
) {

    private val usageStatsManager =
        context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

    fun getForegroundApp(): String? {

        val endTime = System.currentTimeMillis()

        // Look at the last 2 seconds of usage events.
        val startTime = endTime - 2_000

        val events = usageStatsManager.queryEvents(
            startTime,
            endTime
        )

        val event = UsageEvents.Event()

        var foregroundPackage: String? = null
        var latestTimestamp = 0L

        while (events.hasNextEvent()) {

            events.getNextEvent(event)

            if (
                event.eventType ==
                UsageEvents.Event.ACTIVITY_RESUMED
            ) {

                if (event.timeStamp > latestTimestamp) {

                    latestTimestamp = event.timeStamp

                    foregroundPackage =
                        event.packageName
                }
            }
        }

        return foregroundPackage
    }
}