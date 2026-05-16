package com.chandra.practice.deviceinfo.features.battery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryInfoViewModel : ViewModel() {
    private val _batteryData = MutableStateFlow(createEmptyBatteryData())
    val batteryData: StateFlow<BatteryData> = _batteryData.asStateFlow()

    fun updateBatteryData(data: BatteryData) {
        viewModelScope.launch {
            _batteryData.emit(data)
        }
    }

    private fun createEmptyBatteryData() = BatteryData(
        percentage = -1,
        chargingStatus = "Unknown",
        chargingType = "Unknown",
        health = "Unknown",
        cycleCount = -1,
        capacity = -1,
        currentNow = null,
        technology = "Unknown",
        powerSaveMode = "Unknown",
        temperature = -1.0,
        voltage = -1,
        energyCounter = -1,
        isPresent = false,
        healthStatus = "Unknown"
    )
}