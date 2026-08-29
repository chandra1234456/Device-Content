package com.chandra.practice.deviceinfo.ui.screens.storage

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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
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
fun StorageAnalyzerScreen(
    viewModel: StorageViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        return if (gb >= 1.0) {
            String.format(Locale.getDefault(), "%.2f GB", gb)
        } else {
            val mb = bytes / (1024.0 * 1024.0)
            String.format(Locale.getDefault(), "%.1f MB", mb)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Analyzer") },
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(1.dp).fillMaxWidth(0.03f))
                            Text("Internal Storage Overview", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "${formatBytes(uiState.usedBytes)} / ${formatBytes(uiState.totalBytes)} Used",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (uiState.totalBytes > 0) (uiState.usedBytes.toFloat() / uiState.totalBytes) else 0f
                            },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                        )
                    }
                }
            }

            item {
                Text("Category Breakdown", style = MaterialTheme.typography.titleMedium)
            }

            items(uiState.categories) { cat ->
                val percent = if (uiState.totalBytes > 0) (cat.bytes.toDouble() / uiState.totalBytes * 100) else 0.0
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(1.dp).fillMaxWidth(0.04f))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.categoryName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                String.format(Locale.getDefault(), "%.1f%% of internal storage", percent),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            formatBytes(cat.bytes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
