package com.chandra.practice.deviceinfo.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class DeviceInfoItem(
    val category: String,
    val label: String,
    val value: String,
    /** True only for values worth copying elsewhere (an ID, an address) — most rows don't need it. */
    val copyable: Boolean = false,
) {
    val key: String get() = "$category::$label"
}
