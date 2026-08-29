package com.chandra.practice.deviceinfo.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class BatteryDetails(
    val percent: Int,
    val isCharging: Boolean,
    val chargeSource: String,
    val health: String,
    val technology: String,
    val temperatureC: Float,
    val voltageV: Float,
    /** Absolute current draw in mA — direction is [isCharging], not the sign of this value. */
    val currentMa: Int,
    val powerW: Float,
)

/** One sample taken during a "Battery Test" charge-rate measurement. */
@Immutable
data class BatterySample(val elapsedMs: Long, val percent: Int)
