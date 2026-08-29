package com.chandra.practice.deviceinfo.ui.screens.battery

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.model.BatteryDetails
import com.chandra.practice.deviceinfo.data.model.BatterySample
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class BatteryTestState(
    val isRunning: Boolean = false,
    val samples: List<BatterySample> = emptyList(),
    val ratePercentPerMinute: Float? = null,
    val estimatedMinutesToFull: Int? = null,
)

@Immutable
data class BatteryUiState(
    val details: BatteryDetails? = null,
    val test: BatteryTestState = BatteryTestState(),
)

/**
 * Polls [DeviceInfoRepository.batteryDetails] every ~2s while the Battery screen is visible
 * (started/stopped from the screen's lifecycle, not a background service) and, when a
 * "Battery Test" is running, records each poll as a sample to estimate charge/discharge rate.
 */
class BatteryViewModel(private val repository: DeviceInfoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var testStartMs = 0L

    fun startObserving() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                val details = repository.batteryDetails()
                _uiState.update { it.copy(details = details) }
                if (_uiState.value.test.isRunning) {
                    recordSample(details.percent)
                }
                delay(2_000)
            }
        }
    }

    fun stopObserving() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun startTest() {
        if (_uiState.value.test.isRunning) return
        testStartMs = System.currentTimeMillis()
        val startingPercent = _uiState.value.details?.percent ?: 0
        _uiState.update {
            it.copy(test = BatteryTestState(isRunning = true, samples = listOf(BatterySample(0L, startingPercent))))
        }
    }

    fun stopTest() {
        _uiState.update { state ->
            val rate = chargeRatePerMinute(state.test.samples)
            val remainingPercent = 100 - (state.details?.percent ?: 0)
            val minutesToFull = rate?.takeIf { it > 0f }?.let { (remainingPercent / it).toInt() }
            state.copy(
                test = state.test.copy(
                    isRunning = false,
                    ratePercentPerMinute = rate,
                    estimatedMinutesToFull = minutesToFull,
                ),
            )
        }
    }

    private fun recordSample(percent: Int) {
        val elapsed = System.currentTimeMillis() - testStartMs
        _uiState.update { state -> state.copy(test = state.test.copy(samples = state.test.samples + BatterySample(elapsed, percent))) }
    }

    private fun chargeRatePerMinute(samples: List<BatterySample>): Float? {
        if (samples.size < 2) return null
        val first = samples.first()
        val last = samples.last()
        val minutes = (last.elapsedMs - first.elapsedMs) / 60_000f
        return if (minutes <= 0f) null else (last.percent - first.percent) / minutes
    }

    override fun onCleared() {
        stopObserving()
        super.onCleared()
    }

    class Factory(private val repository: DeviceInfoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BatteryViewModel(repository) as T
    }
}
