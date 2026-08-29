package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }

    fun playTone() {
        if (isPlaying) return
        val sampleRate = 44100
        val numSamples = sampleRate * 2 // 2 seconds
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)
        val freq = 440.0 // A4 tone

        for (i in 0 until numSamples) {
            sample[i] = sin(2 * Math.PI * i / (sampleRate / freq))
        }

        var idx = 0
        for (dVal in sample) {
            val val16 = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (val16.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = ((val16.toInt() and 0xff00) shr 8).toByte()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(generatedSnd.size)
            .build()

        track.write(generatedSnd, 0, generatedSnd.size)
        track.play()
        audioTrack = track
        isPlaying = true
    }

    fun stopTone() {
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        isPlaying = false
    }

    DisposableEffect(Unit) {
        onDispose {
            stopTone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speaker Test") },
                navigationIcon = {
                    IconButton(onClick = {
                        stopTone()
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
                Icons.Filled.VolumeUp,
                contentDescription = null,
                modifier = Modifier.height(64.dp).width(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Play a 440Hz test audio tone to verify your device speaker is working clearly.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (isPlaying) stopTone() else playTone()
                },
                modifier = Modifier.fillMaxWidth(0.7f),
            ) {
                Text(if (isPlaying) "Stop Tone" else "Play Test Tone")
            }
            Spacer(Modifier.height(48.dp))
            Text("Did you hear the test tone clearly?", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = {
                        stopTone()
                        onResult(DiagnosticResult.FAIL)
                    },
                ) {
                    Text("No (Fail)")
                }
                Button(
                    onClick = {
                        stopTone()
                        onResult(DiagnosticResult.PASS)
                    },
                ) {
                    Text("Yes (Pass)")
                }
            }
        }
    }
}
