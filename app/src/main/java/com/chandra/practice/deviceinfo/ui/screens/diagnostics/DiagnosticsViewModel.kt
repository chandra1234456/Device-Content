package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.hardware.Sensor
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.data.model.DiagnosticTest
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import com.chandra.practice.deviceinfo.data.repository.DiagnosticsResultsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class DiagnosticsUiState(val tests: List<DiagnosticTest> = emptyList())

private val SENSOR_TEST_TYPES = mapOf(
    DiagnosticTestId.PROXIMITY to Sensor.TYPE_PROXIMITY,
    DiagnosticTestId.ACCELEROMETER to Sensor.TYPE_ACCELEROMETER,
    DiagnosticTestId.GYROSCOPE to Sensor.TYPE_GYROSCOPE,
    DiagnosticTestId.COMPASS to Sensor.TYPE_MAGNETIC_FIELD,
)

class DiagnosticsViewModel(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val resultsRepository: DiagnosticsResultsRepository,
) : ViewModel() {

    val uiState: StateFlow<DiagnosticsUiState> = resultsRepository.results
        .map { results ->
            DiagnosticsUiState(
                tests = DiagnosticTestId.entries.map { id -> DiagnosticTest(id, results[id] ?: DiagnosticResult.NOT_TESTED) },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DiagnosticsUiState(
                tests = DiagnosticTestId.entries.map { DiagnosticTest(it, DiagnosticResult.NOT_TESTED) },
            ),
        )

    /** Instantly verifies the 4 sensor checks (hardware presence); the other 4 tests need a
     *  human to open them and confirm what they saw/felt. */
    fun runAutomaticSensorChecks() {
        SENSOR_TEST_TYPES.forEach { (id, sensorType) ->
            val available = deviceInfoRepository.hasSensor(sensorType)
            resultsRepository.setResult(id, if (available) DiagnosticResult.PASS else DiagnosticResult.FAIL)
        }
    }

    class Factory(
        private val deviceInfoRepository: DeviceInfoRepository,
        private val resultsRepository: DiagnosticsResultsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DiagnosticsViewModel(deviceInfoRepository, resultsRepository) as T
    }
}
