package com.rork.grayzone

import android.util.Log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class UsageMonitorTest(
    private val detector: ForegroundAppDetector
) {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    fun start() {

        scope.launch {

            while (isActive) {

                val packageName =
                    detector.getForegroundApp()

                Log.d(
                    "GRAYZONE",
                    "Foreground app: $packageName"
                )

                delay(1000)
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}