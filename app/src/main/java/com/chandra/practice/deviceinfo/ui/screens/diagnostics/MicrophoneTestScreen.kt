package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    var micLevel by remember { mutableFloatStateOf(0f) }
    var isRecording by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var recordJob by remember { mutableStateOf<Job?>(null) }

    fun startListening() {
        if (isRecording || !hasPermission) return
        isRecording = true
        recordJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val minSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (minSize <= 0) return@launch

            val recorder = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minSize,
                )
            } catch (e: SecurityException) {
                return@launch
            }

            if (recorder.state != AudioRecord.STATE_INITIALIZED) return@launch
            recorder.startRecording()
            val buffer = ShortArray(minSize)

            while (isActive && isRecording) {
                val read = recorder.read(buffer, 0, minSize)
                if (read > 0) {
                    var sum = 0.0
                    for (i in 0 until read) {
                        sum += abs(buffer[i].toDouble())
                    }
                    val avg = sum / read
                    val level = (avg / 15000.0).toFloat().coerceIn(0f, 1f)
                    micLevel = level
                }
                delay(50)
            }

            recorder.stop()
            recorder.release()
        }
    }

    fun stopListening() {
        isRecording = false
        recordJob?.cancel()
        recordJob = null
        micLevel = 0f
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Microphone Test") },
                navigationIcon = {
                    IconButton(onClick = {
                        stopListening()
                        onBack()
                    }) {
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
                Icons.Filled.Mic,
                contentDescription = null,
                modifier = Modifier.height(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))

            if (!hasPermission) {
                Text(
                    text = "Microphone testing requires RECORD_AUDIO permission to capture input levels.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                    Text("Grant Permission")
                }
            } else {
                Text(
                    text = "Speak or make noise near your device microphone to test sound input.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(
                    progress = { micLevel },
                    modifier = Modifier.fillMaxWidth(0.8f).height(12.dp),
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = { if (isRecording) stopListening() else startListening() }) {
                    Text(if (isRecording) "Stop Meter" else "Start Meter")
                }
                Spacer(Modifier.height(48.dp))
                Text("Is the microphone meter reacting to your voice?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = {
                        stopListening()
                        onResult(DiagnosticResult.FAIL)
                    }) {
                        Text("No (Fail)")
                    }
                    Button(onClick = {
                        stopListening()
                        onResult(DiagnosticResult.PASS)
                    }) {
                        Text("Yes (Pass)")
                    }
                }
            }
        }
    }
}
