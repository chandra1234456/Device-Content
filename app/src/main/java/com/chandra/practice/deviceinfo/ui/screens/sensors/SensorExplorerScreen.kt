package com.chandra.practice.deviceinfo.ui.screens.sensors

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorExplorerScreen(
    viewModel: SensorExplorerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Explorer (${uiState.sensors.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search hardware sensors by name or vendor...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                singleLine = true,
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(uiState.filteredSensors) { sensor ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSensor(sensor) },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.04f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = sensor.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Vendor: ${sensor.vendor} · Type: ${sensor.stringType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.selectedSensor?.let { sensor ->
        AlertDialog(
            onDismissRequest = { viewModel.selectSensor(null) },
            title = { Text(sensor.name) },
            text = {
                Column {
                    Text("Vendor: ${sensor.vendor}")
                    Text("Power Draw: ${sensor.power} mA")
                    Text("Max Range: ${sensor.maxRange}")
                    Text("Resolution: ${sensor.resolution}")
                    Spacer(Modifier.height(16.dp))
                    Text("Live Readings:", style = MaterialTheme.typography.titleSmall)
                    if (uiState.liveValues.isEmpty()) {
                        Text("Waiting for sensor events...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        uiState.liveValues.forEachIndexed { index, value ->
                            Text("Axis $index: String.format(%.3f, value) ($value)")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.selectSensor(null) }) {
                    Text("Close")
                }
            },
        )
    }
}
