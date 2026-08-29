package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import com.chandra.practice.deviceinfo.ui.components.EmptyState
import java.util.Locale

private fun sensorTypeFor(testId: DiagnosticTestId): Int = when (testId) {
    DiagnosticTestId.PROXIMITY -> Sensor.TYPE_PROXIMITY
    DiagnosticTestId.ACCELEROMETER -> Sensor.TYPE_ACCELEROMETER
    DiagnosticTestId.GYROSCOPE -> Sensor.TYPE_GYROSCOPE
    DiagnosticTestId.COMPASS -> Sensor.TYPE_MAGNETIC_FIELD
    else -> throw IllegalArgumentException("$testId is not a live-reading sensor test")
}

private fun unitFor(testId: DiagnosticTestId): String = when (testId) {
    DiagnosticTestId.PROXIMITY -> "cm"
    DiagnosticTestId.ACCELEROMETER -> "m/s²"
    DiagnosticTestId.GYROSCOPE -> "rad/s"
    DiagnosticTestId.COMPASS -> "µT"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTestScreen(
    testId: DiagnosticTestId,
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensorType = remember(testId) { sensorTypeFor(testId) }
    val sensor = remember(sensorType) { sensorManager.getDefaultSensor(sensorType) }
    var values by remember { mutableStateOf(floatArrayOf(0f, 0f, 0f)) }
    var hasReading by remember { mutableStateOf(false) }

    DisposableEffect(sensor) {
        if (sensor == null) {
            onDispose {}
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    values = event.values.copyOf().let { if (it.size < 3) it + FloatArray(3 - it.size) else it }
                    hasReading = true
                }

                override fun onAccuracyChanged(changedSensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(testId.title) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (sensor == null) {
                EmptyState(message = "This device doesn't report a ${testId.title} sensor.")
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        onResult(DiagnosticResult.FAIL)
                        onBack()
                    },
                ) { Text("OK") }
            } else {
                Text(
                    text = if (hasReading) "Move your device to see the readings change." else "Waiting for a reading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                SensorReadingCard(testId = testId, values = values)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Are the readings changing as you move the device?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            onResult(DiagnosticResult.PASS)
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Yes, working") }
                    OutlinedButton(
                        onClick = {
                            onResult(DiagnosticResult.FAIL)
                            onBack()
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("No") }
                }
            }
        }
    }
}

@Composable
private fun SensorReadingCard(testId: DiagnosticTestId, values: FloatArray) {
    val unit = unitFor(testId)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (testId == DiagnosticTestId.PROXIMITY) {
                ReadingRow("Distance", String.format(Locale.getDefault(), "%.1f %s", values[0], unit))
            } else {
                ReadingRow("X", String.format(Locale.getDefault(), "%.2f %s", values[0], unit))
                ReadingRow("Y", String.format(Locale.getDefault(), "%.2f %s", values[1], unit))
                ReadingRow("Z", String.format(Locale.getDefault(), "%.2f %s", values[2], unit))
            }
        }
    }
}

@Composable
private fun ReadingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
