package com.chandra.practice.deviceinfo.ui.screens.home

import androidx.compose.runtime.Immutable
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem
import com.chandra.practice.deviceinfo.data.model.HealthScore
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.model.QuickStat

@Immutable
sealed interface CategoryContent {
    @Immutable data object Loading : CategoryContent
    @Immutable data class Success(val items: List<DeviceInfoItem>) : CategoryContent
    @Immutable data class Empty(val message: String) : CategoryContent
    @Immutable data class Error(val message: String) : CategoryContent
}

@Immutable
data class HomeUiState(
    val selectedCategory: InfoCategory = InfoCategory.OVERVIEW,
    val content: CategoryContent = CategoryContent.Loading,
    val quickStats: List<QuickStat> = emptyList(),
    val healthScore: HealthScore? = null,
    val searchQuery: String = "",
)
