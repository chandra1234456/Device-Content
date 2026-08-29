package com.chandra.practice.deviceinfo.data.model

import androidx.compose.runtime.Immutable

enum class ThermalStatus(val label: String) {
    NONE("Normal"),
    LIGHT("Light"),
    MODERATE("Moderate"),
    SEVERE("Severe"),
    CRITICAL("Critical"),
    EMERGENCY("Emergency"),
    UNKNOWN("Unknown"),
}

@Immutable
data class MonitorSnapshot(
    val ramUsageRatio: Float,
    val storageUsageRatio: Float,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val batteryTemperatureC: Float,
    val thermalStatus: ThermalStatus,
    val downloadKBps: Float,
    val uploadKBps: Float,
)

/** A single point-in-time read of cumulative network bytes, used to compute a throughput delta
 *  between two samples — [TrafficStats] only exposes running totals, not a rate. */
data class NetworkTotalsSample(val timestampMs: Long, val rxBytes: Long, val txBytes: Long)
