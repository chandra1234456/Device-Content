package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import com.chandra.practice.deviceinfo.ui.components.EmptyState
import com.chandra.practice.deviceinfo.ui.permissions.hasCameraPermission

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(hasCameraPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> permissionGranted = granted }

    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val flashCameraId = remember {
        runCatching {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()
    }
    var torchOn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (torchOn && flashCameraId != null) {
                runCatching { cameraManager.setTorchMode(flashCameraId, false) }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Flash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                !permissionGranted -> {
                    Text(
                        text = "Testing the flash reuses the same camera permission as the Camera tab. " +
                            "This app never captures photos or video.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text("Grant camera permission")
                    }
                }
                flashCameraId == null -> {
                    EmptyState(message = "This device doesn't report a flash unit.")
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onResult(DiagnosticResult.FAIL)
                            onBack()
                        },
                    ) { Text("OK") }
                }
                else -> {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Tap the button to toggle the flashlight.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val next = !torchOn
                            runCatching { cameraManager.setTorchMode(flashCameraId, next) }
                                .onSuccess { torchOn = next }
                        },
                    ) { Text(if (torchOn) "Turn Off" else "Turn On") }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Did it light up?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = {
                                onResult(DiagnosticResult.PASS)
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("Yes") }
                        OutlinedButton(
                            onClick = {
                                onResult(DiagnosticResult.FAIL)
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("No") }
                    }
                }
            }
        }
    }
}
