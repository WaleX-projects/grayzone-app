package com.rork.grayzone.ui.models

data class ProtectedApp(
    val id: String,
    val name: String,
    val isProtected: Boolean = true,
    val dailyLimitMin: Int = 60,
    val usedTodayMin: Int = 0
)

data class RefillSource(
    val id: String,
    val name: String,
    val baseMinutes: Int,
    val dailyCap: Int,
    val usedToday: Int = 0,
    val enabled: Boolean = true
) {
    /** Refills diminish with each use: 8, 6, 4... until the daily cap. */
    val minutesNow: Int
        get() = if (usedToday >= dailyCap) 0 else maxOf(0, baseMinutes - usedToday * 2)
}

enum class FrictionLevel { NORMAL, MILD, GRAYSCALE, INTERVENTION }

data class Thresholds(
    val mildPercent: Int = 50,
    val grayscalePercent: Int = 25,
    val interventionPercent: Int = 0
)

data class GrayzoneState(
    val onboarded: Boolean = false,
    val allowanceMin: Int = 60,
    val quotaLeftMin: Float = 60f,
    val apps: List<ProtectedApp> = emptyList(),
    val thresholds: Thresholds = Thresholds(),
    val refillSources: List<RefillSource> = emptyList(),
    val paused: Boolean = false,
    val pausedLabel: String? = null,
    val disabled: Boolean = false,
    val interruptedSessions: Int = 0,
    val stoppedAfterFriction: Int = 0,
    val refilledTodayMin: Int = 0
) {
    val totalUsedTodayMin: Int
        get() = apps.filter { it.isProtected }.sumOf { it.usedTodayMin }

    val quotaPercent: Float
        get() = if (allowanceMin <= 0) 0f else (quotaLeftMin / allowanceMin).coerceIn(0f, 1f)

    /** The four friction states are driven by remaining quota percentage. */
    val level: FrictionLevel
        get() {
            val pct = quotaPercent * 100f
            return when {
                pct >= thresholds.mildPercent -> FrictionLevel.NORMAL
                pct >= thresholds.grayscalePercent -> FrictionLevel.MILD
                pct > thresholds.interventionPercent -> FrictionLevel.GRAYSCALE
                else -> FrictionLevel.INTERVENTION
            }
        }
}

object KnownApps {
    val all = listOf(
        ProtectedApp("instagram", "Instagram"),
        ProtectedApp("tiktok", "TikTok"),
        ProtectedApp("x", "X"),
        ProtectedApp("youtube", "YouTube"),
        ProtectedApp("reddit", "Reddit"),
        ProtectedApp("facebook", "Facebook")
    )
}
