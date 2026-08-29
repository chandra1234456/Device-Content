package com.chandra.practice.deviceinfo.ui.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository.StorageCategorySize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StorageAnalyzerUiState(
    val isLoading: Boolean = true,
    val categories: List<StorageCategorySize> = emptyList(),
    val totalBytes: Long = 0L,
    val usedBytes: Long = 0L,
)

class StorageViewModel(
    private val repository: DeviceInfoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageAnalyzerUiState())
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    init {
        loadStorageData()
    }

    fun loadStorageData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val breakdown = repository.getStorageCategoryBreakdown()
            val total = breakdown.sumOf { it.bytes }
            val free = breakdown.firstOrNull { it.categoryName == "Free Storage" }?.bytes ?: 0L
            val used = total - free
            _uiState.update {
                it.copy(
                    isLoading = false,
                    categories = breakdown,
                    totalBytes = total,
                    usedBytes = used,
                )
            }
        }
    }
}
