package com.chandra.practice.deviceinfo.ui.screens.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NetworkAnalyzerUiState(
    val networkItems: List<DeviceInfoItem> = emptyList(),
    val pingMs: Long = -1L,
    val isPinging: Boolean = false,
    val wifiRssiDbm: Int = -60,
    val wifiSignalQuality: String = "Good",
)

class NetworkViewModel(
    private val repository: DeviceInfoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetworkAnalyzerUiState())
    val uiState: StateFlow<NetworkAnalyzerUiState> = _uiState.asStateFlow()

    init {
        loadNetworkInfo()
    }

    fun loadNetworkInfo() {
        viewModelScope.launch {
            val items = repository.getCategoryInfo(InfoCategory.NETWORK)
            _uiState.update { it.copy(networkItems = items) }
            runPingTest()
        }
    }

    fun runPingTest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }
            val ping = repository.measurePing("8.8.8.8")
            _uiState.update { it.copy(pingMs = ping, isPinging = false) }
        }
    }
}
