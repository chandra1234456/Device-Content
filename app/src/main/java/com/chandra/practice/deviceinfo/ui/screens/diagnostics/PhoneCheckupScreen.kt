package com.chandra.practice.deviceinfo.ui.screens.diagnostics

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import kotlinx.coroutines.delay

import com.chandra.practice.deviceinfo.data.model.DiagnosticTest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCheckupScreen(
    tests: List<DiagnosticTest>,
    onRunAutoChecks: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isScanning by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "checkupProgress")

    fun startCheckup() {
        isScanning = true
        progress = 0f
    }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            val stepCount = 10
            for (i in 1..stepCount) {
                delay(200)
                progress = i / stepCount.toFloat()
            }
            onRunAutoChecks()
            isScanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Checkup Flow") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "One-Button Diagnostic Sweep",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Run automated checks across all hardware sensors and diagnostic modules in a single pass.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    if (isScanning) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Testing sensors...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Button(
                            onClick = { startCheckup() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Run Full Checkup Now")
                        }
                    }
                }
            }

            Text(
                text = "Diagnostic Status Summary",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp),
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tests) { test ->
                    val id = test.id
                    val result = test.result
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(id.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp).fillMaxWidth(0.04f))
                            Text(
                                text = id.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            when (result) {
                                DiagnosticResult.PASS -> Icon(Icons.Filled.CheckCircle, contentDescription = "Pass", tint = MaterialTheme.colorScheme.primary)
                                DiagnosticResult.FAIL -> Icon(Icons.Filled.Error, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                                DiagnosticResult.NOT_TESTED -> Icon(Icons.Filled.HourglassTop, contentDescription = "Not Tested", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
