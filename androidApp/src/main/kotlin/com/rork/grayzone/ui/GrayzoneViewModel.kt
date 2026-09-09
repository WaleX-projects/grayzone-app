package com.rork.grayzone.ui

import androidx.lifecycle.ViewModel
import com.rork.grayzone.ui.models.FrictionLevel
import com.rork.grayzone.ui.models.GrayzoneState
import com.rork.grayzone.ui.models.KnownApps
import com.rork.grayzone.ui.models.ProtectedApp
import com.rork.grayzone.ui.models.RefillSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

/**
 * Single source of truth for the prototype. All screens share one instance
 * scoped to the Activity so quota, apps and settings stay consistent.
 */
class GrayzoneViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        GrayzoneState(
            apps = KnownApps.all.map {
                when (it.id) {
                    "instagram" -> it.copy(usedTodayMin = 24)
                    "tiktok" -> it.copy(usedTodayMin = 12)
                    "x" -> it.copy(usedTodayMin = 6)
                    else -> it
                }
            },
            quotaLeftMin = 18f,
            refillSources = listOf(
                RefillSource("walk", "Short walk (10 min)", baseMinutes = 8, dailyCap = 3),
                RefillSource("todo", "Mark a to-do done", baseMinutes = 6, dailyCap = 3),
                RefillSource("reading", "Reading in a calm app (5 min)", baseMinutes = 6, dailyCap = 3)
            )
        )
    )
    val state: StateFlow<GrayzoneState> = _state.asStateFlow()

    private var activeSessionAppId: String? = null
    private var sessionConsumedMin: Float = 0f

    fun completeOnboarding(selectedIds: List<String>, allowanceMin: Int) {
        _state.update { s ->
            s.copy(
                onboarded = true,
                allowanceMin = allowanceMin,
                quotaLeftMin = (allowanceMin - s.totalUsedTodayMin).coerceAtLeast(0).toFloat(),
                apps = s.apps.map { it.copy(isProtected = it.id in selectedIds) }
            )
        }
    }

    fun setAllowance(min: Int) {
        _state.update { s ->
            val delta = (min - s.allowanceMin).toFloat()
            s.copy(allowanceMin = min, quotaLeftMin = (s.quotaLeftMin + delta).coerceAtLeast(0f))
        }
    }

    fun toggleProtected(id: String) = _state.update { s ->
        s.copy(apps = s.apps.map { if (it.id == id) it.copy(isProtected = !it.isProtected) else it })
    }

    fun setAppLimit(id: String, limit: Int) = _state.update { s ->
        s.copy(apps = s.apps.map { if (it.id == id) it.copy(dailyLimitMin = limit) else it })
    }

    fun addApp(name: String) = _state.update { s ->
        s.copy(apps = s.apps + ProtectedApp(id = "custom_${System.nanoTime()}", name = name))
    }

    fun setThreshold(which: String, percent: Int) = _state.update { s ->
        val t = s.thresholds
        val next = when (which) {
            "mild" -> t.copy(mildPercent = percent)
            "grayscale" -> t.copy(grayscalePercent = percent)
            else -> t.copy(interventionPercent = percent)
        }
        s.copy(thresholds = next)
    }

    fun toggleRefillSource(id: String) = _state.update { s ->
        s.copy(refillSources = s.refillSources.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        })
    }

    fun useRefill(id: String) {
        _state.update { s ->
            val src = s.refillSources.firstOrNull { it.id == id } ?: return@update s
            val granted = src.minutesNow
            if (granted <= 0 || !src.enabled) return@update s
            s.copy(
                quotaLeftMin = (s.quotaLeftMin + granted).coerceAtMost(s.allowanceMin.toFloat()),
                refilledTodayMin = s.refilledTodayMin + granted,
                refillSources = s.refillSources.map {
                    if (it.id == id) it.copy(usedToday = it.usedToday + 1) else it
                }
            )
        }
    }

    fun pauseProtection(label: String) = _state.update {
        it.copy(paused = true, pausedLabel = label)
    }

    fun disableProtection() = _state.update {
        it.copy(disabled = true, paused = false, pausedLabel = null)
    }

    fun resumeProtection() = _state.update {
        it.copy(paused = false, disabled = false, pausedLabel = null)
    }

    fun startSession(appId: String) {
        activeSessionAppId = appId
        sessionConsumedMin = 0f
    }

    /** Quota drains in real time while the protected app is open. */
    fun sessionTick(minutes: Float) {
        val s = _state.value
        if (s.paused || s.disabled) return
        val drained = minOf(s.quotaLeftMin, minutes)
        if (drained <= 0f) return
        sessionConsumedMin += drained
        _state.update { st -> st.copy(quotaLeftMin = (st.quotaLeftMin - drained).coerceAtLeast(0f)) }
    }

    fun markInterrupted() = _state.update {
        it.copy(interruptedSessions = it.interruptedSessions + 1)
    }

    fun endSession(stoppedAfterFriction: Boolean) {
        val appId = activeSessionAppId
        val consumed = sessionConsumedMin.roundToInt()
        activeSessionAppId = null
        sessionConsumedMin = 0f
        _state.update { s ->
            s.copy(
                apps = s.apps.map {
                    if (it.id == appId) it.copy(usedTodayMin = it.usedTodayMin + consumed) else it
                },
                stoppedAfterFriction = if (stoppedAfterFriction) s.stoppedAfterFriction + 1 else s.stoppedAfterFriction
            )
        }
    }
}
