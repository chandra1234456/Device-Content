@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.chandra.practice.deviceinfo.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandra.practice.deviceinfo.R
import com.chandra.practice.deviceinfo.data.export.PdfReportExporter
import com.chandra.practice.deviceinfo.data.export.PdfSaveLocation
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.ui.components.AppSearchField
import com.chandra.practice.deviceinfo.ui.components.CategoryChipsRow
import com.chandra.practice.deviceinfo.ui.components.EmptyState
import com.chandra.practice.deviceinfo.ui.components.ErrorState
import com.chandra.practice.deviceinfo.ui.components.HealthScoreCard
import com.chandra.practice.deviceinfo.ui.components.InfoRowCard
import com.chandra.practice.deviceinfo.ui.components.LoadingListState
import com.chandra.practice.deviceinfo.ui.components.StatCard
import com.chandra.practice.deviceinfo.ui.permissions.hasCameraPermission
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    SYSTEM("System", Icons.Filled.Dns),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onOpenSettings: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenMonitor: () -> Unit,
    onOpenNetworkAnalyzer: () -> Unit = {},
    onOpenSensorExplorer: () -> Unit = {},
    onOpenBenchmark: () -> Unit = {},
    onOpenStorageAnalyzer: () -> Unit = {},
    viewModel: HomeViewModel,
    isUpdateReady: Boolean = false,
    onRestartToUpdate: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    var cameraPermissionGranted by remember { mutableStateOf(hasCameraPermission(context)) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraPermissionGranted = granted
        if (granted) viewModel.reloadCurrentCategory()
    }

    // Camera permission can also be granted from system Settings while this screen is backgrounded.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = hasCameraPermission(context)
                if (granted != cameraPermissionGranted) {
                    cameraPermissionGranted = granted
                    if (granted) viewModel.reloadCurrentCategory()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val needsCameraPermission = state.selectedCategory == InfoCategory.CAMERA && !cameraPermissionGranted
    val filteredItems = remember(state.content, state.searchQuery) {
        val items = (state.content as? CategoryContent.Success)?.items.orEmpty()
        if (state.searchQuery.isBlank()) {
            items
        } else {
            items.filter {
                it.label.contains(state.searchQuery, ignoreCase = true) ||
                    it.value.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == MainTab.HOME) "Device Content" else "System Info") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Download PDF") },
                icon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                onClick = { showExportDialog = true },
            )
        },
    ) { padding ->
        when (selectedTab) {
            MainTab.HOME -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    if (isUpdateReady) {
                        item {
                            UpdateReadyBanner(
                                onRestart = onRestartToUpdate,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    state.healthScore?.let { healthScore ->
                        item {
                            HealthScoreCard(
                                healthScore = healthScore,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    item {
                        QuickActionsGrid(
                            onOpenBattery = onOpenBattery,
                            onOpenDiagnostics = onOpenDiagnostics,
                            onOpenMonitor = onOpenMonitor,
                            onOpenNetworkAnalyzer = onOpenNetworkAnalyzer,
                            onOpenSensorExplorer = onOpenSensorExplorer,
                            onOpenBenchmark = onOpenBenchmark,
                            onOpenStorageAnalyzer = onOpenStorageAnalyzer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(state.quickStats, key = { it.label }) { stat -> StatCard(stat) }
                        }
                    }
                }
            }
            MainTab.SYSTEM -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    if (useRail) {
                        CategoryNavigationRail(selected = state.selectedCategory, onSelect = viewModel::selectCategory)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        if (!useRail) {
                            item {
                                CategoryChipsRow(
                                    categories = InfoCategory.entries,
                                    selected = state.selectedCategory,
                                    onSelect = viewModel::selectCategory,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }

                        item {
                            AppSearchField(
                                query = state.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }

                        item { Spacer(Modifier.height(4.dp)) }

                        if (needsCameraPermission) {
                            item {
                                CameraPermissionRationale(
                                    onGrant = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }
                        } else {
                            when (val content = state.content) {
                                is CategoryContent.Loading -> item { LoadingListState() }
                                is CategoryContent.Error -> item {
                                    ErrorState(message = content.message, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                                is CategoryContent.Empty -> item {
                                    EmptyState(message = content.message, modifier = Modifier.padding(horizontal = 16.dp))
                                }
                                is CategoryContent.Success -> {
                                    if (filteredItems.isEmpty()) {
                                        item {
                                            EmptyState(
                                                message = "No results for \"${state.searchQuery}\"",
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                            )
                                        }
                                    } else {
                                        items(filteredItems, key = { it.key }) { item ->
                                            InfoRowCard(
                                                item = item,
                                                onCopy = {
                                                    clipboardManager.setText(AnnotatedString("${item.label}: ${item.value}"))
                                                },
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        val (categoryTitle, exportItems) = viewModel.currentExportData()
        ExportPdfDialog(
            categoryTitle = categoryTitle,
            itemCount = exportItems.size,
            onDismiss = { showExportDialog = false },
            onConfirm = {
                showExportDialog = false
                coroutineScope.launch {
                    try {
                        val result = PdfReportExporter.export(context, appName, categoryTitle, exportItems)
                        val message = when (result) {
                            is PdfSaveLocation.Downloads -> "Saved to Downloads: ${result.fileName}"
                            is PdfSaveLocation.AppFolder -> "Saved to app storage: ${result.fileName}"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        PdfReportExporter.sharePdfReport(context, result)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Couldn't save the PDF: ${e.message ?: "unknown error"}", Toast.LENGTH_LONG).show()
                    }
                }
            },
        )
    }
}

@Composable
private fun ExportPdfDialog(
    categoryTitle: String,
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
        title = { Text("Download PDF") },
        text = { Text("Save a PDF of \"$categoryTitle\" ($itemCount items) to your device's Downloads folder?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Download") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun CameraPermissionRationale(onGrant: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Camera details — how many cameras your device has and which way they face — need " +
                "camera permission. This app never captures photos or video.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onGrant) { Text("Grant camera permission") }
    }
}

@Composable
private fun QuickActionsGrid(
    onOpenBattery: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenMonitor: () -> Unit,
    onOpenNetworkAnalyzer: () -> Unit,
    onOpenSensorExplorer: () -> Unit,
    onOpenBenchmark: () -> Unit,
    onOpenStorageAnalyzer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = listOf(
        Triple(Icons.Filled.BatteryFull, "Battery", onOpenBattery),
        Triple(Icons.Filled.Science, "Diagnose", onOpenDiagnostics),
        Triple(Icons.Filled.Monitor, "Monitor", onOpenMonitor),
        Triple(Icons.Filled.Wifi, "Network", onOpenNetworkAnalyzer),
        Triple(Icons.Filled.Sensors, "Sensors", onOpenSensorExplorer),
        Triple(Icons.Filled.Speed, "Benchmark", onOpenBenchmark),
        Triple(Icons.Filled.Storage, "Storage", onOpenStorageAnalyzer),
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowActions.forEach { (icon, label, onClick) ->
                    QuickActionCard(icon = icon, label = label, onClick = onClick, modifier = Modifier.weight(1f))
                }
                if (rowActions.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun UpdateReadyBanner(onRestart: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "An update has been downloaded",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRestart) { Text("Restart") }
        }
    }
}

@Composable
private fun CategoryNavigationRail(selected: InfoCategory, onSelect: (InfoCategory) -> Unit) {
    NavigationRail {
        InfoCategory.entries.forEach { category ->
            NavigationRailItem(
                selected = category == selected,
                onClick = { onSelect(category) },
                icon = { Icon(category.icon, contentDescription = null) },
                label = { Text(category.title, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}
