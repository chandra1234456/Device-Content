package com.chandra.practice.deviceinfo.ui.screens.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandra.practice.deviceinfo.data.model.BatteryDetails
import com.chandra.practice.deviceinfo.ui.components.CircularGauge
import com.chandra.practice.deviceinfo.ui.components.LoadingListState
import com.chandra.practice.deviceinfo.ui.components.Sparkline
import com.chandra.practice.deviceinfo.ui.theme.HealthDanger
import com.chandra.practice.deviceinfo.ui.theme.HealthGood
import com.chandra.practice.deviceinfo.ui.theme.HealthWarning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(
    onBack: () -> Unit,
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.startObserving()
        onDispose { viewModel.stopObserving() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Battery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val details = state.details
        if (details == null) {
            LoadingListState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BatteryGauge(details)
                Spacer(Modifier.height(12.dp))
                StatusPill(details)
                Spacer(Modifier.height(20.dp))
                BatteryStatsCard(details)
                Spacer(Modifier.height(16.dp))
                BatteryTestCard(
                    test = state.test,
                    onStart = viewModel::startTest,
                    onStop = viewModel::stopTest,
                )
            }
        }
    }
}

private fun batteryLevelColor(details: BatteryDetails): Color = when {
    details.isCharging -> HealthGood
    details.percent <= 15 -> HealthDanger
    details.percent <= 30 -> HealthWarning
    else -> HealthGood
}

@Composable
private fun BatteryGauge(details: BatteryDetails) {
    val color = batteryLevelColor(details)
    CircularGauge(
        progress = details.percent / 100f,
        modifier = Modifier.size(160.dp),
        strokeWidth = 14.dp,
        color = color,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (details.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp),
            )
            Text("${details.percent}%", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StatusPill(details: BatteryDetails) {
    val color = batteryLevelColor(details)
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (details.isCharging) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = if (details.isCharging) "Charging via ${details.chargeSource}" else "${details.health} Battery",
                color = color,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun BatteryStatsCard(details: BatteryDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatRow("Temperature", String.format(Locale.getDefault(), "%.1f°C", details.temperatureC))
            StatRow("Voltage", String.format(Locale.getDefault(), "%.2f V", details.voltageV))
            StatRow(
                "Current",
                String.format(Locale.getDefault(), "%s%d mA", if (details.isCharging) "+" else "-", details.currentMa),
            )
            StatRow("Power", String.format(Locale.getDefault(), "%.2f W", details.powerW))
            StatRow("Technology", details.technology)
            StatRow("Charge Source", details.chargeSource)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
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

@Composable
private fun BatteryTestCard(
    test: BatteryTestState,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Battery Test", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Measure how fast your battery is charging or draining over time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))

            if (test.samples.size >= 2) {
                Sparkline(
                    values = test.samples.map { it.percent.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                )
                Spacer(Modifier.height(12.dp))
            }

            test.ratePercentPerMinute?.let { rate ->
                val label = if (rate >= 0) {
                    String.format(Locale.getDefault(), "Charging at %.2f%%/min", rate)
                } else {
                    String.format(Locale.getDefault(), "Draining at %.2f%%/min", -rate)
                }
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                test.estimatedMinutesToFull?.let { minutes ->
                    Text(
                        text = "Estimated time to full: $minutes min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            if (test.isRunning) {
                Text(
                    text = "Testing… ${test.samples.size} samples collected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) { Text("Stop Test") }
            } else {
                Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start Battery Test") }
            }
        }
    }
}

