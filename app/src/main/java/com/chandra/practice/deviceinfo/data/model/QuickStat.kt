package com.chandra.practice.deviceinfo.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class QuickStat(
    val label: String,
    val value: String,
    val icon: ImageVector,
    /** 0f..1f usage ratio for a small progress ring, null when the stat isn't a percentage. */
    val progress: Float? = null,
)
