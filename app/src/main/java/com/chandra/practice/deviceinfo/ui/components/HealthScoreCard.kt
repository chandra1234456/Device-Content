package com.chandra.practice.deviceinfo.ui.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chandra.practice.deviceinfo.data.model.HealthScore
import com.chandra.practice.deviceinfo.data.model.HealthSubscore
import com.chandra.practice.deviceinfo.ui.theme.healthScoreColor
import com.chandra.practice.deviceinfo.ui.theme.healthScoreLabel

@Composable
fun HealthScoreCard(healthScore: HealthScore, modifier: Modifier = Modifier) {
    val color = healthScoreColor(healthScore.overall)
    var animatedScore by remember { mutableIntStateOf(0) }
    LaunchedEffect(healthScore.overall) {
        animate(initialValue = 0f, targetValue = healthScore.overall.toFloat(), animationSpec = tween(1000)) { value, _ ->
            animatedScore = value.toInt()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Device Health",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            CircularGauge(
                progress = healthScore.overall / 100f,
                modifier = Modifier.size(120.dp),
                color = color,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                    )
                    Text(
                        text = healthScoreLabel(healthScore.overall),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                healthScore.subscores.forEach { sub ->
                    SubscorePill(sub, modifier = Modifier.weight(1f))
                }
            }
            if (healthScore.recommendations.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    healthScore.recommendations.forEach { recommendation ->
                        Text(
                            text = "• $recommendation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscorePill(subscore: HealthSubscore, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = subscore.icon,
            contentDescription = null,
            tint = healthScoreColor(subscore.score),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(text = "${subscore.score}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = subscore.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
