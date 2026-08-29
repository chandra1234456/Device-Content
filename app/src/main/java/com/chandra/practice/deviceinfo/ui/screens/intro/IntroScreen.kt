package com.chandra.practice.deviceinfo.ui.screens.intro

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IntroScreen(
    onAgree: () -> Unit,
    onExit: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var animatedVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animatedVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animatedVisible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "iconScale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (animatedVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "textAlpha",
    )

    val offsetY by animateFloatAsState(
        targetValue = if (animatedVisible) 0f else 50f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "textOffsetY",
    )

    val titleText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
            ),
        ) {
            append("Device ")
        }
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.ExtraBold,
            ),
        ) {
            append("Information")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alpha
                    this.translationY = offsetY
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            
            Icon(
                imageVector = Icons.Filled.Devices,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Animated Title: "Device" (Primary Color) + "Information" (Tertiary Color)
            Text(
                text = titleText,
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            
            Spacer(Modifier.height(8.dp))
            
            // One-line quote about application
            Text(
                text = "“Empowering your phone with deep hardware insights & real-time health intelligence.”",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            
            Spacer(Modifier.height(36.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Privacy & Offline Policy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "All system metrics and diagnostic checks are executed 100% offline on your device. Zero telemetry is collected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onOpenPrivacyPolicy) {
                        Text("Privacy Policy", textDecoration = TextDecoration.Underline)
                    }
                    TextButton(onClick = onOpenTerms) {
                        Text("Terms & Services", textDecoration = TextDecoration.Underline)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { this.alpha = alpha },
        ) {
            Button(
                onClick = onAgree,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Agree & Start Exploring", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Exit")
            }
        }
    }
}
