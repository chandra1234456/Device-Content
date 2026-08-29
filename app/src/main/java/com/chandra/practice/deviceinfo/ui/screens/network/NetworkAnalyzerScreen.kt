package com.chandra.practice.deviceinfo.ui.screens.network

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkAnalyzerScreen(
    viewModel: NetworkViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasLocationPermission = granted
        if (granted) viewModel.loadNetworkInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Network & Wi-Fi Analyzer") },
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
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.03f))
                            Text(
                                text = "Network Latency / Ping",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            if (uiState.isPinging) {
                                Text("Testing...", style = MaterialTheme.typography.bodySmall)
                            } else {
                                OutlinedButton(onClick = { viewModel.runPingTest() }) {
                                    Text("Test Ping")
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (uiState.pingMs >= 0) "${uiState.pingMs} ms (Google DNS 8.8.8.8)" else "Ping test pending...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WifiTethering, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.03f))
                            Text("Wi-Fi Signal Meter", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(12.dp))

                        if (!hasLocationPermission) {
                            Text(
                                text = "Full Wi-Fi details (SSID / BSSID / RSSI signal meter) require Fine Location permission on Android.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                                Text("Grant Location Permission")
                            }
                        } else {
                            Text("Signal Strength: ${uiState.wifiRssiDbm} dBm (${uiState.wifiSignalQuality})")
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { ((uiState.wifiRssiDbm + 100).toFloat() / 70f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp),
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Detailed Network Parameters",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            items(uiState.networkItems) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
