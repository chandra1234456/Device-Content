package com.chandra.practice.deviceinfo.ui.screens.benchmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.repository.BenchmarkRepository
import com.chandra.practice.deviceinfo.data.repository.BenchmarkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BenchmarkUiState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val statusText: String = "Ready to run hardware benchmarks",
    val result: BenchmarkResult? = null,
)

class BenchmarkViewModel(
    private val repository: BenchmarkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BenchmarkUiState())
    val uiState: StateFlow<BenchmarkUiState> = _uiState.asStateFlow()

    fun startBenchmark() {
        if (_uiState.value.isRunning) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, progress = 0f, statusText = "Initializing benchmarks...") }
            val res = repository.runBenchmark { prog, status ->
                _uiState.update { state -> state.copy(progress = prog, statusText = status) }
            }
            _uiState.update { it.copy(isRunning = false, progress = 1.0f, result = res, statusText = "Benchmark Complete!") }
        }
    }
}
