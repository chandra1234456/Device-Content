package com.chandra.practice.deviceinfo.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.repository.SensorExplorerRepository
import com.chandra.practice.deviceinfo.data.repository.SensorInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorExplorerUiState(
    val sensors: List<SensorInfo> = emptyList(),
    val filteredSensors: List<SensorInfo> = emptyList(),
    val searchQuery: String = "",
    val selectedSensor: SensorInfo? = null,
    val liveValues: List<Float> = emptyList(),
)

class SensorExplorerViewModel(
    private val repository: SensorExplorerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensorExplorerUiState())
    val uiState: StateFlow<SensorExplorerUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null

    init {
        loadSensors()
    }

    fun loadSensors() {
        val list = repository.getAllSensors()
        _uiState.update {
            it.copy(
                sensors = list,
                filteredSensors = list,
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.sensors
            } else {
                state.sensors.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.vendor.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredSensors = filtered)
        }
    }

    fun selectSensor(sensor: SensorInfo?) {
        streamJob?.cancel()
        _uiState.update { it.copy(selectedSensor = sensor, liveValues = emptyList()) }

        if (sensor != null) {
            streamJob = viewModelScope.launch {
                repository.getSensorDataStream(sensor.type).collect { sample ->
                    _uiState.update { it.copy(liveValues = sample.values) }
                }
            }
        }
    }
}
