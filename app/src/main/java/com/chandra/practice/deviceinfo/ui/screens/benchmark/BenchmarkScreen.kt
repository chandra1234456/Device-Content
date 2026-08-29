package com.chandra.practice.deviceinfo.ui.screens.benchmark

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchmarkScreen(
    viewModel: BenchmarkViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val animatedProgress by animateFloatAsState(targetValue = uiState.progress, label = "benchmarkProgress")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Benchmark") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Filled.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = uiState.result?.tierRating ?: "Hardware Performance Score",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(Modifier.height(8.dp))
                        if (uiState.result != null) {
                            Text(
                                text = "${uiState.result?.overallScore} Points",
                                style = MaterialTheme.typography.displayMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                text = uiState.statusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        if (uiState.isRunning) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(uiState.statusText, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Button(
                                onClick = { viewModel.startBenchmark() },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (uiState.result != null) "Re-run Benchmark" else "Start Benchmark")
                            }
                        }
                    }
                }
            }

            uiState.result?.let { res ->
                item {
                    Text("Benchmark Breakdown", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.DeveloperBoard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.04f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("CPU Multi-threaded Test", style = MaterialTheme.typography.titleMedium)
                                Text("Execution Time: ${res.details.primeTimeMs} ms", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("${res.cpuScore}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.04f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("RAM Memory Throughput", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Speed: ${String.format(Locale.getDefault(), "%.1f", res.details.memoryThroughputMbps)} MB/s",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text("${res.ramScore}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.04f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Internal Storage Speed", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Write: ${String.format(Locale.getDefault(), "%.1f", res.details.storageWriteMbps)} MB/s · Read: ${String.format(Locale.getDefault(), "%.1f", res.details.storageReadMbps)} MB/s",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text("${res.storageScore}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
