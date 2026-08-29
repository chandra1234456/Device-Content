package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult

private const val GRID_ROWS = 6
private const val GRID_COLS = 4
private const val PASS_COVERAGE_THRESHOLD = 0.9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var touchedCells by remember { mutableStateOf(setOf<Int>()) }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    val coverage = touchedCells.size.toFloat() / (GRID_ROWS * GRID_COLS)

    fun markCell(position: Offset) {
        if (gridSize.width == 0 || gridSize.height == 0) return
        val col = (position.x / (gridSize.width / GRID_COLS.toFloat())).toInt().coerceIn(0, GRID_COLS - 1)
        val row = (position.y / (gridSize.height / GRID_ROWS.toFloat())).toInt().coerceIn(0, GRID_ROWS - 1)
        touchedCells = touchedCells + (row * GRID_COLS + col)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Touch") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = "Drag your finger across every cell to test touch response.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { coverage.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(coverage * 100).toInt()}% covered",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.surfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { gridSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> markCell(offset) },
                            onDrag = { change, _ -> markCell(change.position) },
                        )
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellWidth = size.width / GRID_COLS
                    val cellHeight = size.height / GRID_ROWS
                    for (row in 0 until GRID_ROWS) {
                        for (col in 0 until GRID_COLS) {
                            val isTouched = (row * GRID_COLS + col) in touchedCells
                            drawRect(
                                color = if (isTouched) primaryColor.copy(alpha = 0.6f) else trackColor,
                                topLeft = Offset(col * cellWidth + 2f, row * cellHeight + 2f),
                                size = Size(cellWidth - 4f, cellHeight - 4f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = { touchedCells = emptySet() }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Button(
                    onClick = {
                        onResult(if (coverage >= PASS_COVERAGE_THRESHOLD) DiagnosticResult.PASS else DiagnosticResult.FAIL)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Done") }
            }
        }
    }
}

