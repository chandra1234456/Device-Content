package com.chandra.practice.deviceinfo.ui.screens.monitor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandra.practice.deviceinfo.data.model.MonitorSnapshot
import com.chandra.practice.deviceinfo.data.model.ThermalStatus
import com.chandra.practice.deviceinfo.ui.components.LoadingListState
import com.chandra.practice.deviceinfo.ui.components.Sparkline
import com.chandra.practice.deviceinfo.ui.theme.HealthDanger
import com.chandra.practice.deviceinfo.ui.theme.HealthGood
import com.chandra.practice.deviceinfo.ui.theme.HealthWarning
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    onBack: () -> Unit,
    viewModel: MonitorViewModel,
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
                title = { Text("Live Monitor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val snapshot = state.snapshot
        if (snapshot == null) {
            LoadingListState(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MonitorMetricCard(
                    icon = Icons.Filled.Memory,
                    label = "RAM",
                    valueText = "${(snapshot.ramUsageRatio * 100).toInt()}%",
                    progress = snapshot.ramUsageRatio,
                )
                MonitorMetricCard(
                    icon = Icons.Filled.Storage,
                    label = "Storage",
                    valueText = "${(snapshot.storageUsageRatio * 100).toInt()}%",
                    progress = snapshot.storageUsageRatio,
                )
                MonitorMetricCard(
                    icon = if (snapshot.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryFull,
                    label = if (snapshot.isCharging) "Battery (charging)" else "Battery",
                    valueText = "${snapshot.batteryPercent}%",
                    progress = snapshot.batteryPercent / 100f,
                )
                ThermalCard(snapshot)
                NetworkCard(snapshot, state.downloadHistory)
            }
        }
    }
}

@Composable
private fun MonitorMetricCard(icon: ImageVector, label: String, valueText: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "monitorMetricProgress",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}

private fun thermalColor(status: ThermalStatus): Color = when (status) {
    ThermalStatus.NONE, ThermalStatus.LIGHT, ThermalStatus.UNKNOWN -> HealthGood
    ThermalStatus.MODERATE -> HealthWarning
    ThermalStatus.SEVERE, ThermalStatus.CRITICAL, ThermalStatus.EMERGENCY -> HealthDanger
}

@Composable
private fun ThermalCard(snapshot: MonitorSnapshot) {
    val color = thermalColor(snapshot.thermalStatus)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Thermostat, contentDescription = null, tint = color)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Temperature", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = String.format(Locale.getDefault(), "%.1f°C battery", snapshot.batteryTemperatureC),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(snapshot.thermalStatus.label, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NetworkCard(snapshot: MonitorSnapshot, downloadHistory: List<Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Network Activity", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                NetworkRate(
                    icon = Icons.Filled.ArrowDownward,
                    label = "Download",
                    value = String.format(Locale.getDefault(), "%.1f KB/s", snapshot.downloadKBps),
                )
                NetworkRate(
                    icon = Icons.Filled.ArrowUpward,
                    label = "Upload",
                    value = String.format(Locale.getDefault(), "%.1f KB/s", snapshot.uploadKBps),
                )
            }
            if (downloadHistory.size >= 2) {
                Spacer(Modifier.height(16.dp))
                Sparkline(
                    values = downloadHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                )
            }
        }
    }
}

@Composable
private fun NetworkRate(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.height(16.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

