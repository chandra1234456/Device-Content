package com.chandra.practice.deviceinfo.features.battery

data class BatteryData(
    val percentage: Int,
    val chargingStatus: String,
    val chargingType: String,
    val health: String,
    val cycleCount: Int,
    val capacity: Int,
    val currentNow: Int?,
    val technology: String,
    val powerSaveMode: String,
    val temperature: Double,
    val voltage: Int,
    val energyCounter: Long,
    val isPresent: Boolean,
    val healthStatus: String
)