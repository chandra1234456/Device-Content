package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.hardware.Sensor
import android.content.pm.PackageManager
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

    fun runAutomaticChecks() {
        runAutomaticSensorChecks()
        // Hardware-presence checks only. They confirm the device has the component, not that it
        // works; the dedicated test screens verify actual behaviour.
        HARDWARE_FEATURES.forEach { (id, feature) ->
            resultsRepository.setResult(
                id,
                if (deviceInfoRepository.hasSystemFeature(feature)) DiagnosticResult.PASS else DiagnosticResult.FAIL,
            )
        }
    }

    /** Wi-Fi and Bluetooth have no interactive screen, so tapping them runs this instant check. */
    fun checkHardware(id: DiagnosticTestId): Boolean {
        val feature = HARDWARE_FEATURES[id] ?: return false
        val present = deviceInfoRepository.hasSystemFeature(feature)
        resultsRepository.setResult(id, if (present) DiagnosticResult.PASS else DiagnosticResult.FAIL)
        return present
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

private val HARDWARE_FEATURES = mapOf(
    DiagnosticTestId.SPEAKER to PackageManager.FEATURE_AUDIO_OUTPUT,
    DiagnosticTestId.MICROPHONE to PackageManager.FEATURE_MICROPHONE,
    DiagnosticTestId.CAMERA to PackageManager.FEATURE_CAMERA_ANY,
    DiagnosticTestId.GPS to PackageManager.FEATURE_LOCATION_GPS,
    DiagnosticTestId.WIFI to PackageManager.FEATURE_WIFI,
    DiagnosticTestId.BLUETOOTH to PackageManager.FEATURE_BLUETOOTH,
)
