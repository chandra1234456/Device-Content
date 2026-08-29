package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    val locationSummary = remember(context, hasPermission) {
        if (!hasPermission) return@remember "Location permission needed"
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val providers = lm.getProviders(true)
        "GPS Enabled: $isGpsEnabled\nNetwork Provider: $isNetEnabled\nActive Location Providers: ${providers.joinToString(", ")}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS / Location Test") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))

            if (!hasPermission) {
                Text(
                    text = "GPS testing requires Location permission to check location hardware providers.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                    Text("Grant Permission")
                }
            } else {
                Text(
                    text = locationSummary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(48.dp))
                Text("Are location providers active and functioning?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { onResult(DiagnosticResult.FAIL) }) {
                        Text("No (Fail)")
                    }
                    Button(onClick = { onResult(DiagnosticResult.PASS) }) {
                        Text("Yes (Pass)")
                    }
                }
            }
        }
    }
}
