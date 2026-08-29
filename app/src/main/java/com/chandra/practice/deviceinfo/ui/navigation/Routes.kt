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
    const val SENSOR_TEST = "diagnostics/sensor/{testId}"
    const val WEBVIEW = "webview/{url}/{title}"

    fun webView(url: String, title: String) = "webview/${Uri.encode(url)}/${Uri.encode(title)}"
    fun sensorTest(testId: DiagnosticTestId) = "diagnostics/sensor/${testId.name}"
}
