package com.chandra.practice.deviceinfo.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

enum class DiagnosticResult { NOT_TESTED, PASS, FAIL }

enum class DiagnosticTestId(val title: String, val icon: ImageVector) {
    DISPLAY("Display", Icons.Filled.Monitor),
    TOUCH("Touch", Icons.Filled.SwipeUp),
    VIBRATION("Vibration", Icons.Filled.Vibration),
    FLASH("Flash", Icons.Filled.FlashOn),
    PROXIMITY("Proximity", Icons.Filled.Visibility),
    ACCELEROMETER("Accelerometer", Icons.Filled.Sensors),
    GYROSCOPE("Gyroscope", Icons.Filled.Sensors),
    COMPASS("Compass", Icons.Filled.Explore),
}

data class DiagnosticTest(val id: DiagnosticTestId, val result: DiagnosticResult)
