package com.chandra.practice.deviceinfo.ui.navigation

import android.net.Uri
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId

object Routes {
    const val INTRO = "intro"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PRIVACY_POLICY = "privacy_policy"
    const val TERMS = "terms"
    const val BATTERY = "battery"
    const val MONITOR = "monitor"
    const val DIAGNOSTICS = "diagnostics"
    const val TOUCH_TEST = "diagnostics/touch"
    const val VIBRATION_TEST = "diagnostics/vibration"
    const val FLASH_TEST = "diagnostics/flash"
    const val DISPLAY_TEST = "diagnostics/display"
    const val SPEAKER_TEST = "diagnostics/speaker"
    const val MIC_TEST = "diagnostics/mic"
    const val CAMERA_TEST = "diagnostics/camera"
    const val LOCATION_TEST = "diagnostics/location"
    const val PHONE_CHECKUP = "phone_checkup"
    const val NETWORK_ANALYZER = "network_analyzer"
    const val SENSOR_EXPLORER = "sensor_explorer"
    const val BENCHMARK = "benchmark"
    const val STORAGE_ANALYZER = "storage_analyzer"
    const val SENSOR_TEST = "diagnostics/sensor/{testId}"
    const val WEBVIEW = "webview/{url}/{title}"

    fun webView(url: String, title: String) = "webview/${Uri.encode(url)}/${Uri.encode(title)}"
    fun sensorTest(testId: DiagnosticTestId) = "diagnostics/sensor/${testId.name}"
}
