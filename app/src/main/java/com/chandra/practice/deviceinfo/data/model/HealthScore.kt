package com.chandra.practice.deviceinfo.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class HealthSubscore(val label: String, val score: Int, val icon: ImageVector)

@Immutable
data class HealthScore(
    val overall: Int,
    val subscores: List<HealthSubscore>,
    val recommendations: List<String>,
)
