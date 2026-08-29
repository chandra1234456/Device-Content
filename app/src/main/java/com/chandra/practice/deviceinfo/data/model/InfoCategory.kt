package com.chandra.practice.deviceinfo.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/** All the info categories the dashboard can show. */
enum class InfoCategory(val title: String, val icon: ImageVector) {
    OVERVIEW("Build & OS", Icons.Filled.PhoneAndroid),
    BATTERY("Battery", Icons.Filled.BatteryFull),
    DISPLAY("Display", Icons.Filled.Smartphone),
    MEMORY_STORAGE("Memory & Storage", Icons.Filled.Storage),
    CPU_HARDWARE("CPU & Hardware", Icons.Filled.Memory),
    SENSORS("Sensors", Icons.Filled.Sensors),
    CAMERA("Camera", Icons.Filled.CameraAlt),
    NETWORK("Network", Icons.Filled.Wifi),
    APP_INFO("App Info", Icons.Filled.Info),
    LOCALE("Locale", Icons.Filled.Language),
}
