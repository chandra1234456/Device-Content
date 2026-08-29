package com.chandra.practice.deviceinfo.ui.screens.diagnostics

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.chandra.practice.deviceinfo.data.model.DiagnosticResult

private val TEST_COLORS = listOf(
    "Red" to Color.Red,
    "Green" to Color.Green,
    "Blue" to Color.Blue,
    "White" to Color.White,
    "Black" to Color.Black,
)

@Composable
fun DisplayTestScreen(
    onBack: () -> Unit,
    onResult: (DiagnosticResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    var colorIndex by remember { mutableStateOf(0) }
    var showConfirmation by remember { mutableStateOf(false) }

    val view = LocalView.current
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.Black) {
        if (showConfirmation) {
            DisplayTestConfirmation(
                onPass = {
                    onResult(DiagnosticResult.PASS)
                    onBack()
                },
                onFail = {
                    onResult(DiagnosticResult.FAIL)
                    onBack()
                },
            )
        } else {
            val (name, color) = TEST_COLORS[colorIndex]
            val textColor = if (color == Color.White) Color.Black else Color.White
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .clickable {
                        if (colorIndex < TEST_COLORS.lastIndex) colorIndex++ else showConfirmation = true
                    },
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Exit test", tint = textColor)
                }
                Text(
                    text = "$name — tap anywhere to continue",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun DisplayTestConfirmation(onPass: () -> Unit, onFail: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Did you notice any dead pixels, discoloration, or flickering?",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onPass) { Text("No, looks good") }
            OutlinedButton(onClick = onFail) { Text("Yes, an issue") }
        }
    }
}
