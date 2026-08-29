package com.chandra.practice.deviceinfo.data.health

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Memory
import com.chandra.practice.deviceinfo.data.model.BatteryDetails
import com.chandra.practice.deviceinfo.data.model.HealthScore
import com.chandra.practice.deviceinfo.data.model.HealthSubscore

/**
 * Turns already-collected signals into a single 0-100 score users can understand at a glance.
 *
 * The RAM subscore deliberately does NOT use raw usage percentage the way the storage subscore
 * does: Android intentionally keeps free RAM low by caching recently-used apps, so high RAM
 * usage is normal, not a problem. [ActivityManager.MemoryInfo.lowMemory] is the OS's own signal
 * that the system is actually under memory pressure, and that's what's scored here instead.
 */
object DeviceHealthCalculator {

    fun calculate(
        battery: BatteryDetails,
        isLowMemory: Boolean,
        storageUsageRatio: Float,
        isNetworkConnected: Boolean,
    ): HealthScore {
        val batteryScore = batteryHealthScore(battery)
        val storageScore = ((1f - storageUsageRatio) * 100).toInt().coerceIn(0, 100)
        val memoryScore = if (isLowMemory) 40 else 100
        val connectivityScore = if (isNetworkConnected) 100 else 55

        val subscores = listOf(
            HealthSubscore("Battery", batteryScore, Icons.Filled.BatteryFull),
            HealthSubscore("Storage", storageScore, Icons.Filled.Storage),
            HealthSubscore("Memory", memoryScore, Icons.Filled.Memory),
            HealthSubscore("Network", connectivityScore, Icons.Filled.NetworkCheck),
        )
        val overall = (subscores.sumOf { it.score } / subscores.size.toFloat()).toInt().coerceIn(0, 100)

        val recommendations = buildList {
            if (storageScore < 20) add("Storage is getting full — consider freeing up space.")
            if (batteryScore < 60) add("Battery health isn't optimal — keep an eye on charging habits.")
            if (battery.temperatureC > 40f) add("Battery is running warm (${battery.temperatureC.toInt()}°C) — give it a rest from heavy use.")
            if (memoryScore < 100) add("The system is low on memory right now — closing some apps may help.")
            if (!isNetworkConnected) add("No validated internet connection detected.")
        }

        return HealthScore(overall = overall, subscores = subscores, recommendations = recommendations)
    }

    private fun batteryHealthScore(battery: BatteryDetails): Int {
        val base = when (battery.health) {
            "Good" -> 100
            "Unknown" -> 75
            "Overheat", "Over voltage" -> 55
            "Cold" -> 50
            "Dead", "Failure" -> 20
            else -> 75
        }
        val temperaturePenalty = when {
            battery.temperatureC > 45f -> 25
            battery.temperatureC > 40f -> 10
            else -> 0
        }
        return (base - temperaturePenalty).coerceIn(0, 100)
    }
}
