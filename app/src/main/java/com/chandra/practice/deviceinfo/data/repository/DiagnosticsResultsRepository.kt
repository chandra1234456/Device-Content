package com.chandra.practice.deviceinfo.data.repository

import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session-only diagnostic test results ("did it work just now") — intentionally not persisted;
 * these reset the next time the app is launched, unlike user preferences.
 */
class DiagnosticsResultsRepository {

    private val _results = MutableStateFlow<Map<DiagnosticTestId, DiagnosticResult>>(emptyMap())
    val results: StateFlow<Map<DiagnosticTestId, DiagnosticResult>> = _results.asStateFlow()

    fun setResult(id: DiagnosticTestId, result: DiagnosticResult) {
        _results.value = _results.value + (id to result)
    }
}
