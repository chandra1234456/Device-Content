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
    val estimatedMinutesToEmpty: Int? = null,
    /** Elapsed test time, updated on each poll. */
    val elapsedMs: Long = 0L,
    /** True when a finished test was too short or saw no whole-percent change to give a real rate. */
    val notEnoughData: Boolean = false,
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
        // Samples only arrive while polling, so a test can't keep running once the screen is gone.
        if (_uiState.value.test.isRunning) stopTest()
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
            val percent = state.details?.percent ?: 0
            state.copy(
                test = state.test.copy(
                    isRunning = false,
                    ratePercentPerMinute = rate,
                    estimatedMinutesToFull = rate?.takeIf { it > 0f }?.let { ((100 - percent) / it).toInt() },
                    estimatedMinutesToEmpty = rate?.takeIf { it < 0f }?.let { (percent / -it).toInt() },
                    notEnoughData = rate == null,
                ),
            )
        }
    }

    private fun recordSample(percent: Int) {
        val elapsed = System.currentTimeMillis() - testStartMs
        _uiState.update { state -> state.copy(test = state.test.copy(samples = state.test.samples + BatterySample(elapsed, percent), elapsedMs = elapsed)) }
    }

    private fun chargeRatePerMinute(samples: List<BatterySample>): Float? {
        if (samples.size < 2) return null
        val first = samples.first()
        val last = samples.last()
        // Battery level only moves in whole percents, so a short run or a flat level would report a
        // misleading 0%/min. Require some time and at least one percent of change.
        if (last.elapsedMs - first.elapsedMs < MIN_TEST_DURATION_MS || last.percent == first.percent) return null
        val minutes = (last.elapsedMs - first.elapsedMs) / 60_000f
        return (last.percent - first.percent) / minutes
    }

    override fun onCleared() {
        stopObserving()
        super.onCleared()
    }

    private companion object {
        const val MIN_TEST_DURATION_MS = 60_000L
    }

    class Factory(private val repository: DeviceInfoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BatteryViewModel(repository) as T
    }
}
