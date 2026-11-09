package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.data.model.DiagnosticTest
import com.chandra.practice.deviceinfo.data.model.DiagnosticTestId
import com.chandra.practice.deviceinfo.ui.theme.HealthDanger
import com.chandra.practice.deviceinfo.ui.theme.HealthGood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    onOpenTest: (DiagnosticTestId) -> Unit,
    onOpenCheckup: () -> Unit = {},
    viewModel: DiagnosticsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    DiagnosticsSummary(tests = state.tests)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Test My Phone",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Tap a check below, or run everything that can be verified automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::runAutomaticChecks, modifier = Modifier.weight(1f)) {
                            Text("Quick Scan")
                        }
                        Button(onClick = onOpenCheckup, modifier = Modifier.weight(1f)) {
                            Text("Guided Checkup")
                        }
                    }
                }
            }

            items(state.tests, key = { it.id }) { test ->
                DiagnosticTestCard(
                    test = test,
                    onClick = {
                        if (test.id == DiagnosticTestId.WIFI || test.id == DiagnosticTestId.BLUETOOTH) {
                            val present = viewModel.checkHardware(test.id)
                            Toast.makeText(
                                context,
                                if (present) "${test.id.title} hardware detected" else "${test.id.title} not available on this device",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            onOpenTest(test.id)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsSummary(tests: List<DiagnosticTest>, modifier: Modifier = Modifier) {
    val passed = tests.count { it.result == DiagnosticResult.PASS }
    val failed = tests.count { it.result == DiagnosticResult.FAIL }
    val done = passed + failed
    val progress by animateFloatAsState(
        targetValue = if (tests.isEmpty()) 0f else done / tests.size.toFloat(),
        label = "diagnosticsProgress",
    )
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$done of ${tests.size} tested",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (done == 0) "Nothing checked yet" else "$passed passed · $failed failed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun DiagnosticTestCard(test: DiagnosticTest, onClick: () -> Unit) {
    val (containerColor, statusColor) = when (test.result) {
        DiagnosticResult.PASS -> HealthGood.copy(alpha = 0.14f) to HealthGood
        DiagnosticResult.FAIL -> HealthDanger.copy(alpha = 0.14f) to HealthDanger
        DiagnosticResult.NOT_TESTED -> MaterialTheme.colorScheme.surfaceContainer to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusIcon = when (test.result) {
        DiagnosticResult.PASS -> Icons.Filled.CheckCircle
        DiagnosticResult.FAIL -> Icons.Filled.Cancel
        DiagnosticResult.NOT_TESTED -> Icons.Filled.RadioButtonUnchecked
    }
    val statusText = when (test.result) {
        DiagnosticResult.PASS -> "Pass"
        DiagnosticResult.FAIL -> "Fail"
        DiagnosticResult.NOT_TESTED -> "Not tested"
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${test.id.title}: $statusText" },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = test.id.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(test.id.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(statusText, style = MaterialTheme.typography.labelMedium, color = statusColor)
            }
        }
    }
}
