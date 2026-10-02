@file:OptIn(androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi::class)

package com.chandra.practice.deviceinfo.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.foundation.lazy.itemsIndexed
import com.chandra.practice.deviceinfo.ui.components.CategoryHeader
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
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
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    // Back from the System tab returns to the Home tab first; only Back on Home leaves the app.
    BackHandler(enabled = selectedTab != MainTab.HOME) { selectedTab = MainTab.HOME }

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
                    if (selectedTab == MainTab.SYSTEM) {
                        IconButton(onClick = {
                            val (title, items) = viewModel.currentExportData()
                            if (items.isEmpty()) {
                                Toast.makeText(context, "Nothing to share yet", Toast.LENGTH_SHORT).show()
                            } else {
                                val body = items.joinToString("\n", prefix = "$title\n\n") { "${it.label}: ${it.value}" }
                                val send = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "$appName - $title")
                                    putExtra(Intent.EXTRA_TEXT, body)
                                }
                                context.startActivity(Intent.createChooser(send, "Share $title"))
                            }
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share ${state.selectedCategory.title}")
                        }
                    }
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
            if (selectedTab == MainTab.SYSTEM) {
                ExtendedFloatingActionButton(
                    text = { Text("Download PDF") },
                    icon = { Icon(Icons.Filled.PictureAsPdf, contentDescription = null) },
                    onClick = { showExportDialog = true },
                )
            }
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

                    item {
                        DeviceHeroCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
                            columns = if (useRail) 3 else 2,
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
                                        item {
                                            CategoryHeader(
                                                category = state.selectedCategory,
                                                itemCount = filteredItems.size,
                                                modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                            )
                                        }
                                        itemsIndexed(filteredItems, key = { _, it -> it.key }) { index, item ->
                                            InfoRowCard(
                                                item = item,
                                                onCopy = {
                                                    clipboardManager.setText(AnnotatedString("${item.label}: ${item.value}"))
                                                    Toast.makeText(context, "${item.label} copied", Toast.LENGTH_SHORT).show()
                                                },
                                                isFirst = index == 0,
                                                isLast = index == filteredItems.lastIndex,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.dp),
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
    columns: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val actions = listOf(
        QuickAction(Icons.Filled.BatteryFull, "Battery", "Health & charging", scheme.primaryContainer, scheme.onPrimaryContainer, onOpenBattery),
        QuickAction(Icons.Filled.Science, "Diagnose", "Test your hardware", scheme.tertiaryContainer, scheme.onTertiaryContainer, onOpenDiagnostics),
        QuickAction(Icons.Filled.Monitor, "Monitor", "Live CPU & memory", scheme.secondaryContainer, scheme.onSecondaryContainer, onOpenMonitor),
        QuickAction(Icons.Filled.Wifi, "Network", "Speed & connection", scheme.primaryContainer, scheme.onPrimaryContainer, onOpenNetworkAnalyzer),
        QuickAction(Icons.Filled.Sensors, "Sensors", "Live sensor data", scheme.tertiaryContainer, scheme.onTertiaryContainer, onOpenSensorExplorer),
        QuickAction(Icons.Filled.Speed, "Benchmark", "Rate performance", scheme.secondaryContainer, scheme.onSecondaryContainer, onOpenBenchmark),
        QuickAction(Icons.Filled.Storage, "Storage", "What's using space", scheme.primaryContainer, scheme.onPrimaryContainer, onOpenStorageAnalyzer),
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
        )
        actions.chunked(columns).forEach { rowActions ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowActions.forEach { action -> QuickActionCard(action, Modifier.weight(1f)) }
                repeat(columns - rowActions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val container: Color,
    val content: Color,
    val onClick: () -> Unit,
)

@Composable
private fun QuickActionCard(action: QuickAction, modifier: Modifier = Modifier) {
    Card(
        onClick = action.onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = action.container),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(action.content.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(action.icon, contentDescription = null, tint = action.content)
            }
            Spacer(Modifier.height(12.dp))
            Text(action.title, style = MaterialTheme.typography.titleSmall, color = action.content)
            Text(
                action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = action.content.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DeviceHeroCard(modifier: Modifier = Modifier) {
    val name = remember {
        val maker = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        if (Build.MODEL.startsWith(maker, ignoreCase = true)) Build.MODEL else "$maker ${Build.MODEL}"
    }
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(onPrimary.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PhoneAndroid, contentDescription = null, tint = onPrimary, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleLarge, color = onPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onPrimary.copy(alpha = 0.85f),
                )
            }
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
