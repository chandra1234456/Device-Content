package com.chandra.practice.deviceinfo.ui.screens.monitor

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.model.MonitorSnapshot
import com.chandra.practice.deviceinfo.data.model.NetworkTotalsSample
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 1_500L
private const val HISTORY_SIZE = 30

@Immutable
data class MonitorUiState(
    val snapshot: MonitorSnapshot? = null,
    val downloadHistory: List<Float> = emptyList(),
    val uploadHistory: List<Float> = emptyList(),
)

/** Polls RAM/storage/battery/thermal/network every ~1.5s while the Monitor screen is open —
 *  started/stopped from the screen's lifecycle, not a background service. */
class MonitorViewModel(private val repository: DeviceInfoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var lastNetworkSample: NetworkTotalsSample? = null

    fun startObserving() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                val networkSample = repository.currentNetworkTotals()
                val (downloadKBps, uploadKBps) = throughput(lastNetworkSample, networkSample)
                lastNetworkSample = networkSample

                val battery = repository.batteryDetails()
                val snapshot = MonitorSnapshot(
                    ramUsageRatio = repository.ramUsageRatio(),
                    storageUsageRatio = repository.storageUsageRatio(),
                    batteryPercent = battery.percent,
                    isCharging = battery.isCharging,
                    batteryTemperatureC = battery.temperatureC,
                    thermalStatus = repository.thermalStatus(),
                    downloadKBps = downloadKBps,
                    uploadKBps = uploadKBps,
                )
                _uiState.update {
                    it.copy(
                        snapshot = snapshot,
                        downloadHistory = (it.downloadHistory + downloadKBps).takeLast(HISTORY_SIZE),
                        uploadHistory = (it.uploadHistory + uploadKBps).takeLast(HISTORY_SIZE),
                    )
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopObserving() {
        pollingJob?.cancel()
        pollingJob = null
        lastNetworkSample = null
    }

    /** [TrafficStats] only exposes cumulative totals, so throughput is derived from the delta
     *  between this poll and the previous one. Returns 0/0 for the first sample (no baseline
     *  yet) or if the device doesn't support traffic stats. */
    private fun throughput(previous: NetworkTotalsSample?, current: NetworkTotalsSample): Pair<Float, Float> {
        if (previous == null || current.rxBytes < 0 || current.txBytes < 0) return 0f to 0f
        val deltaSeconds = (current.timestampMs - previous.timestampMs) / 1000f
        if (deltaSeconds <= 0f) return 0f to 0f
        val downloadKBps = (current.rxBytes - previous.rxBytes).coerceAtLeast(0) / 1024f / deltaSeconds
        val uploadKBps = (current.txBytes - previous.txBytes).coerceAtLeast(0) / 1024f / deltaSeconds
        return downloadKBps to uploadKBps
    }

    override fun onCleared() {
        stopObserving()
        super.onCleared()
    }

    class Factory(private val repository: DeviceInfoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MonitorViewModel(repository) as T
    }
}
