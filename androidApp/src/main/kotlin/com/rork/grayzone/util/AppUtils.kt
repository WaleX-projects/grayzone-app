package com.rork.grayzone.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

data class InstalledApp(
    val packageName: String,
    val displayName: String,
    val isSystemApp: Boolean = false
)

object AppUtils {
    private const val tag = "AppUtils"

    /**
     * Get all installed apps (excluding system apps by default)
     */
    fun getInstalledApps(
        context: Context,
        includeSystemApps: Boolean = false
    ): List<InstalledApp> {
        return try {
            val packageManager = context.packageManager
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

            apps
                .filter { app ->
                    // Filter out system apps unless requested
                    if (!includeSystemApps) {
                        (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    } else {
                        true
                    }
                }
                .mapNotNull { app ->
                    try {
                        val displayName = packageManager.getApplicationLabel(app).toString()
                        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                        InstalledApp(
                            packageName = app.packageName,
                            displayName = displayName,
                            isSystemApp = isSystemApp
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .sortedBy { it.displayName }
        } catch (e: Exception) {
            Log.e(tag, "Error getting installed apps", e)
            emptyList()
        }
    }

    /**
     * Get commonly used social media apps (for testing/demo)
     */
    fun getPopularApps(): List<Pair<String, String>> {
        return listOf(
            "com.tiktok.android" to "TikTok",
            "com.instagram.android" to "Instagram",
            "com.twitter.android" to "X (Twitter)",
            "com.google.android.youtube" to "YouTube",
            "com.reddit.frontpage" to "Reddit",
            "com.facebook.katana" to "Facebook",
            "com.snapchat.android" to "Snapchat",
            "com.linkedin.android" to "LinkedIn",
            "com.discord" to "Discord",
            "com.whatsapp" to "WhatsApp"
        )
    }

    /**
     * Filter apps to show most relevant ones (social media, messaging, etc.)
     */
    fun filterRelevantApps(allApps: List<InstalledApp>): List<InstalledApp> {
        val keywords = listOf(
            "tiktok", "instagram", "twitter", "youtube", "reddit", "facebook",
            "snapchat", "linkedin", "discord", "whatsapp", "telegram", "viber",
            "messenger", "pinterest", "twitch", "bereal", "threads"
        )

        return allApps.filter { app ->
            keywords.any { keyword ->
                app.packageName.lowercase().contains(keyword) ||
                        app.displayName.lowercase().contains(keyword)
            }
        }
    }
}
